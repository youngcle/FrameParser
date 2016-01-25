/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;


// placeholder for further specialization
public class ROTCSAggregate extends Aggregate {


	
	public ROTCSAggregate(int rdrGroup, String aggregateName) throws RtStpsException {
		super(rdrGroup, "OMPS-TCSCIENCE-RDR_Aggr");
	}
	public ROTCSAggregate(int rdrGroup) throws RtStpsException {
		super(rdrGroup, RDRName.OMPS_TCScience);
	}
	public ROTCSAggregate(GranuleId beginningGranuleId,
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
				RDRName.OMPS_TCScience);
	}
	


}
