package com.sd.maven.eclipse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Scanner;

public class Learner extends LearnerLogic implements Runnable{
	
	public Learner(int quorumSize) {
		super(quorumSize);
	}

	static String ipAddress = "232.0.0.0";
	static int bufferSize = 1024;
	static int port = 4321;

	public void receiveUDPMessage(String threadName, String ip, int port) throws IOException, InterruptedException {
		byte[] buffer = new byte[bufferSize];
		MulticastSocket socket=new MulticastSocket(port);
		InetSocketAddress group = new InetSocketAddress(ipAddress,port);
		NetworkInterface netIf = NetworkInterface.getByName("bge2");
		Messenger messenger = Messenger.getInstance();
		socket.joinGroup(group, netIf);
		System.out.println("[" +threadName+ "][WOKE]");
		while(true){ 
//			System.out.println("[" +threadName+ "][IDLE]");
			DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
			socket.receive(packet);
			String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
		
			if("ENDCOMS".equals(msg)) {
				System.out.println("[" +threadName+ "][DEAD]");
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
		
		if("ACCEPTED".equals(msgSplitted[0])) {
			ProposalID recvPropID = new ProposalID(Integer.parseInt(msgSplitted[3]),msgSplitted[2]);
			
			receiveAccepted(msgSplitted[1] ,recvPropID, msgSplitted[4]);
		}
	}
	
	@Override
	public void run(){
		try {
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
