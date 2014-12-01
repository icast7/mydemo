package com.ikaz.demo.tftp.server.util.packet;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.ikaz.demo.tftp.server.util.PacketType;
import com.ikaz.demo.tftp.server.util.TransferMode;

/**
 * This represents an *RQ packet
 *  		2 bytes    string   1 byte     string   1 byte
 *	        -----------------------------------------------
 *	 RRQ/  | 01/02 |  Filename  |   0  |    Mode    |   0  |
 *	 WRQ    -----------------------------------------------
 *  
 * @author icastillejos
 * @version 0.0.1
 */
public final class XRQPacket extends TFTPPacket {
	private TransferMode transferMode;
	private String fileName;
	
	public XRQPacket(PacketType packetType, String fileName, TransferMode transferMode) {
		super(packetType);
		this.fileName = fileName;
		this.transferMode = transferMode;
		//ToDo
		//Throw exception if packetType is not RRQPacket or WRQPacket
		byte[] packetTypeArray = packetType.getTypeArray();	
		//Get data with default encoding
		byte[] fileNameArray = fileName.getBytes(Charset.defaultCharset());
		byte[] modeArray = transferMode.getModeName().getBytes(Charset.defaultCharset());
		//Try getting data with US-ASCII encoding
		try {
			fileNameArray = fileName.getBytes("US-ASCII");
			modeArray = transferMode.getModeName().getBytes("US-ASCII");
		}
		catch (UnsupportedEncodingException ex){
			ex.printStackTrace();
		}
	
		this.setArray(packetTypeArray, fileNameArray, separator,modeArray, separator);
	}
	
	public String getFileName(){
		return this.fileName;
	}
	
	public TransferMode getMode(){
		return this.transferMode;
	}
}
