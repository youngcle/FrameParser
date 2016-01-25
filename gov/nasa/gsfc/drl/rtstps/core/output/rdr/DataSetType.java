/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * The valid JPSS data set types, only RDR is supported by this package
 * 
 *
 */
public enum DataSetType {
	RDR, 
	SDR, 
	TDR, 
	EDR, 
	ARP, 
	ANC,
	AUX, 
	IP, 
	GEO, 
	TLM_SDR;
}
