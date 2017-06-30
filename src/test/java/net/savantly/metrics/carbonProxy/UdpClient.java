package net.savantly.metrics.carbonProxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

public class UdpClient {
	
	final int port;
	
    private DatagramSocket socket;
    private InetAddress address;
 
    private byte[] buf;
    
    public UdpClient(int serverPort) throws SocketException, UnknownHostException {
		this.port = serverPort;
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
	}
 
    public void sendMessage(String msg) throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet 
          = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        /*socket.receive(packet);
        String received = new String(
          packet.getData(), 0, packet.getLength());
        return received;*/
    }
 
    public void close() {
        socket.close();
    }
}