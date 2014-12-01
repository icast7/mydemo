package com.ikaz.demo.tftp.server.util.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.ikaz.demo.tftp.server.util.ErrorCode;
import com.ikaz.demo.tftp.server.util.PacketType;
import com.ikaz.demo.tftp.server.util.TransferMode;

public abstract class TFTPPacket {
	private byte[] array;
	private PacketType packetType;
	protected final byte[] separator = new byte[] {0};
	
	/*
	 TFTP Formats
	   Type   Op #     Format without header
	
	          2 bytes    string   1 byte     string   1 byte
	          -----------------------------------------------
	   RRQ/  | 01/02 |  Filename  |   0  |    Mode    |   0  |
	   WRQ    -----------------------------------------------
	          2 bytes    2 bytes       n bytes
	          ---------------------------------
	   DATA  | 03    |   Block #  |    Data    |
	          ---------------------------------
	          2 bytes    2 bytes
	          -------------------
	   ACK   | 04    |   Block #  |
	          --------------------
	          2 bytes  2 bytes        string    1 byte
	          ----------------------------------------
	   ERROR | 05    |  ErrorCode |   ErrMsg   |   0  |
	          ----------------------------------------
	 */
	
	/**
	 * TFTPPacket Protected constructor
	 * @param packetType
	 */
	protected TFTPPacket(PacketType packetType) {
		super();
		this.packetType = packetType;
	}
	
	
	public static TFTPPacket getReceivedPacket(ByteBuffer buffer){
		//Prepare for reading
		if (buffer.position() != 0)
			buffer.flip();
		
		//Get the packet type
		short packetTypeShort = buffer.getShort();

		//Get a byte[numBytes - 2] packet body
		byte[] packetBody =  new byte[buffer.limit() - 2];

		buffer.get(packetBody);
		
		try {
			PacketType packetType = PacketType.getTFTPPacketType(packetTypeShort);
			System.out.println("Received Packet Type: " + packetType);
			switch(packetType){
			case RRQ:
			case WRQ:
				int separatorIndex = Arrays.binarySearch(packetBody, (byte)0);
				
				byte[] filenameBytes = Arrays.copyOfRange(packetBody, 0, separatorIndex);
				String fileName = new String(filenameBytes, "US-ASCII");
				
				byte[] modeBytes = Arrays.copyOfRange(packetBody, separatorIndex, packetBody.length);
				String mode = new String(modeBytes, "US-ASCII");
				
				return new XRQPacket(packetType, fileName, TransferMode.getTransferMode(mode));		
			case DATA:
				ByteBuffer blockNum = ByteBuffer.wrap(Arrays.copyOfRange(packetBody, 0, 2));
				short blockNumDATA = blockNum.getShort();
				return new DATAPacket(blockNumDATA, Arrays.copyOfRange(packetBody, 2, packetBody.length));
			case ACK:
				short blockNumACK = ByteBuffer.wrap(Arrays.copyOfRange(packetBody, 0, 2)).getShort();
				return new ACKPacket(blockNumACK);
			case ERROR:
				short errorCode = ByteBuffer.wrap(Arrays.copyOfRange(packetBody, 0, 2)).getShort();
				System.out.println("ERROR CODE: " + errorCode);
				return new ERRORPacket(ErrorCode.getErrorCode(errorCode));
			default:
				//Return ERROR for unknown packet type
				return new ERRORPacket(ErrorCode.ILLEGAL_OPERATION);
			}
		} catch (UnsupportedEncodingException ex) {
			//Return ERROR packet
			ex.printStackTrace();
			return new ERRORPacket(ErrorCode.ILLEGAL_OPERATION);
		}
	}
	
	/*
	 * Protected static method to build a TFTPPacket byte[] from multiple byte[]s
	 * */
	protected void setArray(byte[]... arrays) {
		int packetSize = 0;
		
		//Calculate packet size
		for(int i = 0; i < arrays.length; i++){
			packetSize += arrays[i].length;
		}
		
		//Build byte[]
		ByteBuffer bbuffer = ByteBuffer.allocate(packetSize);
		for(int j = 0; j < arrays.length; j++){
			bbuffer.put(arrays[j]);
		} 
		this.array = bbuffer.array();
	}
	
	/*
	 * Public methods
	 * */
	
	public byte[] getArray() {
		return this.array;
	}

	public PacketType getPacketType() {
		return packetType;
	}
}