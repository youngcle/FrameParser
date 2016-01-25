/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * An enumeration used by the ATMSRawApplication class
 * 
 *
 */
public enum ATMSScanState {
	Begin,  // start here
	Scanstart, // if the scan count == 1 field is found
	MidScan, // the scan 
	Complete; // complete for some reason, the main one being the enough scans to make a group is complete
}
