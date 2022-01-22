package com.sd.maven.eclipse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Scanner;

public class Accepter extends AcceptorLogic implements Runnable{
	private boolean kill = false;
	static String ipAddress = "231.0.0.0";
	static int bufferSize = 1024;
	static int port = 4321;
	
	public Accepter() {
		super();
	}

	public void receiveUDPMessage(String threadName, String ip, int port) throws IOException, InterruptedException {
		byte[] buffer = new byte[bufferSize];
		MulticastSocket socket=new MulticastSocket(port);
		InetSocketAddress group = new InetSocketAddress(ipAddress,port);
		NetworkInterface netIf = NetworkInterface.getByName("bge1");
		Messenger messenger = Messenger.getInstance();
		socket.joinGroup(group, netIf);
		System.out.println("[" +threadName+ "][WOKE]");
		
		while(!kill){ 
//			System.out.println("[" +threadName+ "][IDLE]");
			DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
			socket.receive(packet);
			String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
			
			if("ENDCOMS".equals(msg)) {
				System.out.println("[" +thisUID+ "][DEAD]");
				break;
			}
			
			System.out.println("[" +threadName+ "][RECV] << "+msg);
			Thread.sleep(500);
			processMessageReceived(msg);
		}
		socket.leaveGroup(group, netIf);
		socket.close();
	}
	
	private void processMessageReceived(String msg) throws IOException {
		String[] msgSplitted = msg.split(",");

		if("PREPARE".equals(msgSplitted[0])) {
			ProposalID recvPropID = new ProposalID(Integer.parseInt(msgSplitted[2]), msgSplitted[1]);
			
			System.out.println("[" +thisUID+ "][SEND] >> PROMISE");
			receivePrepare(msgSplitted[1], recvPropID);
			
		}
		if("ACCEPT".equals(msgSplitted[0])) {
			ProposalID recvPropID = new ProposalID(Integer.parseInt(msgSplitted[2]),msgSplitted[1]);
			
			receiveAcceptRequest(recvPropID, msgSplitted[3]);
		}
	}
	
	private void prepareDataFiles() throws IOException {
		File dataFile = new File(Thread.currentThread().getName()+".txt");
		
		// Read Data if it already exists
		if (dataFile.exists()) {
			Scanner fileReader = new Scanner(dataFile);
			if (fileReader.hasNextLine()) {
		        String data = fileReader.nextLine();
		        if (!data.equals("null") && !data.equals("null\n")) {
		        	String[] dataSplittedString = data.split(",");
		        	ProposalID propID = new ProposalID(Integer.parseInt(dataSplittedString[1]),dataSplittedString[0]);
		        	setAcceptedID(propID);
		        }
		        data = fileReader.nextLine();
		        if (!data.equals("null") && !data.equals("null\n")) {
		        	String[] dataSplittedString = data.split(",");
		        	ProposalID propID = new ProposalID(Integer.parseInt(dataSplittedString[1]),dataSplittedString[0]);
		        	setPromisedID(propID);
		        }
		        data = fileReader.nextLine();
		        if (!data.equals("null") && !data.equals("null\n")) {
		        	setAcceptedValue(data);
		        }
		    }
			fileReader.close();	
		}
	}

	@Override
	public void run(){
		try {
			super.setThisUID(Thread.currentThread().getName());
			prepareDataFiles();
			receiveUDPMessage(Thread.currentThread().getName(),ipAddress, port);
		}catch(IOException | InterruptedException ex){
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void forceKill() {
		System.out.println("[" +Thread.currentThread().getName()+ "][KILLED]");
		Thread.currentThread().stop();
	}
}
