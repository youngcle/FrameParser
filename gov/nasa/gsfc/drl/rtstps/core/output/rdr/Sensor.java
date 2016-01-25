/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * A Sensor is defined by the JPSS/NPOESS documentation.  The only difference
 * here is switching the '-' for a '_' in some entries which are fixed up in 
 * the toString method.  The following sensors are defined:
 * <p><code>
 * A_DCS,
 * ATMS,
 * CRIS,
 * CERES,
 * SARP,
 * SARR,
 * OMPS_NP,
 * OMPS_TC,
 * OMPS_LP,
 * OMPS,
 * VIIRS,
 * SPACECRAFT
 * </code></p>
 * 
 * 
 *
 */
public enum Sensor {
	//NoNameSensor,
	A_DCS,
	ATMS,
	CRIS,
	CERES,
	SARP,
	SARR,
	OMPS_NP,
	OMPS_TC,
	OMPS_LP,
	OMPS,
	VIIRS,
	SPACECRAFT;
	
	/**
	 * Return a String representation of the enumeration, but swap an '_' with '-'.
	 */
	@Override
	public String toString() {
		return this.name().replace('_', '-');
	}
}
