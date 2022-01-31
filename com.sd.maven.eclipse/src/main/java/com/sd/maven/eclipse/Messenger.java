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
	static String ipAddressPropReaderGroup = "234.0.0.0";
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
	
	public void sendRequest(String forUID, Object Value) throws IOException {
		byte[] msg = ("REQUEST,"+forUID+","+Value.toString()).getBytes();
		sendMulticastMessage(msg,ipAddressPropReaderGroup);
	}
	
	public void sendPrepare(ProposalID proposalID) throws IOException {
		byte[] msg = ("PREPARE,"+proposalID.getUID()+","+proposalID.getNumber()).getBytes();
		sendMulticastMessage(msg,ipAddressAcceptorGroup);
	}

	public void sendPromise(String proposerUID, String accepterUID, ProposalID proposalID, ProposalID previousID, Object previousAcceptedValue) throws IOException {
		int prevIDNumber = ((previousID == null) ? -1 : previousID.getNumber());
		String prevIDUID = ((previousID == null) ? "-1" : previousID.getUID());
		String prevAcceptedValue = ((previousAcceptedValue == null) ? "null" : previousAcceptedValue.toString());
		byte[] msg = ("PROMISE,"+proposerUID+","+accepterUID+","+proposalID.getUID()+","+proposalID.getNumber()+","+prevIDUID+","+prevIDNumber+","+prevAcceptedValue).getBytes();
		sendMulticastMessage(msg,ipAddressProposerGroup); // proposer unique
	}
	
	public void sendNegPromise(String proposerUID, String accepterUID, ProposalID proposalID, ProposalID previousID, Object previousAcceptedValue) throws IOException {
		int prevIDNumber = ((previousID == null) ? -1 : previousID.getNumber());
		String prevIDUID = ((previousID == null) ? "-1" : previousID.getUID());
		String prevAcceptedValue = ((previousAcceptedValue == null) ? "null" : previousAcceptedValue.toString());
		byte[] msg = ("NEG_PROMISE,"+proposerUID+","+accepterUID+","+proposalID.getUID()+","+proposalID.getNumber()+","+prevIDUID+","+prevIDNumber+","+prevAcceptedValue).getBytes();
		sendMulticastMessage(msg,ipAddressProposerGroup); // proposer unique
	}
	
	public void sendAccept(ProposalID proposalID, Object proposalValue) throws IOException {
		byte[] msg = ("ACCEPT,"+proposalID.getUID()+","+proposalID.getNumber()+","+proposalValue.toString()).getBytes();
		sendMulticastMessage(msg,ipAddressAcceptorGroup);
	}

	public void sendAccepted(String fromUID, ProposalID proposalID, Object acceptedValue) throws IOException {
		byte[] msg = ("ACCEPTED,"+fromUID+","+proposalID.getUID()+","+proposalID.getNumber()+","+acceptedValue.toString()).getBytes();
		sendMulticastMessage(msg,ipAddressLearnerGroup);
		sendMulticastMessage(msg,ipAddressProposerGroup);
	}
	
	public void onResolution(ProposalID proposalID, Object value) throws IOException {
		byte[] msg = ("RESOLUTION,"+proposalID.getUID()+","+value).getBytes();
		sendMulticastMessage(msg,"233.0.0.0");
		sendMulticastMessage(msg,ipAddressProposerGroup);
		sendMulticastMessage(msg,ipAddressAcceptorGroup);
	}
	
	public void killAll() throws IOException {
		byte[] msg = ("ENDCOMS").getBytes();
		sendMulticastMessage(msg,ipAddressProposerGroup);
		sendMulticastMessage(msg,ipAddressAcceptorGroup);
		sendMulticastMessage(msg,ipAddressLearnerGroup);
		sendMulticastMessage(msg,ipAddressPropReaderGroup);
	}
	
	private void sendMulticastMessage(byte[] msg, String ipAddress) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		InetAddress group = InetAddress.getByName(ipAddress);
		DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
		socket.send(packet);
		socket.close();
	}
}

