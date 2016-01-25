/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.EnumSet;


/**
 * Implements an algorithm for collecting VIIRS segmented or group packets into a cohesive unit so that blocks of them
 * maybe written atomically into the RawApplicationPackets area for a VIIRS RDR file.  The goal then of this class is to
 * keep the segments together in each RawApplicationPackets and to put several together as designated before contents are
 * then ready to be written to the RDR file. 
 * This version just makes on huge granule, it's just pile in as many scans as it gets on the input.
 * It does not check scan integrity or for holes, etc...
 *
 */
public class VIIRSRawApplicationPackets_OneLargeGranule extends RawApplicationPackets {
	private long currentScanNumber = -1;
	
	
	private EnumSet<PacketName> sciPacketRange = EnumSet.range(PacketName.M04, PacketName.DNB_LGS);//FIXSET1
	private Stats stats;
	private int sciPacketCount = 0;
	
	/**
	 * Constructor for creating an nth instance of a VIIRS raw application data packet area
	 * @param satellite  the name of the spacecraft
	 * @param setNum  the set number
	 */
	public VIIRSRawApplicationPackets_OneLargeGranule(SpacecraftId satellite, int setNum, PacketPool packetPool) {
		super(satellite, RDRName.VIIRS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
	
	}
	
	public VIIRSRawApplicationPackets_OneLargeGranule(Stats stats, SpacecraftId satellite, int setNum,  PacketPool packetPool) {
		super(satellite, RDRName.VIIRS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
		
		this.stats = stats;
	}
	
	/**
	 * Constructor which attempts to read the RawApplicationPacket entry that pre-exists.
	 * The contents of the dataspace are read into a memory buffer... assuming it will fit.
	 * @param allRDRId  the rdrAll Groups id
	 * @param setNum the set number of raw entry 
	 * @throws RtStpsException 
	 */
	public VIIRSRawApplicationPackets_OneLargeGranule(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public VIIRSRawApplicationPackets_OneLargeGranule(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}



	/**
	 * Determine if the object will take more packets.  For VIIRS there
	 * are two cases.  First if the collection of packets into this particular
	 * RawApplication has not started, its considered to be not-full.
	 * Second if the scan number matches the current scan number, it is considered
	 * not-full.   If the collection has started and the scan number changes, it 
	 * is considered full and any collected packets should be written to the HDF
	 * @param p packet to be written
	 * @return true or false
	 * @throws RtStpsException 
	 */
	public boolean notFull(Packet p) throws RtStpsException {
		RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
		
		if (rdrName != RDRName.VIIRS_Science) {
			throw new RtStpsException("Not a VIIRS packet [" + p.getApplicationId() + "]");
		}
	
		// look for scan number transitions to signal the start and end of the collections
		// a special case occurs for the first packets read as it is likely this set does not
		// start at a particular scan boundary and so these packets are collected
		
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			
			// the scan number changes only in the science packets, the other calibration/eng
			// are associated with the current scan...
			if (isVIIRSSciencePacket(p)) {
				long scanNumber = VIIRSSciencePacket.getScanNumber(p);
				if (scanNumber != currentScanNumber) {
					scanCounter++;
					currentScanNumber = scanNumber;
				}
			}
			
		}
		
		
	
		return true;
	}
	

	// uses the PacketName set of VIIRS packet enums to check that the packet given is in range
	// OR we could just hardcode the APIDs here but... this so much better obviously right? :-)
	private boolean isVIIRSSciencePacket(Packet p) {
		return sciPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}

	/**
	 * The packet is stored until a packet transition which is checked in the notFull() method.
	 * When full the RawApplicationPacket should then be written to the HDF
	 * @param p VIIRS science packet to be written
	 * @throws RtStpsException the packet is not a VIIRS science packet or the scan number does not match previous packets (notFull was not called first)
	 */
	public void put(Packet p) throws RtStpsException {
		
		sciPacketCount++;
		
		this.updateAppIdCounters(p.getApplicationId());
		
		RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
		
		if (rdrName != RDRName.VIIRS_Science) {
			throw new RtStpsException("Not a VIIRS science packet [" + p.getApplicationId() + "]");
		}
	

		if (p.hasSecondaryHeader()) {
			
			// secondary time stamp only valid in in first or standalone packets...
			if (getFirstTime() == 0L) {
				//System.out.println("Here...");
				setFirstTime(p.getTimeStamp(8));
			}
			
			setLastTime(p.getTimeStamp(8));
		}
		// the packet is deep copied, it is allocated off a pool
		// which may hold previously copied packets...  they must be put
		// back on the pool when done...
		
		Packet pcopy = CopyPacket.deep(p, packetPool);
		
		getPacketList().add(pcopy);
	}
	
	
	/**
	 * Write the collected group of packets to the designated HDF file using the handle
	 * @return true if the RawApplicationPacket was written, false if not
	 * @exception any HDF exceptions are wrapped in an RtStpsException
	 */
	public boolean write(int hdfFile) throws RtStpsException {
		boolean ret = true;
		
	
		
			
			// write returns true/false if it actually wrote anything
			// its ignored here as the scanCounter would not be at scansPerGranule
			// if there no packets...
			// Maybe it should it be double checked just to make sure?  
			super.write(hdfFile); 
		
			if (stats == null) {
				System.out.println("Creating VIIRS Granule -- [" + (this.scanCounter) + "] scans");
			} else {
				stats.sci_createdGranules.value++;
			}
			
			ret = true;
	
		
		System.out.println(">> VIIRS Sci packets in granule: " + sciPacketCount  );
		
		//sciPacketCount = 0;
		//this.currentScanNumber = -1;
		//this.scanCounter = 0;
		

		return ret;
	}
	
	
	/**
	 * Close out the RawApplicationPacket which writes the results to the HDF file and cleans up.
	 * @exception any HDF exceptions are wrapped in an RtStpsException
	 */
	public void close() throws RtStpsException {
		super.close();
		this.currentScanNumber = -1;  // no point of this..
		this.scanCounter = 0; // nor this...
		sciPacketCount = 0;
		//System.out.println("End of Close -- " + this.packetPool.toString());

	}
	
	

}
