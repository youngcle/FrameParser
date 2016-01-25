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
 * Manipulate a date given a packet time in the following format, calculate year,
 * day, month, day of year, hour, minute and seconds.
 * The 10 lines of code which calculate year, day, month were taken from getCal
 * in TimeDate in the checker package.
 * The day of year tables were taken from http://disc.gsfc.nasa.gov/julian_calendar.shtml
 * Note: much of this was taken from an earlier implementation in CRECBuilder
 *
 */
public class PDSDate implements Comparable<PDSDate> {
	private static final double EPOCH_DATE = 2436205.0; //2436204.5;
	public static final long MillisPerDay = 86400000L; 
	public static final long MicrosPerDay = MillisPerDay * 1000L;
	public static final long MicrosPerSecond = 1000000L;
	public static final long MicrosPerMillis = 1000L;
	public static final long MicrosPerMinute = 60L * MicrosPerSecond;
	public static final long MicrosPerHour = 60L * MicrosPerMinute;
	
	public static final long DaysBetweenEpochs = 4383L;  // From Jan 1, 1958 to Jan 1, 1970 (not including this one)
	public static final long SecondsBetweenEpochs = 378691200L;  // no TAI leap
	public static final long MillisBetweenEpochs = SecondsBetweenEpochs * 1000L;
	
	
	// from -- http://disc.gsfc.nasa.gov/julian_calendar.shtml#non-leap_year
	private int[][] dayInPerpetual = { /* 31 x 12 */
		//Day Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec 
		{  1, 32, 60,  91, 121, 152, 182, 213, 244, 274, 305, 335 },
		{  2, 33, 61,  92, 122, 153, 183, 214, 245, 275, 306, 336 },
		{  3, 34, 62,  93, 123, 154, 184, 215, 246, 276, 307, 337 },
		{  4, 35, 63,  94, 124, 155, 185, 216, 247, 277, 308, 338 },
		{  5, 36, 64,  95, 125, 156, 186, 217, 248, 278, 309, 339 },
		{  6, 37, 65,  96, 126, 157, 187, 218, 249, 279, 310, 340 },
		{  7, 38, 66,  97, 127, 158, 188, 219, 250, 280, 311, 341 },
		{  8, 39, 67,  98, 128, 159, 189, 220, 251, 281, 312, 342 },
		{  9, 40, 68,  99, 129, 160, 190, 221, 252, 282, 313, 343 },
		{ 10, 41, 69, 100, 130, 161, 191, 222, 253, 283, 314, 344 },
		{ 11, 42, 70, 101, 131, 162, 192, 223, 254, 284, 315, 345 },
		{ 12, 43, 71, 102, 132, 163, 193, 224, 255, 285, 316, 346 },
		{ 13, 44, 72, 103, 133, 164, 194, 225, 256, 286, 317, 347 },
		{ 14, 45, 73, 104, 134, 165, 195, 226, 257, 287, 318, 348 },
		{ 15, 46, 74, 105, 135, 166, 196, 227, 258, 288, 319, 349 },
		{ 16, 47, 75, 106, 136, 167, 197, 228, 259, 289, 320, 350 },
		{ 17, 48, 76, 107, 137, 168, 198, 229, 260, 290, 321, 351 },
		{ 18, 49, 77, 108, 138, 169, 199, 230, 261, 291, 322, 352 },
		{ 19, 50, 78, 109, 139, 170, 200, 231, 262, 292, 323, 353 },
		{ 20, 51, 79, 110, 140, 171, 201, 232, 263, 293, 324, 354 },
		{ 21, 52, 80, 111, 141, 172, 202, 233, 264, 294, 325, 355 },
		{ 22, 53, 81, 112, 142, 173, 203, 234, 265, 295, 326, 356 },
		{ 23, 54, 82, 113, 143, 174, 204, 235, 266, 296, 327, 357 },
		{ 24, 55, 83, 114, 144, 175, 205, 236, 267, 297, 328, 358 },
		{ 25, 56, 84, 115, 145, 176, 206, 237, 268, 298, 329, 359 },
		{ 26, 57, 85, 116, 146, 177, 207, 238, 269, 299, 330, 360 },
		{ 27, 58, 86, 117, 147, 178, 208, 239, 270, 300, 331, 361 },
		{ 28, 59, 87, 118, 148, 179, 209, 240, 271, 301, 332, 362 },
		{ 29,  0, 88, 119, 149, 180, 210, 241, 272, 302, 333, 363 },
		{ 30,  0, 89, 120, 150, 181, 211, 242, 273, 303, 334, 364 },
		{ 31,  0, 90,   0, 151,   0, 212, 243,   0, 304,   0, 365 }
	};

