/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;

import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate.MissionCal;

/**
 * Add leap seconds calculation to PDSDates
 * @author krice
 *
 */
public class LeapDate extends PDSDate {
	private static final long TAILeap = 10L;  // 10 seconds added in 1972
	private MissionCal adjCal = new MissionCal(); // leap adjusted
	private long adjPacketTime;  // leap adjusted
	private long adjDay;  // leap adjusted
	private long adjMillis; // leap adjusted
	private long adjMicros; // leap adjusted
	//private static long staticLeapsecCount = 0;
	
	public LeapDate(long packetTime) {
		super(packetTime);
		
		// locally calculate the packetTime's micros
		long micros = PDSDate.getMicrosSinceEpoch(packetTime);
		
		//System.out.println("micros from super -- " + micros);
		long leap = TAILeap + leapsSince72(super.getDaySegment());

		micros += (leap * MicrosPerSecond);
				
		adjDay = micros / MicrosPerDay;
		
		micros = micros - (adjDay * MicrosPerDay);
		
		adjMillis = ( micros / 1000L);
		
		adjMicros = micros - (adjMillis * 1000L);
		
		adjPacketTime =   ((adjDay << 48)    & 0xffff000000000000L);
		adjPacketTime |= ((adjMillis << 16)  & 0x0000ffffffff0000L);
		adjPacketTime |=  (adjMicros         & 0x000000000000ffffL);
	
		calendar(adjDay, adjMillis, adjMicros, adjCal);
	}
	

	public LeapDate(int year, int month, int day, int hour, int minute, int second, int milliseconds, int microseconds) {		
		super(year, month, day, hour, minute, second, milliseconds, microseconds);
		
		// locally calculate the packetTime's micros
		long rawDays = super.getDaySegment();
		long rawMillis = super.getMillisSegment();
		long rawMicros = super.getMicrosSegment();
		
		long micros = rawDays * MicrosPerDay;
		micros += rawMillis * 1000L;
		micros += rawMicros;
		
		long leap = TAILeap + leapsSince72(rawDays);
		
		micros += (leap * MicrosPerSecond);
		
		adjDay = micros / MicrosPerDay;
		
		micros = micros - (adjDay * MicrosPerDay);
		
		adjMillis = ( micros / 1000L);
		
		adjMicros =  micros - (adjMillis * 1000L);
		
		adjPacketTime =   ((adjDay << 48)    & 0xffff000000000000L);
		adjPacketTime |= ((adjMillis << 16)  & 0x0000ffffffff0000L);
		adjPacketTime |=  (adjMicros         & 0x000000000000ffffL);
	
		
		calendar(adjDay, adjMillis, adjMicros, adjCal);
		
	}

	


	/**
	 * Return the 64-bit timestamp adjusted by leap seconds
	 * @return long
	 */
	public long getPacketTime() {
		return adjPacketTime;
	}
	
	/**
	 * Get the day segment from the packet time which may have been leap adjusted
	 * @return a long
	 */
	public long getDaySegment() {
		return adjDay;
	}
	
	/**
	 * Get the milliseconds segment from the packet time which may have been leap adjusted
	 * @return a long
	 */
	public long getMillisSegment() {
		return adjMillis;
	}
	
	/**
	 * Get the microseconds segment from the packet time which may have been leap adjusted
	 * @return a long
	 */
	public long getMicrosSegment() {
		return adjMicros;
	}
	
	/**
	 * Get the calculated month of the year
	 * @return a long
	 */
	public long getMonth() {
		return adjCal.month;
	}
	/**
	 * Get the calculated year, four digits
	 * @return a long
	 */
	public long getYear() {
		return adjCal.year;
	}
	/**
	 * Get the calculated day of month
	 * @return an integer
	 */
	public long getDayOfMonth() {
		return adjCal.day;
	}
	/**
	 * Get the calculated day of the year
	 * @return an integer
	 */
	public long getDayOfYear() {
		return adjCal.dayOfYear;
	}
	/**
	 * Get the calculated milliseconds of second
	 * @return a long
	 */
	public long getMilliseconds() {
		return adjCal.milliseconds;
	}
	
