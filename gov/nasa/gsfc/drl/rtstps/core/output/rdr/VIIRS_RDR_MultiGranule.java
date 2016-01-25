/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

/**
 * The VIIRS specialization of the RDR class which creates the /All_Data/XXX-RDR_All and /Data_Products/XXX-RDR structures.
 * This overrides the createRawApplicationPackets method, the bulk of the work is still done by the base RDR class.
 * 
 * This specialization creates one or more 48 scan granules PER RDR FILE
 */
public class VIIRS_RDR_MultiGranule extends RDR {
	private static int scansPerGranule = 48; // default
	private Stats stats = null;
	
	/**
	 * Create a new instance of this class for building the RDR structures
	 * @param allData The {@link AllData} object which created the /All_Data structure
	 * @param dataProds The [@link DataProducts} objects which created the /Data_Products structure
	 * @param dev The Development domain is where the processing is being done, although passed in, this is fixed when the instance is created
	 * @throws RtStpsException Wraps HDF library exceptions
	 */
	public VIIRS_RDR_MultiGranule(AllData allData, DataProducts dataProds, FixedDomainDescription dev) throws RtStpsException {
		//FIXME this calls method createRawApplicationPackets and scansPerGranule is ZERO unless it is a static...
		super(null, RDRName.VIIRS_Science, allData, dataProds, dev);   
		//System.out.println("VIIRS_RDR creation started.");
	}
	

	public VIIRS_RDR_MultiGranule(Stats stats, AllData allData, DataProducts dataProds, FixedDomainDescription dev) throws RtStpsException {
		//FIXME this calls method createRawApplicationPackets and scansPerGranule is ZERO unless it is a static...
		super(stats, RDRName.VIIRS_Science, allData, dataProds, dev);   
		this.stats = stats;
	}
		
	/**
	 * Create the specific {@link VIIRSRawApplicationPackets}, this overrides the base RDR method
	 * @return RawApplicationPackets returns the instance as a generic {@link RawApplicationPackets} object
	 */
	protected RawApplicationPackets createRawApplicationPackets(RDRName rdrName) {
		//nextSetNum();  putting this here makes granule index increment if the raw app was NOT written, a bug then...
		RawApplicationPackets rap;
		//rap = new VIIRSRawApplicationPackets_MultiGranule(stats, SpacecraftId.npp, getSetNum(), scansPerGranule, getPacketPool());			
		rap = new VIIRSRawApplicationPackets(stats, SpacecraftId.npp, getSetNum(), scansPerGranule, getPacketPool());
		this.getRaps().push(rap);
		return rap;
	}
	

}
