/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.text.ParseException;

/**
 * This works for the SDR algorithm with the time code checks turned off -- ONE granule test only
 * @author krice
 *
 */
public class VIIRSGranule extends Granule {
 // 85 400 000
 // 85 350 000
	private final static long lpeateGranuleSize = 1778125L;  // calculated from LPEATE RDRs
	//private long scansPerGranule = 48L;
	//private long endOffset = lpeateGranuleSize * scansPerGranule;
	
	//private LPEATEDate firstDateTime;
	private LPEATEDate firstDateTime;
	private LPEATEDate lastDateTime;
	private PDSDate predictedEndDateTime;
	
	// LPEATE no longer requires truncated times?
	//private PDSDateTrunc beginningTimeDateTime;
	//private PDSDateTrunc predictedEndingTimeDateTime;
	private PDSDate beginningTimeDateTime;
	private PDSDate predictedEndingTimeDateTime;
	
	
	// Defined delta time for granules: 85350000 microseconds or 85.35 seconds.
	// NPP's VIIRS instrument supposedly generates 48-scan granules, but sometimes it is short by one?
	private final static long granuleSize=85350000L;	

	// S-NPP's base epoch time, used for calculating granule boundaries!
	private static long baseTime=1698019234000000L;
	private static boolean baseTimeSet=false;
	
	

	public static boolean isBaseTimeSet() {
		return baseTimeSet;
	}


	public static void setBaseTimeSet(boolean baseTimeSet) {
		VIIRSGranule.baseTimeSet = baseTimeSet;
	}



	public static long getBaseTime() {
		return baseTime;
	}

	public static void setBaseTime() { //uses NPP base time
		VIIRSGranule.baseTimeSet = true;
	}

	public static void setBaseTime(long baseTime) {
		long baseTimedivide=baseTime/1000000;
		long baseTimeEvenSecond=baseTimedivide*1000000;
		VIIRSGranule.baseTime = baseTimeEvenSecond;
		System.out.println("Set VIIRS baseTime="+VIIRSGranule.baseTime);
		VIIRSGranule.baseTimeSet = true;
	}

	/**
	 * Constructor for a Granule instance, the arguments are associated with the granule's attributes.
	 * An instance of this class in this packet is created by some other factory method.
	 * 
	 * @param rap the corresponding RawApplicationPackets area
	 * @param orbit the orbit number of the pass
	 * @param granuleId the granuleId {@link GranuleId}
	 * @param leoaState the LEO state flag
	 * @param docName the document name of the specification controlling this granules construction
	 * @param packetTypes an array of packet types received in this granule
	 * @param packetTypeCounts the counts per type of the packets received
	 * @param referenceId the reference identifier which is a UUID {@link java.util.UUID}
	 * @param granuleNumber the granule number which corresponds to the RawApplicationPackets number in the RDR/HDF file
	 * @param dataSpaceOfRaw the HDF DataSpace handle of the RawApplicationPackets area associated with this granule
	 */
	public VIIRSGranule(RawApplicationPackets rap,
					long orbit,
					GranuleId granuleId,
					LEOAFlag leoaState,
					String docName,
					String[] packetTypes,
					long[] packetTypeCounts,
					ReferenceId referenceId,
					int granuleNumber, 
					int dataSpaceOfRaw) {
		super(rap, 
			orbit, 
			granuleId, 
			leoaState, 
			docName, 
			packetTypes, 
			packetTypeCounts, 
			referenceId, 
			granuleNumber,  
			dataSpaceOfRaw,
			RDRName.VIIRS_Science);
	}
	
	long getEndOffSet() {
		return lpeateGranuleSize * getScansPerGranule();
	}
	
	long getScansPerGranule() {
		
		return 48;  // should be 48 but its the scan count for VIIRSRawAppPacket3 right now
	}

