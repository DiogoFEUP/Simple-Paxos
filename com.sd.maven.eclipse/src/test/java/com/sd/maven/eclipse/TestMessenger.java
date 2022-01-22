package com.sd.maven.eclipse;

//import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.sd.maven.eclipse.Accepter;
import com.sd.maven.eclipse.Client;
import com.sd.maven.eclipse.Learner;
import com.sd.maven.eclipse.Messenger;
import com.sd.maven.eclipse.Proposer;

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
    	int quorumSize = 2;
    	
    	
    	Thread client=new Thread(new Client());
    	client.setName("  Client  ");
    	client.start();
    	
    	
    	Thread n1=new Thread(new Proposer(quorumSize));
    	n1.setName("Proposer-1");
		n1.start();
		Thread n2=new Thread(new Proposer(quorumSize));
		n2.setName("Proposer-2");
		n2.start();
		
		
		Thread n3=new Thread(new Accepter());
		n3.setName("Accepter-1");
		n3.start();
		Thread n4=new Thread(new Accepter());
		n4.setName("Accepter-2");
		n4.start();
		Thread n5=new Thread(new Accepter());
		n5.setName("Accepter-3");
		n5.start();
		
		
		Thread n6=new Thread(new Learner(quorumSize));
		n6.setName("Learner -1");
		n6.start();
		
		// Makes sure everything starts
		Thread.sleep(2000);
		mess.sendRequest("Proposer-1","TEST");
		
		Thread.sleep(8000);
		//mess.killAll();
	}
		
		
        //assertTrue( true );
   
}
