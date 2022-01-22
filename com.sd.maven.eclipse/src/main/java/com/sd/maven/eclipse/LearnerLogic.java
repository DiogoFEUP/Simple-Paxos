package com.sd.maven.eclipse;

import java.io.IOException;
import java.util.HashMap;


public class LearnerLogic{
	
	class Proposal {
		int    acceptCount;
		int    retentionCount;
		Object value;
		
		Proposal(int acceptCount, int retentionCount, Object value) {
			this.acceptCount    = acceptCount;
			this.retentionCount = retentionCount;
			this.value          = value;
		}
	}
	
	private final Messenger      messenger;
	private final int                     quorumSize;
	private HashMap<ProposalID, Proposal> proposals       = new HashMap<ProposalID, Proposal>();
	private HashMap<String,  ProposalID>  acceptors       = new HashMap<String, ProposalID>();
	private Object                        finalValue      = null;
	private ProposalID                    finalProposalID = null;
	
	public LearnerLogic( int quorumSize ) {
		this.messenger  = Messenger.getInstance();
		this.quorumSize = quorumSize;
	}

	public boolean isComplete() {
		return finalValue != null;
	}

	public void receiveAccepted(String fromUID, ProposalID proposalID, Object acceptedValue) throws IOException {
		
		if (isComplete())
			return;

		ProposalID oldPID = acceptors.get(fromUID);
		
		if (oldPID != null && !proposalID.isGreaterThan(oldPID))
			return;
		
		acceptors.put(fromUID, proposalID);

		if (oldPID != null) {
			Proposal oldProposal = proposals.get(oldPID);
			oldProposal.retentionCount -= 1;
			if (oldProposal.retentionCount == 0)
				proposals.remove(oldPID);
		}
        
		if (!proposals.containsKey(proposalID))
			proposals.put(proposalID, new Proposal(0, 0, acceptedValue));

		Proposal thisProposal = proposals.get(proposalID);	
		
		thisProposal.acceptCount    += 1;
		thisProposal.retentionCount += 1;
        
        if (thisProposal.acceptCount == quorumSize) {
        	finalProposalID = proposalID;
        	finalValue      = acceptedValue;
        	proposals.clear();
        	acceptors.clear();
        	
        	System.out.println("[" +fromUID+ "][SEND] >> RESOLUTION");
        	messenger.onResolution(proposalID, acceptedValue);
        }
	}

	public int getQuorumSize() {
		return quorumSize;
	}	

	public Object getFinalValue() {
		return finalValue;
	}

	public ProposalID getFinalProposalID() {
		return finalProposalID;
	}
}
