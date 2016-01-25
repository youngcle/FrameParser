/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * A {@link Collection} is defined by the JPSS/NPOESS documentation for RDRs. This package support four at this time:
 * VIIRS, ATMS, CRIS and SpacecraftAOS Diary (attitude and ephemeris)
 * 
 *
 */
public enum Collection {
    	OMPS_LPSCIENCE_RDR,
	OMPS_NPSCIENCE_RDR,
	OMPS_TCSCIENCE_RDR,
	OMPS_TELEMETRY_RDR,
	VIIRS_SCIENCE_RDR,
	ATMS_SCIENCE_RDR,
	CRIS_SCIENCE_RDR,
	SPACECRAFT_DIARY_RDR;
	
	// FIXME - the _ vs -.  The problem is the specs say "_" or "-" where the attributes have it
	// the other way around.  So in every conversion or lookup, something has to flip. Inevitably 
	// this is just extra and pointless work...
	
	/**
	 * Return the <code>Collection</code> from an RDR name.
	 * @return a <code>Collection</code> matching the RDR name or null if no match is found
	 */
	public static Collection fromRDRName(RDRName rdrName) {
		//System.out.println("Looking for: " + rdrName.getRDRStringName());
		
		Collection[] values = Collection.values();
		for (int i = 0; i < values.length; i++) {

			
			if (values[i].toString().equals(rdrName.getRDRStringName())) {  //NOTE: the underscore, hyphen flip here...
				return values[i];
			}
		}
		return null; // null or noname?
	}
	
	/**
	 * Return the <code>Collection</code> name as String but swap the underscores with a hyphen because this is 
	 * what is needed by the RDR attributes in the HDF file.
	 * @return a String representing the <code>Collection</code> with the '_' replaced by '-'
	 */
	public String toString() {
		return this.name().replace('_', '-');
	}


	/**
	 * Replaces valueOf which doesn't know about the "-" vs "_" issues
	 * @param collectionStr a String like this SPACECRAFT-DIARY-RDR
	 * @return the matching <code>Collection</code> or null if no match is found
	 */
	public static Collection myValueOf(String collectionStr) {
		Collection[] values = Collection.values();
		for (int i = 0; i < values.length; i++) {
			if (values[i].toString().equals(collectionStr)) {
				return values[i];
			}
		}
		return null;
	}
	
}
