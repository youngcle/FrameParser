/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;


/**
 * This is the generic RDR processing class, most of the implementation is in abstract RDR class
 * and this just provides the basic counting style {@link RawApplicationPackets} class and sets the depth
 * of the count to a specific value.   This class is used in packet streams when packets with application
 * identifiers appear that simple do not match any previous define sensor packet... and yet the RDR
 * must be created. Other possibly is to replace this class would be to filter out unwanted packets and 
 * not make any granules or raw apps for them.
 * 
 *
 */
@Deprecated
public final class BasicRDR extends RDR {

	/**
	 * Create the BasicRDR and set the counting depth to 20 packets
	 * @param rdrName The RDR name
	 * @param allData The /All_Data structure
	 * @param dataProds The /Data_Products structure
	 * @param dev The processing is done in a development environment
	 * @throws RtStpsException Wrapped HDF exceptions
	 */
	public BasicRDR(RDRName rdrName, AllData allData, DataProducts dataProds, FixedDomainDescription dev) throws RtStpsException  {
		super(null, rdrName, allData, dataProds, dev);
		setDepth(20);
	}
	
	/**
	 * Create the generic counting RawApplicationPackets, this overrides the base RDR method
	 * @param rdrName the RDR of interest
	 * @return an instance of this class as the more generic RawApplicationsPacket
	 */
	@Override
	protected RawApplicationPackets createRawApplicationPackets(RDRName rdrName) {
		// nextSetNum();  putting this here makes granule index increment if the raw app was NOT written, a bug then...
		
		RawApplicationPackets rap = new CountingRawApplicationPackets(SpacecraftId.npp, rdrName, getSetNum(), getDepth());
		
		this.getRaps().push(rap);
		
		return rap;
	}
}
