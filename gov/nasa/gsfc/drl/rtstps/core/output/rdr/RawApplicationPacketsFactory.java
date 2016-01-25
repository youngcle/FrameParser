/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

/**
 * Factory style class to build specific RawAppPackets classes such as VIIRSRawApplicationPackets.
 * At this time this class is only used by Granule when it dereferences to the RawApplicationPacket
 * associated with it.
 * 
 * Typically used in a READ scenario for an already existing RDR file
 *
 */
public class RawApplicationPacketsFactory {

	public static RawApplicationPackets make(int rawAppsId, RDRName rdrName, int granuleNumber) throws RtStpsException {
		if (rdrName == RDRName.CRIS_Science) {
			return new CRISRawApplicationPackets(rawAppsId, granuleNumber, true);
		} else if (rdrName == RDRName.ATMS_Science) {
			return new ATMSRawApplicationPackets(rawAppsId, granuleNumber, true);
		} else if (rdrName == RDRName.VIIRS_Science) {
			return new VIIRSRawApplicationPackets(rawAppsId, granuleNumber, true); 
		} else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude) {
			return new SpacecraftDiaryRawApplicationPackets2(rawAppsId, granuleNumber, true);
		}
		
		return null;
	}

	public static RawApplicationPackets make(RDRName rdrName, int rootGroup, int counter) throws RtStpsException {
		if (rdrName == RDRName.CRIS_Science) {
			return new CRISRawApplicationPackets(rootGroup, counter);
		} else if (rdrName == RDRName.ATMS_Science) {
			return new ATMSRawApplicationPackets(rootGroup, counter);
		} else if (rdrName == RDRName.VIIRS_Science) {
			return new VIIRSRawApplicationPackets(rootGroup, counter); 
		} else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude) {
			return new SpacecraftDiaryRawApplicationPackets2(rootGroup, counter);
		}
		
		return null;
	}

	public static RawApplicationPackets makeRandomAccess(RDRName rdrName, int rootGroup, int counter) throws RtStpsException {
		
		return new RawApplicationPacketsRandomAccess(rdrName, rootGroup, counter);

	}
	
}
