/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
/** 
 * Write or read packets from the ApStorageArea area
 * 
 *
 */
public class ApStorageArea {
	private CopyOnWriteArrayList<Packet> packets;
	
	// note these are only used for the "read" side of this class
	private int offset;
	private byte[] data;

	private int apStorageSize = 0;
	
	/**
	 * Constructor for manipulating the ApStorageArea, in preparation for writing to 
	 * a memory block through the write method.  Use this in conjunction with the write method.
	 * @param packets a list of packets to write
	 */
	public ApStorageArea(CopyOnWriteArrayList<Packet> packets) {
		this.packets = packets;
		
		// calculate the size needed to store it in a contiguous byte array
		for (Packet p : packets) {
			apStorageSize  += p.getSize();
		}
	}
	
	/**
	 * Constructor to read out of the ApStorage area, use this conjunction with the read method
	 * below.
	 * @param data a <code>byte</code> array of packet data, including header of ApStorage area
	 * @param offset an offset into the array where the packet data starts
	 */
	public ApStorageArea(byte[] data, int offset, int end) {
		this.offset = offset;
		this.data = data;
		apStorageSize = offset-end;
	}
	
	/**
	 * Read up to size amount of packets.  Note that this assumes the packets are uniform in the data.  The packet data is not copied.
	 * @param offset an offset past the header in the ApStorage area to the raw data
	 * @param size the amount to read in bytes
	 * @return packets a list of new Packets containing the data -- note the data is not copied into the new packets but simply points to it
	 */
	public List<Packet> read(int offset, int size) {
		int start = this.offset + offset;
		int end = start + size;
		List<Packet> packets = new LinkedList<Packet>();
		
		//System.out.println("start: " + start + " end: " + end + " size: " + size);
		while (start < end) {
			//System.out.println("AppId: " + Packet.getApplicationId(start, data) + " Length: " + Packet.getPacketSize(start, data));
			packets.add(new Packet(start, data));  // note this is a "no copy packet" for now...
			start += Packet.getPacketSize(start, data);
		}
		
		return packets;
	}
	/**
	 * Write the packet list to the specified data buffer at the given offset.  The data buffer is assumed
	 * to be of sufficient size to fit the packets.
	 * @param data the data to be written
	 * @param offset an offset into that data
	 * @return the new offset after the data written
	 */
	public int write(byte[] data, int offset) {
	//System.out.println("Counter based Offset into storage area: " + offset);
	    
	    
		for (Packet p : packets) {

		    
		    	try 
		    	{
		    	    System.arraycopy(p.getData(), 0, data, offset, p.getSize());	    
		    	}
		    	
		    	catch (ArrayIndexOutOfBoundsException e)
		    	{	    	    
		    	    data = java.util.Arrays.copyOf(data, data.length + p.getSize());
		    	    System.arraycopy(p.getData(), 0, data, offset, p.getSize());
		    	    
		    	}
		    	
		    	offset += p.getSize();

		}
		return offset;
	}
	
	/**
	 * The size in bytes needed to store the packets of interest in a contiguous byte array
	 * @return
	 */
	public int getSize() {
		return apStorageSize;
	}
}
