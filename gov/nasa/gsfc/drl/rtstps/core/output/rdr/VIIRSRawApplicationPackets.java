/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.VIIRSSciencePacket;
import java.util.EnumSet;


/**
 * Implements an algorithm for collecting VIIRS segmented or group packets into a cohesive unit so that blocks of them
 * maybe written atomically into the RawApplicationPackets area for a VIIRS RDR file.  The goal then of this class is to
 * keep the segments together in each RawApplicationPackets and to put several together as designated before contents are
 * then ready to be written to the RDR file. 
 * 
 * This makes multiple 48 scan VIIRS granules in one file
 */
public class VIIRSRawApplicationPackets extends RawApplicationPackets {
	private long currentScanNumber = -1;
	private int scanCounter = 0;
	private int scansPerGranule;
	private EnumSet<PacketName> sciPacketRange = EnumSet.range(PacketName.M04, PacketName.DNB_LGS); //FIXSET1
	private EnumSet<PacketName> engPacketRange = EnumSet.range(PacketName.ENG, PacketName.ENG);
	private EnumSet<PacketName> calPacketRange = EnumSet.range(PacketName.CAL, PacketName.CAL);
	private Stats stats;
	private int sciPacketCount = 0;
	private long lastCalPacketScanNumber = -999;
	private long lastEngPacketScanNumber = -999;
	private int lastEngPacketIndex;
	private int lastCalPacketIndex;
	
	private static long currentgranule_startboundary=-1L;
	private static long currentgranule_endboundary=-1L;
	private static long granuleSize=85350000;

	// Maximum number of VIIRS packets is the following:
	//    48 scans * (479 + 17 DNB_MGS + 17 DNG_LGS)  packets =  24624 packets
	private static long VIIRSGRANULE_MAXPACKETS=24624L;
	
	/**
	 * Constructor for creating an nth instance of a VIIRS raw application data packet area
	 * @param satellite  the name of the spacecraft
	 * @param setNum  the set number
	 */
	public VIIRSRawApplicationPackets(SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.VIIRS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
		//System.out.println("Constructor: Scans per granule: " + this.scansPerGranule);
		this.scansPerGranule = scansPerGranule;
	}
	
	public VIIRSRawApplicationPackets(Stats stats, SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.VIIRS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
		//System.out.println("Constructor: Scans per granule: " + this.scansPerGranule);
		this.scansPerGranule = scansPerGranule;
		
		this.stats = stats;
	}
	
