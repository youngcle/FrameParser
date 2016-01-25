/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.io.File;
import java.util.List;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;



public class RDRFileReader {
	
	private String filename;
	private File file;
	private int hdfFile;
	private int propertyId;
	private long[] userBlockSize = new long[1];
	private NPOESSFilename nfilename;
	private int rdrCount;
	
	
	/**
	 * Open an NPOESS filename for reading
	 * @param filename a filename in the NPOESSFilename format
	 * @throws RtStpsException a variety of HDF and RtStpsExceptions
	 */
	public RDRFileReader(String filename) throws RtStpsException {
		
		try {
			
			this.filename = filename;
			
			file = new File(filename);
			
			// the error messages in HDF are horribly obscure -- first just see if the file exists
			// and go from there...
			if (file.exists() == false) {
				throw new RtStpsException("File not found: " + filename);
				
			}
			nfilename = new NPOESSFilename(file.getName());
			
			//propertyId = H5.H5Pcreate (HDF5Constants.H5P_FILE_CREATE);
			
			hdfFile = H5.H5Fopen(filename, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
			
			propertyId = H5.H5Fget_create_plist(hdfFile);
			
			H5.H5Pget_userblock(propertyId, userBlockSize);
	
			if (hdfFile < 0) {
				throw new RtStpsException("File not found: " + filename);
			}
			//this.rdrName = rdrName;
		} catch (HDF5LibraryException e) {
			System.out.println("Detailed message: " + e.getMinorError(e.getMinorErrorNumber()));
			throw new RtStpsException(e.getMinorError(e.getMinorErrorNumber()), e);
		}
		
		
	}
	public File getFile() {
		return file;
	}
	public String getFilename() {
		return filename;
	}
	public NPOESSFilename getNPOESSFilename() {
		return nfilename;
	}
	/**
	 * Return the product identifiers of the NPOESSFilename -- these are in the name itself
	 * @return a list of ProductIdentifers
	 */
	public List<ProductIdentifiers> getProductIndentifiers() {
		return nfilename.getProductIdentifiers();
	}
	public AllDataReader createAllDataReader() throws RtStpsException {
		return new AllDataReader(hdfFile);
	}

	public UserBlockReader createUserBlockReader() throws RtStpsException {
		return new UserBlockReader(filename, (int)userBlockSize[0]);
	}
	
	public DataProductsReader createDataProductsReader() throws RtStpsException {
		return new DataProductsReader(hdfFile);
	}

	public void close() throws RtStpsException {
		
		try {
			try{
				H5.H5Pclose(propertyId);
			} catch(HDF5LibraryException e){

			}
			H5.H5Fclose(hdfFile);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
		
		// clean up any leftover housekeeping
		HDF5Util.cleanup();
	
	}
	


}
