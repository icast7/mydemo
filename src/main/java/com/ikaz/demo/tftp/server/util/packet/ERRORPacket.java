package com.ikaz.demo.tftp.server.util.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.ikaz.demo.tftp.server.util.ErrorCode;
import com.ikaz.demo.tftp.server.util.PacketType;

/**
 * This represents an ERROR packet
 *         		2 bytes  2 bytes        string    1 byte
 *	          ----------------------------------------
 *	   ERROR | 05    |  ErrorCode |   ErrMsg   |   0  |
 *	          ----------------------------------------
 * @author icastillejos
 * @version 0.0.1
 */
public final class ERRORPacket extends TFTPPacket {
	private short errorCode;
	private String errorMsg;	
	
	public ERRORPacket(ErrorCode errorCode){
		super(PacketType.ERROR);
		this.errorCode = (short) errorCode.getCode();

		byte[] packetTypeArray = PacketType.ERROR.getTypeArray();	
		
		ByteBuffer errorCodeBB = ByteBuffer.allocate(2);
		errorCodeBB.putShort(errorCode.getCode());
		byte[] errorCodeBytes = errorCodeBB.array();
	
		//Set default error message
		byte[] errorMsgBytes = ErrorCode.NOT_DEFINED.getMsg().getBytes(Charset.defaultCharset());
		try{
			//Try getting the msg with US-ASCII encoding
			ErrorCode err = ErrorCode.getErrorCode(errorCode.getCode());
			errorMsgBytes = err.getMsg().getBytes("US-ASCII");
		}
		catch (UnsupportedEncodingException ex){
			ex.printStackTrace();
		}
		this.setArray(packetTypeArray, errorCodeBytes, errorMsgBytes);
	}

	public short getErrorCode() {
		return errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
}