	/**
	 * Constructor which attempts to read the RawApplicationPacket entry that pre-exists.
	 * The contents of the dataspace are read into a memory buffer... assuming it will fit.
	 * @param allRDRId  the rdrAll Groups id
	 * @param setNum the set number of raw entry 
	 * @throws RtStpsException 
	 */
	public VIIRSRawApplicationPackets(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public VIIRSRawApplicationPackets(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}

	/**
	 * !!! WARNING: THIS SHOULD ALWAYS BE CALLED BEFORE THE put(Packet p) FUNCTION !!!
	 * Determine if the current granule is already full.  For VIIRS there
	 * are two cases.  First if the collection of packets into this particular
	 * RawApplication has not started, its considered to be not-full.
	 * Second if the scan number matches the current scan number, it is considered
	 * not-full. 
	 * If the collection has started and the scan number changes, it is considered full 
	 * and packets collected for the scan should be written to the HDF. However, a full 
	 * collection (scan) does NOT necessarily equate to a full granule!
	 * @param p packet to be written
	 * @return true or false
	 * @throws RtStpsException 
	 */
	public boolean notFull(Packet p) throws RtStpsException {
		if (isVIIRSEnggPacket(p)) {
			System.out.println("Engineering packet.");
			System.out.println("E.P. Timestamp is: " + p.getTimeStamp(8));
		}
		
		// Get the proper RDR based on packet appID. At this point, we expect packet filtering to
		// work properly, hence we complain if we receive non-VIIRS Science packets.
		RDRName rdrName = RDRName.fromAppId(p.getApplicationId());	
		if (rdrName != RDRName.VIIRS_Science) {
			throw new RtStpsException("Not a VIIRS packet [" + p.getApplicationId() + "]");
		}
		
		// -------------------------------------------
		// A COLLECTION is defined for our purposes as ALL THE PACKETS ASSOCIATED WITH A PARTICULAR SCAN NUMBER
		// for VIIRS. At the start of the packet stream, this may not be clear and there may be a special
		// case of having no scan number yet but a valid VIIRS packet.  This will be collected and result in
		// a 'runt' VIIRS granule.
		//
		// e.g. Middle/Last VIIRS science packets are received before the first First packet, hence we lack metadata?
		//      (Scan number for VIIRS science packets are in the metadata field of the First packets, hence all time
		//	 and scan number checks in this function are only done for First packets)
		// -------------------------------------------
			
		// to start: assume the granule is not full...
		boolean notFullStatus = true;
		// Indicates if we just transitioned from one scan to another. Example: From scan counter field value 1 to 2
		boolean scanCountTransition = false;  
		
		// Look for scan number transitions to signal the start and end of the collections.
		// A special case occurs for the First packets read as it is likely this set does not
		// start at a particular scan boundary and so these packets are collected
		long scanNumber = -1L;
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			// The scan number changes only in the science packets, the other calibration/engineering packets
			// are associated with the current scan...
			if (isVIIRSSciencePacket(p)) {
				scanNumber = VIIRSSciencePacket.getScanNumber(p);
			}
			if (isVIIRSEnggPacket(p)) {
				System.out.println("Engineering packet.");
				System.out.println("E.P. Timestamp is: " + p.getTimeStamp(8));
				scanNumber = ViirsEnggPacket.getScanNumber(p);

				// Check if this is the first engineering packet we are receiving
				if(lastEngPacketScanNumber==-999){
					//Check if we have already received a Calibration Packet (LEGACY)
					if(lastCalPacketScanNumber >= 0){
						//We will have to remove all packets with this scan number
						//System.out.println("******Need to remove packets with scan-number " + 
						//			lastCalPacketScanNumber +
						//			" scanCounter= " + scanCounter);
						//packetPool.flush(getPacketList());
						//scanCounter=0;
						//setFirstTime(0L);
					}
					lastEngPacketScanNumber=scanNumber;	
				}
			}
			if (isVIIRSCalPacket(p)) {
				scanNumber = VIIRSCalibrationPacket.getScanNumber(p);
			}
			
			// Scan number changed. If the delta is > +1, it has to be a dropout.
			// Either way, scan number changed so we must indicate a scan transition.
			if (scanNumber != currentScanNumber) {
				if(currentScanNumber < 0)
					System.out.println("NEW GRANULE START " + currentScanNumber+":"+scanNumber);
				else if(scanNumber - currentScanNumber > 1)
					System.out.println("DROPOUT DETECTED "+currentScanNumber+":"+scanNumber);
				else if(scanNumber - currentScanNumber < 0)
					System.out.println("OUT OF ORDER PACKET "+currentScanNumber+":"+scanNumber);
				scanCountTransition = true;
			}
		}
		
		// When we receive VIIRS Science First packets (since only they use 2nd header). Usually indicates the
		// start of a new scan, but that doesn't necessesarily mean the granule is full already.
		if (p.hasSecondaryHeader()) {
			// Packet IET time is packet timestamp converted to microseconds since epoch
			long ietTimeCurrentPacket = LeapDate.getMicrosSinceEpoch(p.getTimeStamp(8));

			// In this case, we start a new granule. Calculate new start/end boundaries.
			if(currentScanNumber == -1) {
				if(!VIIRSGranule.isBaseTimeSet()){
					//VIIRSGranule.setBaseTime(ietTimeCurrentPacket);
					VIIRSGranule.setBaseTime();
				}    
				currentgranule_startboundary=VIIRSGranule.getStartBoundary(ietTimeCurrentPacket);
				currentgranule_endboundary=VIIRSGranule.getEndBoundary(ietTimeCurrentPacket);
				System.out.println("Setting startBoundary and endBoundary"+
							ietTimeCurrentPacket+" "+
							currentgranule_startboundary+" "+
							currentgranule_endboundary);	
			}
			// If the ietTime is > startBoundary and <= endBoundary (or should it be >= startBoundary and < endBoundary)
			// then granule is not full; else it is full
			if(ietTimeCurrentPacket >= currentgranule_startboundary && ietTimeCurrentPacket < currentgranule_endboundary)
				notFullStatus = true;
			else			
				notFullStatus = false;
		}
		
		// If we started a new scan and granule is not yet full, ensure that all counters are updated accordingly.
		if (scanCountTransition && notFullStatus) {
			currentScanNumber = scanNumber;
			System.out.println("Current scan number = "+currentScanNumber);
			if (isVIIRSSciencePacket(p)) 
				System.out.println("**Transitioning on Science Packet: Must be start of swath or a dropout");
			else if (isVIIRSEnggPacket(p)) {
				System.out.println("**Transitioning on Eng. Packet: "+scanNumber);
				++scanCounter;  
				return true;
			}
			else if (isVIIRSCalPacket(p))
				System.out.println("**Transitioning on Cal. Packet: Must be start of swath or a dropout");	
		}

		return notFullStatus;
	}

	// uses the PacketName set of VIIRS packet enums to check that the packet given is in range
	// OR we could just hardcode the APIDs here but... this so much better obviously right? :-)
	private boolean isVIIRSSciencePacket(Packet p) {
		return sciPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}
	private boolean isVIIRSEnggPacket(Packet p) {
		return engPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}
	private boolean isVIIRSCalPacket(Packet p) {
		return calPacketRange.contains(PacketName.fromAppId(p.getApplicationId()));
	}

