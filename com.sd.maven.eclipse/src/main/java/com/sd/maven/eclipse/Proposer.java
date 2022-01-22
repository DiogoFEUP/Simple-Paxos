package com.sd.maven.eclipse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class Proposer extends ProposerLogic implements Runnable{
	
	private static String ipAddress = "230.0.0.0";
	private static int bufferSize = 1024;
	private static int port = 4321;
	private Object val;
	
	public Proposer(int quorumSize) {
		super(quorumSize);
	}

	public void receiveUDPMessage(String threadName, String ip, int port) throws IOException, InterruptedException {
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
			
			if("ENDCOMS".equals(msg)) {
				System.out.println("[" +threadName+ "][DEAD]");
				break;
			}
			if (!msg.split(",")[1].equals(proposerUID)) 
				continue;
			
			System.out.println("[" +threadName+ "][RECV] << "+msg);
			Thread.sleep(500);
			processMessageReceived(msg);
		}
		socket.leaveGroup(group, netIf);
		socket.close();
	}
	
	private void processMessageReceived(String msg) throws IOException {
		String[] msgSplitted = msg.split(",");
		
		if("REQUEST".equals(msgSplitted[0])) {
			if(msgSplitted[1].equals(proposerUID)) {
				val = msgSplitted[2];
				setProposal(val);
				System.out.println("[" +proposerUID+ "][SEND] >> PREPARE");
				prepare();
			}
		}
		if("PROMISE".equals(msgSplitted[0])) {
			ProposalID propID = new ProposalID(Integer.parseInt(msgSplitted[4]),msgSplitted[3]);
			ProposalID prevPropID = (msgSplitted[6].equals("-1") || msgSplitted[5].equals("-1")) ? null : new ProposalID(Integer.parseInt(msgSplitted[6]),msgSplitted[5]);
			Object prevValue = (msgSplitted[7].equals("null")) ? null : msgSplitted[7];
			
			
			receivePromise( msgSplitted[2], propID, prevPropID, prevValue);
		}
	}

	@Override
	public void run(){
		try {
			setProposerUID(Thread.currentThread().getName());
			setProposalID(new ProposalID(0, proposerUID));
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