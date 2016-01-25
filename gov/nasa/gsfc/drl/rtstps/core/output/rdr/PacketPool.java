/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.List;

/**
 * A packet pool manages a pool of packets for reuse.  It does not track
 * where the packet have gone and leaves it up to user to ensure good behavior
 * and put no longer needed packets back in the pool. 
 * 
 * 
 *
 */
public interface PacketPool {

	/**
	 * Get a packet of size 
	 * @param size size in bytes needed hold packet including header and body
	 * @return Packet
	 * @throws RtStpsException If size is not legal
	 */
	public Packet get(int size) throws RtStpsException;
	
	/**
	 * Give the packet to the pool for storage
	 * @param packet
	 * @throws RtStpsException If illegal size
	 */
	public void put(Packet packet) throws RtStpsException;
	
	
	/**
	 * Take all the packets on the supplied list and hang them on the internal pool list
	 * @param packetList the list of packets to put on the pool
	 * @throws RtStpsException if this fails
	 */
	public void flush(List<Packet> packetList) throws RtStpsException;

	/**
	 * Clean up the pool in some way
	 */
	public void preen();
	
	/**
	 * Return all packets in the pool back to the Java heap.
	 */
	public void drain();
	
	public void removeEnd(List<Packet> packetList,int startIndex)throws RtStpsException;
}
