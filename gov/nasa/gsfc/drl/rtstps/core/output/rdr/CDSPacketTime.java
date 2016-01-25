/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;


import java.util.Calendar;
import java.util.Date;


/**
 * A helper class for working with a segmented time of 16 bits of day, 32 bits of milliseconds and 16 bits micros
 * 
 *
 */
@Deprecated
public class CDSPacketTime {
	public static final long JavaToTIA_InMillis = 378691200L * 1000L;
	private long days; // days since epoch
	private long millis; // millis per day
	private long micros; // micros per milli
	private long time; // the whole thing, byte flipped around in one spot

	/**
	 * Give a packet and pull out the raw time stamp field
	 * The user is expected to know the packet has a time stamp
	 * @param timeStamp  the 64-bit timestamp from the packet
	 */
    public CDSPacketTime(long timeStamp) {
    	this.time = timeStamp;
   	
    	days = (timeStamp >> 48) & 0x0000ffffL;  // this is a 16 bit quantity
    	
    	millis = (timeStamp >> 16) & 0x00000000ffffffffL; // this is a 32 bit quantity
    	
    	micros = timeStamp & 0x0000ffffL; // this is a 16 bit quantity
 	    
    }
 
    /**
     * Return the day count
     * @return day count in a long
     */
    public long getDays() {
    	return days;
    }
    
    /**
     * Return the millis count
     * @return millis count in a long
     */
    public long getMillis() {
    	return millis;
    }
    
    /**
     * Return the micros count
     * @return micros count in a long
     */
    public long getMicros() {
    	return micros;
    }
    
    /**
     * Return the entire time field which is 64-bits, use it as an unsigned value.
     * Since Java long is signed, some care must be taken in this regard.
     * @return the 64-bit time field as a long
     */
    public long getTime() {
    	return time;
    }
    
    /**
     * Return a {@link Date} of the time.  The time is converted to a {@link PDSDate} internally which handles
     * all the epoch conversion stuff and timezone issues, and then from that is converted to a Date.
     * @return date and time represented in a <code>Date</code>
     */
    public Date getDate() {
    	PDSDate pdsDate = new PDSDate(time);
    	
		Calendar pdsCal = Calendar.getInstance();

		pdsCal.set(Calendar.YEAR,(int)pdsDate.getYear());
		pdsCal.set(Calendar.DAY_OF_YEAR,(int)pdsDate.getDayOfYear());
		pdsCal.set(Calendar.HOUR_OF_DAY,(int)pdsDate.getHours());
		pdsCal.set(Calendar.MINUTE,(int)pdsDate.getMinutes());
		pdsCal.set(Calendar.SECOND,(int)pdsDate.getSeconds());
    	
		return pdsCal.getTime();
    }
    
     
    /**
     * Return a comma delimited string of "[Days=xxx], [Millis=yyy], [Micros=zzz]"
     */
    @Override
    public String toString() {
    	return ("[Days=" + days + "], [Millis=" + millis + "], [Micros=" + micros + "]");
    } 

}