	/**
	 * !!! WARNING: ALWAYS CALL notFull(Packet p) FIRST BEFORE CALLING THIS FUNCTION !!!
	 * The packet is stored until a packet transition which is checked in the notFull() method.
	 * When full the RawApplicationPacket should then be written to the HDF
	 * 
	 * @param p VIIRS science packet to be written
	 * @throws RtStpsException the packet is not a VIIRS science packet or the scan number does not 
	 * match previous packets (because notFull was not called first)
	 */
	public void put(Packet p) throws RtStpsException {
		// Update our counters
		//sciPacketCount++;
		this.updateAppIdCounters(p.getApplicationId());

		// Get the RDR this packet belongs to, based on packet appID. Complain if it's not a VIIRS
		// Science packet (which means filtering did not work properly).
		RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
		if (rdrName != RDRName.VIIRS_Science) {
			throw new RtStpsException("Not a VIIRS science packet [" + p.getApplicationId() + "]");
		}
	
		// No scan lock yet, this is could be APID=826 or maybe 825 from a the previous scan
		// so we throw it out...
		if ((currentScanNumber < 0) && (isVIIRSSciencePacket(p) == false)) {
			return;
		}
		
		// If the packet uses a secondary header, we want to check and record various timestamp information.
		// In the case of VIIRS Science packets, only First packets use the CCSDS secondary header.
		if (p.hasSecondaryHeader()) {
			// Do not continue if the timestamp is corrupted, as it could mess up our Aggregates.
		    	if(p.getTimeStamp(8) <= 0){
		    	    System.out.println("Corrupted timestamp, not using packet.");
		    	    return;
		    	}
		    
			// this handles the case that the collection was full, and
			// the block of packets was written...  and then the packet
			// that was trying to be written is written immediately after this 
			if (currentScanNumber < 0) {
				currentScanNumber = VIIRSSciencePacket.getScanNumber(p);
			}

			if (getFirstTime() == 0L) {
				setFirstTime(p.getTimeStamp(8));
			}
			
			// Make this packet timestamp the latest timestamp we have encountered...
			setLastTime(p.getTimeStamp(8));
			// Initialize the first system time, if it's not done so already.
			if (getFirstSystemTime() == 0L) {
				setFirstSystemTime(PDSDate.getCurrentSystemTimeMicrosSinceEpoch());
			}
			// Set the latest system time since receiving this science packet
			setLastSystemTime(PDSDate.getCurrentSystemTimeMicrosSinceEpoch());
		}

		// The packet is then deep copied; it is allocated off a pool
		// which may hold previously copied packets...  they must be put
		// back on the pool when done...
		Packet pcopy = CopyPacket.deep(p, packetPool);
		getPacketList().add(pcopy);
		sciPacketCount++;
		// Was the packet put into list a Calibration or Engineering Packet?
		// If so record their scan no. & index as the latest we've encountered.
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()){
			if (isVIIRSEnggPacket(p)) {
				lastEngPacketScanNumber = ViirsEnggPacket.getScanNumber(p);
				lastEngPacketIndex = getPacketList().size()-1;
			}
			if (isVIIRSCalPacket(p)) {
				lastCalPacketScanNumber = VIIRSCalibrationPacket.getScanNumber(p);
				lastCalPacketIndex = getPacketList().size()-1;
			}
		}
		
	}
	
	/**
	 * Write the collected group of packets to the designated HDF file using the handle
	 * @return true if the RawApplicationPacket was written, false if not
	 * @exception any HDF exceptions are wrapped in an RtStpsException
	 */
	public boolean write(int hdfFile) throws RtStpsException {
		boolean ret = true;
		
		// Return false immediately if we don't have any scans
		if(this.scanCounter == 0){
			return false;
		}
		
		//System.out.println("In Write -- " + this.packetPool.toString());
		//if(lastEngPacketScanNumber>lastCalPacketScanNumber)
		//	packetPool.removeEnd(getPacketList(),lastEngPacketIndex);
		
		// DRO would probably be fine to use a static value for number of APs expected
		float percentMissingData= ((VIIRSGRANULE_MAXPACKETS-sciPacketCount)/ (float)VIIRSGRANULE_MAXPACKETS)*100;
		System.out.println("Percent Missing="+percentMissingData);
		setPercentMissingData(percentMissingData);
		
		// Make sure that wasn't the only packet and there were no engineering packets in the scan
		if(getPacketList().size()>0 && lastEngPacketScanNumber!=-999)
			super.write(hdfFile); 
		else
			ret=false;
		
		if (stats == null) {
			System.out.println("Creating VIIRS Granule -- [" + this.scanCounter + "] scans");
		} else {
			stats.sci_createdGranules.value++;
		}
		//Set start and endBoundary for the next granule
		//currentgranule_startboundary=currentgranule_endboundary;
		//currentgranule_endboundary=currentgranule_endboundary+granuleSize;
				
		System.out.println(">> VIIRS Sci packets in granule: " + sciPacketCount + " Packet per scan: " + (float)sciPacketCount / (float)scansPerGranule);
		System.out.println(">> VIIRS app ids = " + super.appIdCountersToString(scansPerGranule));
		sciPacketCount = 0;

		// Reset current scan number and total scan counter, so we can start collecting for a new granule
		this.currentScanNumber = -1;
		this.scanCounter = 0;
		
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
	}

}
