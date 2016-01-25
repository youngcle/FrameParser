/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

/**
 * The OMPS specialization of the RDR class which creates the /All_Data/XXX-RDR_All and /Data_Products/XXX-RDR structures.
 * This overrides the createRawApplicationPackets method, the bulk of the work is still done by the base RDR class.
 * 
 *
 */
public class ROLPS_RDR extends RDR {
	private static int scansPerGranule = 3;
	private Stats stats = null;
	/**
	 * Create a new instance of this class for building the RDR structures
	 * @param allData The {@link AllData} object which created the /All_Data structure
	 * @param dataProds The [@link DataProducts} objects which created the /Data_Products structure
	 * @param dev The Development domain is where the processing is being done, although passed in, this is fixed when the instance is created
	 * @param scansPerGranule  The number of scans this sensor defines for a <code>Granule</code>
	 * @throws RtStpsException Wraps HDF library exceptions
	 */
	public ROLPS_RDR(AllData allData, DataProducts dataProds, FixedDomainDescription dev, int scansPerGranule) throws RtStpsException {
		super(null, RDRName.OMPS_LPScience, allData, dataProds, dev);
		System.out.println("OMPS RDR creation started.");
		this.scansPerGranule = scansPerGranule;
	}
	

	public ROLPS_RDR(Stats stats, AllData allData, DataProducts dataProds, FixedDomainDescription dev, int scansPerGranule) throws RtStpsException {
		super(stats, RDRName.OMPS_LPScience, allData, dataProds, dev);
		if (stats == null) {
			System.out.println("OMPS RDR creation started.");
		}
		this.scansPerGranule = scansPerGranule;
		this.stats = stats;
	}


	/**
	 * Create the specific {@link ATMSRawApplicationPackets}, this overrides the base RDR method
	 * @return RawApplicationPackets returns the instance as a generic {@link RawApplicationPackets} object
	 */
	protected RawApplicationPackets createRawApplicationPackets(RDRName rdrName) {
		// nextSetNum();  putting this here makes granule index increment if the raw app was NOT written, a bug then...
		
		RawApplicationPackets rap = new ROLPSRawApplicationPackets(stats, SpacecraftId.npp, getSetNum(), scansPerGranule, getPacketPool());
		
		
		this.getRaps().push(rap);
		
		return rap;
	}
	

}
