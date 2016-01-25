/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;


import java.util.LinkedList;
import java.util.List;

/**
 * The list of active RDRs which various access functions.  The main feature is that the SpacecraftDiary is always 
 * weighted to be at the END of the list.
 * The list then is not alphabetical, simple in the order the RDR creation process started with the special case that
 * the SpacecraftAOS Diary is at the end of the list.
 * NOTE: this is currently not used and may be deprecated in the near future.
 * 
 *
 */
class WeightedRDRList {
	private LinkedList<RDR> weightedList = new LinkedList<RDR>();

	/**
	 * Put the RDR object in question in the list and weight according to some internal algorithm.
	 * In this case the attitude and ephemeris packet is always at the end of the list because it must
	 * be processed last for the construction of RDRs.
	 * @param rdr an RDR object of interest
	 */
	void put(RDR rdr) {
		if (rdr.getRDRName() == RDRName.NPP_Ephemeris_and_Attitude) {
			weightedList.addLast(rdr);
		} else {
			weightedList.addFirst(rdr);
		}
	}
	
	/**
	 * Find the RDR by name or return null
	 * @param rdrName the RDR of interest
	 * @return the RDR in the list or null if it does not exist
	 */
	RDR get(RDRName rdrName) {
		
		for (RDR rdr : weightedList) {
			if (rdr.getRDRName().getRDRStringName().equals(rdrName.getRDRStringName())) {
				return rdr;
			}
		}
		
		return null;
	}
	
	/**
	 * Get all the RDRs in the list
	 * @return the list of RDRs held in an instance of this class
	 */
	List<RDR> getList() {
		return weightedList;
	}
	
}
