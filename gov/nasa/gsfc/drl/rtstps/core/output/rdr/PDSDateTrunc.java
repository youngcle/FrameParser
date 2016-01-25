/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;



/**
 * PDSDate but gives the ability to add some micros and truncates to the 100000 micros like LPEATEDate
 *
 */
public class PDSDateTrunc extends PDSDate {
	private final static long trunc = 100000L; // 100000L
	
	private long rawPacketTime;  // from the mission timestamp
	private long rawDay;  // day segment
	private long rawMillis; // millis segment
	private long rawMicros; // micros segment




	
	private MissionCal rawCal = new MissionCal();  // not leap adjusted
	
	
	
	/**
	 * Constructor, supply a packet time in the following format from epoch 1/1/58.
	 * <pre>
	 *    Uint16 day since 1/1/1958
	 *    Uint32 millisecond of day
	 *    Uint16 microsecond of millisecond
	 * </pre>
	 * An internal calendar is calculated in both leap adjusted and non-leap adjusted
	 * times which may be accessed through various method calls.
	 * 
	 * @param packetTime the time from the secondary header missions time
	 */
	public PDSDateTrunc(long packetTime) {
		super(packetTime);
		
		
		this.rawPacketTime = packetTime;
		rawDay = (packetTime >> 48) & 0x0ffffL;
		rawMillis = (packetTime >> 16) & 0x0ffffffffL;
		rawMicros = packetTime & 0x0ffffL;
		
		
		calendar(rawDay, rawMillis, rawMicros, rawCal);
		
	}
	

	/**
	 * Add the amount specific to an instance of LPEATEDate and return a new PDSDate adjusted by that amount.
	 * Note: this is added to the leap adjusted and truncated value... this is a special case
	 * 
	 * @param amountInMicros the amount to add
	 * @return a new instance of PDSDate
	 */
	public PDSDateTrunc addMicros(long amountInMicros) {
		
			long micros = this.getMicrosSinceEpoch();

			micros += amountInMicros;
			
			long day = micros / MicrosPerDay;
			
			micros = micros - (day * MicrosPerDay);
			
			long millis = (micros / 1000L);
			
			micros = micros - (millis * 1000L);
			
			long packetTime =   ((day << 48)    & 0xffff000000000000L);
			packetTime |= ((millis << 16)  & 0x0000ffffffff0000L);
			packetTime |=  (micros         & 0x000000000000ffffL);

			return new PDSDateTrunc(packetTime);
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
	
}
