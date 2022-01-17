package com.sd.maven.eclipse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class Accepter implements Runnable{
	static String ipAddress = "231.0.0.0";
	static int bufferSize = 1024;
	static int port = 4321;

	public void receiveUDPMessage(String threadName, String ip, int port) throws IOException {
		byte[] buffer = new byte[bufferSize];
		MulticastSocket socket=new MulticastSocket(port);
		InetSocketAddress group = new InetSocketAddress(ipAddress,port);
		NetworkInterface netIf = NetworkInterface.getByName("bge1");
		Messenger messenger = Messenger.getInstance();
		socket.joinGroup(group, netIf);
		System.out.println("[" +threadName+ "][WOKE]");
		while(true){ 

//			System.out.println("[" +threadName+ "][IDLE]");
			DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
			socket.receive(packet);
			String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
			System.out.println("[" +threadName+ "][RECV] << "+msg);
			
			String[] msgSplitted = msg.split(",");
			if("ENDCOMS".equals(msg) || msgSplitted.length < 2) {
				System.out.println("[" +threadName+ "][DEAD]");
				
				break;
			}
			if("PREPARE".equals(msgSplitted[0])) {
				ProposalID recvPropID = new ProposalID(Integer.parseInt(msgSplitted[1]));
				
				/*
				 *  
				 *  PROMISE logic
				 *  
				 */
				
				System.out.println("[" +threadName+ "][SEND] >> PROMISE");
				messenger.sendPromise(recvPropID, null, null,"230.0.0.0");
			}
			if("ACCEPT".equals(msgSplitted[0])) {
				ProposalID recvPropID = new ProposalID(Integer.parseInt(msgSplitted[1]));
				
				/*
				 *  
				 *  ACCEPTED logic
				 *  
				 */
				
				System.out.println("[" +threadName+ "][SEND] >> ACCEPTED");
				messenger.sendAccepted(recvPropID, msgSplitted[2]);
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
