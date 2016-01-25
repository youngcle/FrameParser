/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Build the packet tracker list in the StaticHeader
 * 
 *
 */
public class PacketTrackerList {
	private CopyOnWriteArrayList<PacketTrackerItem> packetTrackerItems = new CopyOnWriteArrayList<PacketTrackerItem>();
	// build the list and calculate its HDF size

	private static final int STARTGROUP = 1;
	private static final int SCANGROUP = 2;
	private static final int PacketTrackerItemSize = 24;

	private int totalSize = 0;

	// NOTE: the treemap sorts naturally and so the lists by apid are sorted from least to greatest when in inserted into the map
	private TreeMap<Integer, List<PacketTrackerItem>> trackerMap = new TreeMap<Integer, List<PacketTrackerItem>>();
	private RDRName rdrName;
	
	/**
	 * Construct the PacketTrackerList from the list of packets for a granule.  Once constructed it may be
	 * written to the StaticHeader
	 * @param rdrName the name of RDR for this list of packets
	 * @param packetList the list of packets for a granule
	 */
	public PacketTrackerList(RDRName rdrName, CopyOnWriteArrayList<Packet> packetList) {
		
		this.rdrName = rdrName;
		
		// build up the tracker by app id in a hash map
		// assumption is the packets are time ordered in the list
		int offset = 0;
		for (Packet packet : packetList) {
			Integer appId = packet.getApplicationId();
			if (trackerMap.containsKey(appId) == false) {
				trackerMap.put(appId, new CopyOnWriteArrayList<PacketTrackerItem>());
			}
			trackerMap.get(appId).add(new PacketTrackerItem(packet, offset));
			offset += packet.getPacketSize();
		}
	
		// NOT USED.  Although it works in a sense and make empty entries in the table
		// our notion of expected packets is a little primitive and does not seem to match
		// well with external RDRs we've seen.   At this time expected == recei
		// EXCEPT if received is ZERO, we make it expected = 1
		
		// analyze what's been create by determining if there are any missing packets
		// fill them in with a default...
		
		// ask the RDR for the list of app ids
		EnumSet<PacketName> packetNames = rdrName.getPacketsInRDR();
		Iterator<PacketName> pni = packetNames.iterator();
		
		while (pni.hasNext()) {
			PacketName packetName = pni.next();
			int appId = packetName.getAppId();
			
			int expectedPackets = rdrName.getGranuleSize() * packetName.getTotalGroupCount();
			
			// add it if its completely empty
			if (trackerMap.containsKey(appId) == false) {
				trackerMap.put(appId, new CopyOnWriteArrayList<PacketTrackerItem>());
			}

			// Get the list of packets for that app id
			
			CopyOnWriteArrayList<PacketTrackerItem> ptil = (CopyOnWriteArrayList<PacketTrackerItem>) trackerMap.get(Integer.valueOf(appId));
	
			int receivedPackets = ptil.size();
			
			//if (receivedPackets == 0) {
			//	ptil.add(new PacketTrackerItem());
				//++receivedPackets;
			//}			
			for(int i= receivedPackets ;i<expectedPackets;i++) 
					ptil.add(new PacketTrackerItem());			
			
			
		}
	}
	
	
	/**
	 * Construct the in memory "packed" version of the PacketTrackerList from a List of packets.
	 * Once constructed it may then be written to the StaticHeader.
	 * (NOTE: this class predates almost all the other classes in this package and was part of now defunct
	 *  early implementation of the RDR builder.   The algorithm below needs to be revisited and better
	 *  documented in the context of the new package classes.  Although it seems to work)
	 * @param packetList a list of packet to be made into an in memory PacketTrackerList
	 *
	@Deprecated
	public PacketTrackerList(List<Packet> packetList, boolean orig) {
		int state = STARTGROUP;
		int nextPacket = 0;
		PacketTrackerItem currentTrackerItem = null;
		Packet p;

		// scan the list,  attempt to build out tracker items per APID
		// taking into account both normal full segmented groups with a proper first, middle and last packet
		// and short groups that may be missing the first (for example at frame lock) packets
		// or standalone packets (such as APID=11)... 
		// this does not track specific groups per se... just tries to count things that are grouped and alike
		//
		while (nextPacket < packetList.size()) {

			p = packetList.get(nextPacket);

			switch(state) {

			case STARTGROUP:

				currentTrackerItem = new PacketTrackerItem();
				currentTrackerItem.setAppId(p.getApplicationId());
				if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
					currentTrackerItem.setObsTime(IETTime.fromTimeStamp(p.getTimeStamp(8)));
				}
				currentTrackerItem.setSequenceNumber(p.getSequenceCounter());
				currentTrackerItem.addSize(p.getPacketSize());
				currentTrackerItem.addCount(1);
				currentTrackerItem.setOffset(totalSize); // with luck this is the same as the zero based offset from apStorate...
				packetTrackerItems.add(currentTrackerItem); // add it to the list now but further processing will effect its fields
				state = SCANGROUP;
				++nextPacket;
				
				totalSize += p.getPacketSize(); // running total of all packets in granule, used in the offset
				break;

			case SCANGROUP:

				if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
					// either the next packet is a first or standalone packet and
					// the start of the group is unexpectedly short... 
					// Or the normal case would be all the middle and last packets
					// have been collected, and this is the beginning of a new group
					// Either way, we are done.  The next packet in start group area
					// is this packet, which it will get again ... so the nextPacket
					// is not incremented...
					//
					state = STARTGROUP;
					//totalSize += PacketTrackerItem.getHDFSize();
					//totalSize += p.getPacketSize();

				} else if (p.getApplicationId() != currentTrackerItem.getAppId()) {
					// in this case the group is not a group after all, the application ids do not match
					// and it this is not a start or standalone packet... for example two middle packets
					// with different appids appear... this is an error case, not like to be seen but 
					// we can check for it and do something sensible...
					// Since its an appid transition, its a new group even if its not formed properly
					// the nextPacket is not incremented and it will be re-read in the start group section.
					//
					state = STARTGROUP;
					//totalSize += PacketTrackerItem.getHDFSize();
					//totalSize += p.getPacketSize();
				} else {
					// The normal case: another packet in the group, a middle or last packet
					// add more to the size and increment the next packet... so it can be read.
					currentTrackerItem.addSize(p.getPacketSize());
					currentTrackerItem.addCount(1);
					++nextPacket;
					
					totalSize += p.getPacketSize(); // running total of all packets in granule, used in the offset
				}
				break;
			}
			
		}
	}
	*/
	/**
	 * This constructs the list from memory which has likely just been read from the HDF and is in the StaticHeader.
	 * This is the "read" side of the class. 
	 * @param offset
	 * @param data
	 * @param end
	 */
	public PacketTrackerList(int offset, byte[] data, int end) {
		
		while (offset < end) {
			PacketTrackerItem pktTrackerItem = new PacketTrackerItem(data, offset);
			packetTrackerItems.add(pktTrackerItem);
			offset += PacketTrackerItem.getHDFSize();
		}
	}
	
