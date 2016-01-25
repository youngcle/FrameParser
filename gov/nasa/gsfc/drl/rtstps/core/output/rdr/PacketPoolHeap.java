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
 * This is not a true pool, it gets packets from the heap and gives them back to the heap
 * if they are in a list...
 * 
 * This one works the best, looks like this whole effort was just competing against the Java heap.
 * 
 * KISS I guess.
 * 
 * 
 *
 */
public class PacketPoolHeap implements PacketPool {

	private long packetsCreated = 0;
	private long packetsDestroyed = 0;
	
	
	public PacketPoolHeap(String string, Stats stats) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void drain() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush(List<Packet> packetList) throws RtStpsException {
		packetsDestroyed += packetList.size();
		while (packetList.size() > 0) {
			packetList.remove(0);
		}
		
	}

	@Override
	public Packet get(int size) throws RtStpsException {
		++packetsCreated;
		return new Packet(size);
	}

	@Override
	public void preen() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void put(Packet packet) throws RtStpsException {
		packet = null;  // not sure this does anything, call flush...
		packetsDestroyed++;
	}
	
	public String toString() {
		return "Packets Created [" + packetsCreated + 
		       "], Packets Destroyed [" + packetsDestroyed + 
		       "], Packets Delta [" + (packetsCreated-packetsDestroyed) + "]";
	}
	@Override
	public void removeEnd(List<Packet> packetList,int startIndex) throws RtStpsException {
		while(startIndex<packetList.size())
		 packetList.remove(packetList.size()-1);
	}
	

}
