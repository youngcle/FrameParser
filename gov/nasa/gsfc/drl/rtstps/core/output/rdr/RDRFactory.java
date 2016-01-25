/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.util.HashMap;

/**
 * A factory class & method for creating specific RDR based on various input parameters.
 * This class is used while processing packets in real time from an input stream.
 * 
 *
 */
final public class RDRFactory {
	private static HashMap<RDRName, RDR> rdrTable = new HashMap<RDRName, RDR>();
	
	/**
	 * For any RDR object created, it may be stored here through this method, duplicates  
	 * @param rdrName RDRName of interest
	 * @param rdr RDR object of interest
	 */
	public static void add(RDRName rdrName, RDR rdr) {
		rdrTable.put(rdrName, rdr);
	}
	
	/**
	 * Create the RDR of interest using the supplied argumets.  If no RDR match is found, a default or basic RDR is created 
	 * using this class {@link BasicRDR}
	 * @param anRdrName the RDRName
	 * @param allData the All_Data object
	 * @param dataProducts the DataProducts object
	 * @param dev the DRL domain
	 * @return a specific RDR
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	public static RDR createRDR(RDRName anRdrName, AllData allData, DataProducts dataProducts, FixedDomainDescription dev) throws RtStpsException {
		// FIXME for now this is basically hardcoded and the rdrTable is ignored...
		
		if (anRdrName == RDRName.VIIRS_Science) {
			//return new VIIRS_RDR(allData, dataProducts, dev, 48);
			return new VIIRS_RDR_MultiGranule(allData, dataProducts, dev); // for sanjeeb
		} else if (anRdrName == RDRName.ATMS_Science) {
			return new ATMS_RDR(allData, dataProducts, dev, 12);
		} else if (anRdrName == RDRName.CRIS_Science) {
			return new CRIS_RDR(allData, dataProducts, dev, 4);
		} else if (anRdrName == RDRName.OMPS_LPScience) {
			return new ROLPS_RDR(allData, dataProducts, dev, 4);
		} else if (anRdrName == RDRName.OMPS_NPScience) {
			return new RONPS_RDR(allData, dataProducts, dev, 4);
		} else if (anRdrName == RDRName.OMPS_TCScience) {
			return new ROTCS_RDR(allData, dataProducts, dev, 4);
		} 
		
		throw new RtStpsException("RDR [" + anRdrName.toString() + "] is not supported");
	}

	public static RDR createRDR(Stats stats, RDRName anRdrName, AllData allData, DataProducts dataProducts, FixedDomainDescription dev) throws RtStpsException {
		// FIXME for now this is basically hardcoded and the rdrTable is ignored...
		
		if (anRdrName == RDRName.VIIRS_Science) {
			//return new VIIRS_RDR(stats, allData, dataProducts, dev, 48);
			return new VIIRS_RDR_MultiGranule(stats, allData, dataProducts, dev); // for sanjeeb
		} else if (anRdrName == RDRName.ATMS_Science) {
			return new ATMS_RDR(stats, allData, dataProducts, dev, 12);
		} else if (anRdrName == RDRName.CRIS_Science) {
			return new CRIS_RDR(stats, allData, dataProducts, dev, 4);
		} else if (anRdrName == RDRName.OMPS_LPScience) {
			return new ROLPS_RDR(allData, dataProducts, dev, 4);
		} else if (anRdrName == RDRName.OMPS_NPScience) {
			return new RONPS_RDR(allData, dataProducts, dev, 4);
		} else if (anRdrName == RDRName.OMPS_TCScience) {
			return new ROTCS_RDR(allData, dataProducts, dev, 4);
		}
		
		throw new RtStpsException("RDR [" + anRdrName.toString() + "] is not supported");
	}

}
