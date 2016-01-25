/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;


// placeholder for further specialization
public class ROLPSAggregate extends Aggregate {


	
	public ROLPSAggregate(int rdrGroup, String aggregateName) throws RtStpsException {
		super(rdrGroup, "OMPS-LPSCIENCE-RDR_Aggr");
	}
	public ROLPSAggregate(int rdrGroup) throws RtStpsException {
		super(rdrGroup, RDRName.OMPS_LPScience);
	}
	public ROLPSAggregate(GranuleId beginningGranuleId,
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
				RDRName.OMPS_LPScience);
	}
	


}
