/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * Helper class for converting packet times to IETTime which is micros from epoch.
 * The conversion is stored at a 64-bit signed Java long.  Calculations show that it
 * will remain non-negative and this is enough bit-space to hold value for many years
 * into the future.  However... those calculations could be wrong.
 * 
 *
 */
@Deprecated
public class IETTime {
	private final static long microsPerDay = 86400000000L;
	private final long time;
	
	/**
	 * Construct an IETTime from a {@link CDSPacketTime} which is the timestamp
	 * format of the mission packets.
	 * @param packetTime a CDSPacketTime from a packet
	 */
	public IETTime(CDSPacketTime packetTime) {
		long days = packetTime.getDays();
		long millis = packetTime.getMillis();
		long micros = packetTime.getMicros();
		
		time = days * microsPerDay + millis * 1000L + micros;	
	}
	

	/**
	 * Return the calculated time in a long from microseconds since the mission epoch.
	 * It should be enough non-negative space to handle mission years quite a bit into the future.
	 * @return a long of the time in microseconds since epoch
	 */
	public long getTime() {
		return time;
	}


	public static long fromTimeStamp(long timeStamp) {
		long days = (timeStamp >> 48) & 0x0000ffffL;  // this is a 16 bit quantity
    	
    	long millis = (timeStamp >> 16) & 0x00000000ffffffffL; // this is a 32 bit quantity
    	
    	long micros = timeStamp & 0x0000ffffL; // this is a 16 bit quantity
    	
    	return days * microsPerDay + millis * 1000L + micros;
	}
}
