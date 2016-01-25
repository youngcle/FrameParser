/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;


/**
 * A Granule identifier is a way of calculating a unique identifier for the IDPS system.  It is specified as part of
 * RDR attributes for granules in the specifications.   This implements the formula.   One item the firstAscendingNodeAfterLaunch is
 * currenlty used from what Raytheon uses in their DRO package.
 */
public class GranuleId {
	private String name;
	
	// FIXME this is hack for now until this gets defined better...
	//public static PDSDate  firstAscendingNodeAfterLaunch = new PDSDate(1958, 0, 1, 0, 0, 0, 0, 0); // new PDSDate(2009, 0, 25, 0, 0, 0, 0, 0);
//	public static PDSDate  firstAscendingNodeAfterLaunch = new PDSDate(2011, 10, 28, 10, 50, 0, 0, 0); // new PDSDate(2009, 0, 25, 0, 0, 0, 0, 0);
	public static PDSDate firstAscendingNodeAfterLaunch = new PDSDate(2011,9,23,00,00,34,0,0);
	public static long baseTime = 1698019234000000L;

	/**
	 * Use only for the case of reading the ID from the attributes in an HDF file
	 * @param granuleIdStr
	 */
	public GranuleId(String granuleIdStr) {
		this.name = granuleIdStr;
	}
	
	/**
	 * Build a granuleId from various inputs
	 * @param spacecraftId the spacecraft identifier
	 * @param pktObservationTime the observation time of the packet
	 * @param firstAscendingNodeAfterLaunch the first ascending now after launch (currently not known)
	 * @param granuleSizeInTime the granule size in time, this would be time span from first and last packets in the granule in microseconds 
	 */
	/*public GranuleId(SpacecraftId spacecraftId, PDSDate pktObservationTime, PDSDate firstAscendingNodeAfterLaunch, long granuleSizeInTime) {

		System.out.println("In this function.");
		
		long packetDate = firstAscendingNodeAfterLaunch.getPacketTime();
		//long micros = PDSDate.getMicrosSinceEpoch(packetDate);
		long micros = LeapDate.getMicrosSinceEpoch(packetDate);
		
		System.out.println(micros);
		
		long nowPacketDate = pktObservationTime.getPacketTime();
		long nowMicros = PDSDate.getMicrosSinceEpoch(nowPacketDate);

		System.out.println(nowMicros);
		//nowMicros = nowMicros + 55800000;
		name = String.format("%s%012d", spacecraftId.toString(), granuleIDValue(nowMicros, micros, granuleSizeInTime) );
		System.out.println("granuleid="+name);
	}*/
	
	/**
	 * Build a granule ID from various inputs, most importantly the times are in 64-bit quantities
	 * @param spacecraftId the spacecraft identifier
	 * @param pktObservationTime the packet observation time in microseconds
	 * @param firstAscendingNodeAfterLaunch the first ascending node after launch (currently unknown)
	 * @param granuleSizeInTime the granule size in time (microseconds)
	 */
	public GranuleId(SpacecraftId spacecraftId, long pktObservationTime, long firstAscendingNodeAfterLaunch, long granuleSizeInTime) {
		//PDSDate obsTime = new PDSDate(pktObservationTime);
		//PDSDate fanLaunchTime = new PDSDate(firstAscendingNodeAfterLaunch);
		
		//name = String.format("%s%012d", spacecraftId.toString(), granuleIDValue(obsTime, GranuleId.firstAscendingNodeAfterLaunch, granuleSizeInTime) );
		name = String.format("%s%012d", spacecraftId.toString(), granuleIDValue(pktObservationTime, baseTime, granuleSizeInTime) );
		System.out.println("***************** "+name);
	}
	/*public GranuleId(SpacecraftId spacecraftId, long pktObservationTime, long granuleSizeInTime) {
		long fanalPacketTime = GranuleId.firstAscendingNodeAfterLaunch.getPacketTime();
		long fanalEpochTime = PDSDate.getMicrosSinceEpoch(fanalPacketTime);
		name = String.format("%s%012d", spacecraftId.toString(), granuleIDValue(pktObservationTime, fanalEpochTime, granuleSizeInTime));
	}*/
	
