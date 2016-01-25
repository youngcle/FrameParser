/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketI;

import java.util.Iterator;


/**
 * Using the StaticHeader of a particular RawApplicationPackets item, read the contents using a sequential access style interface.
 * This class simply indexes into the StaticHeader packet region and read each packet in a sequential manner.
 * Note that this classes constructor is package private, and a method in each StaticHeader must be used 
 * to create it.
 * 
 * 
 *
 */
public class SequentialPacketReader implements Iterator<PacketI> {

	private PacketFactoryI factory;
	private byte[] data;
	private int index = 0;
	
	/**
	 * Make a new reader by providing PacketFactory (some way to create new Packets) and the
	 * StaticHeader of interest
	 * @param factory a way to create new Packets
	 * @param staticHeader the StaticHeader from a RawApplicationsPackets of interest
	 */
	SequentialPacketReader(PacketFactoryI factory, StaticHeader staticHeader) {
		this.data = staticHeader.getData();
		this.index = staticHeader.getApStorageOffset();
		this.factory = factory;
	}
	
	public int getIndex() {
		return this.index;
	}
	public int getDataLength() {
		return data.length;
	}
	/**
	 * Return a Packet constructed from an index into the buffer
	 * @return the next Packet
	 */
	@Override
	public PacketI next() {

		PacketI p = factory.make(index, data);

		index += getPacketSize();
		
		
		return p;
	}

	/**
	 * Return true or false if another packet is available
	 */
	@Override
	public boolean hasNext() {
		return (index < data.length); // FIXME this technically should probably be nextPktPos field in the static header?
	}



	@Override
	public void remove() {
		throw new RtStpsRuntimeException("No implemented or supported");
	}
	
	private int getAppID() {
		int appid = (((data[index] << 8) & 0x0ff00) | (data[index+1] & 0x0ff));
		appid &= 0x7ff;
		return appid;
	}
	
	// this returns the length field of from the CCSDS header
	private int getPacketLength() {
		int packetLen = (((data[index+4] << 8) & 0x0ff00) | (data[index+5] & 0x0ff));
		packetLen &= 0x0ffff;
		return packetLen;
	}
	
	// this returns the full length of the packet by taking the headers length field
	// adding the header size to it and then adding 1... 
	private int getPacketSize() {
		return (6 + getPacketLength() + 1);
	}
	


}
