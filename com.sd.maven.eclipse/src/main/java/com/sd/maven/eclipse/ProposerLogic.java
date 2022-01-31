package com.sd.maven.eclipse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class ProposerLogic {

	protected Messenger messenger;
	protected String proposerUID;
	protected final int quorumSize;

	volatile ProposalID proposalID;
	protected volatile Object proposedValue = null;
	protected volatile ProposalID lastAcceptedID = null;
	protected volatile HashSet<String> promisesReceived = new HashSet<String>();
	protected volatile HashSet<String> negPromisesReceived = new HashSet<String>();

	public ProposerLogic(int quorumSize) {
		this.messenger = Messenger.getInstance();
		this.quorumSize = quorumSize;
		this.proposalID = new ProposalID(0, proposerUID);
	}

	public void setProposal(Object value) {
		// if ( proposedValue == null )
		proposedValue = value;
	}

	public void prepare() throws IOException {
		promisesReceived.clear();

		negPromisesReceived.clear();

		proposalID.incrementNumber();

		System.out.println("\n[" + proposerUID + "][SEND] >> PREPARE");

		messenger.sendPrepare(proposalID);
	}

	public void receivePromise(String fromUID, ProposalID proposalID, ProposalID prevAcceptedID,
			Object prevAcceptedValue) throws IOException {
		if (!proposalID.equals(this.proposalID) || promisesReceived.contains(fromUID)) {
			return;
		}
		promisesReceived.add(fromUID);

		if (prevAcceptedID != null) {
			if (lastAcceptedID == null || prevAcceptedID.isGreaterThan(lastAcceptedID)) {
				lastAcceptedID = prevAcceptedID;

				if (prevAcceptedValue != null)
					proposedValue = prevAcceptedValue;
			}

			if (promisesReceived.size() == quorumSize && proposedValue != null) {
				System.out.println("[" + proposerUID + "][SEND] >> ACCEPT");
				messenger.sendAccept(this.proposalID, proposedValue);
			}
		} else {
			if (lastAcceptedID == null || proposalID.isGreaterThan(lastAcceptedID)) {
				lastAcceptedID = proposalID;
			}
			
			if (promisesReceived.size() == quorumSize && proposedValue != null) {
				System.out.println("\n[" + proposerUID + "][SEND] >> ACCEPT");
				messenger.sendAccept(this.proposalID, proposedValue);
			}
		}
	}

	public void receiveNegPromise(String fromUID, ProposalID proposalID, ProposalID prevAcceptedID,
			Object prevAcceptedValue) throws IOException {
		/*
		 * if ( !proposalID.equals(this.proposalID) ||
		 * negPromisesReceived.contains(fromUID) ) { System.out.println("Aqui"); return;
		 * }
		 */

		negPromisesReceived.add(fromUID);
		// System.out.println("Saiu");
		if (prevAcceptedID != null) {
			if (lastAcceptedID == null || prevAcceptedID.isGreaterThan(lastAcceptedID)) {
				lastAcceptedID = prevAcceptedID;

				if (prevAcceptedValue != null) {
					File dataFile = new File("Requests.txt");
					Path pathFromFile = dataFile.toPath();
					String content = Files.readString(pathFromFile);
					if (content.indexOf(prevAcceptedValue + "\n") != -1) {
						proposedValue = prevAcceptedValue;
					} else if (content.indexOf(prevAcceptedValue + "") != -1) {
						proposedValue = prevAcceptedValue;
					} else {
						System.out.println("Received nonexistant value. Ignoring...");
					}
				}
			}

			if (negPromisesReceived.size() == quorumSize) {
				//System.out.println("321");
				this.proposalID.setNumber(lastAcceptedID.getNumber());
				prepare();
			}
		} else {
			if (lastAcceptedID == null || proposalID.isGreaterThan(lastAcceptedID)) {
				lastAcceptedID = proposalID;
			}
			if (negPromisesReceived.size() == quorumSize) {
				//System.out.println("123");
				this.proposalID.setNumber(lastAcceptedID.getNumber());
				prepare();
			}
		}
	}

	public Messenger getMessenger() {
		return messenger;
	}

	public String getProposerUID() {
		return proposerUID;
	}

	public int getQuorumSize() {
		return quorumSize;
	}

	public ProposalID getProposalID() {
		return proposalID;
	}

	public void setProposalID(ProposalID proposalID) {
		this.proposalID = proposalID;
	}

	public Object getProposedValue() {
		return proposedValue;
	}

	public ProposalID getLastAcceptedID() {
		return lastAcceptedID;
	}

	public int numPromises() {
		return promisesReceived.size();
	}

	public void setProposerUID(String proposerUID) {
		this.proposerUID = proposerUID;
	}

}