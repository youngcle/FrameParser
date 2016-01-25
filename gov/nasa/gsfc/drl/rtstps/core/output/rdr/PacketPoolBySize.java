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
 * A very simplistic pooling class for packets for use by the HDF output module only.
 * It assumes good behavior by the user -- packets are created and given away and then
 * the expectation is the user will put them back in the pool when done... if not they
 * are lost to the garbage collector.
 * The packets are put on array of lists by packet size.  When pulled off the queue the size
 * is used as an index.  If there are no packets at that size, the array is searched by adding
 * one to size until it reaches the end.  If some packet is found, it is reset to the requested size.
 * This does not cost too much, no new memory is needed.
 * If on the other hand this fails, then the search proceeds from the first smallest index (7) to 
 * the original size.   If a packet is found, it is reset to the new size.  This results in new
 * memory being created inside of the Packet class itself.
 * If both of these fail, then the Java heap is used.
 * 
 *
 */
public class PacketPoolBySize implements PacketPool {
	private static final int CCSDSMaxSize = 65535+7; // packet length field: 0xffff + 6 (HDR) + 1
	private static final int CCSDSMinSize = 7;  // packet length field: 0 + 6 (HDR) + 1
	private Packets[] pool = new Packets[PacketPoolBySize.CCSDSMaxSize+1];
	private long totalOnPool = 0;
	private long totalCreated;
	private long totalMemory;
	private String whoAmI;
	private Stats stats = null;
	private boolean sciencePool = false;
	private int timeToClearCounter = 0;
	private final int timeToClear = 10000;  // guess
	
	/**
	 * init, create and initialize the pool
	 */
	public PacketPoolBySize(String poolName, Stats stats) {
		// CCSDS packets cannot be less 7 bytes in overall length
		
		for (int i = CCSDSMinSize; i < pool.length; i++) {
			pool[i] = new Packets();
		}
		whoAmI = poolName;
		if (whoAmI.toLowerCase().contains("science")) {
			sciencePool = true;
		}
		this.stats = stats;
	}
	
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
		
		// search, try to find something above it in size...
		// that's makes the reset efficient...
		//
		Packet packet = findPacketBySize(size);

		return packet; // assumption here is that it will be put back on... when done, the pool does not know you got this
	}
	



	/**
	 * Give the packet to the pool for storage
	 * @param packet
	 * @throws RtStpsException If illegal size
	 */
	public void put(Packet packet) throws RtStpsException {

		// instead of using the size of the packet, use its true data length
		// this may avoid needless reallocation of packets as the sizes bobble around
		
		int size = packet.getData().length;
		
		// a probably pointless paranoid check
		if ((size < CCSDSMinSize) || (size > CCSDSMaxSize)) {
			throw new RtStpsException("Illegal Packet size [" + size + "] being pooled, must be within CCSDS legal limits.");
		}
		pool[size].push(packet);
		++totalOnPool;
		statPacketPool();
		pool[size].touched++;
	}
	
	/**
	 * Take all the packets on the supplied list and hang them on the internal pool list
	 * @param packetList the list of packets to put on the pool
	 * @throws RtStpsException
	 */
	public void flush(List<Packet> packetList) throws RtStpsException {
		while (packetList.size() > 0) {
			Packet p = packetList.remove(0);
			this.put(p);
		}
		
		// flush is usually called after a write... so after that, try to clean the list
		// of any dead wood 
		preen();
	}
	
	/**
	 * Send little used packets back to the heap
	 */
	public void preen() {
		
		for (int i = CCSDSMinSize; i < CCSDSMaxSize; i++) {
			if ((pool[i].touched <= pool[i].size()) && (pool[i].size() > 0)) {
				Packets packets = pool[i];
				while (packets.size() > 0) {
					packets.pop();
				}
			}
			pool[i].touched = 0; //pool[i].size();  // reset touched to the size of each list at each size index
		}
	}


	
	@Override
	public void drain() {
		for (int i = 0; i < pool.length; i++) {
			Packets packets = pool[i];
			if (packets != null) {
				while (packets.size() > 0) {
					packets.pop();
				}
			}
		}
	}
	
	
	
	public String toString() {
		return "[" + whoAmI + "] Created Packets: " +  totalCreated + " Total On Pool: " + totalOnPool + " MemUsed: " + totalMemory / (1024 * 1024) + "MB";
	}
	private Packet findPacketBySize(int size) {
		
		Packets packets = null;
		
		// index to size, retrieve list of packets ...
		// or if this fails search up the list for a larger
		// packet that is unused.  Picking one of these
		// will keep the Packet from re-allocating its data 
		// buffer
		
		boolean found = false;
		for (int i = size; i < pool.length; i++) {
			packets = pool[i];
			if (packets.size() > 0) {
				packets.touched++;
				found = true;
				break;
			}
		}
		
		// if that didn't work, pick a smaller sized packet off the list
		// and re-use it.  This means when the packet is reset its data
		// buffer will be re-allocated off the heap... start from the sizes
		// nearest the asked for size... not sure this matters.
		if (found == false) {
			for (int i = size; --i >= CCSDSMinSize;) {
				packets = pool[i];
				if (packets.size() > 0) {
					totalMemory += size; // when it's resized it will create this amount (not really counting turned in memory though)
					statMemoryUsed();
					packets.touched++;
					found = true;
					break;
				}
			}
		}
		
		Packet packet = null;
		
		if (found == true) {
			// if we found one, resize as it may be larger or smaller...
			packet = packets.pop();
			packet.reset(size);
			--totalOnPool;
			statPacketPool();
		} else {
			// we are forced to make a new packet
			packet = new Packet(size);
			totalMemory += size;
			++totalCreated;
			
			statMemoryUsed();
			statPacketsCreated();

		}
		return packet;
	}
	
	private void statPacketPool() {
		if (stats != null) {
			if (sciencePool) {
				stats.sci_freePoolPackets.value = totalOnPool;
			} else {
				stats.ae_freePoolPackets.value = totalOnPool;
			}
		} 
	}
	private void statMemoryUsed() {
		if (stats != null) {
			if (sciencePool) {
				stats.sci_packetsMemory.value = totalMemory / (1024L * 1024L);
			} else {
				stats.ae_packetsMemory.value = totalMemory / (1024L * 1024L);
			}
		}
	}

	private void statPacketsCreated() {
		if (stats != null) {
			if (sciencePool) {
				stats.sci_createdPackets.value = totalCreated;
			} else {
				stats.ae_createdPackets.value = totalCreated;
			}
		}
	}
	public void removeEnd(List<Packet> packetList,int startIndex)throws RtStpsException {
		
	}
}
