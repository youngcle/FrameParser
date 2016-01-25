/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * Official mission names
 * 
 *
 */
public enum MissionName {
	NPP,
	NPOESS,
	NPP_NPOESS;
	
	@Override
	/**
	 * Returns the mission name as string, the _ is replaced with / in the NPP_NPOESS item.
	 * @return a string of the name
	 */
	public String toString() {
		return this.name().replace('_', '/');
	}
}
