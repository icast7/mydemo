package com.ikaz.demo.tftp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import com.ikaz.demo.tftp.server.util.PacketType;
import com.ikaz.demo.tftp.server.util.TransferMode;
import com.ikaz.demo.tftp.server.util.packet.DATAPacket;
import com.ikaz.demo.tftp.server.util.packet.TFTPPacket;
import com.ikaz.demo.tftp.server.util.packet.XRQPacket;

/**
 * This class was built for testing the TFTP SERVER without having to interact with the WS
 * This class creates a DatagramChannel to send and receive TFTP packets to/from the server
 * [NOT USED FOR WS] 
 * @author icastillejos
 */
public class TFTPClient {
	//public final static int PORT = 7;
	//Using port 5007 to run without sudo
	public final static int PORT = 5007;
	
	private final static int LIMIT = 3;
	private final static int BUFFER_SIZE = 1024;
	
	public static void main(String[] args){
		String hostname = "localhost";
		SocketAddress remote;
		try {
			if (args.length > 0)
				hostname = args[0];
				
			remote = new InetSocketAddress(hostname, PORT);
		} catch (RuntimeException ex){
			System.err.println("Usage: java UDPEchoClientWithChannels host");
			return;
		}
		
		try (DatagramChannel channel = DatagramChannel.open()){
			channel.configureBlocking(false);
			channel.connect(remote);
			
			Selector selector = Selector.open();
			channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			
			
			int n = 0;
			int numbersRead = 0;
			while (true){
				if (numbersRead == LIMIT) 
					break;
				//Wait 60 secs for a connection
				selector.select(60000);
				Set<SelectionKey> readyKeys = selector.selectedKeys();
				if (readyKeys.isEmpty() && n == LIMIT){
					//All packets written and not expecting anymore
					break;
				} else {
					Iterator<SelectionKey> iterator = readyKeys.iterator();
					while(iterator.hasNext()){
						SelectionKey key = (SelectionKey) iterator.next();
						iterator.remove();
						
						if (key.isReadable()){
							buffer.clear();
							channel.read(buffer);
							//Build received packet
							TFTPPacket p = TFTPPacket.getReceivedPacket(buffer);
							switch (p.getPacketType()){
								case DATA:
									String o = new String(((DATAPacket)p).getData(), "US-ASCII");
									System.out.println(((DATAPacket)p).getBlockNumber() + "|||" + o);
								case ACK:
									String os = new String(((DATAPacket)p).getData(), "US-ASCII");
									System.out.println(((DATAPacket)p).getBlockNumber() + "|||" + os);
								case ERROR:
									
								default:	
							}
							numbersRead ++;
						}
						if (key.isWritable()){
							buffer.clear();
							XRQPacket x = new XRQPacket(PacketType.RRQ, "test.txt", TransferMode.NETASCII);
							buffer.put(x.getArray());
							buffer.flip();
							channel.write(buffer);
							System.out.println("Wrote: " + buffer.capacity());
							n++;
							if (n == LIMIT){
								////All packets written and not expecting anymore
								key.interestOps(SelectionKey.OP_READ);
							}
						}
					}
				}				
			}
			System.out.println("Echoed " + numbersRead + " out of " + LIMIT + " sent");
			System.out.println("Success rate: " +  100.0 * numbersRead/LIMIT + "%");
		} catch (IOException ex) {
			System.err.println(ex);
		} catch (Exception ex){
			System.err.println(ex);
		}
	}
}
