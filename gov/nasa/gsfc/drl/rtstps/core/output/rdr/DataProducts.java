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
 * Creates the Data_Products (/Data_Products) area of the HDF data structure in the HDF/RDR file, once created
 * this class has a factory method for creating specific {@link RDRProduct}
 * instances.
 * 
 * 
 *
 */
public class DataProducts {
	private int hdfFile;
	private int dataProductGroup;
	
	/**
	 * Create the /Data_Products area of the HDF/RDR file.
	 * @param hdfFile the input handle for the HDF file
	 * @throws RtStpsException wraps any HDF library exceptions
	 */
	public DataProducts(int hdfFile) throws RtStpsException {
		this.hdfFile = hdfFile;
		//The Data_Products
		try {
			dataProductGroup = H5.H5Gcreate(hdfFile, "/Data_Products", 0);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
	}
		
	/**
	 * Factory method for making RDRProduct instances once the /Data_Products area has been created.
	 * This then allows the creation of Sensor-XXX-RDR constructs: aggregates and granules.
	 * @param rdrName the RDR name
	 * @param sensor the sensor
	 * @param collection the collection
	 * @param processingDomain the processing domain (Dev)
	 * @return an RDRProduct instance
	 * @throws RtStpsException wraps any HDF library exceptions
	 */
	public RDRProduct createRDRProduct(RDRName rdrName, Sensor sensor, 
										Collection collection, 
										FixedDomainDescription processingDomain) throws RtStpsException  {
		
		return new RDRProduct(hdfFile, dataProductGroup, rdrName, sensor, collection, DataSetType.RDR, processingDomain);
	}
	
	/**
	 * Return the /Data_Products HDF handle
	 * @return the handle as an <code>int</code>
	 */
	public int getDataProductsHandle() {
		return dataProductGroup;
	}
	
	/**
	 * Close the HDF /Data_Products group handle
	 * @throws RtStpsException wraps any HDF library exceptions
	 */
	public void close() throws RtStpsException {
		try {
			H5.H5Gclose(dataProductGroup);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
	}

	
	
}