	// from http://disc.gsfc.nasa.gov/julian_calendar.shtml#leap_year
	private int[][] dayInLeapYear = { /* 31 x 12 */
		// Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec 
		{  1, 32, 61,  92, 122, 153, 183, 214, 245, 275, 306, 336 },
		{  2, 33, 62,  93, 123, 154, 184, 215, 246, 276, 307, 337 },
		{  3, 34, 63,  94, 124, 155, 185, 216, 247, 277, 308, 338 },
		{  4, 35, 64,  95, 125, 156, 186, 217, 248, 278, 309, 339 },
		{  5, 36, 65,  96, 126, 157, 187, 218, 249, 279, 310, 340 },
		{  6, 37, 66,  97, 127, 158, 188, 219, 250, 280, 311, 341 },
		{  7, 38, 67,  98, 128, 159, 189, 220, 251, 281, 312, 342 },
		{  8, 39, 68,  99, 129, 160, 190, 221, 252, 282, 313, 343 },
		{  9, 40, 69, 100, 130, 161, 191, 222, 253, 283, 314, 344 },
		{ 10, 41, 70, 101, 131, 162, 192, 223, 254, 284, 315, 345 },
		{ 11, 42, 71, 102, 132, 163, 193, 224, 255, 285, 316, 346 },
		{ 12, 43, 72, 103, 133, 164, 194, 225, 256, 286, 317, 347 },
		{ 13, 44, 73, 104, 134, 165, 195, 226, 257, 287, 318, 348 },
		{ 14, 45, 74, 105, 135, 166, 196, 227, 258, 288, 319, 349 },
		{ 15, 46, 75, 106, 136, 167, 197, 228, 259, 289, 320, 350 },
		{ 16, 47, 76, 107, 137, 168, 198, 229, 260, 290, 321, 351 },
		{ 17, 48, 77, 108, 138, 169, 199, 230, 261, 291, 322, 352 },
		{ 18, 49, 78, 109, 139, 170, 200, 231, 262, 292, 323, 353 },
		{ 19, 50, 79, 110, 140, 171, 201, 232, 263, 293, 324, 354 },
		{ 20, 51, 80, 111, 141, 172, 202, 233, 264, 294, 325, 355 },
		{ 21, 52, 81, 112, 142, 173, 203, 234, 265, 295, 326, 356 },
		{ 22, 53, 82, 113, 143, 174, 204, 235, 266, 296, 327, 357 },
		{ 23, 54, 83, 114, 144, 175, 205, 236, 267, 297, 328, 358 },
		{ 24, 55, 84, 115, 145, 176, 206, 237, 268, 298, 329, 359 },
		{ 25, 56, 85, 116, 146, 177, 207, 238, 269, 299, 330, 360 },
		{ 26, 57, 86, 117, 147, 178, 208, 239, 270, 300, 331, 361 },
		{ 27, 58, 87, 118, 148, 179, 209, 240, 271, 301, 332, 362 },
		{ 28, 59, 88, 119, 149, 180, 210, 241, 272, 302, 333, 363 },
		{ 29, 60, 89, 120, 150, 181, 211, 242, 273, 303, 334, 364 },
		{ 30,  0, 90, 121, 151, 182, 212, 243, 274, 304, 335, 365 },
		{ 31,  0, 91,   0, 152,   0, 213, 244,   0, 305,   0, 366 }
	};
	
