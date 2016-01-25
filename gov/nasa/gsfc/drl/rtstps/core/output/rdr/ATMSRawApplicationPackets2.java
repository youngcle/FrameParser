/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * Build the RawApplicationPackets (or read a pre-existing one) for the HDF for ATMS.
 * Due to problems with the ATMS packets in the test data sets, this just collect X
 * seconds worth at a time...
 * 
 * 
 *
 */



/************
 * DEPRECATED
 ************/





public class ATMSRawApplicationPackets2 extends RawApplicationPackets {
	
	private final static long GranuleSizeInSeconds = 32;
	
	private long timeSpanInSeconds = 0L;
	private long localFirstTime = 0L;
	private long localCurrentTime = 0L;

	private Stats stats = null;

	/**
	 * Constructor for creating an nth instance of a ATMS raw application data packet area builder.
	 * @param satellite  the name of the spacecraft as a SpacecraftId
	 * @param setNum  the set number
	 * @param scansPerGranule the number of sensor scans per granule
	 */
	public ATMSRawApplicationPackets2(SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.ATMS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
	}
	

	public ATMSRawApplicationPackets2(Stats stats, SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.ATMS_Science, setNum, packetPool);
		
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
	public ATMSRawApplicationPackets2(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public ATMSRawApplicationPackets2(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}
	

	public boolean notFull(Packet p) throws RtStpsException {

		// collect time info
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			calcTimeSpanInSeconds(p);
		}
		
		if (timeSpanInSeconds > GranuleSizeInSeconds) {
			// update the 'first' time to current
			localFirstTime = localCurrentTime;
			return false; // FULL
		}
		return true;  // NOT FULL
	}
	
	

	private void calcTimeSpanInSeconds(Packet p) {
		if (localFirstTime == 0L) {
			
			localFirstTime = PDSDate.getMicrosSinceEpoch(p.getTimeStamp(8)) / 1000000L;
		}
		
		localCurrentTime = PDSDate.getMicrosSinceEpoch(p.getTimeStamp(8)) / 1000000L;
	
		timeSpanInSeconds = localCurrentTime - localFirstTime;
		
	}

	@Override
	public void put(Packet p) throws RtStpsException {
		
		this.updateAppIdCounters(p.getApplicationId());
		
		RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
		
		if (rdrName != RDRName.ATMS_Science) {
			throw new RtStpsException("Not a ATMS science packet [" + p.getApplicationId() + "]");
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
	 * Write the ATMSRawApplication structure to the given HDF file specified by the HDF input handle
	 * @param hdfFile input handle to a previous opened HDF file
	 * @return true if the write succeeds
	 * @throws RtStpsException Wraps HDF exceptions
	 */
	@Override
	public boolean write(int hdfFile) throws RtStpsException {
		
		if (stats == null) {
			System.out.println("Creating ATMS  of [" + GranuleSizeInSeconds + "] seconds");
		} else {
			stats.sci_createdGranules.value++;
		}
		return super.write(hdfFile);

	}
	

	
	/**
	 * Close out the RawApplicationPacket which writes the results to the HDF file and cleans up.
	 */
	public void close() throws RtStpsException {
		super.close();
	}

}
