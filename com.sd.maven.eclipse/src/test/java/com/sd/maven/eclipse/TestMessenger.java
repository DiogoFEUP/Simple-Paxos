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
    	
    	// CHANGE PARAMETERS HERE
    	int n_acceptors = 5;
    	int n_values = 1;
    	
    	int quorumSize = (n_acceptors%2 == 0) ?
    			(int)((float) n_acceptors / 2 + 1) :
    			(int)((float) n_acceptors / 2 + 0.5);
      	
		for (int n=0; n< n_acceptors; n++) {
			Thread n0=new Thread(new Accepter());
			n0.setName("Accepter-"+n);
			n0.start();	
		}
		
		Thread n6=new Thread(new Learner(quorumSize));
		n6.setName("Learner -1");
		n6.start();
		
		// Makes sure everything starts
		Thread.sleep(200);
		Thread n1=new Thread(new Proposer(quorumSize));
    	n1.setName("Proposer-1");
		n1.start();

		
		Thread.sleep(2000);
		Thread client1=new Thread(new Client("Valor0"));
		client1.setName("  Client  ");
		client1.start();
		for(int i=1; i<n_values;i++) {
			Thread client2=new Thread(new Client("Valor" + i));
			client2.setName("  C1ient  ");
			client2.start();
		}

		Thread.sleep(300000); // makes sure everything stays alive 
		mess.killAll();
	}
		
		
        //assertTrue( true );
   
}