	private long rawPacketTime;  // from the mission timestamp
	private long rawDay;  // day segment
	private long rawMillis; // millis segment
	private long rawMicros; // micros segment



	class MissionCal {
		long year;
		long month;
		long day;
		long hours;
		long minutes;
		long seconds;
		long milliseconds;
		long microseconds;
		long dayOfYear;
	};
	
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
	public PDSDate(long packetTime) {
		this.rawPacketTime = packetTime;
		rawDay = (packetTime >> 48) & 0x0ffffL;
		rawMillis = (packetTime >> 16) & 0x0ffffffffL;
		rawMicros = packetTime & 0x0ffffL;
		
		
		calendar(rawDay, rawMillis, rawMicros, rawCal);

		
	}
	

	
	/**
	 * Rough approximation into Mission Epoch date/time, leap is adjusted off.
	 * @param year the year
	 * @param month the month, starts at 0 for January
	 * @param day the day of month, starts at 1
	 * @param hour the hour of day
	 * @param minute the minute of the hour
	 * @param second the seconds in the minute
	 * @param milliseconds the milliseconds of the second
	 * @param microseconds of the millis
	 * @throws RtStpsException 
	 */
	
	public PDSDate(int year, int month, int dayOfMonth, int hour, int minute, int second, int milliseconds, int microseconds) { 
		

		// add up all the days since the epoch
		int days = this.daysSinceEpoch(year);
		int dayOfYear = this.dayOfYear(year, month, dayOfMonth-1);
		
		
		days += (dayOfYear-1);  // subtract one day since a full day has not elapsed on the specified month/day/time
		
	
		
		long t_microseconds = days * MicrosPerDay;
		
		t_microseconds += hour * MicrosPerHour;
		t_microseconds += minute * MicrosPerMinute;
		t_microseconds += second * MicrosPerSecond;
		t_microseconds += milliseconds * MicrosPerMillis;
		t_microseconds += microseconds;
		
		
		long c_Day = t_microseconds / MicrosPerDay;
		
		long temp = t_microseconds - (c_Day * MicrosPerDay);
		
		long c_Millis = (temp / 1000L);
		
		long c_Micros = temp - (c_Millis * 1000L);

		
		this.rawDay = c_Day;
		this.rawMillis = c_Millis;
		this.rawMicros =  c_Micros;
		

		rawPacketTime =   ((rawDay << 48)    & 0xffff000000000000L);
		rawPacketTime |= ((rawMillis << 16)  & 0x0000ffffffff0000L);
		rawPacketTime |=  (rawMicros         & 0x000000000000ffffL);
		

	
		calendar(rawDay, rawMillis, rawMicros, rawCal);
	

	}


	public PDSDate(Date dateTime) {
		Calendar pdsCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		
		pdsCal.setTime(dateTime);
		
		int year = pdsCal.get(Calendar.YEAR);
		int month = pdsCal.get(Calendar.MONTH);
		int dayOfMonth = pdsCal.get(Calendar.DAY_OF_MONTH);
		long hour = (long)pdsCal.get(Calendar.HOUR_OF_DAY);
		long minute = (long)pdsCal.get(Calendar.MINUTE);
		long second = (long)pdsCal.get(Calendar.SECOND);
		long milliseconds = (long)pdsCal.get(Calendar.MILLISECOND);
		long microseconds = 0L;
		
		// add up all the days since the epoch
		int days = this.daysSinceEpoch(year);
		int dayOfYear = this.dayOfYear(year, month, dayOfMonth-1);
		
		
		days += (dayOfYear-1);  // subtract one day since a full day has not elapsed on the specified month/day/time
		
	
		
		long t_microseconds = days * MicrosPerDay;
		
		t_microseconds += hour * MicrosPerHour;
		t_microseconds += minute * MicrosPerMinute;
		t_microseconds += second * MicrosPerSecond;
		t_microseconds += milliseconds * MicrosPerMillis;
		t_microseconds += microseconds;
		
		
		long c_Day = t_microseconds / MicrosPerDay;
		
		t_microseconds = t_microseconds - (c_Day * MicrosPerDay);
		
		long c_Millis = (t_microseconds / 1000L);
		
		long c_Micros = t_microseconds - (c_Millis * 1000L);

		
		this.rawDay = c_Day;
		this.rawMillis = c_Millis;
		this.rawMicros =  c_Micros;
		

		rawPacketTime =   ((rawDay << 48)    & 0xffff000000000000L);
		rawPacketTime |= ((rawMillis << 16)  & 0x0000ffffffff0000L);
		rawPacketTime |=  (rawMicros         & 0x000000000000ffffL);
		

	
		calendar(rawDay, rawMillis, rawMicros, rawCal);
	
	}


