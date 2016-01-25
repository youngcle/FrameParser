/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class TimeTest {

	public static void main(String[] args) {
		PDSDate test = new PDSDate(0L);
		
		System.out.println("First day? " + test.toString());
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss.SSS zzz yyyy");
		
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		System.out.println("Timestamp " + sdf.format(test.getDate()));
	}
}
