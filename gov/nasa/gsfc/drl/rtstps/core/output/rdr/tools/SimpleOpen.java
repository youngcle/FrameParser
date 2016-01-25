/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Print the attributes of Products and Granules to the console
 * 
 *
 */
public class SimpleOpen {

	/**
	 * @param args the RDR HDF file
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("No file to process... ");
			System.exit(-1);
		}
			
			try {
				
				String filename = args[0];
				System.out.println("Open filename -- " + filename);
				
				int hdfFile = H5.H5Fopen(filename, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
				
				//this.rdrName = rdrName;
			} catch (HDF5LibraryException e) {
				System.out.println("Detailed message: " + e.getMinorError(e.getMinorErrorNumber()));
				e.printStackTrace();
			}
			
			
		
	}

}