	/**
	 * Return the raw original 64-bit packet time
	 * @return long
	 */
	final public long getOriginalPacketTime() {
		return this.rawPacketTime;
	}

	/**
	 * Return the 64-bit timestamp
	 * @return long
	 */
	public long getPacketTime() {
		return this.rawPacketTime;
	}
	
	/**
	 * Get the day segment from the packet time
	 * @return a long
	 */
	public long getDaySegment() {
		return rawDay;
	}
	
	/**
	 * Get the raw milliseconds segment from the packet time
	 * @return a long
	 */
	public long getMillisSegment() {
		return rawMillis;
	}

	/**
	 * Get the raw microseconds segment from the packet time
	 * @return a long
	 */
	public long getMicrosSegment() {
		return rawMicros;
	}

	/**
	 * Get the calculated month of the year
	 * @return a long
	 */
	public long getMonth() {
		return rawCal.month;
	}
	/**
	 * Get the calculated year, four digits
	 * @return a long
	 */
	public long getYear() {
		return rawCal.year;
	}
	/**
	 * Get the calculated day of month
	 * @return an integer
	 */
	public long getDayOfMonth() {
		return rawCal.day;
	}
	/**
	 * Get the calculated day of the year
	 * @return an integer
	 */
	public long getDayOfYear() {
		return rawCal.dayOfYear;
	}
	/**
	 * Get the calculated milliseconds of second
	 * @return a long
	 */
	public long getMilliseconds() {
		return rawCal.milliseconds;
	}
	
	/**
	 * Get any remaining microseconds of millisecond
	 * @return a long
	 */
	public long getMicroseconds() {
		return rawCal.microseconds;
	}
	/**
	 * Get the calculated seconds of minute
	 * @return a long
	 */
	public long getSeconds() {
		return rawCal.seconds;
	}
	/**
	 * Get the calculated minutes of hour
	 * @return a long
	 */
	public long getMinutes() {
		return rawCal.minutes;
	}
	/**
	 * Get the calculated hours of day
	 * @return a long
	 */
	public long getHours() {
		return rawCal.hours;
	}
	
