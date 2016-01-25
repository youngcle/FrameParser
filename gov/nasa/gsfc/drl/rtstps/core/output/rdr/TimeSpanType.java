/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * This is used with the SpacecraftAOS Diary calculation to mark List<Packet> as being in one of
 * four states in relations the beginning and ending times requested.
 * -- Enclosed - the asked for time span is enclosed by the packets in the list
 * -- Open - sorry, the span couldn't be enclosed at all
 * -- BeginningTimeOpen - it could be enclosed only on the ending time requested, that is the beginning is open
 * -- EndingTimeOpen - it could be enclosed only on the beginning time requested, that is the ending is open
 * 
 * 
 *
 */
public enum TimeSpanType {
	Enclosed,  // the asked for timespan is enclosed by the packets in the list
	Open, // sorry, the span couldn't be enclosed at all
	BeginningTimeOpen, // it could be enclosed only on the ending time requested
	EndingTimeOpen; // it could be enclosed only on the beginning time requested
}
