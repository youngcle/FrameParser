/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;






/**
 * Specific variation of PDSDate to match LPEATE RDRs and SDR processing algorithms which adds leap seconds
 * and truncation to nearest 10th of a second in SOME methods.  The underlying packet time will be not be however
 * but this is not used where object of this class are used.
 *
 */
public class LPEATEDate extends LeapDate {
	
	private final static long trunc = 100000L; // 100000L
	
	/**
	 * Zero out the micros and truncate the millis to the nearest 10th of a second
	 * @param packetTime
	 */
	public LPEATEDate(long packetTime) {
		super(packetTime);

	}
	
	
	/**
	 * Add the amount specific to an instance of LPEATEDate and return a new PDSDate adjusted by that amount.
	 * Note: this is added to the leap adjusted and truncated value... this is a special case
	 * 
	 * @param amountInMicros the amount to add
	 * @return a new instance of PDSDate
	 */
	public PDSDate addMicros(long amountInMicros) {
		
			long micros = this.getMicrosSinceEpoch();

			micros += amountInMicros;
			
			long day = micros / MicrosPerDay;
			
			micros = micros - (day * MicrosPerDay);
			
			long millis = (micros / 1000L);
			
			micros = micros - (millis * 1000L);
			
			long packetTime =   ((day << 48)    & 0xffff000000000000L);
			packetTime |= ((millis << 16)  & 0x0000ffffffff0000L);
			packetTime |=  (micros         & 0x000000000000ffffL);

			return new PDSDate(packetTime);
	}
	
	
	/**
	 * Rough approximation into Mission Epoch date/time.  
	 * @param year the year
	 * @param month the month, starts at 0 for January
	 * @param day the day of month, starts at 1
	 * @param hour the hour of day
	 * @param minute the minute of the hour
	 * @param second the seconds in the minute
	 * @param milliseconds the milliseconds of the second
	 * @param microseconds the microseconds
	 * @throws RtStpsException 
	 */
	
	public LPEATEDate(int year, int month, int day, int hour, int minute, int second, int milliseconds, int microseconds) { 
		
		
		super(year, month, day, hour, minute, second, milliseconds, microseconds);

	}

	/**
	 * Get the calculated milliseconds of second but truncated to 10th of a second
	 * @return a long
	 */
	public long getMilliseconds() {
		return ((super.getMilliseconds()/100L) * 100L);
	}
	
	/**
	 * Get any remaining microseconds of millisecond, always truncated to zero
	 * @return a long
	 */
	public long getMicroseconds() {
		return 0L;
	}
	
	


	/**
	 * Return the micros since the mission epoch, this is leap adjusted
	 * @return 64 bits of microseconds, signed
	 */
	public long getMicrosSinceEpoch() {
		long micros = super.getMicrosSinceEpoch();
		
		micros = (micros / trunc) * trunc;
		
		return micros;
	}
	

	
	/**
	 * Static variation given a 64-bit mission time (segmented) with leap
	 * @param packetTime 48 bits of time, 16 bits millis and 16 bits of micros
	 * @return the micros since epoch in a signed 64-bit quantity
	 */
	public static long getMicrosSinceEpoch(long packetTime) {
		//long micros = PDSDate.getMicrosSinceEpoch(packetTime);
		long micros = LeapDate.getMicrosSinceEpoch(packetTime);
		micros = (micros / trunc) * trunc;

		return micros;
	}




	


}
