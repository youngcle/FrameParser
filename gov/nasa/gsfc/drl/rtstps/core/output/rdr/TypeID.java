/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * Specified by the JPSS/NPOESS documentation.  In some cases any '-' in the name
 * is replaced with a '-' when it is converted to a String.
 * 
 *
 */
public enum TypeID {
	//NoNameTypeId,
	HSKDWELL,
	SSMDWELL,
	IMDWELL,
	DIAG_SCI,
	SCIENCE,
	CALIBRATION,
	DIAGEXPONE,
	DIAGEXPTWO,
	DIA_CAL,
	DWELL,
	DUMP,
	FSW_BOOTUP,
	DIAGNOSTIC,
	DIAGTELEMETRY,
	TELEMETRY,
	DIARY;
	
	@Override
	public String toString() {
		return this.name().replace('_', '-');
	}
}
