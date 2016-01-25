/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import java.util.EnumSet;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * Build the RawApplicationPackets (or read a pre-existing one) for the HDF for OMPS.
 * This version uses the scan start bit to collect scan lines to make granules of 32 secs
 * worth approximately.
 * 
 * Current 
 *
 */
public class ROLPSRawApplicationPackets extends RawApplicationPackets {
	
    	private long lastTime = 0l;
    	private boolean lastTimeSet = false;
    	private long baseTime = 0l;
    	private long firstTime = -1l;
	private int scanCounter = 0;
	private EnumSet<PacketName> sciPacketRange = EnumSet.of(PacketName.LP1, PacketName.LP2); //FIXSET1
	private EnumSet<PacketName> engPacketRange = EnumSet.range(PacketName.ENG_TEMP, PacketName.ENG_HS);
	private EnumSet<PacketName> calPacketRange = EnumSet.range(PacketName.ATMS_CAL, PacketName.ATMS_CAL);
	private Stats stats;
	private int sciPacketCount = 0;
	private final int scansPerGranule = 2;
	//private final int sciPacketsPerScan = 104;

	private int scanCount = -1;  // FIXME there was a bug in VIIRS module and this should be zero and check below should >= I think...
	
	private static long currentgranule_startboundary=-1L;
	private static long currentgranule_endboundary=-1L;
	private static long granuleSize=37437000;

	// Maximum number of OMPS LP packets per granule is the following:
	// (2 groups of CCSDS packets) * ( 168 Long + 106 Short exposure packets per CCSDS group)
	//private static long OMPSGRANULE_MAXPACKETS = 548L;
	// FIXME: 548 max packets would be expected based on MDFCB, but empirical evidence suggests 714 instead?
	private static long OMPSGRANULE_MAXPACKETS = 714L;
	
	
	private boolean isOMPSSciencePacket(Packet p) {
		return sciPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}
	private boolean isATMSEnggPacket(Packet p) {
		return engPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}
	private boolean isATMSCalPacket(Packet p) {
		return calPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}
	
	//private boolean scanCheck = false;

	/**
	 * Constructor for creating an nth instance of a ATMS raw application data packet area builder.
	 * @param satellite  the name of the spacecraft as a SpacecraftId
	 * @param setNum  the set number
	 * @param scansPerGranule the number of sensor scans per granule
	 */
	public ROLPSRawApplicationPackets(SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.OMPS_LPScience, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
	}
	

