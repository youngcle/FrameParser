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
 * Supports the reading of RDR files /All_Data group using an <code>Iterator</code>.
 * The iterator returns a {@link RDRAllReader} for each specific RDR found in 
 * the group.  The HDF file is assumed to have been been opened outside
 * this class.
 * 
 * 
 *
 */
public class AllDataReader implements Iterator<RDRAllReader> {
	private int allDataGroupHandle;
	private long numObjects;
	private long counter = 0;
	
	
	/**
	 * Open the /All_Data group of the HDF file specified in the input argument as a handle
	 * to an already open HDF file.
	 * 
	 * @param hdfFile a handle the already open HDF file
	 * @throws RtStpsException Wraps any HDF library exceptions in an RtStpsException
	 */
	public AllDataReader(int hdfFile) throws RtStpsException {
		
		try {
			allDataGroupHandle = H5.H5Gopen(hdfFile, "/All_Data/");
		
			long[] numObjects = new long[1];
		
			H5.H5Gget_num_objs(allDataGroupHandle, numObjects);
		
			this.numObjects = numObjects[0];
		
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
	}
	
	/**
	 * Alternate constructor that takes as input an already open /All_Data handle instead of the HDF root handle.
	 * To differentiate the two constructors a fake 'useAllData' argument is must be given to differentiate the two constructors.
	 * @param allDataGroupHandle already opened HDF handle to the /All_Data area of an RDR file
	 * @param useAllData value is ignored but differentiates constructors
	 * @throws RtStpsException Wraps any HDF library exceptions in an RtStpsException
	 */
	public AllDataReader(int allDataGroupHandle, boolean useAllData) throws RtStpsException {
		try {
			this.allDataGroupHandle = allDataGroupHandle;
		
			long[] numObjects = new long[1];
		
			H5.H5Gget_num_objs(allDataGroupHandle, numObjects);
		
			this.numObjects = numObjects[0];
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
	}
	

	
	/**
	 * Return an RDR All_Data reader ({@link RDRAllReader}) for the specified RDR in the /All_Data if it exists.
	 * @param rdrName the name of the RDR of interest
	 * @return an instance of <code>RDRAllReader</code> for the named RDR if it exists in the /All_Data
	 * @throws RtStpsException Wraps any HDF library exceptions, including if the specified RDR does not exist.
	 */
	public RDRAllReader getRDRAllReaderByRDRName(RDRName rdrName) throws RtStpsException {
		return new RDRAllReader(allDataGroupHandle, rdrName);
	}
	
	/**
	 * Close the <code>RDRAllReader</code>
	 * @throws RtStpsException Wraps any HDF library exceptions
	 */
	public void close() throws RtStpsException {
		try {
			H5.H5Gclose(allDataGroupHandle);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
	}

	/**
	 * Return the number of items in the group.
	 * @return an <code>int</code> count of the number of items
	 */
	public int getCount() {
		return (int)this.numObjects;
	}
	
	/**
	 * Return the names of the items found in the group
	 * @return a string array containing the names in the group
	 * @throws RtStpsException Wraps any HDF library exceptions
	 */
	public String[] getNames() throws RtStpsException {
		
		String names[] = new String[(int)numObjects];
		
		try {

			String name[] = new String[1];
			
			for (long i = 0; i < numObjects; i++) {
				
				H5.H5Gget_objname_by_idx(allDataGroupHandle, i, name, 256L );
				//long size = ;
				names[(int)i] = new String(name[0]);  // copy it just to make sure it doesn't get obliterated somehow... 
			}
			
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		
		return names;
	}

	
	/**
	 * Return if there is another item in the /All_Data
	 * @return true if here is another item, false if there is not
	 */
	@Override
	public boolean hasNext() {
		return (counter < numObjects);
	}


	/**
	 * Return the next {@link RDRAllReader}
	 * @return an instance of RDRAllReader
	 */
	@Override
	public RDRAllReader next() {
		
		RDRAllReader rar = null;
		try {
			String name[] = new String[1];

			H5.H5Gget_objname_by_idx(allDataGroupHandle, counter, name, 256L ) ;
			
			//long size = ; System.out.println("I have found yea: " + name[0] + " Size: " + size);
			
			rar = new RDRAllReader(allDataGroupHandle, name[0]);
			
		} catch (HDF5LibraryException e) {
			throw new RtStpsRuntimeException(e);
		} catch (RtStpsException e) {
			throw new RtStpsRuntimeException(e);
		}
		
		++counter;
		
		return rar;
	}

	/**
	 * Similar to next but specialized for the RandomAccess reader.  It still internally
	 * increments the next counter for the Iterator interface.  This is a special case used
	 * for certain applications and not visible as public
	 * Return the next {@link RDRAllRandomAccessReader}
	 * @return an instance of RDRAllRandomAccessReader
	 */
	RDRAllRandomAccessReader nextRandomAccess() {
		
		RDRAllRandomAccessReader rar = null;
		try {
			String name[] = new String[1];

			H5.H5Gget_objname_by_idx(allDataGroupHandle, counter, name, 256L ) ;
			
			//long size = ; System.out.println("I have found yea: " + name[0] + " Size: " + size);
			
			rar = new RDRAllRandomAccessReader(allDataGroupHandle, name[0]);
			
		} catch (HDF5LibraryException e) {
			throw new RtStpsRuntimeException(e);
		} catch (RtStpsException e) {
			throw new RtStpsRuntimeException(e);
		}
		
		++counter;
		
		return rar;
	}
	

	/**
	 * Not supported
	 * @throws RtStpsRuntimeException
	 */
	@Override
	public void remove() {
		throw new RtStpsRuntimeException("Not implemented or supported");	
	}


	
	
}
