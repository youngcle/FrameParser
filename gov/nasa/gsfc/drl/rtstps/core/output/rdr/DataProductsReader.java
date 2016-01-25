/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.util.Iterator;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Supports the reading of RDR files /Data_Products group using an <code>Iterator</code>.
 * The iterator returns a {@link RDRProduct} for each specific RDR found in 
 * the group.  The HDF file is assumed to have been been opened outside
 * this class.
 * 
 * 
 *
 */
public class DataProductsReader implements Iterator<RDRProduct> {
	private int dataProductsGroup;
	private long numObjects;
	private long counter = 0;
	private String[] names;
	private RDRName[] rdrNames;
	
	/**
	 * Open the /Data_Products group of the HDF file specified in the input argument as a handle
	 * to an already open HDF file.
	 * 
	 * @param hdfFile a handle the already open HDF file
	 * @throws RtStpsException Wraps any HDF library exceptions in an RtStpsException
	 */
	public DataProductsReader(int hdfFile) throws RtStpsException {
		// open the Data_Products group and get the number of RDR_alls in it...
		long[] numObjects = new long[1];
		try {
			dataProductsGroup = H5.H5Gopen(hdfFile, "/Data_Products");
		
			H5.H5Gget_num_objs(dataProductsGroup, numObjects);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		//System.out.println("Opened /Data_Products, number of object: " + numObjects[0]);
		
		this.numObjects = numObjects[0];
		
		finish();
	}
	
	/**
	 * Alternate constructor uses a pre-existing "Data_Products" ID instead of the hdf file id,
	 * the fake 'useProductsHandle' is just here to differentiate the two constructors
	 * @param dataProductsHandle already opened HDF handle to the "Data_Products" area
	 * @param useProductsHandle value is ignored but differentiates constructors
	 * @throws RtStpsException Wraps any HDF library exceptions in an RtStpsException
	 */
	public DataProductsReader(int dataProductsHandle, boolean useProductsHandle) throws RtStpsException {
		// open the All_Data group and get the number of RDR_alls in it...
		this.dataProductsGroup = dataProductsHandle;
		
		long[] numObjects = new long[1];
		
		try {
			H5.H5Gget_num_objs(dataProductsGroup, numObjects);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		
		this.numObjects = numObjects[0];
	

		finish();
		
	}
	
	/**
	 * Common to both constructors
	 * @throws RtStpsException Wraps any HDF library exceptions in an RtStpsException
	 */
	private void finish() throws RtStpsException {
	
		// get the names of the items in the Data_Products
		names = getNames();
		
		// convert them to RDRNames, these will be used in the Iterator
		rdrNames = new RDRName[(int)numObjects];
		
		for (int i = 0; i < (int)numObjects; i++) {

			//System.out.println("Convert name : " + names[i]);
			rdrNames[i] = RDRName.fromRDRNameStr(names[i]);
		}
	
		
	}
	
	/**
	 * Given the string name of the RDR DataProduct, return its specific RDRProduct if it can be found.
	 * This is an alternative to the Iterator interface.
	 * @param rdrProductName an RDR DataProduct name like SPACECRAFT-DIARY-RDR
	 * @return RDRProduct the designated product or null if it not found
	 * @throws RtStpsException wraps any HDF library exceptions in an RtStpsException
	 */
	public RDRProduct getRDRDataProductByName(String rdrProductName) throws RtStpsException {
		for (long i = 0L; i < numObjects; i++) {
			if (names[(int)i].equals(rdrProductName)) {
				RDRName rdrName = RDRName.fromRDRNameStr(rdrProductName);
				RDRProduct rdrProduct = null;
				
				rdrProduct = new RDRProduct(dataProductsGroup, rdrName);
				
				return rdrProduct;
			}
		}
		return null;
	}
	
	/**
	 * Close the data products HDF group handle
	 * @throws RtStpsException wraps any HDF library exceptions in an RtStpsException
	 */
	public void close() throws RtStpsException {
		try {
			H5.H5Gclose(dataProductsGroup);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
	}

	/**
	 * Returns the number of items found the /Data_Products area.
	 * @return an <code>int</code> count of items
	 */
	public int getCount() {
		return (int)this.numObjects;
	}
	
	/**
	 * Return the string array of the name of the items found in the /Data_Products area.
	 * @return a <code>String</code> array consisting of the names found in the /Data_Products area
	 * @exception throws an {@link RtStpsRuntimeException} exception if it any HDF error occurs
	 */
	public String[] getNames() {
		
		String names[] = new String[(int)numObjects];
		
		try {

			String name[] = new String[1];
			
			for (long i = 0; i < numObjects; i++) {
				
				H5.H5Gget_objname_by_idx(dataProductsGroup, i, name, 256L );
				//long size = ;
				names[(int)i] = new String(name[0]);  // copy it just to make sure it doesn't get obliterated somehow... 
			}
			
		} catch (HDF5LibraryException e) {
			throw new RtStpsRuntimeException(e);
		} 
		
		return names;
	}


	/**
	 * Determine if there are more /Data_Product items to read
	 * @return true if there are, false if not
	 */
	@Override
	public boolean hasNext() {
		return (counter < numObjects);
	}


	/**
	 * Get the next RDRProduct, item from the /Data_Product area.
	 * @return the next item as an {@link RDRProduct}
	 * @exception throws an {@link RtStpsRuntimeException} exception if it any HDF error occurs
	 */
	@Override
	public RDRProduct next() {
		
		RDRProduct rdpr = null;
	
		try {	
			rdpr = new RDRProduct(dataProductsGroup, rdrNames[(int)counter]);
		
		} catch (RtStpsException e) {
			throw new RtStpsRuntimeException(e);
		} 
		
		++counter;
		
		return rdpr;
	}


	@Override
	/** 
	 * Not implemented
	 * @exception throws an {@link RtStpsRuntimeException} exception if called
	 */
	public void remove() {
		throw new RtStpsRuntimeException("Not implemented or supported");
	}


	
	
}
