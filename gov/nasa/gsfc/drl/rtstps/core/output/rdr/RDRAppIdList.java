/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Create a list of RDRAppIdItems ... the list can be created so that it can then be written to the StaticHeader, 
 * or if the list can be created from a memory buffer likely read from the HDF.
 * 
 *
 */
public class RDRAppIdList {
	private List<RDRAppIdItem> rdrAppIdList = new LinkedList<RDRAppIdItem>();

	// make warning go away
	private static final long serialVersionUID = 1L;
	// build the list and calculate its HDF size
	private final static int RDRItemSize = 16 + 4 * 4;
	private int appIdCount[] = new int[2048];
	
	
	/**
	 * Constructor for building the AppIdList and eventually writing the contents to the HDF file.
	 * Pass in the already constructed packetTracker info to do so.  This is is the "write" side of
	 * class. Note, the values of each supplied appID item must ultimate be copied to a buffer using
	 * the RDRAppIdItem's write method.
	 * @param rdrName 
	 * @param packetTracker the list of packet trackers
	 */
	public RDRAppIdList(RDRName rdrName, List<Packet> packets, PacketTrackerList packetTracker) {
		int numAppIds = rdrName.getNumberOfAppIdsInRDR();
		Set<PacketName> packetNames = rdrName.getPacketsInRDR();
		
		// seems exceedingly unlikely...
		if (packetNames.size() != numAppIds) {
			throw new RtStpsRuntimeException("The number of packets in the RDR named list does not equals the calculated number of packets in the RDR");
		}
		
		// count the packets by app id...
		// Using the original packet list vs asking the packet tracker... should be the same
		for (Packet p : packets) {
			this.appIdCount[p.getApplicationId()] += 1;
		}
		
		// build the AppIdItem list
		Iterator<PacketName> pni = packetNames.iterator();
		
		while (pni.hasNext()) {
			PacketName packetName = pni.next();
			int appId = packetName.getAppId();
			RDRAppIdItem rdrItem = new RDRAppIdItem(rdrName, packetName, packetTrackerIndex(appId, packetTracker), this.appIdCount[appId]);
			
			rdrAppIdList.add(rdrItem);
		}
	}
	
	private int packetTrackerIndex(int appId, PacketTrackerList packetTracker) {
		return packetTracker.getIndex(appId);
	}

	/**
	 * This constructs the list from memory which has likely just been read from the HDF and is in the StaticHeader.
	 * This is the "read" side of the class. 
	 * @param offset offset into the data buffer
	 * @param data byte array of data
	 * @param end the end offset containing the StaticHeader
	 */
	public RDRAppIdList(int offset, byte[] data, int end) {
		
		while (offset < end) {
			RDRAppIdItem rdrItem = new RDRAppIdItem(data, offset);
			rdrAppIdList.add(rdrItem);
			offset += RDRAppIdItem.getHDFSize();
		}
	}
	
	/**
	 * Write the list to the provided data array at the given offset
	 * @param data a byte array target
	 * @param offset the offset into the buffer
	 * @return the new offset or end
	 */
	public int write(byte[] data, int offset)  {

		for (RDRAppIdItem item : rdrAppIdList) {
			offset += item.write(data, offset);
		}
		return offset;
	}
	
	/**
	 * Return the appIdItem list
	 * @return the List of RDRAppIdItems
	 */
	public List<RDRAppIdItem> getAppIdItemList() {
		return rdrAppIdList;
	}
	
	/**
	 * Get the HDF dataset size
	 * @return size in bytes
	 */
	public int getSize() {
		return rdrAppIdList.size() * RDRItemSize;
	}

}
