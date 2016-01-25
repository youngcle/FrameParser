/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Create an "XXX-RDR_All" object which is used to write the XXX-RDR_All group in the HDF file,
 * which is the group that contains all of the RawApplicationPacket_(n) for XXX-RDR
 */
public class RDRAll {
	private int rdrAllGroup;
	private int hdfFile;
	
	/**
	 * Create an RDR_All for specific RDR such as: CRIS-Science-RDR_All
	 * @param hdfFile the handle of the HDF file
	 * @param rdrName the RDR name of interest
	 * @throws RtStpsException wraps any HDF exception
	 */
	RDRAll(int hdfFile, RDRName rdrName) throws RtStpsException {
	
		this.hdfFile = hdfFile;
		
		// create the group for the RDR itself
		String name = rdrName.getRDRStringName() + "_All";
		
		try {
			rdrAllGroup = H5.H5Gcreate(hdfFile, "/All_Data/" + name, 0);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * Return HDFFile handle which is may not be available to certain classes otherwise
	 * @return the HDFfile handle passed in to the constructor
	 */
	public final int getRDRAllHdfFileHandle() {
		return hdfFile;
	}
	
	/**
	 * Write a RawApplicationsPackets to the hdf file
	 * @param rawApplicationPackets an instance of a RawApplicationPackets to the RDR_All of interest
	 * @return true if the raw application packet was written, false if not
	 * @throws RtStpsException wraps any HDF exception
	 */
	public boolean write(RawApplicationPackets rawApplicationPackets) throws RtStpsException {
		return rawApplicationPackets.write(hdfFile);
	}
	
	/**
	 * Close the RDR_All group
	 * @throws RtStpsException wraps any HDF exception
	 */
	public void close() throws RtStpsException {
		try {
			H5.H5Gclose(rdrAllGroup);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
	}

}