	/**
	 * Write the constructed PacketTrackerList to the supplied data buffer
	 * @param data the byte array for the data
	 * @param offset the offset into the array
	 * @return the new offset
	 */
	public int write(byte[] data, int offset) {
	
		// get the keys and then value by key
		// doing it this way to maybe maintain overall set ordering
		// although this probably isn't important...
		Set<Integer> keys = trackerMap.keySet();
		for (Integer key : keys) {
			List<PacketTrackerItem> values = trackerMap.get(key);
			for (PacketTrackerItem pti : values) {
				offset += pti.write(data, offset);
			}
		}
		return offset;
	}
	/**
	public int write(byte[] data,  int offset, boolean orig) {
		for (PacketTrackerItem pti : packetTrackerItems) {
			offset += pti.write(data, offset);
		}
		return offset;
	}
	**/
	/**
	 * Return the dataset size in bytes
	 * @return the size in bytes
	 */
	public int getSize() {
		int size = 0;
		
		// this seems safest... vs short cuts
		Set<Integer> keys = trackerMap.keySet();
		for (Integer key : keys) {
			List<PacketTrackerItem> values = trackerMap.get(key);
			size += (values.size() * PacketTrackerItemSize);
		}
		
		return size;
	}
	
	
	public List<PacketTrackerItem> getPacketTrackerItemList() {
		return packetTrackerItems;
	}
	public int getIndex(int appId) {
		if (trackerMap.containsKey(appId) == false) {
			return -1;
		}
		// otherwise loop through the set until the app id is reached
		// at each key, add up the number of items in the list...
		// since this is the way the set is written, it should line up
		// ... fingers crossed
		
		int index = 0;
		Set<Integer> keys = trackerMap.keySet();
		Iterator<Integer> iterator = keys.iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			if (key == appId) {
				break;
			}
			List<PacketTrackerItem> values = trackerMap.get(key);
			if (key==822) System.out.println("Nof of PktTracker Items for 822/823"+values.size());
			index += values.size();
		}
		
		
		return index;
	}

}
