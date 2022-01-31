package com.sd.maven.eclipse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Scanner;
import java.util.LinkedList;

public class ProposerReader implements Runnable {

	private static String ipAddress = "234.0.0.0";
	private static int bufferSize = 1024;
	private static int port = 4321;
	private String val;
	private String proposerUID = null;

	public ProposerReader(String proposerUID) {
		this.proposerUID = proposerUID;
	}

	public void insertToFile(String msg) throws IOException, InterruptedException {
		File dataFile = new File("Requests" + ".txt");

		if (!dataFile.exists()) {
			dataFile.createNewFile();
		}

		FileWriter fileWriter = new FileWriter(dataFile.getName(), true);
		if (msg != null) {

			fileWriter.write(msg);
			fileWriter.write("\n");
		}
		fileWriter.close();
	}

	public void receiveUDPMessage(String threadName, String ip, int port) throws IOException, InterruptedException {
		byte[] buffer = new byte[bufferSize];
		MulticastSocket socket = new MulticastSocket(port);
		InetSocketAddress group = new InetSocketAddress(ipAddress, port);
		NetworkInterface netIf = NetworkInterface.getByName("bge4");
		Messenger messenger = Messenger.getInstance();
		socket.joinGroup(group, netIf);
		System.out.println("[" + threadName + "][WOKE]");

		while (true) {
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

			//Thread.sleep(20);

		}
		socket.leaveGroup(group, netIf);
		socket.close();
	}

	private void processMessageReceived(String msg) throws IOException, InterruptedException {
		String[] msgSplitted = msg.split(",");

		if ("REQUEST".equals(msgSplitted[0])) {
			if (msgSplitted[1].equals(proposerUID)) {
				val = msgSplitted[2];
				insertToFile(val);
			} else {
				System.out.println("[" + proposerUID + "] >> ERROR");
			}
		} else {
			System.out.println("[" + proposerUID + "] >> FORMAT ERROR");
		}
	}

	@Override
	public void run() {
		try {
			receiveUDPMessage(Thread.currentThread().getName(), ipAddress, port);
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
