package com.ikaz.demo.tftp.server.util;

/**
 * Error code enum for error codes defined on https://www.ietf.org/rfc/rfc1350.txt
 * @author icastillejos
 * @version 0.0.1
 */
public enum ErrorCode {
	/*
	Error Codes
	   Value     Meaning
	
	   0         Not defined, see error message (if any).
	   1         File not found.
	   2         Access violation.
	   3         Disk full or allocation exceeded.
	   4         Illegal TFTP operation.
	   5         Unknown transfer ID.
	   6         File already exists.
	   7         No such user. 
	 */
	NOT_DEFINED((short) 0, "Not defined, see error message (if any)."), 
	FILE_NOT_FOUND((short) 1, "File not found."),
	ACCESS_VIOLATION((short)2, "Access violation."),
	DISK_FULL((short) 3, "Disk full or allocation exceeded."),
	ILLEGAL_OPERATION((short) 4, "Illegal TFTP operation."),
	UNKNOWN_TRANSFER_ID((short) 5, "Unknown transfer ID."),
	FILE_EXISTS((short) 6, "File already exists."),
	NO_SUCH_USER((short) 7, "No such user.");
	
	private short code;
	private String msg;
	
	private ErrorCode(short code, String msg){
		this.code = code;
		this.msg = msg;
	}
	
	public short getCode(){
		return this.code;
	}
	
	public String getMsg(){
		return this.msg;
	}
	
	//Static Methods
	public static ErrorCode getErrorCode(short code){
		for (ErrorCode errorCode : ErrorCode.values()){
			if (errorCode.getCode() == code){
				return errorCode;
			}
		}
		//If no match is found return not defined
		return ErrorCode.NOT_DEFINED;
	}
}
