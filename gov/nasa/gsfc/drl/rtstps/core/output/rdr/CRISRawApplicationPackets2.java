/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;



import java.util.EnumSet;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Build CRIS [@link RawApplicationPackets} by capturing groups scan packets between packet with
 * and application ID of 1289.  The first group captured is likely going to be short due
 * to the fact that frame lock could occur at any time.  Scans groups that form a granule are specified
 * externally in the constructor.
 * 
 * Current
 *
 */
public class CRISRawApplicationPackets2 extends RawApplicationPackets {
	private final int scansPerGranule = 4; // approx x secs worth
	private Stats stats = null;
	private int scanCount = 12;  // FIXME also likely a bug
	private int sciPacketCount = 0;
	private EnumSet<PacketName> sciPacketRange = EnumSet.range(PacketName.NLW1, PacketName.SSW9); //FIXSET1
	private EnumSet<PacketName> engPacketRange = EnumSet.range(PacketName.CRIS_ENG, PacketName.CRIS_ENG);
	private EnumSet<PacketName> calPacketRange = EnumSet.range(PacketName.CLW1, PacketName.EIGHT_S_SCI);
	private static long currentgranule_startboundary=-1L;
	private static long currentgranule_endboundary=-1L;
	private long firstTime = -1l;
	private long baseTime = 0l;
	private boolean fullGranule = false;
	private static long CRISGRANULE_MAXPACKETS=2476L+1238L;
	
	private boolean isCRISSciencePacket(Packet p) {
		return sciPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}
	private boolean isCRISEnggPacket(Packet p) {
		return engPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}
	private boolean isCRISCalPacket(Packet p) {
		return calPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}
	
	
	
	/**
	 * Constructor to initialize the RawApplicationPackets algorithm for CRIS.
	 * @param satellite the satellite ID as defined by the mission documents
	 * @param setNum the set number which is appended to the end of each RawApplicationPacket in the HDF
	 * @param scansPerGranule The number of scans to collect to make one granule
	 */
	public CRISRawApplicationPackets2(SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.CRIS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
		
		
		
	}
	
