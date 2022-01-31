package com.sd.maven.eclipse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Scanner;
import java.util.LinkedList;

public class Proposer extends ProposerLogic implements Runnable {

	private static String ipAddress = "230.0.0.0";
	private static int bufferSize = 1024;
	private static int port = 4321;
	private Object val;
	long t1;
	long t2;
	int flag = 0;

	
	public Proposer(int quorumSize) {
		super(quorumSize);
	}

	public void ReceiveResponse(String threadName, String ip, int port) throws IOException, InterruptedException {
		byte[] buffer = new byte[bufferSize];
		MulticastSocket socket = new MulticastSocket(port);
		InetSocketAddress group = new InetSocketAddress(ipAddress, port);
		NetworkInterface netIf = NetworkInterface.getByName("bge0");
		Messenger messenger = Messenger.getInstance();
		socket.joinGroup(group, netIf);
		
		long start = 0;
		while (true) {
			
			if (flag == 0) {
				t1 = System.nanoTime();
				prepare();
				flag = 1;
			}
//			System.out.println("[" +threadName+ "][IDLE]");
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			
			if ("ENDCOMS".equals(msg)) {
				System.out.println("[" + threadName + "][DEAD]");
				break;
			}
			if (!msg.split(",")[1].equals(proposerUID))
				continue;
			System.out.println("[" + threadName + "][RECV] << " + msg);			
			processMessageReceived(msg);
			//Thread.sleep(500);
		}
		socket.leaveGroup(group, netIf);
		socket.close();
	}

	private void processMessageReceived(String msg) throws IOException {
		String[] msgSplitted = msg.split(",");

		if ("PROMISE".equals(msgSplitted[0])) {
			ProposalID propID = new ProposalID(Integer.parseInt(msgSplitted[4]), msgSplitted[3]);
			ProposalID prevPropID = (msgSplitted[6].equals("-1") || msgSplitted[5].equals("-1")) ? null
					: new ProposalID(Integer.parseInt(msgSplitted[6]), msgSplitted[5]);
			Object prevValue = (msgSplitted[7].equals("null")) ? null : msgSplitted[7];

			receivePromise(msgSplitted[2], propID, prevPropID, prevValue);
		}
		if ("NEG_PROMISE".equals(msgSplitted[0])) {
			ProposalID propID = new ProposalID(Integer.parseInt(msgSplitted[4]), msgSplitted[3]);
			ProposalID prevPropID = (msgSplitted[6].equals("-1") || msgSplitted[5].equals("-1")) ? null
					: new ProposalID(Integer.parseInt(msgSplitted[6]), msgSplitted[5]);
			Object prevValue = (msgSplitted[7].equals("null")) ? null : msgSplitted[7];
			
			receiveNegPromise(msgSplitted[2], propID, prevPropID, prevValue);
		}
		if ("RESOLUTION".equals(msgSplitted[0])) {
			t2 = System.nanoTime();
			System.out.println("Resolution achieved in: " + (double)((double)(t2-t1)/1000) + " us");
			File dataFile = new File("Requests.txt");
			Path pathFromFile = dataFile.toPath();
			String content = Files.readString(pathFromFile);
			//System.out.println(content);
			
			if (content.indexOf(msgSplitted[2] + "\r\n") != -1) {
//				System.out.println("Case 0 with \\r\\n: \n");
				content = content.replaceFirst(msgSplitted[2] + "\r\n", "");
			}			
			else if (content.indexOf(msgSplitted[2] + "\n") != -1) {
//				System.out.println("Case 1 with newline: \n");
				content = content.replaceFirst(msgSplitted[2] + "\n", "");
			} 
			else if (content.indexOf(msgSplitted[2] + "\r") != -1) {
//				System.out.println("Case 2 with \\r: \n");
				content = content.replaceFirst(msgSplitted[2] + "\r", "");
			}			
			else if (content.indexOf(msgSplitted[2]) != -1) {
//				System.out.println("Case 3 without newline: \n");
				content = content.replaceFirst(msgSplitted[2], "");
			} else {
				System.out.println("Mild Error on Resolution: Tried to resolve nonexistant value.");
			}
			
			dataFile.delete();
			dataFile.createNewFile();

			FileWriter fileWriter = new FileWriter("Requests.txt");
			fileWriter.write(content);
			fileWriter.close();

			String next = null;
			while(next == null) {
				next = readFile();
			}
			System.out.println();
			setProposal(next);
			t1 = System.nanoTime();
			prepare();

		}
	}

	private String readFile() throws IOException {
		File dataFile = new File("Requests.txt");
		// Read Data if it already exists
		if (dataFile.exists()) {
			Scanner fileReader = new Scanner(dataFile);
			if (fileReader.hasNextLine()) {
				String data = fileReader.nextLine();
				if (!data.equals("null") && !data.equals("null\n")) {
					fileReader.close();
					return data;
				} else {
					System.out.println("???????????????????");
				}
				fileReader.close();
			}
			else {
				fileReader.close();
				return null;
			}
		} else {
			return "ERROR2";
		}
		return "ERROR";
	}

	@Override
	public void run() {
		try {
			System.out.println("[" + Thread.currentThread().getName() + "][WOKE]");
			setProposerUID(Thread.currentThread().getName());
			setProposalID(new ProposalID(0, proposerUID));
			String value = null;
			Thread n1 = new Thread(new ProposerReader(proposerUID));
			n1.setName(proposerUID + "-Reader");
			n1.start();
			while(value == null || value.equals("ERROR") || value.equals("ERROR2")) {
				value = readFile();
			}
			setProposal(value);
			ReceiveResponse(Thread.currentThread().getName(), ipAddress, port);
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void forceKill() {
		System.out.println("[" + Thread.currentThread().getName() + "][KILLED]");
		Thread.currentThread().stop();
	}
}