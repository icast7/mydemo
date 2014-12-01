package com.ikaz.demo.tftp.server.util;

/** 
 * Transfer modes defined on https://www.ietf.org/rfc/rfc1350.txt
 * Currently only NETASCII is supported
 * @author icastillejos
 * @version 0.0.1
 */
public enum TransferMode {
	/*
	Tranfer Modes
	   Value     	Meaning	
	   netascii	"8 bit ASCII"
	   octect   "Raw 8 bit bytes"
	  - mail     Obsolete, should not be implemented
	 */
	NETASCII("netascii"), 
	OCTECT("octect");
	
	private String modeName;
	
	private TransferMode(String modeName){
		this.modeName = modeName;
	}
	
	public String getModeName(){
		return this.modeName;
	}
	
	public static TransferMode getTransferMode(String mode){
		if (NETASCII.toString().equalsIgnoreCase(mode))
			return NETASCII;
		else 
			//Default to OCTECT
			return OCTECT;
	}
}