/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.IETTime;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.CDSPacketTime;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.GranuleId;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.SpacecraftId;
public class PDSDateCheck {

	
	public static void main(String[] args) {
		
		long oldBaseTime = 1300968033000000L;
		long newBaseTime = 1698019234000000L;

		//PDSDate p1 = new PDSDate(2012, 0, 3, 16, 28, 44, 60, 0);	
		
		PDSDate fanl = new PDSDate(2011, 9, 23, 00, 00, 34, 0, 0);
		PDSDate ofanl = new PDSDate(1999, 2, 24, 12, 00, 33, 0, 0);
		
		long testTime = ofanl.getPacketTime();
		long myOtherTime = ofanl.getMicrosSinceEpoch(testTime);
		System.out.println("IET of old NPP basetime: " + myOtherTime);
		System.out.println("Should match:            " + oldBaseTime);
		
		long newTestTime = fanl.getPacketTime();
		long newOtherTime = fanl.getMicrosSinceEpoch(newTestTime);
		System.out.println("IET of new NPP basetime: " + newOtherTime);
		System.out.println("Should match:            " + newBaseTime);
		System.out.println();
		
		//System.out.println(fanl.getPacketTime());
		//NEED TO ADD 34 SECONDS DUE TO LEAP SECONDS
		
		//long t1 = p1.getPacketTime();
		
		//PDSDate nowPacketTime = new PDSDate(2012, 0, 3, 16, 29, 55, 000, 0);
		
		//TO CONVERT TO IET TIME ADD 34 SECONDS TO UTC DATETIME
		
		PDSDate nowPacketTime = new PDSDate(2012, 0, 3, 16, 30, 29, 000, 0);
		long t4 = nowPacketTime.getPacketTime();
		long myTime = nowPacketTime.getMicrosSinceEpoch(t4);
		System.out.println("IET of input time: " + myTime);
		
		long granuleSize = 85350000L;
		long elapsedTime = myTime - oldBaseTime; 
		long granuleNumber = elapsedTime / granuleSize;
		long timeCode = (granuleNumber * granuleSize) / 100000;
		
		String granuleID = String.valueOf(timeCode);	
		
		System.out.println("IET timecode used as below: 1704299429000000");
		// arguments are npp ID, sample input time, old NPP basetime in UTC date format, and VIIRS granule size
		//GranuleId granID = new GranuleId(SpacecraftId.npp, new PDSDate(2012, 0, 3, 16, 29, 18, 600, 0), new PDSDate(1999, 2, 24, 12, 00, 33, 0, 0), 85350000L);
		//System.out.println("Granule ID when using old base time   : " + granID.toString());
		System.out.println("Should match gran ID from ppt which is: " + "NPP004033313256");
		
		long testTime2 = GranuleId.firstAscendingNodeAfterLaunch.getPacketTime();
		long myOtherTime2 = GranuleId.firstAscendingNodeAfterLaunch.getMicrosSinceEpoch(testTime2);
		
		CDSPacketTime tempCDS = new CDSPacketTime(testTime2);
		
		System.out.println("PDSPacketTime of new fanl: " + testTime2);
		System.out.println("Other time: " + myOtherTime2);
		System.out.println("CDSPacketTime of new fanl: " + tempCDS.getTime());
		
		System.out.println();
		
		//System.out.println("Base time in use: " + myOtherTime2);
		//GranuleId newgranID = new GranuleId(SpacecraftId.npp,1704299429000000L,85350000L);;
		//System.out.println("Input time: " + 1704299429000000L);
		//System.out.println("GranuleID result: " + newgranID.toString());
		
		//System.out.println("Base time in use: " + myOtherTime2);
		//GranuleId newgranID = new GranuleId(SpacecraftId.npp,1704299429000000L,85350000L);;
		//System.out.println("Input time: " + 1704299429000000L);
		//System.out.println("GranuleID result: " + newgranID.toString())
		
		//System.out.println("Base time in use: " + myOtherTime2);
		//GranuleId newgranID = new GranuleId(SpacecraftId.npp,1707404849300000L,31997000L);;
		//System.out.println("Input time: " + 1707404849300000L);
		//System.out.println("GranuleID result: " + newgranID.toString());
		
		System.out.println("Base time in use: " + myOtherTime2);
		//GranuleId newgranID = new GranuleId(SpacecraftId.npp,1707404853900000L,85350000L);
		System.out.println("Input time: " + 1707404853900000L);
		//System.out.println("GranuleID result: " + newgranID.toString());
		//GranuleId newgranID2 = new GranuleId(SpacecraftId.npp,new PDSDate(2012,1,8,15,6,38,100,0),new PDSDate(2011,9,23,00,00,34,0,0),85350000L);
		//System.out.println("GranuleID2 result: " + newgranID2.toString());
		GranuleId newgranID3 = new GranuleId(SpacecraftId.npp,1707404853900000L,1698019234000000L,85350000L);
		System.out.println("GranuleID3 result: " + newgranID3.toString());
		
		//IETTime temp = new IETTime(123);
		//CDSPacketTime cds = new CDSPacketTime()
		
		//long temp = IETTime.fromTimestamp(123L);

	}
}
