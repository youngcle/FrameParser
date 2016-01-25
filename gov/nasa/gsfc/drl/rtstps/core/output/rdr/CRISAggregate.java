/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;


//placeholder for further specialization

public class CRISAggregate extends Aggregate {


	
	public CRISAggregate(int rdrGroup, String aggregateName) throws RtStpsException {
		super(rdrGroup, "CRIS-SCIENCE-RDR_Aggr");
	}
	public CRISAggregate(int rdrGroup) throws RtStpsException {
		super(rdrGroup, RDRName.CRIS_Science);
	}
	public CRISAggregate(GranuleId beginningGranuleId,
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
				RDRName.CRIS_Science);
	}
	



}
