package com.sd.maven.eclipse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class UDPMulticastClient implements Runnable {

   public static void main(String[] args) {
      Thread t1=new Thread(new UDPMulticastClient());
      t1.start();
      Thread t2=new Thread(new UDPMulticastClient());
      t2.start();
      Thread t3=new Thread(new UDPMulticastClient());
      t3.start();
   }

   public void receiveUDPMessage(String threadName, String ip, int port) throws IOException {
      byte[] buffer=new byte[1024];
      MulticastSocket socket=new MulticastSocket(4321);
      InetSocketAddress group = new InetSocketAddress("230.0.0.0",4321);
      NetworkInterface netIf = NetworkInterface.getByName("bge0");
      socket.joinGroup(group, netIf);
      
      while(true){
    	  
         System.out.println("[" +threadName+ "]Waiting for multicast message...");
         DatagramPacket packet=new DatagramPacket(buffer,
            buffer.length);
         socket.receive(packet);
         String msg=new String(packet.getData(),
         packet.getOffset(),packet.getLength());
         System.out.println("[" +threadName+ "][Multicast UDP message received] >> "+msg);
         if("OK".equals(msg)) {
            System.out.println("[" +threadName+ "]No more message. Exiting : "+msg);
            break;
         }
      }
      socket.leaveGroup(group, netIf);
      socket.close();
   }

   @Override
   public void run(){
	   try {
	      receiveUDPMessage(Thread.currentThread().getName(),"230.0.0.0", 4321);
	   }catch(IOException ex){
	      ex.printStackTrace();
	   }
   }
}