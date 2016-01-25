/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

/**
 * A common root class for {@link Granule} and {@link Aggregate} as the both exist in the Data_Product area without any good way
 * to differentiate them ... mainly for the <code>Iterators</code> here.
 * 
 *
 */
public abstract class CommonDataSetObject {
	
	public abstract String getName();
	
	public abstract void close() throws RtStpsException;
}
