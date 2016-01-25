/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.text.ParseException;


//placeholder for further specialization
public class SpacecraftDiaryGranule extends Granule {
	
	
	private static long endOffset = 20000000L;
	private LPEATEDate firstDateTime;
	private LPEATEDate lastDateTime;
	private PDSDate predictedEndDateTime;
	//private PDSDateTrunc beginningTimeDateTime;
	//private PDSDateTrunc predictedEndingTimeDateTime;
	private PDSDate beginningTimeDateTime;
	private PDSDate predictedEndingTimeDateTime;

	private final static long granuleSize=20000000;

	private static long baseTime=1698019234000000L;
	private static boolean baseTimeSet=false;
	
	

	public static boolean isBaseTimeSet() {
		return baseTimeSet;
	}


	public static void setBaseTimeSet(boolean baseTimeSet) {
		SpacecraftDiaryGranule.baseTimeSet = baseTimeSet;
	}



	public static long getBaseTime() {
		return baseTime;
	}


	public static void setBaseTime() { //will use the default value- NPP base time
		SpacecraftDiaryGranule.baseTimeSet = true;
	}

	public static void setBaseTime(long baseTime) {
		long baseTimedivide=baseTime/1000000;
		long baseTimeEvenSecond=baseTimedivide*1000000;
		SpacecraftDiaryGranule.baseTime = baseTimeEvenSecond;
		SpacecraftDiaryGranule.baseTimeSet = true;
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
	public SpacecraftDiaryGranule(RawApplicationPackets rap,
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
				RDRName.NPP_Ephemeris_and_Attitude);
		System.out.println("writing one diary granule. with dataSpaceRaw: " + dataSpaceOfRaw);
	}
	/**
	 * Read the Granule out of the HDF file and fill in the various attributes and items in it for
	 * these access methods
	 * @param groupId the HDF group handle
	 * @param granuleName  the name of the granule
	 * @throws ParseException 
	 */
	public SpacecraftDiaryGranule(int groupId, String granuleName) throws RtStpsException {
		super(groupId, granuleName);
	}
	
	@Override
	public PDSDate getBeginningObservationDateTime(RawApplicationPackets rap) {
		this.firstDateTime = new LPEATEDate(rap.getFirstTime());  // packet time
		return firstDateTime;
	}

	
	@Override
	public PDSDate getEndingObservationDateTime(RawApplicationPackets rap) {
		this.lastDateTime = new LPEATEDate(rap.getLastTime());  // this is saved for no particular reason at this time
		
		// this is the one that's returned, it's the predicated time
		// that's the first time plus the predicated offset... 
		
		this.predictedEndDateTime = firstDateTime.addMicros(endOffset);
		return predictedEndDateTime;
	}

	@Override
	public PDSDate getBeginningTimeDateTime() {
		//beginningTimeDateTime = new PDSDateTrunc(firstDateTime.getOriginalPacketTime());
		beginningTimeDateTime = LeapDate.ietToPDSNoLeap( getBeginningIET() );
		// FIXME this should be TRUNCATED as well
		return beginningTimeDateTime;
	}
	@Override
	public PDSDate getEndingTimeDateTime() {
		//this.predictedEndingTimeDateTime = beginningTimeDateTime.addMicros(endOffset);
		this.predictedEndingTimeDateTime = LeapDate.ietToPDSNoLeap( getEndingIET() );
		return predictedEndingTimeDateTime;

	}
	@Override
	long getScansPerGranule() {
		// TODO Auto-generated method stub
		return 0;
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
		   // the integer division will give the desired floor value.
		   long granuleNumber = elapsedTime / granuleSize;

		   // Multiply the granule number by the granule size, then add the spacecraft
		   // base time to obtain the granule start boundary time. Add the granule
		   // size to the granule start boundary time to obtain the granule end
		   // boundary time.
		   long startBoundary = (granuleNumber * granuleSize) + baseTime;
		   long endBoundary = startBoundary + granuleSize;
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

		   // Multiply the granule number by the granule size, then add the spacecraft
		   // base time to obtain the granule start boundary time. Add the granule
		   // size to the granule start boundary time to obtain the granule end
		   // boundary time.
		   long startBoundary = (granuleNumber * granuleSize) + baseTime;
		   long endBoundary = startBoundary + granuleSize;
		   return endBoundary;
	}


}
