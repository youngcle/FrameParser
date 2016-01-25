/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;


//placeholder for further specialization
public class VIIRSAggregate extends Aggregate {

	
	public VIIRSAggregate(int rdrGroup, String aggregateName) throws RtStpsException {
		super(rdrGroup, "VIIRS-SCIENCE-RDR_Aggr");
	}
	public VIIRSAggregate(int rdrGroup) throws RtStpsException {
		super(rdrGroup, RDRName.VIIRS_Science);
	}
	public VIIRSAggregate(GranuleId beginningGranuleId,
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
				RDRName.VIIRS_Science);
	}
	




}
