package com.sd.maven.eclipse;

//import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class TestMessenger 
{
    /**
     * Test Proposer Acceptor messaging
     * @throws InterruptedException 
     * @throws IOException 
     */
    @Test
    public void testProposerAcceptor() throws InterruptedException, IOException
    {
    	Messenger mess = Messenger.getInstance();
    	
    	Thread client=new Thread(new Client());
    	client.setName("   Client   ");
    	client.start();
    	
    	Thread n1=new Thread(new Proposer());
    	n1.setName(" Proposer-1 ");
		n1.start();
//		Thread n2=new Thread(new Proposer());
//		n2.setName("Proposer-2");
//		n2.start();
		Thread n3=new Thread(new Accepter());
		n3.setName(" Accepter-1 ");
		n3.start();
//		Thread n4=new Thread(new Accepter());
//		n4.setName("Accepter-2");
//		n4.start();
		Thread n5=new Thread(new Learner());
		n5.setName(" Learner -1 ");
		n5.start();
		
		Thread.sleep(2000);
		mess.sendRequest("valueeee");
		
//		ProposalID propID = new ProposalID(1);
//		mess.sendPrepare(propID);
		
		Thread.sleep(2000);
		mess.killAll();
	}
		
		
        //assertTrue( true );
   
}
