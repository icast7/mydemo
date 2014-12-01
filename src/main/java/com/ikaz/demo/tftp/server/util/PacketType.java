package com.ikaz.demo.tftp.server.util;

import java.nio.ByteBuffer;

/** 
 * TFTP Packet type enum for types defined on https://www.ietf.org/rfc/rfc1350.txt
 * @author icastillejos
 * @version 0.0.1
 */
public enum PacketType {
	/*  TFTP supports five types of packets, all of which have been mentioned above:
	  opcode  operation
	    1     Read request (RRQ)
	    2     Write request (WRQ)
	    3     Data (DATA)
	    4     Acknowledgment (ACK)
	    5     Error (ERROR)
	 */
	RRQ(1), 
	WRQ(2),
	DATA(3),
	ACK(4),
	ERROR(5);
	
	private short code;

	private PacketType(int code){
		this.code = (short)code;
	}
	
	public short getCode(){
		return this.code;
	}
	
	public byte[] getTypeArray(){
		//Get packet type code as byte array
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort(this.code);
		return bb.array();
	}
	
	public static PacketType getTFTPPacketType(short code){
		for (PacketType tftpPacketType : PacketType.values()){
			if (tftpPacketType.getCode() == code){
				return tftpPacketType;
			}
		}
		//ToDo throw/handle exception if packet type is not valid
		return null;
	}
}
