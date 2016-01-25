/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Provides one method to clean up the HDF5 libs possibly open descriptors.
 * If this is not done the interface/library will eventually blow a gasket.
 * Even with careful tracking of all open descriptors it *seems* that inevitably
 * there is some descriptors left open someplace, and this can lead to a leak...
 * Which will result in the HDF5 JNI crashing the JVM.   The approach is to walk
 * through the descriptor table and simply call every close function call available
 * since there is no way to tell by the descriptor itself which kind it is...
 * Yuck.  Ok fine, I admit it, it's yucky but it seems to work.
 * 
 * 
 *
 */
public class HDF5Util {
	/**
	 * This cleans up the HDF5 interface by attempting to close all
	 * open descriptors.  It does this by getting the descriptor list
	 * and trying to close each by calling the various H5.XXXclose() 
	 * methods, ignoring any thrown exceptions.
	 * If at the end, it is unable to close the entire library a runtime
	 * exception is thrown.
	 */
	public static void cleanup() {
		// Get total number of open HDF5 identifiers, and force close them all
		int descriptors = H5.getOpenIDCount();
		int status;
		System.out.println("HDF5Util.cleanup(): " + descriptors + " identifier(s) left");
		for(int i = 0; i < descriptors; i++){
			int descriptor = H5.getOpenID(i);
			try {
				status = H5.H5Aclose(descriptor);
			} catch (HDF5Exception e) {
				
			}
			try {
				status = H5.H5Dclose(descriptor);
			} catch (HDF5Exception e) {
				
			}
			try {
				status = H5.H5Fclose(descriptor);
			} catch (HDF5Exception e) {
				
			}
			try {
				status = H5.H5Oclose(descriptor);
			} catch (HDF5Exception e) {
				
			}
			try {
				status = H5.H5Gclose(descriptor);
			} catch (HDF5Exception e) {
				
			}
			try {
				status = H5.H5Pclose(descriptor);
			} catch (HDF5Exception e) {
				
			}
			try {
				status = H5.H5Sclose(descriptor);
			} catch (HDF5Exception e) {
				
			}
			try {
				status = H5.H5Tclose(descriptor);
			} catch (HDF5Exception e) {
				
			}
			
		}

		// Do some manual garbage collection. This helps with freeing up resources
		try{
			H5.H5garbage_collect();
		}
		catch(HDF5Exception e){
			System.out.println("HDF5Util.cleanup(): Unable to do manual garbage collection.");
		}

		// Then, close the HDF5 Library. Throw an exception if close fails.
		try{
			status = H5.H5close();
		} 
		catch(HDF5Exception e){
			throw new RtStpsRuntimeException(e);
		}
		
	}
	
	public static void openLibrary(){
		try{
			// Apparently, closing the HDF5 Library when it's not open yet
			// causes ugly HDF5-DIAG errors. Probably shouldn't do this...
			//try{
			//	H5.H5close();
			//}
			//catch(HDF5LibraryException e){
			//	System.out.println("HDF5 Library not yet open");
			//}

			H5.H5open();
			int openIDs = H5.getOpenIDCount();
			System.out.println("H5.getOpenIDCount(): " + openIDs);
		}
		catch(HDF5LibraryException e){
			System.out.println("ERROR! Could not open HDF5 Library!");
			throw new RtStpsRuntimeException(e);
		}
	}
}