	/**
	* Converts an IET time to a PDSDate object. Does NOT
	* remove leap seconds from the IET time.
	*/
	public static PDSDate ietToPDS(long arbIET){
		// Compute the day segment, subtract from total
		long ietTime = arbIET;
		long eqDaySegment = (ietTime / MicrosPerDay);
		ietTime = ietTime - (eqDaySegment * MicrosPerDay);

		// Compute the millisecond segment, subtract from total; remains are micros. segment.
		long eqMillisSegment = ( ietTime / MicrosPerMillis );
		long eqMicrosSegment = ietTime - (eqMillisSegment * MicrosPerMillis);

		long packetTime =   ((eqDaySegment << 48)    & 0xffff000000000000L);
		packetTime |= ((eqMillisSegment << 16)  & 0x0000ffffffff0000L);
		packetTime |=  (eqMicrosSegment         & 0x000000000000ffffL);

		return new PDSDate(packetTime);
	}

	
	/**
	 * Return the micros since the mission epoch
	 * @return 64 bits of microseconds, signed
	 */
	public long getMicrosSinceEpoch() {
		long micros = (getDaySegment() * MicrosPerDay) + (getMillisSegment() * MicrosPerMillis) + getMicrosSegment();
		
		return micros;
	}
	

	
	/**
	 * Static variation given a 64-bit mission time (segmented), no leap
	 * @param packetTime 48 bits of time, 16 bits millis and 16 bits of micros
	 * @return the micros since epoch in a signed 64-bit quantity
	 */
	public static long getMicrosSinceEpoch(long packetTime) {
		long rawDay = (packetTime >> 48) & 0x0ffffL;
		long rawMillis = (packetTime >> 16) & 0x0ffffffffL;
		long rawMicros = packetTime & 0x0ffffL;
		
		long micros = (rawDay * MicrosPerDay) + (rawMillis * MicrosPerMillis) + rawMicros;

		return micros;
	}
	
	/**
	 * Determine if this is a leapyear or not
	 * Alg obtained from wikipedia
	 * 	if year modulo 4 is 0
	 * then
	 *     if year modulo 100 is 0
	 *          then
	 *              if year modulo 400 is 0
	 *                  then
	 *                      is_leap_year
	 *              else
	 *                  not_leap_year
	 *      else is_leap_year
	 * else not_leap_year
	 * @param year
	 * @return true if 
	 */
	public boolean isLeapYear (int year) {
		//return ((((year % 4) == 0) && ((year % 100) != 0)) || ((year % 400) == 0));
		
		boolean leapYear = false;
		//if ((year % 400) ==  0)
	    //   leapYear = true;
		//else if ((year % 100) == 0)
	    //   leapYear = false;
	    //else if ((year % 4) == 0)
	    //   leapYear = true;
	   // else
	    //   leapYear = false;

		if ((year % 4) == 0) {
			if ((year % 100) == 0) {
				if ((year % 400) == 0) {
					leapYear = true;
				} else {
					leapYear = false;
				}
			} else {
				leapYear = true;
			}
		} else {
			leapYear = false;
		}
		
		return leapYear;
	}


	/**
	 * Count up the days since the epoch per year.  The leaps year days
	 * are added based on the leap year calculation... no provision is 
	 * made to check that the year given is on or before the epoch year
	 * @param year a year since 1958
	 * @return the total days including leap year days since that year
	 */
	private int daysSinceEpoch(int year) {
		// add up all the days since the epoch
		int days = 0;
		for (int y = 1958; y < year; y++) {
			if (isLeapYear(y)) {
				days += 366;
			} else {
				days += 365;
			}
		}
		return days;
	}
	
