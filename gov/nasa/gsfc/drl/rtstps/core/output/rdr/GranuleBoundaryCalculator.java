/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.Version;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

public class GranuleBoundaryCalculator{
	/*
	* Takes in a RawApplicationPackets object, and calculates its start boundary (in IET time)
	* @param rap the RawApplicationPackets object whose start/end boundary is to be calculated
	* @param boundary value indicating which boundary to be calculated; 0 for start, 1 for end.
	* @return a long representing the boundary in IET time.
	*/
	public static long getBoundary(RawApplicationPackets rap, int boundary) throws RtStpsException{
		// First, convert the RawApplicationPackets' first CDS time into IET
		long ietTime =  LeapDate.getMicrosSinceEpoch(rap.getFirstTime());
		RDRName rdrName = rap.getRdrName();

		// Each RAP type is hardcoded as they have their own implementation of getStart/EndBoundary.
		// Add new RAP types here as needed:
		if( rdrName == RDRName.NPP_Ephemeris_and_Attitude ){
			if(boundary == 0)
				return SpacecraftDiaryGranule.getStartBoundary(ietTime);
			else
				return SpacecraftDiaryGranule.getEndBoundary(ietTime);
		}
		else if( rdrName == RDRName.VIIRS_Science ){
			if(boundary == 0)
				return VIIRSGranule.getStartBoundary(ietTime);
			else
				return VIIRSGranule.getEndBoundary(ietTime);
		}
		else if( rdrName == RDRName.ATMS_Science ){
			if(boundary == 0)
				return ATMSGranule.getStartBoundary(ietTime);
			else
				return ATMSGranule.getEndBoundary(ietTime);
		}
		else if( rdrName == RDRName.CRIS_Science ){
			if(boundary == 0)
				return CRISGranule.getStartBoundary(ietTime);
			else
				return CRISGranule.getEndBoundary(ietTime);
		}
		else if( rdrName == RDRName.OMPS_NPScience ){
			if(boundary == 0)
				return RONPSGranule.getStartBoundary(ietTime);
			else
				return RONPSGranule.getEndBoundary(ietTime);
		}
		else if( rdrName == RDRName.OMPS_TCScience ){
			if(boundary == 0)
				return ROTCSGranule.getStartBoundary(ietTime);
			else
				return ROTCSGranule.getEndBoundary(ietTime);
		}
		else if( rdrName == RDRName.OMPS_LPScience ){
			if(boundary == 0)
				return ROLPSGranule.getStartBoundary(ietTime);
			else
				return ROLPSGranule.getEndBoundary(ietTime);
		}
		else{
			throw new RtStpsException("Unsupported RawApplicationPackets type! Cannot calculate granule boundaries!");
		}
	}

	public static long getGranuleSize( RawApplicationPackets rap ) throws RtStpsException {
		// FIXME: Make sure any new instruments are added here;
		// the instrument's implementation of Granule.java MUST have a static
		// getGranulesize() function!
		RDRName rdrName = rap.getRdrName();

		if(rdrName == RDRName.NPP_Ephemeris_and_Attitude)
			return SpacecraftDiaryGranule.getGranulesize();
		else if(rdrName == RDRName.VIIRS_Science)
			return VIIRSGranule.getGranulesize();
		else if(rdrName == RDRName.ATMS_Science)
			return ATMSGranule.getGranulesize();
		else if(rdrName == RDRName.CRIS_Science)
			return CRISGranule.getGranulesize();
		else if(rdrName == RDRName.OMPS_NPScience)
			return RONPSGranule.getGranulesize();
		else if(rdrName == RDRName.OMPS_TCScience)
			return ROTCSGranule.getGranulesize();
		else if(rdrName == RDRName.OMPS_LPScience)
			return ROLPSGranule.getGranulesize();
		else
			throw new RtStpsException("Unsupported RawApplicationPackets type! Cannot calculate granule size!");
	}

	public static long adjustedEndBoundary( long startBoundary, long granuleSize, long granuleTimeSpan){
		// Take floor value of granule time span divided by granule size, which will give us the
		// number of complete granules that can fit within granuleTimeSpan. Default value is 1.		
		long numGran = granuleTimeSpan / granuleSize;
		if(numGran < 1L)
			numGran = 1L;

		// Return the adjusted end boundary:
		return ( startBoundary + (numGran * granuleSize) );
	}

	public static long getAdjustedEndBoundary( RawApplicationPackets rap, long granTimeSpan ) throws RtStpsException{
		return adjustedEndBoundary( getBoundary(rap, 0), getGranuleSize(rap), granTimeSpan );
	}
}
