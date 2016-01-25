/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * This is for reading pre-existing RawApplicationPackets only, used by the RDRViewer only
 * @author krice
 *
 */
public class RawApplicationPacketsRandomAccess extends RawApplicationPackets {
	RawApplicationPacketsRandomAccess(RDRName rdrName, int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum, true, true);
	}
	@Override
	public boolean notFull(Packet p) throws RtStpsException {
		// not used or implemented
		return false;
	}

	@Override
	public void put(Packet p) throws RtStpsException {
		// not used or implemented

	}

}