	/**
	 * Get any remaining microseconds of millisecond
	 * @return a long
	 */
	public long getMicroseconds() {
		return adjCal.microseconds;
	}
	/**
	 * Get the calculated seconds of minute
	 * @return a long
	 */
	public long getSeconds() {
		return adjCal.seconds;
	}
	/**
	 * Get the calculated minutes of hour
	 * @return a long
	 */
	public long getMinutes() {
		return adjCal.minutes;
	}
	/**
	 * Get the calculated hours of day
	 * @return a long
	 */
	public long getHours() {
		return adjCal.hours;
	}
	
	/**
	 * Remove the leaps segments by recalculating it based on the day segment value
	 * and remove it, and remaking a new PDSDate.
	 * @param dateTime
	 * @return a new PDSDate
	 */
	public static PDSDate removeLeap(PDSDate dateTime) {
		long daySegment = dateTime.getDaySegment();
		
		long leap = TAILeap + leapsSince72(daySegment);

		long micros = dateTime.getMicrosSinceEpoch() - (leap * MicrosPerSecond);
		
		long daySeg = micros / MicrosPerDay;
		
		micros = micros - (daySeg * MicrosPerDay);
		
		long millisSeg = ( micros / 1000L);
		
		long microsSeg =  micros - (millisSeg * 1000L);
		
		long packetTime =   ((daySeg << 48)    & 0xffff000000000000L);
		packetTime |= ((millisSeg << 16)  & 0x0000ffffffff0000L);
		packetTime |=  (microsSeg         & 0x000000000000ffffL);

		return new PDSDate(packetTime);
	}

