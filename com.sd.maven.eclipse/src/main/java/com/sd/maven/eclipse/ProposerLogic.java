package com.sd.maven.eclipse;

import java.io.IOException;
import java.util.HashSet;


public class ProposerLogic{
	
	protected Messenger  messenger;
    protected String              proposerUID;
    protected final int           quorumSize;

    		  volatile ProposalID          proposalID;
    protected volatile Object              proposedValue      = null;
    protected volatile ProposalID          lastAcceptedID     = null;
    protected volatile HashSet<String>     promisesReceived   = new HashSet<String>();
    
    public ProposerLogic(int quorumSize) {
		this.messenger   = Messenger.getInstance();
		this.quorumSize  = quorumSize;
		this.proposalID  = new ProposalID(0, proposerUID);
	}

	public void setProposal(Object value) {
		if ( proposedValue == null )
			proposedValue = value;
	}

	public void prepare() throws IOException {
		promisesReceived.clear();
		
		proposalID.incrementNumber();
		
		messenger.sendPrepare(proposalID);
	}

	public void receivePromise(String fromUID, ProposalID proposalID, ProposalID prevAcceptedID, Object prevAcceptedValue) throws IOException {
		if ( !proposalID.equals(this.proposalID) || promisesReceived.contains(fromUID) )  {
			return;
		}
        promisesReceived.add( fromUID );

        if (lastAcceptedID == null || prevAcceptedID.isGreaterThan(lastAcceptedID))
        {
        	lastAcceptedID = prevAcceptedID;

        	if (prevAcceptedValue != null)
        		proposedValue = prevAcceptedValue;
        }

        if (promisesReceived.size() == quorumSize && proposedValue != null ) {
        	System.out.println("[" +proposerUID+ "][SEND] >> ACCEPT");
        	messenger.sendAccept(this.proposalID, proposedValue);
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