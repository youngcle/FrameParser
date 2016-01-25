/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Build VIIRS [@link RawApplicationPackets} by capturing groups scan packets between packet with
 * and application ID of 800 with a secondary header timestamp.  The first group captured is likely going to be short due
 * to the fact that frame lock could occur at any time.  Scans groups that form a granule are specified
 * externally in the constructor.
 * 
 * 
 *
 */

// FIXME this one does not work well on the Orbit2 dateset which seems to have a long stretch of bad scans resulting
// in the accumulation of a large number of unrelated packets...
@Deprecated
public class VIIRSRawApplicationPackets2 extends RawApplicationPackets {
	private final int scansPerGranule = 48; // approx x secs worth
	private Stats stats = null;
	private int scanCount = 0;
	private int sciPacketCount = 0;
	
	/**
	 * Constructor to initialize the RawApplicationPackets algorithm for VIIRS.
	 * @param satellite the satellite ID as defined by the mission documents
	 * @param setNum the set number which is appended to the end of each RawApplicationPacket in the HDF
	 * @param scansPerGranule The number of scans to collect to make one granule
	 */
	public VIIRSRawApplicationPackets2(SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.VIIRS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
		
		
		
	}
	
	public VIIRSRawApplicationPackets2(Stats stats, SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
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
	 * @throws NullPointerException 
	 * @throws HDF5LibraryException 
	 */
	public VIIRSRawApplicationPackets2(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public VIIRSRawApplicationPackets2(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}

	/**
	 * Granules are created from scans.  Scans in VIIRS are lots of packets the first 800 w/with a 
	 * secondaryheader is used here to signal the start of a scan.   We collect it and everything after it
	 * as one scan until it re-appears.  Anything more complex than this can be built on top of this
	 * one assumption.
	 * 
	 * @param p  a Packet to be put into the VIIRS RawApplicationPackets area
	 * @return true if it can be put there, or false if it is full and should be written to disk first
	 * @exception wraps any HDF exceptions in RtStpsException
	 */
	public boolean notFull(Packet p) throws RtStpsException {
		
		// every appearance of first 800 w/secondary header means start of scan
		boolean state = true;
		
		if (p.isFirstPacketInSequence() && p.hasSecondaryHeader()) {
			if (p.getApplicationId() == 800) {
				
						if ((scanCount + 1) > scansPerGranule) {
							state = false; // it's full in otherwords
						
						} else {
							++scanCount;
						}
						//System.out.println("VIIRs Current scan count -- " + scanCount);
						
					
			}
		}
		return state;  // true == not full, false == full
		

	}
	
	
	
	/**
	 * Put the packet into the VIIRSRawApplications area, the method <code>notFull</code> should
	 * have been called first to check if this is allowed or not.
	 * @param p a Packet to put into it
	 * @exception wraps any HDF exceptions in RtStpsException
	 */
	public void put(Packet p) throws RtStpsException {
		if (scanCount <= 0) { // throw away any initial packets if we've not seen the start of a scan...
			if (stats == null) {
				System.out.println("Trimming VIIRS [" + p.getApplicationId() + "]");
			} else {
				//stats.sci_trimmedPackets.value++;
			}
			return;
		}
		sciPacketCount++;
		
		this.updateAppIdCounters(p.getApplicationId());
		
		RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
		
		if (rdrName != RDRName.VIIRS_Science) {
			throw new RtStpsException("Not a VIIRS science packet [" + p.getApplicationId() + "]");
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
				System.out.println("Deleting partial VIIRS Granule -- [" + scanCount + "] scans");
			} else {
				stats.sci_discardedGranules.value++;
			}

			ret = false;
		} else {

			if (stats == null) {
				System.out.println("Creating VIIRS Granule with [" + scanCount + "] scans");
			} else {
				stats.sci_createdGranules.value++;
			}

			ret = super.write(hdfFile);
		}
		
		System.out.println(">> VIIRS Sci packets in granule: " + sciPacketCount + " Packet per scan: " + (float)sciPacketCount / (float)scansPerGranule);
		scanCount = 0;
		
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
		scanCount = 0;
		
		sciPacketCount = 0;
	}


}
