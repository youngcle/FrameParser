/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

/**
 * The Diary specialization of the RDR class which creates the /All_Data/XXX-RDR_All and /Data_Products/XXX-RDR structures.
 * This overrides the createRawApplicationPackets method, the bulk of the work is still done by the base RDR class.
 * 
 *
 */
public class SpacecraftDiaryRDR_MultipleGranule extends RDR {
	private Stats stats = null;
	
	/**
	 * Create a new instance of this class for building the RDR structures
	 * @param stats 
	 * @param allData The {@link AllData} object which created the /All_Data structure
	 * @param dataProds The [@link DataProducts} objects which created the /Data_Products structure
	 * @param dev The Development domain is where the processing is being done, although passed in, this is fixed when the instance is created
	 * @throws RtStpsException Wraps HDF library exceptions
	 */
	public SpacecraftDiaryRDR_MultipleGranule(Stats stats, AllData allData, DataProducts dataProds, FixedDomainDescription dev) throws RtStpsException {
		super(stats, RDRName.NPP_Ephemeris_and_Attitude, allData, dataProds, dev);
		this.stats = stats;
	}
	
	/**
	 * Create a new instance of this class for building the RDR structures, pre-loaded with the given RawApplicationPackets. REQUIRED for copying
	 * co-temporal SpacecraftAOS Diary granules when creating multiple RDR files for a single pass!
	 * @param stats 
	 * @param allData The {@link AllData} object which created the /All_Data structure
	 * @param dataProds The [@link DataProducts} objects which created the /Data_Products structure
	 * @param dev The Development domain is where the processing is being done, although passed in, this is fixed when the instance is created
	 * @param RAP the RawApplicationPackets of the co-temporal SpacecraftAOS Diary granule
	 * @throws RtStpsException Wraps HDF library exceptions
	 */
	public SpacecraftDiaryRDR_MultipleGranule(Stats stats, 
						AllData allData, 
						DataProducts dataProds, 
						FixedDomainDescription dev, 
						RawApplicationPackets RAP) throws RtStpsException {
		super(stats, RDRName.NPP_Ephemeris_and_Attitude, allData, dataProds, dev, RAP);
		this.stats = stats;
	}

	/**
	 * Create the specific {@link ATMSRawApplicationPackets}, this overrides the base RDR method
	 * @return RawApplicationPackets returns the instance as a generic {@link RawApplicationPackets} object
	 */
	protected RawApplicationPackets createRawApplicationPackets(RDRName rdrName) {
		// nextSetNum();  putting this here makes granule index increment if the raw app was NOT written, a bug then...
		
		RawApplicationPackets rap = new SpacecraftDiaryRawApplicationPackets2(stats, SpacecraftId.npp, getSetNum(), getPacketPool());
		
		
		this.getRaps().push(rap);
		
		return rap;
	}
	

}
