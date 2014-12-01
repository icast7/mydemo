package com.ikaz.demo.tftp.server.util.packet;

import java.nio.ByteBuffer;

import com.ikaz.demo.tftp.server.util.PacketType;

/**
 * This class represents an ACK packet
 *         2 bytes    2 bytes
 *         -------------------
 *  ACK   | 04    |   Block #  |
 *         --------------------
 * @author icastillejos
 * @version 0.0.1
 */
public final class ACKPacket extends TFTPPacket {
	private short blockNumber;

	public ACKPacket(short blockNumber) {
		super(PacketType.ACK);
		this.blockNumber = blockNumber;
			
		byte[] packetTypeArray = PacketType.ACK.getTypeArray();	
		
		ByteBuffer blockNumberBB = ByteBuffer.allocate(2);
		blockNumberBB.putShort(this.blockNumber);
		byte[] blockNumberBytes = blockNumberBB.array();
		
		this.setArray(packetTypeArray, blockNumberBytes);
	}

	public short getBlockNumber() {
		return blockNumber;
	}
}