	/**
	* Removes leap seconds from an IET time and converts it to a PDSDate
	* object.
	*/
	public static PDSDate ietToPDSNoLeap(long arbIET){
		// Compute the pre-leapsec removal day segment
		long ietTime = arbIET;
		long tempDaySegment = (ietTime / MicrosPerDay);

		// Subtract leap seconds from arbitrary IET time
		long leap = TAILeap + leapsSince72(tempDaySegment);
		ietTime = ietTime - ( leap * MicrosPerSecond );

		// Compute day segment, subtract from total
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
	 * Static variation given a 64-bit mission time (segmented)
	 * @param packetTime 48 bits of time, 16 bits millis and 16 bits of micros
	 * @return the micros since epoch in a signed 64-bit quantity
	 */
	public static long getMicrosSinceEpoch(long packetTime) {
		long daySegment = (packetTime >> 48) & 0x0ffffL;
		long millisSegment = (packetTime >> 16) & 0x0ffffffffL;
		long microsSegment = packetTime & 0x0ffffL;

		long millis = (daySegment * MillisPerDay) + millisSegment; 
		
		long leap = TAILeap + leapsSince72(daySegment);

		millis += (leap * 1000L);
		
		long micros = (millis * 1000L) + microsSegment;

		return micros;
	}
	
	// basically a table in code...
	// pass in the days since epoch and the number leap
	// seconds to add is returned
	// FIXME will have to expanded every year from this one (2011)
	// that a leap second is added
	private static long leapsSince72(long daysSinceEpoch) {
		
		//if(staticLeapsecCount > 0)
		//	return staticLeapsecCount;
		
		BufferedReader br = null;
		FileReader fr = null;
		String line;
		boolean past1972 = false;
		long leapsecCount = 0;
		File file = null;
		
		// we need to specially handle Windows as it treats the working directory as bin
		// as opposed to Linux which treats the root directory as the working directory
		//if(System.getProperty("os.name").startsWith("Windows"))
		//	file = new File("../");
		//else
			file = new File("./");
		
		File [] files = file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name)
			{
				if(name.startsWith("leapsec") && name.endsWith("dat"))
					return true;
				else
					return false;
			}
		});
		
		for (File datFile : files) 
		{
			file = datFile;
		}
		
		try {
			
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
		} catch (FileNotFoundException e1) 
		{
			System.out.println("Failed to open LEAPSEC file. Please ensure leapsec.dat file is available in RT-STPS root directory.");
			System.exit(1);
		}
		
		try {
			
			while ((line = br.readLine()) != null)
			{		
				//prior leap seconds have already been accounted for in time calculations
				if (line.contains("1972 JAN"))
					past1972 = true;
				
				//ensure we're reading a proper line that indicates a leapsec
				else if(past1972 && (line.contains("TAI-UTC")))
					leapsecCount = leapsecCount + 1;
			}
			
		} catch (IOException e) {
			System.out.println("Failed to read from LEAPSEC file stream.");
			System.exit(1);
		}
		
		try {
			br.close();
		} catch (IOException e) {
			System.out.println("Failed to close LEAPSEC file stream.");
		}
		
		//System.out.println("leapsec count is " + leapsecCount);
		
		//staticLeapsecCount = leapsecCount;
		return leapsecCount;
		
		/*
		
		long leaps = 0L;
		
		// Jun 30 1972
		if (daysSinceEpoch >= 5295L) {
			leaps += 1L;
		}
		// Dec 31 1972
		if (daysSinceEpoch >= 5479L) {
			leaps += 1L;
		} 
		// Dec 31 1973
		if (daysSinceEpoch >= 5844L) {
			leaps += 1L;
		} 
		// Dec 31 1974
		if (daysSinceEpoch >= 6209L) {
			leaps += 1L;
		} 
		// Dec 31 1975
		if (daysSinceEpoch >= 6574L) {
			leaps += 1L;
		} 
		// Dec 31 1976
		if (daysSinceEpoch >= 6940L) {
			leaps += 1L;
		} 
		// Dec 31 1977
		if (daysSinceEpoch >= 7305L) {
			leaps += 1L;
		} 
		// Dec 31 1978
		if (daysSinceEpoch >= 7670L) {
			leaps += 1L;
		} 
		// Dec 31 1979
		if (daysSinceEpoch >= 8035L) {
			leaps += 1L;
		} 
		// Jun 30 1981
		if (daysSinceEpoch >= 8582L) {
			leaps += 1L;
		} 
		// Jun 30 1982
		if (daysSinceEpoch >= 8947L) {
			leaps += 1L;
		} 
		// Jun 30 1983
		if (daysSinceEpoch >= 9312L) {
			leaps += 1L;
		} 
		// Jun 30 1985
		if (daysSinceEpoch >= 10043L) {
			leaps += 1L;
		}
		// Dec 31 1987
		if (daysSinceEpoch >= 10957L) {
			leaps += 1L;
		}
		// Dec 31 1989
		if (daysSinceEpoch >= 11688L) {
			leaps += 1L;
		}
		// Dec 31 1990
		if (daysSinceEpoch >= 12053L) {
			leaps += 1L;
		}
		// Jun 30 1992
		if (daysSinceEpoch >= 12600L) {
			leaps += 1L;
		}
		// Jun 30 1993
		if (daysSinceEpoch >= 12965L) {
			leaps += 1L;
		}
		// Jun 30 1994
		if (daysSinceEpoch >= 13330L) {
			leaps += 1L;
		}
		// Dec 31 1995
		if (daysSinceEpoch >= 13879L) {
			leaps += 1L;
		} 
		// Jun 30 1997
		if (daysSinceEpoch >= 14426L) {
			leaps += 1L;
		} 
		// Dec 31 1998
		if (daysSinceEpoch >= 14975L) {
			leaps += 1L;
		}
		// Dec 31 2005
		if (daysSinceEpoch >= 17532L) {
			leaps += 1L;
		}
		// Dec 31 2008
		if (daysSinceEpoch >= 18628L) {
			leaps += 1L;
		}
		// Jun 30 2012
		if (daysSinceEpoch >= 19905L) {
			leaps += 1L;
		}

		return leaps; */
	}
}
