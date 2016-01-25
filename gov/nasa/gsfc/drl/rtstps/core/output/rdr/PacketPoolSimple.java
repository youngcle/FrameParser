/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.LinkedList;
import java.util.List;

/**
 * A very simplistic pooling class for packets for use by the HDF output module only.
 * It assumes good behavior by the user -- packets are created and given away and then
 * the expectation is the user will put them back in the pool when done... if not they
 * are lost to the garbage collector.
 * 
 * This implements a single list of fixed max sized packets which are always resized to
 * asked for size.   Then when they are put back on the queue, they are resized back to
 * their maximum.
 * 
 * Chews up memory ... essentially deprecated.
 * 
 * 
 *
 */
@Deprecated
public class PacketPoolSimple implements PacketPool {
	private static final int CCSDSMaxSize = 65535+7; // packet length field: 0xffff + 6 (HDR) + 1
	private static final int CCSDSMinSize = 7;  // packet length field: 0 + 6 (HDR) + 1
	private static LinkedList<Packet> pool = new LinkedList<Packet>();
	private static int totalCreated = 0;
	
	/**
	 * Ask for a get a packet of size 
	 * @param size size in bytes needed hold packet including header and body
	 * @return Packet
	 * @throws RtStpsException If size is not legal
	 */
	public Packet get(int size) throws RtStpsException {
		if ((size < CCSDSMinSize) || (size > CCSDSMaxSize)) {
			throw new RtStpsException("Illegal Requested Packet size [" + size + "] must be within CCSDS legal limits.");
		}
		
		Packet packet = null;
		if (pool.isEmpty()) {
			packet = new Packet(size);
			//packet.reset(size);
			System.out.println("Total created: " + ++totalCreated);
		} else {
			packet = pool.pop();
			packet.reset(size);
		}
		return packet; // assumption here is that it will be put back on... when done, the pool does not know you got this
	}
	
	/**
	 * Give the packet to the pool for storage
	 * @param packet
	 * @throws RtStpsException If illegal size
	 */
	public void put(Packet packet) throws RtStpsException {
		// a probably pointless paranoid check
		int size = packet.getSize();
		if ((size < CCSDSMinSize) || (size > CCSDSMaxSize)) {
			throw new RtStpsException("Illegal Packet size [" + size + "] being pooled, must be within CCSDS legal limits.");
		}
		pool.push(packet);
		
		System.out.println("**** Total On Pool: " + pool.size());
	}
	
	/**
	 * Take all the packets on the supplied list and hang them on the internal pool list
	 * @param packetList the list of packets to put on the pool
	 * @throws RtStpsException
	 */
	public  void flush(List<Packet> packetList) throws RtStpsException {
		while (packetList.size() > 0) {
			Packet p = packetList.remove(0);
			this.put(p);
		}
	}

	@Override
	public void drain() {
		while (pool.size() > 0) {
			pool.pop();
		}
	}

	@Override
	public void preen() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeEnd(List<Packet> packetList,int startIndex)throws RtStpsException {
		
	}

	
}
