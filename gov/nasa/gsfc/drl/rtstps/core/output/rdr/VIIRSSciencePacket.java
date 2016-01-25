/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * Support some aspects of a VIIRS Science Packet, mainly helper method(s)
 * used in VIIRSRawApplicationPackets class to determine the scan number of
 * the VIIRS packets being processed.
 * 
 */
public class VIIRSSciencePacket {

	/**
	 * Given a known VIIRS science packet, return the scan number field.
	 * @param p a VIIRS science packet
	 * @return integer scan number
	 */
	public static long getScanNumber(Packet p) {
		byte[] data = p.getData();
		int offset = 6 + 28;
		long scanNum = ( (int)data[offset] << 24 ) | 
						(((int)data[offset+1] << 16) & 0x00ff0000) | 
						(((int)data[offset+2] <<  8) & 0x0000ff00) | 
						((int)data[offset+3] & 0x000000ff) ;
		
		scanNum &= 0x00000000ffffffffL;
		
		return scanNum;
	}
}
