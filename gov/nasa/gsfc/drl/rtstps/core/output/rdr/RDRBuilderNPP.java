/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

final public class RDRBuilderNPP {
	
	private RDRFileWriter rdrBuilderNPP;

	public RDRBuilderNPP(String outputDirectoryPath, Stats stats) throws RtStpsException {
		RDR.DocumentName = "D34862-02_NPOESS-CDFCB-X-Vol-II_D_20090603_I1.5.0.pdf";
		
		// Note: It seems that the rdrcount passed to the RDRFileWriter is only 1. I'm guessing this represents the ___-SCIENCE-RDR and
		// the SPACECRAFT-DIARY-RDR is already included automatically in RDR files?
		rdrBuilderNPP = new RDRFileWriter(stats, outputDirectoryPath, 1, 1, Origin.all, MissionName.NPP, Origin.all, PlatformShortName.NPP, 0);
	}	

	public RDRBuilderNPP(String outputDirectoryPath, Stats stats, String outputMode, long granSpan) throws RtStpsException {
		RDR.DocumentName = "D34862-02_NPOESS-CDFCB-X-Vol-II_D_20090603_I1.5.0.pdf";
		
		// Note: It seems that the rdrcount passed to the RDRFileWriter is only 1. I'm guessing this represents the ___-SCIENCE-RDR and
		// the SPACECRAFT-DIARY-RDR is already included automatically in RDR files?
		rdrBuilderNPP = new RDRFileWriter(stats, outputDirectoryPath, 1, 1, Origin.all, MissionName.NPP, Origin.all, PlatformShortName.NPP, granSpan);

		//[C]: Enable multi-mode if RDR is to be granulated, or if a temporal span is specified
		if( outputMode.equals("granulate") || granSpan > 0L ){
			rdrBuilderNPP.setMultiMode(true);
		}
	}
	
	public void put(Packet p) throws RtStpsException {
		rdrBuilderNPP.put(p);
	}
	
	public void close(boolean hdfCleanup) throws RtStpsException {
		rdrBuilderNPP.close(hdfCleanup);
	}
	
	public String toString() {
		return rdrBuilderNPP.toString();
	}
}
