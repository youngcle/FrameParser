/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

@Deprecated
class HDF5Stats {
	int packetCount = 0;
	long firstPacketTime = 0l;
	long lastPacketTime = 0l;
	private boolean setFirstTime = true;
	
	void setTime(long rawTime) {
		if (setFirstTime) {
			firstPacketTime = rawTime;
			setFirstTime = false;
		}
		lastPacketTime = rawTime;
	}
	
}
