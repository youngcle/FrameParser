/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

public class SpacecraftDiaryRawApplicationPackets2 extends RawApplicationPackets {

	private long firstTime = -1l;
	private boolean fullGranule = false;
	private Stats stats = null;
	private long currentTimeOfGranule = 0l;
	
	private long currentgranule_startboundary=-1L;
	private long currentgranule_endboundary=-1L;
	private int diaryPacketCount = 0; 
	
	//Maximum no of packets in a granule = 21*3=63
	private static long DIARYGRANULE_MAXPACKETS=63;

	public SpacecraftDiaryRawApplicationPackets2(SpacecraftId satellite, int setNum, PacketPool packetPool) {
		super(satellite,  RDRName.NPP_Ephemeris_and_Attitude, setNum, packetPool);
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
	}
	
	public SpacecraftDiaryRawApplicationPackets2(Stats stats, SpacecraftId satellite, int setNum, PacketPool packetPool) {
		super(satellite,  RDRName.NPP_Ephemeris_and_Attitude, setNum, packetPool);
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
	 * @throws NullPointerException 
	 * @throws HDF5LibraryException 
	 * @throws RtStpsException 
	 */
	public SpacecraftDiaryRawApplicationPackets2(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public SpacecraftDiaryRawApplicationPackets2(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}


	@Override
	public boolean notFull(Packet p) throws RtStpsException {
	    	
	    	//if(this.getPacketList().size() > 60)
	    	 //   return false;
	    	
		boolean notFull = true;
		//System.out.println("S/C APID:"+p.getApplicationId()+":"+p.getTimeStamp(8));
		
			long ietTimeCurrentPacket=LeapDate.getMicrosSinceEpoch(p.getTimeStamp(8));
			long timestamp = p.getTimeStamp(8);
			
			if(this.firstTime < 0l) //Starting a granule
			{
				if(!SpacecraftDiaryGranule.isBaseTimeSet())
					SpacecraftDiaryGranule.setBaseTime();
					//SpacecraftDiaryGranule.setBaseTime(ietTimeCurrentPacket);
				this.firstTime = timestamp;
				currentgranule_startboundary=SpacecraftDiaryGranule.getStartBoundary(ietTimeCurrentPacket);
				currentgranule_endboundary=SpacecraftDiaryGranule.getEndBoundary(ietTimeCurrentPacket);
				System.out.println("Setting Diary startBoundary and endBoundary"+ietTimeCurrentPacket+" "+currentgranule_startboundary+" "+currentgranule_endboundary);
				
			}
			//If the ietTime is > startBoundary and <=endBoundary (or should it be >=startBoundary and < endBoundary)
			//then granule is not full; else it is full
			if(ietTimeCurrentPacket>=currentgranule_startboundary && ietTimeCurrentPacket<currentgranule_endboundary)
			{
				notFull = true;
			}
			else
			{			
				notFull=false;
				fullGranule=true;
			}
		
		return notFull;
	}
	/*public boolean notFull(Packet p) throws RtStpsException {
		boolean notFull = true;
		//System.out.println("S/C APID:"+p.getApplicationId()+":"+p.getTimeStamp(8));
		if (p.getApplicationId() == 11) {  // idea is we allow apid=0 and 8 in but only check for fullness on apid=11
			long timestamp = p.getTimeStamp(8);
			if (this.firstTime < 0l) {
				this.firstTime = timestamp;
			} else {
				
				if (greaterThanEqualTo(timestamp, firstTime, 20)) {
					notFull = false;  // if 20secs have gone by, the granule is full
					fullGranule = true;
				}
			}
		}
		return notFull;
	}*/

	@Override
	public void put(Packet p) throws RtStpsException 
	{
		diaryPacketCount++;
		this.updateAppIdCounters(p.getApplicationId());
		
		if (p.getApplicationId() == 11) {// idea is we allow apid=0 and 8 in but only check for fullness on apid=11

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
	    
	    	int i = 0;
	    	int size = getPacketList().size();
	    
		boolean ret = true;
		
		//DRO would probably be fine to use a static value for number of APs expected
		float percentMissingData= ((DIARYGRANULE_MAXPACKETS - diaryPacketCount) / (float) DIARYGRANULE_MAXPACKETS) * 100;
		System.out.println("Percent Missing="+percentMissingData);
		if(percentMissingData < 0)
		{
		    System.out.println("Too large granule!");
		}
		/* statistical analysis shows that spacecraft diary granules that contain more than 96 percent missing data
		 * will invariably have not enough data to present a proper timestamp
		 */
		if(percentMissingData > 96)
		{
			System.out.println("SpacecraftAOS Diary has more than 96% missing data.");
			return false;
		}
		setPercentMissingData(percentMissingData);
		
		if (fullGranule == true) 
		{
			if (stats == null) 
			{
				System.out.println("Creating SpacecraftAOS Diary Granule -- [" + 20 + "] seconds"); // FIXME do we show them the real time or the near time?
			} 
			else 
			{
				stats.ae_createdGranules.value++;
			}
			
			ret = super.write(hdfFile);		
		} 
		else 
		{	
			if (stats == null) 
			{
				double time = currentTimeOfGranule/1000000.0;
				System.out.println("Creating Partial SpacecraftAOS Diary Granule -- [" + time + "] seconds");
			} 
			else 
			{
				stats.ae_createdGranules.value++;
			}
			
			ret = super.write(hdfFile);
		}
		
		fullGranule = false;
		this.firstTime = -1l;
		
		return ret;
	}
	
	/**
	 * Close out the RawApplicationPacket which writes the results to the HDF file and cleans up.
	 * @exception any HDF exceptions are wrapped in an RtStpsException
	 */
	public void close() throws RtStpsException {
		super.close();
		this.firstTime = -1L; 
		fullGranule = false;
	}
	
	// see if time between current time and old time is X seconds or more
	public boolean greaterThanEqualTo(long timeStamp1, long timeStamp2, int seconds) {
		
		long rawDay1 = (timeStamp1 >> 48) & 0x0ffffL;
		long rawMillis1 = (timeStamp1 >> 16) & 0x0ffffffffL;
		long rawMicros1 = timeStamp1 & 0x0ffffL;
		
		long micros1 = 86400000000l * rawDay1;
		micros1 += rawMillis1 * 1000l;
		micros1 += rawMicros1;
		
		long rawDay2 = (timeStamp2 >> 48) & 0x0ffffL;
		long rawMillis2 = (timeStamp2 >> 16) & 0x0ffffffffL;
		long rawMicros2 = timeStamp2 & 0x0ffffL;
		
		long micros2 = 86400000000l * rawDay2;
		micros2 += rawMillis2 * 1000l;
		micros2 += rawMicros2;
		
		currentTimeOfGranule = micros1 - micros2;
		
		long tmp2 = (long) seconds * 1000000l;
		return (currentTimeOfGranule >= tmp2);
	}
	
}
