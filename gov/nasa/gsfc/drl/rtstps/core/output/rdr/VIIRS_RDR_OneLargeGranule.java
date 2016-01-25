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
 *  This specialization creates one large granule for however many scans appear on its input
 */
public class VIIRS_RDR_OneLargeGranule extends RDR {
	
	private Stats stats = null;
	
	/**
	 * Create a new instance of this class for building the RDR structures
	 * @param allData The {@link AllData} object which created the /All_Data structure
	 * @param dataProds The [@link DataProducts} objects which created the /Data_Products structure
	 * @param dev The DEV domain is where the processing is being done, although passed in, this is fixed when the instance is created
	 * @param scansPerGranule  The number of scans this sensor defines for a <code>Granule</code>
	 * @throws RtStpsException Wraps HDF library exceptions
	 */
	public VIIRS_RDR_OneLargeGranule(AllData allData, DataProducts dataProds, FixedDomainDescription dev) throws RtStpsException {
		super(null, RDRName.VIIRS_Science, allData, dataProds, dev);  //FIXME this calls method createRawApplicationPackets and scansPerGranule is ZERO unless it is a static... 
		System.out.println("VIIRS_RDR creation started.");
		
	}
	

	public VIIRS_RDR_OneLargeGranule(Stats stats, AllData allData, DataProducts dataProds, FixedDomainDescription dev) throws RtStpsException {
		super(stats, RDRName.VIIRS_Science, allData, dataProds, dev);  //FIXME this calls method createRawApplicationPackets and scansPerGranule is ZERO unless it is a static... 
		if (stats == null) {
			System.out.println("VIIRS_RDR creation started.");
		}
		
		this.stats = stats;
	}
		

	/**
	 * Create the specific {@link VIIRSRawApplicationPackets}, this overrides the base RDR method
	 * @return RawApplicationPackets returns the instance as a generic {@link RawApplicationPackets} object
	 */
	protected RawApplicationPackets createRawApplicationPackets(RDRName rdrName) {
		//nextSetNum();  putting this here makes granule index increment if the raw app was NOT written, a bug then...
		
		//System.out.println("createRawApplicationPackets: " + scansPerGranule);
		RawApplicationPackets rap;
		

		rap = new VIIRSRawApplicationPackets_OneLargeGranule(stats, SpacecraftId.npp, getSetNum(), getPacketPool());			
	
		
		this.getRaps().push(rap);
		
		return rap;
	}
	

}
