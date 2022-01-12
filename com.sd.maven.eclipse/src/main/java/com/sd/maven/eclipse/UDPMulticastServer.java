package com.sd.maven.eclipse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPMulticastServer {

   public static void sendUDPMessage(String message,
   String ipAddress, int port) throws IOException {
      DatagramSocket socket = new DatagramSocket();
      InetAddress group = InetAddress.getByName(ipAddress);
      byte[] msg = message.getBytes();
      DatagramPacket packet = new DatagramPacket(msg, msg.length,
         group, port);
      socket.send(packet);
      socket.close();
   }

   public static void main(String[] args) throws IOException {
      sendUDPMessage("1º Message", "230.0.0.0", 4321);
      sendUDPMessage("2º Message", "230.0.0.0", 4321);
      sendUDPMessage("3º Message", "230.0.0.0", 4321);
      sendUDPMessage("OK", "230.0.0.0", 4321);
   }
}