	/**
	 * Calculate the day of the year using the calendar tables.
	 * @param month the month of the year starting at zero for January
	 * @param day the day of the month starting at zero for the 1st day
	 * @return the day of the year
	 */
	private int dayOfYear(int year, int month, int day) {
		int dayOfYear = 0;
		if (isLeapYear(year)) {
			dayOfYear = dayInLeapYear[day][month];
		} else {
			dayOfYear = dayInPerpetual[day][month]; 
		}
		return dayOfYear;
	}
	/**
	 * Return the Java Date, the epoch is converted and the timezone is UTC
	 * The returned Date if simply printed will be relative this your timezone.  To convert to UTC,
	 * use the DateFormat classes.
	 * @return the Date in the local time zone, specify the UTC to display it in proper mission time timezone
	 */
	public Date getDate() {

		//System.out.println(this.toString());
		
		//Calendar pdsCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		
		Calendar pdsCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		
		//pdsCal.set(Calendar.ZONE_OFFSET, 0);
		//pdsCal.set(Calendar.DST_OFFSET, 0);
		
		pdsCal.set(Calendar.YEAR,(int)this.getYear());
		
		//System.out.println("Time?  " + pdsCal.getTime());
		pdsCal.set(Calendar.MONTH, (int)this.getMonth());
		//System.out.println("Time?  " + pdsCal.getTime());
		pdsCal.set(Calendar.DAY_OF_YEAR, (int)this.getDayOfYear());
		//System.out.println("Time?  " + pdsCal.getTime());
		pdsCal.set(Calendar.HOUR_OF_DAY,(int)this.getHours());
		//System.out.println("Time?  " + pdsCal.getTime());
		pdsCal.set(Calendar.MINUTE,(int)this.getMinutes());
		//System.out.println("Time?  " + pdsCal.getTime());
		pdsCal.set(Calendar.SECOND,(int)this.getSeconds());
		//System.out.println("Time?  " + pdsCal.getTime());
		pdsCal.set(Calendar.MILLISECOND, (int)this.getMilliseconds());
		//System.out.println("Time?  " + pdsCal.getTime());

		//Calendar output = Calendar.getInstance();   
		//output.set(Calendar.YEAR, pdsCal.get(Calendar.YEAR));   
		//output.set(Calendar.MONTH, pdsCal.get(Calendar.MONTH));   
		//output.set(Calendar.DAY_OF_MONTH, pdsCal.get(Calendar.DAY_OF_MONTH));   
		//output.set(Calendar.HOUR_OF_DAY, pdsCal.get(Calendar.HOUR_OF_DAY));   
		//output.set(Calendar.MINUTE, pdsCal.get(Calendar.MINUTE));   
		//output.set(Calendar.SECOND, pdsCal.get(Calendar.SECOND));   
		//output.set(Calendar.MILLISECOND, pdsCal.get(Calendar.MILLISECOND)); 
		
		return pdsCal.getTime();
		//return output.getTime();
	}

	
	/**
	 * Return just the raw fields in a string
	 */
	public String toRawFields() {
		
		return("RawDay[" + rawDay + "]" +
		          				" RawMillis[" + rawMillis + "]" +
		        				" RawMicros[" + rawMicros + "]");
	}
	
	/**
	 * Return just the raw fields, static version
	 */
	public static String toRawFields(long packetTime) {
		
		long rawDay = (packetTime >> 48) & 0x0ffffL;
		long rawMillis = (packetTime >> 16) & 0x0ffffffffL;
		long rawMicros = packetTime & 0x0ffffL;
		
		return("RawDay[" + rawDay + "]" +
		          				" RawMillis[" + rawMillis + "]" +
		        				" RawMicros[" + rawMicros + "]");
	}
	
	/**
	 * Convert the results to String
	 */
	@Override
	public String toString() {
		return "Year[" + this.getYear() + "]" + 
				" Month[" + this.getMonth() + "]" +
				" Day[" + this.getDayOfMonth() + "]" +
				" (DayOfYear[" + this.getDayOfYear() + "])" +
				" Hours[" + this.getHours() + "]" +
				" Minutes[" + this.getMinutes() + "]" +
				" Seconds[" + this.getSeconds() + "]" +
				" Milliseconds[" + this.getMilliseconds() + "]" +
				" Microseconds[" + this.getMicroseconds() + "]" +
				" -- DaySegment[" + this.getDaySegment() + "]" +
				" MillisSegment[" + this.getMillisSegment() + "]" +
				" MicrosSegment[" + this.getMicrosSegment() + "]";
		
	}
		 
