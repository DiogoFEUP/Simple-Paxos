package com.sd.maven.eclipse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class Client implements Runnable{
	static String ipAddress = "233.0.0.0";
	static int port = 4321;

	public void receiveUDPMessage(String threadName, String ip, int port) throws IOException {
		byte[] buffer=new byte[1024];
		MulticastSocket socket=new MulticastSocket(4321);
		InetSocketAddress group = new InetSocketAddress(ipAddress,4321);
		NetworkInterface netIf = NetworkInterface.getByName("bge3");
		Messenger messenger = Messenger.getInstance();
		socket.joinGroup(group, netIf);

		while(true){ 

			System.out.println("[" +threadName+ "][WOKE]");
			DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
			socket.receive(packet);
			String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
			String[] msgSplitted = msg.split(",");
			System.out.println("[" +threadName+ "][RECV] >> "+msg);
			if("ENDCOMS".equals(msg) || msgSplitted.length < 2) {
				break;
			}
			if("REQUEST".equals(msgSplitted[0])) {
				System.out.println("[" +threadName+ "][SEND] >> ACCEPT");
				ProposalID propID = new ProposalID(Integer.parseInt(msgSplitted[1]));
				
				/*
				 *  
				 *  Accept logic
				 *  
				 */
				
				messenger.sendAccept(propID, msgSplitted[1]);
			}
			if("RESOLUTION".equals(msgSplitted[0])) {
				System.out.println("[" +threadName+ "][DEAD]");
				break;
			}
		}
		socket.leaveGroup(group, netIf);
		socket.close();
	}

	@Override
	public void run(){
		try {
			receiveUDPMessage(Thread.currentThread().getName(),ipAddress, 4321);
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
}
