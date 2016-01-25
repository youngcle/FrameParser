/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

public class CRISSciencePacket 
{

    /**
	 * Given a known CRIS science packet, return the scan number field.
	 * @param p a CRIS science packet
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
	// when the scan start flag goes high in 528
	// collect packets until it flips again
	// when it flips, that's one scan's worth...
	// count the transitions, when a granules worth it collected
	// then its time to write them
	//returns true if start of scan
	//false otherwise
	public static boolean checkForScanStart(Packet p)
	{
	    if (p.isStandalonePacket() && p.hasSecondaryHeader()) 
	    {
		if (p.getApplicationId() == 528) 
		{
		    byte[] data = p.getData();
		    int offset = 6 + 8 + 2;
		    int scanStart = ((int)data[offset] >>> 7) & 0x00000001;
		    if (scanStart == 1) 
		    {
			//++scanCounter;
			return true;
		    }
		}
	    }
	    
	    return false;
	}
    
    
}
