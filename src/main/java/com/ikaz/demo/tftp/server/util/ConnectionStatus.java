package com.ikaz.demo.tftp.server.util;

/**
 * This class represents the TFTP server status for a single connection (WIP)
 * [NOT USED FOR WS] 
 * @author icastillejos
 */
public class ConnectionStatus{
	public enum Status {IDLE, READING, WRITING};
	
	private Status status;
	private short blockNumber; 

	public ConnectionStatus(Status status,short blockNumber){
		
	}
	
	public Status getStatus() {
		return status;
	}

	public short getBlockNumber() {
		return blockNumber;
	}
}
