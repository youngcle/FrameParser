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
 * Open a RDR_All such as CRIS-SCIENCE-RDR_All in an HDF file
 * Return RawApplicationPacket at the specified index if its in range.
 *
 */
class RDRAllRandomAccessReader {
	private int rootGroup;
	private long numObjects;
	private long counter = 0;
	private String rdrNameStr;
	private RDRName rdrName;
	
	/**
	 * Construct an RDR reader, this includes the "All" at the end of the name.  Note
	 * this is package private and must be called by another class 
	 * @param group the RDR_All HDF group of interest
	 * @param rdrNameAll the name of the RDR in a String, includes the "All" in the name, i.e. "CRIS-SCIENCE-RDR_All"
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	RDRAllRandomAccessReader(int group, String rdrNameAll) throws RtStpsException {
		this.rdrName = RDRName.fromRDRNameStr(rdrNameAll.substring(0, rdrNameAll.indexOf('_')));
		
		open(group, rdrNameAll);
	}
	
	/**
	 * Construct an RDR reader, using RDRName object
	 * @param group the RDR_All HDF group of interest
	 * @param rdrName the RDR name of interest as an {@link RDRName}
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	RDRAllRandomAccessReader(int group, RDRName rdrName) throws RtStpsException {
		this.rdrName = rdrName;
		open(group, rdrName.getRDRStringName() + "_All");
	}
	
	/**
	 * Construct an RDR reader, using RDRName object and an already open group.
	 * This is used only in the dereference case from Aggregate.  The flag is not checked,
	 * just its presence.
	 * @param group the RDR_All HDF group of interest
	 * @param rdrName the RDR name of interest as an {@link RDRName}
	 * @param doNotOpen a way to differentiate this constructor, the value is not used
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	RDRAllRandomAccessReader(int group, RDRName rdrName, boolean doNotOpen) throws RtStpsException {
		this.rdrName = rdrName;
		this.rootGroup = group;
		long[] numObjects = new long[1];
		try {
			H5.H5Gget_num_objs(this.rootGroup, numObjects);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		
		this.numObjects = numObjects[0];
		
	}
	
	/**
	 * Open the RDR_All group
	 * @param group the RDR_All HDF group of interest
	 * @param rdrName the fully constructed RDR name
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	private void open(int group, String rdrNameStr) throws RtStpsException {
		System.out.println("Opening " + rdrNameStr);
		this.rdrNameStr = rdrNameStr;
		try {
			this.rootGroup = H5.H5Gopen(group, rdrNameStr);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e.getMinorError(e.getMinorErrorNumber()), e);
		} 
		
		long[] numObjects = new long[1];
		
		try {
			H5.H5Gget_num_objs(this.rootGroup, numObjects);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		
		this.numObjects = numObjects[0];

		
		//System.out.println("Objects == " + this.numObjects);
	}

	
	/**
	 * Close the RDR_All group
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	public void close() throws RtStpsException {
		try {
			H5.H5Gclose(rootGroup);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
	}


	/**
	 * Return the number of objects (RawApplicationPackets) in the RDR_All
	 * @return the size (number of objects)
	 */
	public int size() {
		return (int)numObjects;
	}


	/**
	 * Return the RawApplicationPacket item at the specific index in the RDR_All if it exists or null if it does not
	 * @return the next {@link RawApplicationPackets} or null
	 */
	public RawApplicationPackets get(int index) {
		
		if ((index < 0) || (index >= numObjects)) {
			return null;
		}
		RawApplicationPackets rap = null;
		
		try {
			rap = RawApplicationPacketsFactory.makeRandomAccess(rdrName, rootGroup, index);
		} catch (RtStpsException e) {
			throw new RtStpsRuntimeException(e);
		}

		
		
		return rap;
	}

	@Deprecated
	public String getRDRName() {
		// FIXME this should get the name from the HDF object..
		// FIXME FIXME -- this should be the RDR name but in some cases it the actual name used in the HDF sub-tree which is a variation
		// FIXME FIXME -- and this depends on which constructor is called...
		// FIXME FIXME -- in fact looking at the code this seems to only be set when the constructor converts the RDRName and adds the "_All" to the end
		// FIXME FIXME -- which means this method should probably return the actual RDRName and another method return the name used in the HDF metatree
		return rdrNameStr;
	}
	
	/**
	 * This is intended to return the name of the thing used in the HDF substree which will be something like: SPACECRAFT-DIARY-RDR_All
	 * Where the getRDRName() should just return a RDRName object.  This is mirrored on the RDRProduct side.
	 * @return String that has the RDR name used in the HDF subtree
	 */
	public String getMetaRDRName() {
		
		// FIXME see above
		return rdrNameStr;
	}

	
}
