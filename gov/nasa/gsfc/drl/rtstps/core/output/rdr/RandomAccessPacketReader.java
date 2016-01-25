/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.List;

/**
 * Using the StaticHeader of a particular RawApplicationPackets item, read the contents using a random access style interface.
 * This class used the various fields in the StaticHeader to access the packets held in the packet region.
 * Note that this classes constructor is package private, and a method in each StaticHeader must be used 
 * to create it.
 * 
 * 
 *
 */
public class RandomAccessPacketReader {
	private PacketFactoryI pf;
	private StaticHeader staticHeader;
	private RDRAppIdList rdrAppIdList;
	private PacketTrackerList packetTrackerList;
	private ApStorageArea apStorageArea;
	private int position = -1;
	private boolean state = false;
	
	private int packetTrackerPacketCount = 0;
	private int packetTrackerIndex = -1; 
	
	/**
	 * Make a new reader by providing PacketFactory (some way to create new Packets) and the
	 * StaticHeader of interest
	 * @param packetFactory a way to create new Packets
	 * @param staticHeader the StaticHeader from a RawApplicationsPackets of interest
	 */
	RandomAccessPacketReader(PacketFactoryI packetFactory,  StaticHeader staticHeader) {
		pf = packetFactory;
		this.staticHeader = staticHeader;
		
		//int length = staticHeader.getPktTrackerOffset() - staticHeader.getAppIdListOffset();
		
		rdrAppIdList = new RDRAppIdList(staticHeader.getAppIdListOffset(), 
										staticHeader.getData(), 
										staticHeader.getPktTrackerOffset());
		
		
		//length = staticHeader.getApStorageOffset() - staticHeader.getPktTrackerOffset();
		
		//System.out.println("pkt tracker Length = " + length);
		
		packetTrackerList = new PacketTrackerList(staticHeader.getPktTrackerOffset(), 
												  staticHeader.getData(), 
												  staticHeader.getApStorageOffset());
		
		apStorageArea = new ApStorageArea(staticHeader.getData(), staticHeader.getApStorageOffset(), staticHeader.getNextPktPos());
		
		
	}

	public RDRAppIdList getRDRAppIdList() {
		return rdrAppIdList;
	}
	public PacketTrackerList getPacketTrackerList() {
		return packetTrackerList;
	}
	public ApStorageArea getApStorageArea() {
		return apStorageArea;
	}
	public boolean findPacketTrackerByName(PacketName packetname) {
		for (RDRAppIdItem rdrAppIdItem : rdrAppIdList.getAppIdItemList()) {
			//System.out.println("rdrAppIdItem.getName() -- " + rdrAppIdItem.getName() + " ==? " + packetname.toString());
			if (rdrAppIdItem.getName().equals(packetname.toString())) {
				//System.out.println("rdrAppIdItem.getName() -- " + rdrAppIdItem.getName() + " == packetname " + packetname.toString());
				packetTrackerPacketCount = rdrAppIdItem.getPktsReserved();
				packetTrackerIndex = rdrAppIdItem.getPktTrackerIndex();
				return true;
			}
		}
		packetTrackerPacketCount = 0;
		packetTrackerIndex = -1;
		return false;
	}
	
	public boolean hasNext() {
		return (packetTrackerPacketCount > 0);
	}
	
	/**
	 * Return one packet from the packet tracker.  This packet is 'no copied' from the ApStorageBuffer.
	 * @return either null if the offset is zero or one no copied Packet
	 */
	public Packet next() {
		PacketTrackerItem packetTrackerItem = packetTrackerList.getPacketTrackerItemList().get(packetTrackerIndex);
		
		int offset = packetTrackerItem.getOffset();
		
		if (offset < 0) {
			return null;
		}
		
		int size = packetTrackerItem.getSize();
		
		List<Packet> packets = apStorageArea.read(offset, size);  // returns list of no-copy packets, only 1 in this case it should be
		
		--packetTrackerPacketCount;
		packetTrackerIndex++;
		
		return packets.get(0);
	}
}
