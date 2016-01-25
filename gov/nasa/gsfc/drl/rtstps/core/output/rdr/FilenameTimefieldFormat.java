/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Enforce certain time field formats for the the RDR filename. Time zone is UTC.  Uses {@link SimpleDateFormat}.
 * 
 *
 */
public class FilenameTimefieldFormat {
	private static SimpleDateFormat dfTime = initializeTime1();
	private static SimpleDateFormat mfTime = initializeTime2();
	private StartStopFieldId fieldId;
	
	// initialize the time formats and set the time zone to UTC.
	private static SimpleDateFormat initializeTime1() {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf;
	}
	private static SimpleDateFormat initializeTime2() {
		SimpleDateFormat sdf = new SimpleDateFormat("SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf;
	}
	
	/**
	 * Construct a new FileTimefieldFormat using the {link@ StartStopFieldId} as an input.  Time zone is UTC.
	 * @param fieldId a StartStopFieldId which is used in the official name
	 */
	public FilenameTimefieldFormat(StartStopFieldId fieldId) {
		this.fieldId = fieldId;
	}
	
	/**
	 * Format the given Date into proper FilenameTimeField
	 * @param timeDate the time/date of interest
	 * @return a String that enforces the format
	 */
	public String format(Date timeDate) {
		String tmpMilliStr = mfTime.format(timeDate);
		int tmpTenths = Integer.parseInt(tmpMilliStr);
		tmpTenths = tmpTenths / 100;
		
		String timeStr = fieldId.toString() + dfTime.format(timeDate)  + String.format("%1d", tmpTenths) ;
		
		return timeStr;
	}
	
	/**
	 * Parse the given string according to the FilenameTimefieldFormat and return it as a Date
	 * @param timeStr the string containing the time and date
	 * @return a Date with the time and date from the string encoded in it
	 * @throws ParseException throws ParseException if the string cannot be parsed according to the format
	 */
	public Date parse(String timeStr) throws ParseException {
		if ((timeStr.charAt(0) != StartStopFieldId.e.toString().charAt(0)) && 
				(timeStr.charAt(0) != StartStopFieldId.t.toString().charAt(0))) {
			throw new ParseException("Time string [" + timeStr + "] must start with a \'" + StartStopFieldId.t + 
										" not a \'" + StartStopFieldId.e + "\'" , 0);			
		}
		
		
		return dfTime.parse(timeStr.substring(1));
	}
}
