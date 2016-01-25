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
 * Build CRIS [@link RawApplicationPackets} by capturing groups scan packets between packet with
 * and application ID of 1289.  The first group captured is likely going to be short due
 * to the fact that frame lock could occur at any time.  Scans groups that form a granule are specified
 * externally in the constructor.
 * 
 * 
 *
 */
public class CRISRawApplicationPackets extends RawApplicationPackets {
	private boolean scansStarted = false;
	private int scanCounter = 0;
	private int scansPerGranule;
	private Stats stats = null;
	private boolean firstPacket = true;
	
	/**
	 * Constructor to initialize the RawApplicationPackets algorithm for CRIS.
	 * @param satellite the satellite ID as defined by the mission documents
	 * @param setNum the set number which is appended to the end of each RawApplicationPacket in the HDF
	 * @param scansPerGranule The number of scans to collect to make one granule
	 */
	public CRISRawApplicationPackets(SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.CRIS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
		
		this.scansPerGranule = scansPerGranule;
		
	}
	
	public CRISRawApplicationPackets(Stats stats, SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.CRIS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
		
		this.scansPerGranule = scansPerGranule;
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
	public CRISRawApplicationPackets(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public CRISRawApplicationPackets(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}

	/**
	 * Check that this object is not full and can accumulate more packets before being written.
	 * Specifically this is the CRIS variation which collects packets as singular scans between
	 * the 1289 packets.   The first group is special as it may not be a full scan as lock may 
	 * have occurred at any time.  The granule for it then will likely be short.
	 * @param p  a Packet to be put into the CRIS RawApplicationPackets area
	 * @return true if it can be put there, or false if it is full and should be written to disk first
	 * @exception wraps any HDF exceptions in RtStpsException
	 */
	public boolean notFull(Packet p) throws RtStpsException {
		boolean scanNotFull = true;
		int appId = p.getApplicationId();
		
		RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
		
		if (rdrName != RDRName.CRIS_Science) {
			throw new RtStpsException("Not a CRIS packet [" + appId + "]");
		}
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			if (scansStarted) {
				// if the scans have started, that is the first 1289 has appeared
				// then determine if the number of scans processed is enough to make
				// a full granule.
				// The scanCounter is set to scan BEING COUNTED so it must increment
				// past the scansPerGranule in order for the granule to be full.
				// These items will be reset in the write/close methods ...
				//
				if (appId == 1289) {
					++scanCounter;
					
					if (scanCounter >= scansPerGranule) {
						scanNotFull = false;
					}
				} else {
					// collect them
					scanNotFull = true;
				}
				
			} else {
				// if the packet 1289 has not appeared yet, all the packets
				// from CRIS are collected until that time...
				// When it finally appears, the scan will be marked as started but all the
				// collected packets up to this time are put into a granule
				// which should be granule_0.  To do that this method returns
				// true first, that is that's its full but marks the scan state as started
				// but scanCounter is left at 0 until it is processed above
				// 
				if (appId == 1289) {
					if (firstPacket) {
						firstPacket = false;
						scansStarted = true;
						scanNotFull = true;
					} else {
						scansStarted = true;
						scanNotFull = false;
					}
				} else {
					// otherwise all packets are simply collected
					scanNotFull = true;
				}
			}
		}
		// otherwise any non-first packets are simply collected
		// although there should not be any for CRIS
		
		return scanNotFull;
	}
	
	
	
	/**
	 * Put the packet into the CRISRawApplications area, the method <code>notFull</code> should
	 * have been called first to check if this is allowed or not.
	 * @param p a Packet to put into it
	 * @exception wraps any HDF exceptions in RtStpsException
	 */
	public void put(Packet p) throws RtStpsException {
		
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
		if (scanCounter < scansPerGranule) {

			if (stats == null) {
				System.out.println("Deleting partial CRIS Granule -- [" + scanCounter + "] scans");
			} else {
				stats.sci_discardedGranules.value++;
			}

			ret = false;
		} else {

			if (stats == null) {
				System.out.println("Creating CRIS Granule with [" + scanCounter + "] scans");
			} else {
				stats.sci_createdGranules.value++;
			}

			ret = super.write(hdfFile);
		}
		this.scanCounter = 0;
		
		return ret;
	}

	/**
	 * Close the RDR/HDF file specifically for the RawApplicationsPackets.  This method should be 
	 * called once specific RawApplicationPacket_x has been written. 
	 * @exception wraps any HDF exceptions in RtStpsException
	 */
	public void close() throws RtStpsException  {
		super.close();
		this.scansStarted = false; 
		this.scanCounter = 0; 
	}


}