	public ROLPSRawApplicationPackets(Stats stats, SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.OMPS_LPScience, setNum, packetPool);
		
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
	 * @throws RtStpsException Wraps any HDF exception
	 */
	public ROLPSRawApplicationPackets(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public ROLPSRawApplicationPackets(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}
	

	public boolean notFull(Packet p) throws RtStpsException 
	{
	    
	    RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
	   
	    if (rdrName.toString().indexOf("OMPS") == -1) 
	    {
		throw new RtStpsException("Not an OMPS packet [" + p.getApplicationId() + "]");
	    }
	    
	    boolean notFullStatus = true;
	    long ietTimeCurrentPacket =  0;
	    
	    if(p.hasSecondaryHeader())
	    {
		//System.out.println();
		//System.out.println("Found secondary header. Huzzah!");
		//System.out.println();
		ietTimeCurrentPacket = LeapDate.getMicrosSinceEpoch(p.getTimeStamp(8));
		lastTime = ietTimeCurrentPacket;
		lastTimeSet = true;
	    }
	    else if(lastTimeSet == true)
	    {
		ietTimeCurrentPacket = lastTime + 1;
	    }
	    else
		return true;
			
	    if(this.firstTime < 0l) //Starting a granule
	    {
		if(this.baseTime == 0l)
		{
		    System.out.println("Base time was not set.");
		    ROLPSGranule.setBaseTime();
		    this.baseTime = ROLPSGranule.getBaseTime();
		    System.out.println("BASE TIME SET TO: " + this.baseTime);
		    
		    currentgranule_startboundary = ROLPSGranule.getStartBoundary(ietTimeCurrentPacket);
		    currentgranule_endboundary = ROLPSGranule.getEndBoundary(ietTimeCurrentPacket);
		    System.out.println("Setting OMPS startBoundary and endBoundary "+ ietTimeCurrentPacket+" "+currentgranule_startboundary+" "+currentgranule_endboundary);
				
		}
	    }
	    //If the ietTime is > startBoundary and <=endBoundary (or should it be >=startBoundary and < endBoundary)
	    //then granule is not full; else it is full
	    if(ietTimeCurrentPacket>=currentgranule_startboundary && ietTimeCurrentPacket<currentgranule_endboundary)
	    {
		notFullStatus = true;
		//System.out.println("Not full OMPS granule.");
	    }
	    else
	    {			
		notFullStatus = false;
		System.out.println("Full OMPS granule.");
		System.out.println("iettimecurrentpacket: " + ietTimeCurrentPacket);
		System.out.println("currentgranule_startboundary: " + currentgranule_startboundary);
		System.out.println("currentgranule_endboundary: " + currentgranule_endboundary);
		//fullGranule = true;
		this.firstTime = -1l;
	    }
	   
	    return notFullStatus;
	    
	}
		/*
		if (p.isStandalonePacket() && p.hasSecondaryHeader()) {
			if (p.getApplicationId() == 528) {
				byte[] data = p.getData();
				int offset = 6 + 8 + 2;
				int scanStart = ((int)data[offset] >>> 7) & 0x00000001;
				if (scanStart == 1) 
				{	
					++scanCounter;
				}
			}
		}
		return (scanCounter < scansPerGranule);  // true == not full, false == full 
		*/
	
	
	

	

	@Override
	public void put(Packet p) throws RtStpsException 
	{
	    if(lastTimeSet == false)
		return;
		
	    if (scanCounter < 0) { // throw away any initial packets if we've not seen the start of a scan...
		if (stats == null) {
			//System.out.println("Trimming ATMS [" + p.getApplicationId() + "]");
		} else {
			//stats.sci_trimmedPackets.value++;
		}
		return;
	    }
	  
	    this.updateAppIdCounters(p.getApplicationId());
	
	    RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
	
	    if (rdrName.toString().indexOf("OMPS") == -1) 
	    {
		throw new RtStpsException("Not an OMPS science packet [" + p.getApplicationId() + "]");
	    }
	    
	    // collect time info
	    if (p.hasSecondaryHeader()) {

		// secondary time stamp only valid in in first or standalone packets...
		if (getFirstTime() == 0L) {
			System.out.println("Set first time.");
			setFirstTime(p.getTimeStamp(8));
		}
		
		setLastTime(p.getTimeStamp(8));
	    }

	    Packet pcopy = CopyPacket.deep(p, packetPool);
	    getPacketList().add(pcopy);
	    sciPacketCount++;
	}
	
	
	/**
	 * Write the ATMSRawApplication structure to the given HDF file specified by the HDF input handle
	 * @param hdfFile input handle to a previous opened HDF file
	 * @return true if the write succeeds
	 * @throws RtStpsException Wraps HDF exceptions
	 */
	@Override
	public boolean write(int hdfFile) throws RtStpsException {
		
		// Don't attempt to write if we're at 0 scans, similiar to VIIRS
		if(this.scanCount == 0)
		{
			return false;
		}
		
		boolean ret = false;
		System.out.println("OMPS LP sci packet count: " + sciPacketCount);
		float percentMissingData= ((OMPSGRANULE_MAXPACKETS-sciPacketCount)/(float)OMPSGRANULE_MAXPACKETS) * 100;
		setPercentMissingData(percentMissingData);
		/*if (this.scanCounter < this.scansPerGranule) {
			if (stats == null) {
				System.out.println("Deleting partial ATMS Granule -- [" + this.scanCounter + "] scans, collected packets [" + this.sciPacketCount + "]");
			} else {
				stats.sci_discardedGranules.value++;
			}	
			ret = false;
		} else*/ {
			if (stats == null) {
				System.out.println("Creating OMPS granule of [" + this.scanCounter + "] scans");
			} else {
				stats.sci_createdGranules.value++;
			}
			super.write(hdfFile);
			ret = true;
			
		}
		
		System.out.println("OMPS Sci packets in granule: " + sciPacketCount + " Packet per scan: " + (float)sciPacketCount / (float)scansPerGranule);
		System.out.println("OMPS app ids = " + super.appIdCountersToString(scansPerGranule));
		scanCount = -1;
		
		sciPacketCount = 0;

		return ret;
	}
	

	
	/**
	 * Close out the RawApplicationPacket which writes the results to the HDF file and cleans up.
	 */
	public void close() throws RtStpsException {
		super.close();
		
		scanCount = -1;
		sciPacketCount = 0;
	}

}