	/**
	 * Calculate the new year, month, days of year, hours, minutes, seconds, milliseconds...
	 */
	protected void calendar(long daySegment, long millisSegment, long microsSegment, MissionCal cal) {
		
		double jdUTC = daySegment + EPOCH_DATE;
		
	      long l,n;
	      	      
	      // original
	      l = (long)(jdUTC + 0.5) + 68569L;
	      n = 4L*l/146097L;
	      l = l - (146097L*n+3L)/4L;

	      cal.year = (4000L*(l+1L)/1461001L);

	      l = l - 1461L*cal.year/4L + 31L;

	      cal.month = (80L*l/2447L);
	      cal.day = (l-2447L*cal.month/80L);

	      l = cal.month/11L;

	      cal.month = cal.month + 2L - 12L*l;
	      cal.year = (100L*(n-49L) + cal.year + l);
	      
	      cal.month = cal.month - 1;
	      	      
	      if (isLeapYear((int) cal.year)) {
	    	  cal.dayOfYear = dayInLeapYear[(int)cal.day-1][(int)cal.month]; //-1];
	      } else {
	    	  cal.dayOfYear = dayInPerpetual[(int)cal.day-1][(int)cal.month]; //-1];
	      }
	      /**
	      long tsecs = (millisSegment / 1000L);
	      
	      long tmins = tsecs/60L;
	      
	      cal.hours = (tmins / 60L);	     
	      
	      cal.minutes = tmins - (cal.hours * 60L);
	      
	      cal.seconds = tsecs - ((cal.minutes * 60L) + (cal.hours * 60L * 60L));
	      
	      cal.milliseconds = millisSegment - (tsecs * 1000L);
	      
	      cal.microseconds = microsSegment;
	      **/
	      
	      // alt
			
			long t_millis = (daySegment * MillisPerDay) + millisSegment; 
			
			long t_micros = (t_millis * 1000L) + microsSegment;
			
			long microsPerSecond = 1000000L;
			long microsPerMinute = microsPerSecond * 60L;
			long microsPerHour = microsPerMinute * 60L;
			long microsPerMillis = 1000L;
			
			// Calculate days, subtract from total:
			long days = t_micros / MicrosPerDay;
			t_micros = t_micros - (days * MicrosPerDay);

			// Calculate hours, subtract from total:
			cal.hours = t_micros / microsPerHour;
			t_micros = t_micros - (cal.hours * microsPerHour);

			// Calculate minutes, subtract from total:
			cal.minutes = (t_micros / microsPerMinute);
			t_micros = t_micros - (cal.minutes * microsPerMinute);

			// Calculate seconds, subtract from total:
			cal.seconds = (t_micros / microsPerSecond);
			t_micros = t_micros - (cal.seconds * microsPerSecond);

			// Calculate milliseconds, subtract from total:
			cal.milliseconds = (t_micros / microsPerMillis);
			t_micros = t_micros - (cal.milliseconds * microsPerMillis);

			// Remains are microseconds:
			cal.microseconds = t_micros;
			/**
			System.out.println("Hours2 = " + hours2 + 
								" Minutes2 = " + minutes2 + 
								" Seconds2 = " + seconds2 + 
								" millis2 = " + millis2 + 
								" micros2 = " + micros2);
					**/
			

	}	
	

	@Override
	public int compareTo(PDSDate dateTime) {
		/**
		if (this.rawDay < dateTime.rawDay) {
			return -1;
		} else if (this.rawDay > dateTime.rawDay) {
			return 1;
		}
		// ok same day... check the time fields
		if (this.rawMillis < dateTime.rawMillis){
			return -1;
		} else if (this.rawMillis > dateTime.rawMillis) {
			return 1;
		}
		// ok same day and millis ... check the micros
		if (this.rawMicros < dateTime.rawMicros){
			return -1;
		} else if (this.rawMicros > dateTime.rawMicros) {
			return 1;
		}
		// if it gets this far, they are the same...
		 * 
		 */
		long first = this.getMicrosSinceEpoch();
		long second = dateTime.getMicrosSinceEpoch();
		
		if (first < second) {
			return -1;
		}
		if (first > second) {
			return 1;
		}
		return 0;
	}
	
	public static long getCurrentSystemTimeMicrosSinceEpoch() {
		long t = System.currentTimeMillis() +  MillisBetweenEpochs;
		
		t = t * 1000L;
		
		return t;
	}

	

}
