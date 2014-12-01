package com.ikaz.demo.tftp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import com.ikaz.demo.tftp.server.util.ErrorCode;
import com.ikaz.demo.tftp.server.util.packet.ACKPacket;
import com.ikaz.demo.tftp.server.util.packet.DATAPacket;
import com.ikaz.demo.tftp.server.util.packet.ERRORPacket;
import com.ikaz.demo.tftp.server.util.packet.TFTPPacket;
import com.ikaz.demo.tftp.server.util.packet.XRQPacket;

/**
 * This class implements a TFTP SERVER (WIP)
 * This class creates a DatagramChannel to send and receive TFTP packets to/from clients
 * @author icastillejos
 */
public class TFTPServer {
		//public final static int PORT = 7;
	    //Using port 5007 to run without sudo
		public final static int PORT = 5007;
		public final static int MAX_PACKET_SIZE =  1024;
		public final static int MAX_PAYLOAD_SIZE =  512;
		
		public static void main(String[] args){
			try (DatagramChannel channel = DatagramChannel.open()) {
				System.err.println("Server started at port " + PORT + "...");
				DatagramSocket socket = channel.socket();
				SocketAddress address = new InetSocketAddress(PORT);
				socket.bind(address);
				ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
				while(true){
					SocketAddress client = channel.receive(buffer);
					System.out.println("Received " + buffer.position() + " bytes");
					TFTPPacket response = buildResponsePackage(buffer);	

					//Clear the buffer
					buffer.clear();
					
					//Respond
					buffer.put(response.getArray());
					System.out.println("Sent " + buffer.position() + " bytes");
					System.out.println("Sent Packet Type:" +  response.getPacketType());
					buffer.flip();
					channel.send(buffer, client);
					buffer.clear();
				}
			} catch (IOException ex){
				System.err.println(ex);
			}
		}

		private static TFTPPacket buildResponsePackage(ByteBuffer buffer){
			//Build received packet
			TFTPPacket p = TFTPPacket.getReceivedPacket(buffer);
			//Create response packet
			switch(p.getPacketType()) {
				case RRQ:
					//Return DATAPACKET with Block #
					String fileNameR= ((XRQPacket)p).getFileName();
					File fR = new File("TFTPFolder", fileNameR);
					
					try (FileChannel in = new FileInputStream(fR).getChannel()){
						ByteBuffer readBuffer = ByteBuffer.allocate(MAX_PAYLOAD_SIZE);
						in.read(readBuffer);
						return new DATAPacket((short) 0, readBuffer.array());
					} catch (FileNotFoundException e) {
						//Print stack trace and return ERROR if file not found
						e.printStackTrace();
						//Return ERROR if file DOES NOT exists
						return new ERRORPacket(ErrorCode.FILE_NOT_FOUND);
					} catch (IOException e) {
						e.printStackTrace();
					}
				case WRQ:
					String fileNameW= ((XRQPacket)p).getFileName();
					
					if (Files.exists(Paths.get("TFTPFolder", fileNameW), LinkOption.NOFOLLOW_LINKS)) {
						//Return ERROR if file exists
						return new ERRORPacket(ErrorCode.FILE_EXISTS);
					} else {
						//Return ACK with Block  0
						return new ACKPacket((short) 0);
					}
				case ACK:
					//Not implemented yet
					//ToDo: Under normal circumstances this would send the next block, based on the block number, 
					//in the TFTP file to the client.
					//For the WS implementation this probably will not do anything (WIP)
					//
				case DATA:
					//Not implemented yet
					//ToDo:Write new TFTP Files
				case ERROR:
					//Not implemented yet
					//ToDo:Terminate connection to the client
				default:
					break;
			}	
			return null;
		}
}