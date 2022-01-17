package com.sd.maven.eclipse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.locks.ReentrantLock;

public class Messenger {
	private static Object mutex = new Object();
	private static Messenger messagerInstance = null;
	
	static String ipAddressProposerGroup = "230.0.0.0";
	static String ipAddressAcceptorGroup = "231.0.0.0";
	static String ipAddressLearnerGroup = "232.0.0.0";
	static int port = 4321;

	private Messenger() {
		// Constructor stuff
	}
	
	public static Messenger getInstance() {
		synchronized(mutex) { // Making sure there isn't concurrency creating the singleton
			if (messagerInstance == null)
				messagerInstance = new Messenger();
		}
 
        return messagerInstance;
	}
	
	public void sendRequest(Object Value) throws IOException {
		byte[] msg = ("REQUEST,"+Value.toString()).getBytes();
		sendMulticastMessage(msg,ipAddressProposerGroup);
	}
	
	public void sendPrepare(ProposalID proposalID) throws IOException {
		byte[] msg = ("PREPARE,"+proposalID.getNumber()).getBytes();
		sendMulticastMessage(msg,ipAddressAcceptorGroup);
	}

	public void sendPromise(ProposalID proposalID, ProposalID previousID, Object previousAcceptedValue, String ipAddressProposer) throws IOException {
		int prevIDNumber = ((previousID == null) ? 0 : previousID.getNumber());
		String prevAcceptedValue = ((previousAcceptedValue == null) ? "null" : previousAcceptedValue.toString());
		byte[] msg = ("PROMISE,"+proposalID.getNumber()+","+prevIDNumber+","+prevAcceptedValue).getBytes();
		sendMulticastMessage(msg,ipAddressProposerGroup);
	}
	
	public void sendAccept(ProposalID proposalID, Object proposalValue) throws IOException {
		byte[] msg = ("ACCEPT,"+proposalID.getNumber()+","+proposalValue.toString()).getBytes();
		sendMulticastMessage(msg,ipAddressAcceptorGroup);
	}

	public void sendAccepted(ProposalID proposalID, Object acceptedValue) throws IOException {
		byte[] msg = ("ACCEPTED,"+proposalID.getNumber()+","+acceptedValue.toString()).getBytes();
		sendMulticastMessage(msg,ipAddressLearnerGroup);
		sendMulticastMessage(msg,ipAddressProposerGroup);
	}
	
	public void onResolution(ProposalID proposalID, Object value) throws IOException {
		byte[] msg = ("RESOLUTION,"+value).getBytes();
		sendMulticastMessage(msg,"233.0.0.0");
	}
	
	public void killAll() throws IOException {
		byte[] msg = ("ENDCOMS").getBytes();
		sendMulticastMessage(msg,ipAddressProposerGroup);
		sendMulticastMessage(msg,ipAddressAcceptorGroup);
		sendMulticastMessage(msg,ipAddressLearnerGroup);
	}
	
	private void sendMulticastMessage(byte[] msg, String ipAddress) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		InetAddress group = InetAddress.getByName(ipAddress);
		DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
		socket.send(packet);
		socket.close();
	}
}