	public CRISRawApplicationPackets2(Stats stats, SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.CRIS_Science, setNum, packetPool);
		
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
	 * @throws NullPointerException 
	 * @throws HDF5LibraryException 
	 */
	public CRISRawApplicationPackets2(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public CRISRawApplicationPackets2(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}

	/**
	 * Granules are created from scans.  Scans in CRIS are lots of packets but the 8 second
	 * packet <b>seems</b> to indicate the start of a scan.   We collect it and everything after it
	 * as one scan until it re-appears.  Anything more complex than this can be built on top of this
	 * one assumption.
	 * 
	 * @param p  a Packet to be put into the CRIS RawApplicationPackets area
	 * @return true if it can be put there, or false if it is full and should be written to disk first
	 * @exception wraps any HDF exceptions in RtStpsException
	 */
	public boolean notFull(Packet p) throws RtStpsException {
		
	    //if(!isCRISSciencePacket(p))
		//return true;
	    
	    //if (CRISSciencePacket.checkForScanStart(p))
		//scanCounter++;
	    
	    RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
		
	    if (rdrName != RDRName.CRIS_Science) 
	    {
		throw new RtStpsException("Not a CRIS packet [" + p.getApplicationId() + "]");
	    }
	    
	    boolean notFullStatus = true;
	    
	    //System.out.println("S/C APID:"+p.getApplicationId()+":"+p.getTimeStamp(8));
		
	    long ietTimeCurrentPacket=LeapDate.getMicrosSinceEpoch(p.getTimeStamp(8));
	    long timestamp = p.getTimeStamp(8);
			
	    if(this.firstTime < 0l) //Starting a granule
	    {
		if(this.baseTime == 0l)
		{
		    System.out.println("Base time was not set.");
		    CRISGranule.setBaseTime();
		    this.baseTime = CRISGranule.getBaseTime();
		    //SpacecraftDiaryGranule.setBaseTime(ietTimeCurrentPacket);
		    this.firstTime = timestamp;
		    currentgranule_startboundary=CRISGranule.getStartBoundary(ietTimeCurrentPacket);
		    currentgranule_endboundary = CRISGranule.getEndBoundary(ietTimeCurrentPacket);
		    System.out.println("Setting CRIS startBoundary and endBoundary"+ietTimeCurrentPacket+" "+currentgranule_startboundary+" "+currentgranule_endboundary);
				
		}
	    }
	    //If the ietTime is > startBoundary and <=endBoundary (or should it be >=startBoundary and < endBoundary)
	    //then granule is not full; else it is full
	    if(ietTimeCurrentPacket>=currentgranule_startboundary && ietTimeCurrentPacket<currentgranule_endboundary)
	    {
		notFullStatus = true;
	    }
	    else
	    {			
		notFullStatus = false;
		System.out.println("Full CRIS granule.");
		System.out.println("iettimecurrentpacket: " + ietTimeCurrentPacket);
		System.out.println("currentgranule_startboundary: " + currentgranule_startboundary);
		System.out.println("currentgranule_endboundary: " + currentgranule_endboundary);
		fullGranule = true;
		this.firstTime = -1l;
	    }
	   
	    return notFullStatus;
	    
	}
		

	
	
	
	
	/**
	 * Put the packet into the CRISRawApplications area, the method <code>notFull</code> should
	 * have been called first to check if this is allowed or not.
	 * @param p a Packet to put into it
	 * @exception wraps any HDF exceptions in RtStpsException
	 */
	public void put(Packet p) throws RtStpsException 
	{
	    	float percentMissingData= ((CRISGRANULE_MAXPACKETS-sciPacketCount)/ (float)CRISGRANULE_MAXPACKETS)*100;
		//System.out.println("Percent Missing="+percentMissingData);
		setPercentMissingData(percentMissingData);
		if (scanCount < 0) { // throw away any initial packets if we've not seen the start of a scan...
			if (stats == null) {
				System.out.println("Trimming CRIS [" + p.getApplicationId() + "]");
			} else {
				//stats.sci_trimmedPackets.value++;
			}
			return;
		}
		sciPacketCount++;
		
		this.updateAppIdCounters(p.getApplicationId());
		
		RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
		
		if (rdrName != RDRName.CRIS_Science) {
			throw new RtStpsException("Not a CRIS science packet [" + p.getApplicationId() + "]");
		}

		// collect time info
		if (p.hasSecondaryHeader()) {

			// secondary time stamp only valid in in first or standalone packets...
			if (getFirstTime() == 0L) {
				//System.out.println("Here...");
				setFirstTime(p.getTimeStamp(8));
			}
			
			setLastTime(p.getTimeStamp(8));
		}
		Packet pcopy = CopyPacket.deep(p, packetPool);
		getPacketList().add(pcopy);
	}
	
	/**
	 * Write the accumulate packets to the RDR/HDF file.  Note that this method
	 * must be used in conjunction with method <code>notFull</code> and <code>put</code>.
	 * @param hdfFile the handle to the RDR/HDF file that the packets should written to
	 * @return true if the write succeeded
	 * @exception wraps any HDF exceptions in RtStpsException
	 */
	public boolean write(int hdfFile) throws RtStpsException  {
		
		boolean ret = true;
		if (this.scanCount < this.scansPerGranule) {

			if (stats == null) {
				System.out.println("Deleting partial CRIS Granule -- [" + scanCount + "] scans");
			} else {
				stats.sci_discardedGranules.value++;
			}

			ret = false;
		} else {

			if (stats == null) {
				System.out.println("Creating CRIS Granule with [" + scanCount + "] scans");
			} else {
				stats.sci_createdGranules.value++;
			}

			ret = super.write(hdfFile);
		}
		
		System.out.println("CRIS Sci packets in granule: " + sciPacketCount + " Packets per scan: " + (float)sciPacketCount / (float)scansPerGranule);
		System.out.println("CRIS app ids = " + super.appIdCountersToString(scansPerGranule));
		scanCount = -1;
		
		sciPacketCount = 0;
		
		return ret;
	}

	/**
	 * Close the RDR/HDF file specifically for the RawApplicationsPackets.  This method should be 
	 * called once specific RawApplicationPacket_x has been written. 
	 * @exception wraps any HDF exceptions in RtStpsException
	 */
	public void close() throws RtStpsException  {
		super.close();
		scanCount = -1;
		
		sciPacketCount = 0;
	}


}
