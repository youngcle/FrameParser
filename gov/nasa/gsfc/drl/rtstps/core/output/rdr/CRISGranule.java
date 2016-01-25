/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.text.ParseException;

public class CRISGranule extends Granule {
	
	
	private final static long endOffset = 31997000L;
	private LPEATEDate firstDateTime;
	private LPEATEDate lastDateTime;
	private PDSDate predictedEndDateTime;
	//private PDSDateTrunc beginningTimeDateTime;
	//private PDSDateTrunc predictedEndingTimeDateTime;
	private PDSDate beginningTimeDateTime;
	private PDSDate predictedEndingTimeDateTime;
	private final static long granuleSize=31997000L;
	private static long baseTime=1698019234000000L;
	private static boolean baseTimeSet=false;
	
	public static boolean isBaseTimeSet() {
		return baseTimeSet;
	}

	public static void setBaseTimeSet(boolean baseTimeSet) {
	    CRISGranule.baseTimeSet = baseTimeSet;
	}

	public static long getBaseTime() {
		return baseTime;
	}

	public static void setBaseTime() { //uses NPP base time
		CRISGranule.baseTimeSet = true;
	}

	public static void setBaseTime(long baseTime) {
		long baseTimedivide=baseTime/1000000;
		long baseTimeEvenSecond=baseTimedivide*1000000;
		CRISGranule.baseTime = baseTimeEvenSecond;
		System.out.println("Set CRIS baseTime="+CRISGranule.baseTime);
		CRISGranule.baseTimeSet = true;
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
	public CRISGranule(RawApplicationPackets rap,
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
				RDRName.CRIS_Science);
	}
	/**
	 * Read the Granule out of the HDF file and fill in the various attributes and items in it for
	 * these access methods
	 * @param groupId the HDF group handle
	 * @param granuleName  the name of the granule
	 * @throws ParseException 
	 */
	public CRISGranule(int groupId, String granuleName) throws RtStpsException {
		super(groupId, granuleName);
	}
	
	@Override
	public PDSDate getBeginningObservationDateTime(RawApplicationPackets rap) {
		this.firstDateTime = new LPEATEDate(rap.getFirstTime());  // packet time
		return firstDateTime;
	}

	
	@Override
	public PDSDate getEndingObservationDateTime(RawApplicationPackets rap) {
		this.lastDateTime = new LPEATEDate(rap.getLastTime());  // packet time
		this.predictedEndDateTime = firstDateTime.addMicros(getEndOffset() + 50000L);
		return lastDateTime;
	}
	@Override
	public PDSDate getBeginningTimeDateTime() {
		// TODO Auto-generated method stub
		//beginningTimeDateTime = new PDSDateTrunc(firstDateTime.getOriginalPacketTime());
		beginningTimeDateTime = LeapDate.ietToPDSNoLeap( getBeginningIET() );
		return beginningTimeDateTime;
	}
	@Override
	public PDSDate getEndingTimeDateTime() {
		// TODO Auto-generated method stub
		
		//this.predictedEndingTimeDateTime = beginningTimeDateTime.addMicros(getEndOffset() + 50000L);
		this.predictedEndingTimeDateTime = LeapDate.ietToPDSNoLeap( getEndingIET() );
		return predictedEndingTimeDateTime;
	}
	@Override
	long getScansPerGranule() {
		// TODO Auto-generated method stub
		return 0;
	}
	long getEndOffset() {
		return 31997000L;
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
		   System.out.println("CRIS granule ID="+granuleIDValue);
		   // Multiply the granule number by the granule size, then add the spacecraft
		   // base time to obtain the granule start boundary time. Add the granule
		   // size to the granule start boundary time to obtain the granule end
		   // boundary time.
		   long startBoundary = (granuleNumber * granuleSize) + baseTime;
		   long endBoundary = startBoundary + granuleSize;
		   return endBoundary;
	}


}