	/**
	 * Read the Granule out of the HDF file and fill in the various attributes and items in it for
	 * these access methods
	 * @param groupId the HDF group handle
	 * @param granuleName  the name of the granule
	 * @throws ParseException 
	 */
	public VIIRSGranule(int groupId, String granuleName) throws RtStpsException {
		super(groupId, granuleName);
	}
	@Override
	public PDSDate getBeginningObservationDateTime(RawApplicationPackets rap) {
		this.firstDateTime = new LPEATEDate(rap.getFirstTime());  // packet time
		
		
		// ignore this, from an earlier test
		//long t_microseconds = 1422168873750000L;
		//long MicrosPerDay = 86400000L * 1000L;
		//long c_Day = t_microseconds / MicrosPerDay;
		
		//t_microseconds = t_microseconds - (c_Day * MicrosPerDay);
		
		//long c_Millis = (t_microseconds / 1000L);
		
		//long c_Micros = t_microseconds - (c_Millis * 1000L);

		
		//long rawDay = c_Day;
		//long rawMillis = c_Millis;
		//long rawMicros =  c_Micros;
		

		//long rawPacketTime =   ((rawDay << 48)    & 0xffff000000000000L);
		//rawPacketTime |= ((rawMillis << 16)  & 0x0000ffffffff0000L);
		//rawPacketTime |=  (rawMicros         & 0x000000000000ffffL);
		
		
		//this.firstDateTime = new PDSDateTrunc(rawPacketTime);  // VIIRS test
		return firstDateTime;
	}

	
	@Override
	public PDSDate getEndingObservationDateTime(RawApplicationPackets rap) {
		this.lastDateTime = new LPEATEDate(rap.getLastTime());  // this is saved for no particular reason at this time
		
		// this is the one that's returned, it's the predicated time
		// that's the first time plus the predicated offset... 
		
		this.predictedEndDateTime = firstDateTime.addMicros(getEndOffSet() + 50000L);
		
		// viirs test
		//PDSDateTrunc predictedEndDateTime =  new PDSDateTrunc(lastDateTime.getOriginalPacketTime()); // VIIRS partial granule test
		//this.predictedEndDateTime = predictedEndDateTime.addMicros(1350000L);
		//return this.predictedEndDateTime;
		return this.lastDateTime;
	}
	@Override
	public PDSDate getBeginningTimeDateTime() {
		// Warning: This requires the Granule's beginning IET to be calculated first!
		//beginningTimeDateTime = new PDSDateTrunc(firstDateTime.getOriginalPacketTime());
		beginningTimeDateTime = LeapDate.ietToPDSNoLeap( getBeginningIET() );
		// FIXME this should be TRUNCATED as well
		return beginningTimeDateTime;
	}
	@Override
	public PDSDate getEndingTimeDateTime() {
		// Warning: This requires the Granule's ending IET to be calculated first!
		//System.out.println("Endoffset == " + getEndOffSet());
		
		//this.predictedEndingTimeDateTime = beginningTimeDateTime.addMicros( getEndOffSet() + 50000L);
		this.predictedEndingTimeDateTime = LeapDate.ietToPDSNoLeap( getEndingIET() );
		// viirs test
		//predictedEndingTimeDateTime = new PDSDateTrunc(lastDateTime.getOriginalPacketTime());
		
		//predictedEndingTimeDateTime = predictedEndingTimeDateTime.addMicros(1350000L);
		return predictedEndingTimeDateTime;

	}

	public static long getGranulesize() {
		return granuleSize;
	}

	public static long getStartBoundary(long arbitraryTime)
	{
		
		 //Taken from InfUtil_GranuleID.cpp
		 // ----------------------------------------------------------------------
		   // The following steps calculate the granule ID, granule start time, and
		   // granule end time.
		   // ----------------------------------------------------------------------

		   // Subtract the spacecraft base time from the arbitrary time to obtain
		   // an elapsed time. 
		   long elapsedTime = arbitraryTime - baseTime;

		   // Divide the elapsed time by the granule size to obtain the granule number; 
		   // the integer division will give the desired FLOOR value.
		   long granuleNumber = elapsedTime / granuleSize;

		   // Multiply the granule number by the granule size, then add the spacecraft
		   // base time to obtain the granule start boundary time. Add the granule
		   // size to the granule start boundary time to obtain the granule end
		   // boundary time.
		   long startBoundary = (granuleNumber * granuleSize) + baseTime;
		   //long endBoundary = startBoundary + granuleSize;
		   return startBoundary;
	}
	public static long getEndBoundary(long arbitraryTime)
	{
		
		 //Taken from InfUtil_GranuleID.cpp
		 // ----------------------------------------------------------------------
		   // The following steps calculate the granule ID, granule start time, and
		   // granule end time.
		   // ----------------------------------------------------------------------

		   // Subtract the spacecraft base time from the arbitrary time to obtain
		   // an elapsed time. 
		   long elapsedTime = arbitraryTime - baseTime;

		   // Divide the elapsed time by the granule size to obtain the granule number; 
		   // the integer division will give the desired floor value.
		   long granuleNumber = elapsedTime / granuleSize;
		   long granuleIDValue = (granuleNumber * granuleSize) / 100000;
                   System.out.println("granule ID="+granuleIDValue);

		   // Multiply the granule number by the granule size, then add the spacecraft
		   // base time to obtain the granule start boundary time. Add the granule
		   // size to the granule start boundary time to obtain the granule end
		   // boundary time.
		   long startBoundary = (granuleNumber * granuleSize) + baseTime;
		   long endBoundary = startBoundary + granuleSize;
		   return endBoundary;
	}


}
