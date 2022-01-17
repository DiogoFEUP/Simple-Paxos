package com.sd.maven.eclipse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class Proposer implements Runnable{
	private static String ipAddress = "230.0.0.0";
	private static int bufferSize = 1024;
	private static int port = 4321;
	private Object val;

	public void receiveUDPMessage(String threadName, String ip, int port) throws IOException {
		byte[] buffer = new byte[bufferSize];
		MulticastSocket socket=new MulticastSocket(port);
		InetSocketAddress group = new InetSocketAddress(ipAddress,port);
		NetworkInterface netIf = NetworkInterface.getByName("bge0");
		Messenger messenger = Messenger.getInstance();
		socket.joinGroup(group, netIf);
		System.out.println("[" +threadName+ "][WOKE]");
		
		while(true){ 

//			System.out.println("[" +threadName+ "][IDLE]");
			DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
			socket.receive(packet);
			String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
			String[] msgSplitted = msg.split(",");
			System.out.println("[" +threadName+ "][RECV] << "+msg);
			
			if("ENDCOMS".equals(msg) || msgSplitted.length < 2) {
				System.out.println("[" +threadName+ "][DEAD]");
				
				break;
			}
			if("REQUEST".equals(msgSplitted[0])) {
				val = msgSplitted[1];
				System.out.println("[" +threadName+ "][SEND] >> PREPARE");
				ProposalID propID = new ProposalID(1);
				
				/*
				 *  
				 *  PREPARE logic
				 *  
				 */
				
				messenger.sendPrepare(propID);
			}
			if("PROMISE".equals(msgSplitted[0])) {
				System.out.println("[" +threadName+ "][SEND] >> ACCEPT");
				ProposalID propID = new ProposalID(Integer.parseInt(msgSplitted[1]));
				
				/*
				 *  
				 *  Accept logic
				 *  
				 */
				
				messenger.sendAccept(propID, val);
			}
			
			
		}
		socket.leaveGroup(group, netIf);
		socket.close();
	}

	@Override
	public void run(){
		try {
			receiveUDPMessage(Thread.currentThread().getName(),ipAddress, port);
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
}