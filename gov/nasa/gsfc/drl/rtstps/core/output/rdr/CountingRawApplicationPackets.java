/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * Create a raw application packet dataset in an HDF file for an RDR based on a count of incoming packets such as:
 * '/All_Data/VIIRS-SCIENCE-RDR/RawApplicationPackets0'
 * This is a very basic and overly simplistic implementation that is largly designed to be subclasses for each specific sensor.
 * 
 * The dataset is created based on a timed holding of packets hung up in the object. (constructor, notFull, put)
 * The time is based on the clock time when the object was created and a given user argument of seconds in the future.
 * It assumed the object will be used immediately to put packets in it and then create the HDF dataset.
 * Once the time span has occurred, the accumulated packets may then be written to the HDF file,  
 * created in the RDR dataset structure. (writeRDR)
 * A static header is calculated from the packets given to the object, then the packets
 * themselves are written to the dataset.
 * The object should be explicitly closed by calling close or the HDF API will get a heap exception eventually.
 * 
 *
 */
@Deprecated
public class CountingRawApplicationPackets extends RawApplicationPackets {
	private int maxPacketCount=0;
	private int packetCounter=0;
	
	

	/**
	 * Constructor for creating an nth instance of a raw application data packet area
	 * @param satellite  the name of the spacecraft
	 * @param rdrName  the rdrName of the RDR dataset (i.e. VIIRS-SCIENCE-RDR)
	 * @param setNum  the set number
	 * @param packetCount  counts first or standalone packets, middle are last packets are accepted regardless of specified count
	 */
	public CountingRawApplicationPackets(SpacecraftId satellite, RDRName rdrName, int setNum, int packetCount) {
		super(satellite, rdrName, setNum, null);
		
		this.maxPacketCount = packetCount;
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
	}
	
	/**
	 * Constructor which attempts to read the RawApplicationPacket entry that pre-exists.
	 * The contents of the dataspace are read into a memory buffer... assuming it will fit.
	 * @param allRDRId  the rdrAll Groups id
	 * @param setNum the set number of raw entry
	 * @throws RtStpsException 
	 */
	public CountingRawApplicationPackets(int allRDRId, int setNum) throws RtStpsException  {
		super(allRDRId, setNum);
	}
	

	

	/**
	 * Determine if the object will take more packets.  Only packets
	 * that are first or stand alone packets are considered in against 
	 * the packet count.  Middle or last packets are always allowed.
	 * This is an attempt to prevent the splitting up of segmented packets. 
	 * @param p packet to be written
	 * @return true or false
	 */
	public boolean notFull(Packet p) {
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			return (packetCounter < maxPacketCount);
		}
		return true;
	}
	
	
	/**
	 * Give the object a packet to hang up, eventually to be written
	 * to the HDF.  No checking is done here to see if the limit has been
	 * reached, use notFull above first.  
	 * If the packet is a first or stand alone packet, the count is incremented.
	 * Otherwise if it is a middle or last packet, the count is unchanged.
	 * This is an attempt to not split up segmented or group packets.
	 * @param p packet to be written
	 * @throws RtStpsException 
	 */
	public void put(Packet p) throws RtStpsException {
		
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			++packetCounter;
			
			// secondary time stamp only valid in in first or standalone packets...
			if (getFirstTime() == 0L) {
				setFirstTime(p.getTimeStamp(8));
			}
			
			setLastTime(p.getTimeStamp(8));
		}
		// the packet is deep copied, it is allocated off a pool
		// which may hold previously copied packets...  they must be put
		// back on the pool when done...
		
		Packet pcopy = CopyPacket.deep(p, null);
		
		getPacketList().add(pcopy);
	}
	
	
}
