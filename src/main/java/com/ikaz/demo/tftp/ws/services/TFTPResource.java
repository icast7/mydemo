package com.ikaz.demo.tftp.ws.services;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import com.ikaz.demo.tftp.ws.domain.*;
import com.ikaz.demo.tftp.server.util.packet.*;
import com.ikaz.demo.tftp.server.util.*;
import com.ikaz.demo.tftp.ws.services.exception.*;



/**
 * This class contains the TFTP WS Resources
 * @author icastillejos
 */
@Path("/tftp")
public class TFTPResource {
	public final static int PORT = 5007;
	public final static int MAX_PACKET_SIZE =  1024;
	public final static String TFTP_SERVER =  "localhost";
	
	@GET
	@Produces("application/xml")
	public String getTFTPFiles(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			 + "<response>This is a response to a GET method call to test connectivity with the client"
			 + "</response>";
	}
	
	/**
	 * Read file from TFTP, defaulted to reading the first file block (512B) as defined in the TFTP RFC 
	 * @param i 
	 * @param path: File path
	 * @return Response
	 */
	@GET
	@Path("{path}")
	@Produces("application/xml")
	public Response readTFTPFile(@PathParam("path") String path,
								@Context UriInfo uriInfo){
		return readTFTPFile(path, 0, uriInfo);
	}
	
	/**
	 * Read file from TFTP, this method reads the 'n' file block (512B) as defined in the TFTP RFC 
	 * @param path: File path
	 * @param blockNumber: File block number
	 * @return Response
	 */
	@GET
	@Path("{path}/{blockNumber}")
	@Produces("application/xml")
	public Response readTFTPFile(@PathParam("path") String path, 
								@PathParam("blockNumber") int blockNumber,
								@Context UriInfo uriInfo){
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		XRQPacket readPacket = new XRQPacket(PacketType.RRQ, path, TransferMode.NETASCII);
		//Get packet from TFTP Server
		TFTPPacket p = sendAndReceiveTFTPPacket(readPacket);
		TFTPFile tftpFile = new TFTPFile();
		if (p.getPacketType() ==  PacketType.DATA){
			String s = new String(((DATAPacket)p).getData());
			tftpFile.setFilePath(path);
			tftpFile.setStringContent(s);
			//Add links to other sections of the file
			ArrayList<Link> links = new ArrayList<Link>();
			if (((DATAPacket)p).getBlockNumber() > 0){	
				//Return link to the previous file block
				int previous = ((DATAPacket)p).getBlockNumber() - 1;
				URI previousBlock = builder.clone().build(path, previous);
				Link previousBlockLink = Link.fromUri(previousBlock).rel("previous").type("application/xml").build();
	            links.add(previousBlockLink);
			}
			//ToDo: This needs to check with the TFTP server to see what the MAX valid block number is
			if (((DATAPacket)p).getBlockNumber() >= 0){
				//Return link to the next file block
				int next = ((DATAPacket)p).getBlockNumber() + 1;
				URI nextBlock = builder.clone().build(path, next);
				System.out.println("Next File Block:" + nextBlock.toASCIIString());
				Link nextBlockLink = Link.fromUri(nextBlock).rel("next").type("application/xml").build();

	            links.add(nextBlockLink);
			}
			tftpFile.setLinks(links);
			Response.ResponseBuilder rb = Response.ok(tftpFile);
			return rb.build();
		}
		return null;
		
	}
	
	/**
	 * Write file in TFTP, this method returns an ACK that the file was created 
	 * @param path: File path
	 * @param tftpFile: File contents
	 * @return Response
	 */
	@POST
	@Path("{path}")
	@Consumes("application/xml")
	public Response writeTFTPFile(@PathParam("path") String path, TFTPFile tftpFile) {
		XRQPacket writePacket = new XRQPacket(PacketType.WRQ, path, TransferMode.NETASCII);
		//Get packet from TFTP Server
		TFTPPacket p = sendAndReceiveTFTPPacket(writePacket);
		int responseBlockNumber = 0;
		if (p.getPacketType() ==  PacketType.ACK){
			responseBlockNumber = ((ACKPacket)p).getBlockNumber();
		}
		TFTPFile newtftpFile = new TFTPFile();
		newtftpFile.setBlockNumber(responseBlockNumber);
		return Response.created(URI.create("/tftp/" + newtftpFile.getFilePath() + "/" + newtftpFile.getBlockNumber())).build();
	}

	@PUT
	@Path("{id}")
	@Consumes("application/xml")
	public void updateFile(@PathParam("id") int id, TFTPFile update){
		TFTPFile file = new TFTPFile();
		file.setFilePath(update.getFilePath());
	}
	
	
	/**
	 * This method sends and receives a TFTP packet from/to the TFTP server
	 * @param TFTPPacket to send
	 * @return received TFTPPacket
	 */
	private TFTPPacket sendAndReceiveTFTPPacket(TFTPPacket tftpPacket) {
		try (DatagramSocket socket = new DatagramSocket()){
			InetAddress server = InetAddress.getByName("localhost");
			//Send packet byte[]
			byte[] data = tftpPacket.getArray();
			DatagramPacket output = new DatagramPacket(data, data.length, server, 5007);
			socket.send(output);
			
			//Receiving up to 1KB, in theory the server should not send packets larger than ~512+
			byte[] buffer = new byte[1024];
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			socket.receive(dp);
			ByteBuffer bb = ByteBuffer.wrap(dp.getData());
			TFTPPacket response = TFTPPacket.getReceivedPacket(bb);	
			
			//Handle ERROR Packets from Server
			if (response.getPacketType() == PacketType.ERROR){
				handleErrorPacket((ERRORPacket)response);
			}
			//Return all other Packets
			return response;
		} catch (Exception e){
			e.printStackTrace();
		} 
		//If no valid packet is received return null
		return null;
	}
	
	/**
	 * This method handles ERROR packets received from the TFTP server
	 * @param response: The ERROR packet
	 * @throws Exception: Specific exception mapped to the ERROR codes defined by the TFTP RFC
	 */
	private void handleErrorPacket(ERRORPacket response) throws Exception {
		ErrorCode errorCode =  ErrorCode.getErrorCode(response.getErrorCode());
		switch(errorCode) {
			case FILE_NOT_FOUND:
				throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND.getMsg());
			case FILE_EXISTS:
				throw new FileFoundException(ErrorCode.FILE_EXISTS.getMsg());
			default:
				throw new Exception("An specific Exception has not been implemented for the TFTP Error: " + errorCode.getMsg());
			//ToDo: Add exceptions for each TFTP ERROR code
		}
	}
}
