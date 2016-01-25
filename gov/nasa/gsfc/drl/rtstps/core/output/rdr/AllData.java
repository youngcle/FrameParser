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
 * Build the AllData portion of the RDR HDF file structure. The HDF file handle is passed into the class
 * and it assumed that it was opened or created in another location.
 * Once successfully created, the object may be used to create the RDR All area as well.
 * 
 * 
 *
 */
public class AllData {
	private int hdfFile;
	private int allDataGroup;

	
	/**
	 * Constructor for AllData structure in the RDR uses the HDF handle as an argument to the HDF file of interest.
	 * Given the handle it creates the "/All_Data" group in the RDR/HDF file.  Note that the HDF group is probably not
	 * complete until the close method is called at the end of processing.
	 * 
	 * @param hdfFile a handle to an HDF file of interest
	 * @throws RtStpsException Wraps any HDF library exceptions in an RtStpsException
	 */
	public AllData(int hdfFile) throws RtStpsException {
		this.hdfFile = hdfFile;
		try {
			// Create the root group.
			allDataGroup = H5.H5Gcreate(hdfFile, "/All_Data", 0);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
	}
	
	/**
	 * Create an RDR_All group in /All_Data for the named RDR. And instance of RDRAll is created which is
	 * used to manage that specified XXXX-RDR_All. (e.g. VIIRS-SCIENCE-RDR_All)
	 * 
	 * @param rdrName the name of an RDR such as VIIRS-SCIENCE-RDR, specified using the list of supported RDRs in the RDRName enumeration
	 * @return returns an instance of RDRAll for the named RDR
	 * @throws RtStpsException Wraps any HDF library exceptions in an RtStpsException
	 */
	public RDRAll createRDRAll(RDRName rdrName) throws RtStpsException {
		return new RDRAll(hdfFile, rdrName);
	}
	
	/**
	 * Return the HDF group handle for the All_Data group
	 * @return an integer handle to the HDF group
	 */
	public int getAllDataGroupHandle() {
		return allDataGroup;
	}
	
	/**
	 * Close the HDF All_Data group.  This must be called to complete the HDF creation of All_Data and
	 * should be done after the completion of processing.
	 * 
	 * @throws RtStpsException Wraps any HDF library exceptions in an RtStpsException
	 */
	public void close() throws RtStpsException {
		try {
			H5.H5Gclose(allDataGroup);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
	}
	


	
}
