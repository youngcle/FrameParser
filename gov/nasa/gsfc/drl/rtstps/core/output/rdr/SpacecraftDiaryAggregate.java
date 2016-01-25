/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

public class SpacecraftDiaryAggregate extends Aggregate {

	
	
	public SpacecraftDiaryAggregate(int rdrGroup, String aggregateName) throws RtStpsException {
		super(rdrGroup, "SPACECRAFT-DIARY-RDR_Aggr");
	}
	public SpacecraftDiaryAggregate(int rdrGroup) throws RtStpsException {
		super(rdrGroup, RDRName.NPP_Ephemeris_and_Attitude);
	}
	public SpacecraftDiaryAggregate(GranuleId beginningGranuleId,
	 		GranuleId endingGranuleId,
	 		long beginningOrbit,
	 		long endingOrbit,
	 		PDSDate beginningDateTime,
			PDSDate endingDateTime,
			long granuleCount) {
		
		super(beginningGranuleId, 
				endingGranuleId, 
				beginningOrbit, 
				endingOrbit, 
				beginningDateTime, 
				endingDateTime, 
				granuleCount, 
				RDRName.NPP_Ephemeris_and_Attitude);
	}

	



}
