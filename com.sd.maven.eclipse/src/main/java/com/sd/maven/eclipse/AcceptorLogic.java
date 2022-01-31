package com.sd.maven.eclipse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AcceptorLogic{
	
	protected Messenger  messenger;
			  ProposalID promisedID;
		      ProposalID acceptedID;
			  Object     acceptedValue;
			  String     thisUID;


	public AcceptorLogic() {
		this.messenger = Messenger.getInstance();
    }

	public void receivePrepare(String proposerUID, ProposalID proposalID) throws IOException {
		if (this.promisedID != null && proposalID.equals(promisedID)) { // duplicate message
			System.out.println("[" +thisUID+ "][SEND] >> PROMISE");
			messenger.sendPromise(proposerUID, thisUID, promisedID, acceptedID, acceptedValue);
		}
		else if (this.promisedID == null || proposalID.isGreaterThan(promisedID)) {
			promisedID = proposalID;
			System.out.println("[" +thisUID+ "][SEND] >> PROMISE");
			messenger.sendPromise(proposerUID, thisUID, promisedID, acceptedID, acceptedValue);
		}
		else{
			System.out.println("[" +thisUID+ "][SEND] >> NEGPROMISE");
			messenger.sendNegPromise(proposerUID, thisUID, promisedID, acceptedID, acceptedValue);
		}
	}

	public void receiveAcceptRequest(ProposalID proposalID, Object value) throws IOException {
		if (promisedID == null || proposalID.isGreaterThan(promisedID) || proposalID.equals(promisedID)) {
			promisedID    = proposalID;
			acceptedID    = proposalID;
			acceptedValue = value;
			
			updateDataFile();
			
			System.out.println("[" +thisUID+ "][SEND] >> ACCEPTED");
			messenger.sendAccepted(thisUID, acceptedID, acceptedValue);
		}
	}
	
	
	public void updateDataFile() throws IOException {
		File dataFile = new File(Thread.currentThread().getName()+".txt");
		
		if (!dataFile.exists()) {
			dataFile.createNewFile();
		} else {
			dataFile.delete();
			dataFile.createNewFile();
		}
		
		FileWriter fileWriter = new FileWriter(Thread.currentThread().getName()+".txt");
		if(acceptedID != null) {
			fileWriter.write(acceptedID.getUID()+","+acceptedID.getNumber());
			fileWriter.write("\n");
		} else {
			fileWriter.write("null");
			fileWriter.write("\n");
		}
		
		if(promisedID != null) {
			fileWriter.write(promisedID.getUID()+","+promisedID.getNumber());
			fileWriter.write("\n");
		} else {
			fileWriter.write("null");
			fileWriter.write("\n");
		}
		
		if(acceptedValue != null) {
			fileWriter.write(acceptedValue.toString());
			fileWriter.write("\n");
		} else {
			fileWriter.write("null");
			fileWriter.write("\n");
		}
		fileWriter.close();
		
	}


	public ProposalID getPromisedID() {
		return promisedID;
	}

	public ProposalID getAcceptedID() {
		return acceptedID;
	}

	public Object getAcceptedValue() {
		return acceptedValue;
	}

	public String getThisUID() {
		return thisUID;
	}
	
	public void setThisUID(String thisUID) {
		this.thisUID = thisUID;
	}

	public void setAcceptedID(ProposalID acceptedID) {
		this.acceptedID = acceptedID;
	}

	public void setAcceptedValue(Object acceptedValue) {
		this.acceptedValue = acceptedValue;
	}

	public void setPromisedID(ProposalID promisedID) {
		this.promisedID = promisedID;
	}
	
	
}