	/**
	 * This implements the formula, taking from the JPSS documentation
	 * @param pktObservationTime the packet observation time in microseconds
	 * @param firstAscendingNodeAfterLaunch the first ascending node after launch (currently unknown)
	 * @param granuleSizeInTime the first ascending node after launch (currently unknown)
	 * @return the calculated identifier as an <code>long</code>
	 */
	private long granuleIDValue(long pktObservationTime, long firstAscendingNodeAfterLaunch, long granuleSizeInTime) {
		
		//long numberG = (pktObservationTime - firstAscendingNodeAfterLaunch ) / granuleSizeInTime;  // floor
		//long granuleStartTime = numberG * granuleSizeInTime + firstAscendingNodeAfterLaunch;
		//long granuleEndTime = granuleStartTime + granuleSizeInTime;
		//long granuleIDValue = (numberG * granuleSizeInTime ) / 100000L; // 10^5;
		
		
		long elapsedTime = pktObservationTime - firstAscendingNodeAfterLaunch;
		long granuleNumber = elapsedTime / granuleSizeInTime;
		long granuleIDValue = (granuleNumber * granuleSizeInTime) / 100000;
		
		//long numberG = (pktObservationTime - firstAscendingNodeAfterLaunch ) / granuleSizeInTime;  // floor
		long granuleStartTime = granuleIDValue * granuleSizeInTime + firstAscendingNodeAfterLaunch;
		long granuleEndTime = granuleStartTime + granuleSizeInTime;
		name=String.valueOf(granuleIDValue);
		return granuleIDValue;

		//System.out.println("GranuleIDValue = " + granuleIDValue);
	}

	
	public static long calcGranuleIDValue(long pktObservationTime, long firstAscendingNodeAfterLaunch, long granuleSizeInTime){
		long elapsedTime = pktObservationTime - firstAscendingNodeAfterLaunch;
		long granuleNumber = elapsedTime / granuleSizeInTime;
		long granuleIDValue = (granuleNumber * granuleSizeInTime) / 100000;
		
		//long numberG = (pktObservationTime - firstAscendingNodeAfterLaunch ) / granuleSizeInTime;  // floor
		long granuleStartTime = granuleIDValue * granuleSizeInTime + firstAscendingNodeAfterLaunch;
		long granuleEndTime = granuleStartTime + granuleSizeInTime;

		return granuleIDValue;
	}
	

	/**
	 * This implements the formula, taking from the JPSS documentation but uses {@link PDSDate} as inputs
	 * @param pktObservationTime the packet observation time as a <code>PDSDate</code>
	 * @param firstAscendingNodeAfterLaunch the first ascending node after launch (currently unknown) as a <code>PDSDate</code>
	 * @param granuleSizeInTime the first ascending node after launch (currently unknown)
	 * @return the calculated identifier as an <code>long</code>
	 */
	/*private long granuleIDValue(LeapDate pktObservationTime, LeapDate firstAscendingNodeAfterLaunch, long granuleSizeInTime) {
		
		System.out.println("in this other private function.");
		
		//System.out.println("obsTime: "  + pktObservationTime.toString());
		
		//System.out.println("firstAscendingNodeAfterLaunch: "  + firstAscendingNodeAfterLaunch.toString());
		
		//System.out.println("GranuleSizeInTime: " + granuleSizeInTime);
		

		
		return this.granuleIDValue(pktObservationTime.getMicrosSinceEpoch(),//+55800000, 
							firstAscendingNodeAfterLaunch.getMicrosSinceEpoch(), 
							granuleSizeInTime); 
		
	}*/
	
	/**
	 * Return the GranuleId in a <code>String</code>
	 * @return a string with the granule identifier
	 */
	public String toString() {
		return String.valueOf(name);
	}
}
