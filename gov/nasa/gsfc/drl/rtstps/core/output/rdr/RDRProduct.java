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
 * Create an RDR Product in the /Data_Products and provide methods to write the {@link Aggregate} and {@link Granule} or it can be used
 * read a pre-existing RDR Product and its Aggregate, and any Granules in it.  Implements the Iterator interface for this purpose.
 * 
 * Basically a class that keeps the first/last granule and the aggregate for convenience
 */
public class RDRProduct implements Iterator<CommonDataSetObject> {
	private int hdfFile;
	private int rootGroup;
	private int rdrGroup;
	private RDRName rdrName;
	private Sensor sensor;
	private Collection collection;
	private DataSetType dataSetType;
	private FixedDomainDescription processingDomain;
	private Aggregate aggregate;
	private Granule firstGranule = null;
	private Granule lastGranule = null;
		
	private long granuleCount = 0L;
	
	private long numObjects = 0L;
	private long counter = 0L;
	
	/**
	 * Constructor for creating the specific RDR DataProduct, note this is package private
	 * so it must be used by a another class in this package to create instances of it. {@link DataProducts}
	 * @param hdfFile the HDF file handle
	 * @param rootGroup the "Data_Product" handle
	 * @param rdrName the RDR name of the product like "VIIRS_SCIENCE_RDR"
	 * @param sensor the sensor
	 * @param collection the collection
	 * @param dataSetType the dataSetType
	 * @param processingDomain the processing domain
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	 RDRProduct(int hdfFile,
				int rootGroup,
				RDRName rdrName,
				Sensor sensor, 
				Collection collection, 
				DataSetType dataSetType, 
				FixedDomainDescription processingDomain ) throws RtStpsException {
		
		this.hdfFile = hdfFile;
		this.rootGroup = rootGroup;
		this.rdrName = rdrName;
		this.sensor = sensor;
		this.collection = collection;
		this.dataSetType = dataSetType;
		this.processingDomain = processingDomain;
		
		// create the group for the RDR itself
		String name = "/Data_Products/" + rdrName.getRDRStringName();
		//System.out.println("Name = " + name);
		
		try {
			rdrGroup = H5.H5Gcreate(hdfFile, name, 0);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 

		
		writeAttributes();
		
	}
	
	/**
	 * Constructor for reading the contents of a pre-existing RDR product.  Give the name of product
	 * which must match in the Data_Products.   Basically trying open "/Data_Products/SPACECRAFT-DIARY-RDR"
	 * @param dataProductHandle the "/Data_Products" handle
	 * @param rdrName the name of the RDR that should be in the /Data_Products, if not an exceptions it thrown
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	public RDRProduct(int dataProductHandle, RDRName rdrName) throws RtStpsException {
		this.rdrName = rdrName;
		
		//System.out.println("Trying to open: " + rdrName.getRDRStringName());
		
		try {
			rdrGroup = H5.H5Gopen( dataProductHandle, rdrName.getRDRStringName() );
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		
		//if (rdrGroup < 0) {
		//	System.out.println("Failed to open " + rdrName.getRDRStringName());
		//} else {
		//	System.out.println("Success in opening " + rdrName.getRDRStringName());
		//}
		
		readAttributes();
						
		long[] numObjects = new long[1];
		
		try {
			H5.H5Gget_num_objs(rdrGroup, numObjects);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
		
		this.numObjects = numObjects[0];
		this.granuleCount = this.numObjects - 1L;
		
		//System.out.println("Found the number of granules to read there is: " + granuleCount);
	}
	
	/**
	 * Read the aggregate off the HDF file and return the Aggregate object populate
	 * with the info held there.  This is just a short-cut to the Iterator below.
	 * @return a Aggregate object
	 * @throws RtStpsException HDF exceptions are wrapped here
	 */
	public Aggregate readAggregate() throws RtStpsException {
		return Aggregate.factory(rdrGroup, rdrName);
	}
	
	/**
	 * Write the Aggregate associated with this RDR Product
	 * @param aggregate the {@link Aggregate} of interest
	 * @throws RtStpsException wraps any HDF exception
	 */
	public void write(Aggregate aggregate) throws RtStpsException  {
		this.aggregate = aggregate;
		aggregate.write(hdfFile);
		aggregate.close();
	}
	
	/**
	 * Write a Granule associated with this RDR Product
	 * @param granule the {@link Granule}
	 * @throws RtStpsException wraps any HDF exception
	 */
	public void write(Granule granule) throws RtStpsException {
		if (firstGranule == null) {
			firstGranule = granule;
		}
		lastGranule = granule;
		System.out.println("in write granule in RDR product.");
		granule.write(hdfFile, rootGroup, rdrGroup);
		
		granule.close();
		
		++granuleCount;
	}
	
	/**
	 * Return the Instrument_Short_Name
	 * @return a {@link Sensor}
	 */
	public Sensor getInstrument_Short_Name() {
		return getSensor();
	}
	
	/**
	 * Return the N_Collection_Short_Name
	 * @return a {@link Collection}
	 */
	public Collection getN_Collection_Short_Name() {
		return getCollection();
	}
	
	/**
	 * Return the N_Dataset_Type_Tag
	 * @return a {@link DataSetType}
	 */
	public DataSetType getN_Dataset_Type_Tag() {
		return getDataSetType();
	}
	
	/**
	 * Return the N_Processing_Domain
	 * @return a {@link FixedDomainDescription}
	 */
	public FixedDomainDescription getN_Processing_Domain() {
		return getProcessingDomain();
	}
	
	/**
	 * Return the RDRName
	 * @return an {@link RDRName}
	 */
	public RDRName getRDRName() {
		return rdrName;
	}
	
	/**
	 * This is intended to return the RDR name used in the HDF sub-tree which will be something like: SPACECRAFT-DIARY-RDR which is constructed
	 * from an RDRName type/class object.
	 * @return a String like SPACECRAFT_DIARY_RDR
	 */
	public String getMetaRDRName() {
		
		return rdrName.getRDRStringName();
	}
	/**
	 * Return the Sensor
	 * @return a {@link Sensor}
	 */
	public Sensor getSensor() {
		return sensor;
	}
	
	/**
	 * Return the Collection
	 * @return a {@link Collection}
	 */
	public Collection getCollection() {
		return collection;
	}
	
	/**
	 * Return the DataSetType
	 * @return a {@link DataSetType}
	 */
	public DataSetType getDataSetType() {
		return dataSetType;
	}
	
	/**
	 * Return the FixedDomainDescription
	 * @return a {@link FixedDomainDescription}
	 */
	public FixedDomainDescription getProcessingDomain() {
		return processingDomain;
	}
	
	/**
	 * Return the Aggregate associated with this RDR Product
	 * @return the {@link Aggregate}
	 */
	public Aggregate getAggregate() {
		return aggregate;
	}
	
	/**
	 * Return the first GranuleId for the first Granule in this RDR Product
	 * @return a {@link GranuleId} for the first Granule
	 */
	public GranuleId getBeginningGranuleId() {
		if (firstGranule == null) return null;
		return firstGranule.getGranuleId();
	}
	
	/**
	 * Return the last GranuleId for the last Granule in this RDR Product
	 * @return a {@link GranuleId} for the last Granule
	 */
	public GranuleId getEndingGranuleId() {
		if (lastGranule == null) return null;
		return lastGranule.getGranuleId();
	}
	
	/**
	 * Return the beginning orbit
	 * @return a <code>long</code> containing the orbit
	 */
	public long getBeginningOrbit() {
		return firstGranule.getOrbit();
	}
	
	/**
	 * Return the ending orbit
	 * @return a <code>long</code> containing the orbit
	 */
	public long getEndingOrbit() {
		return lastGranule.getOrbit();
	}
	
	/**
	 * Return the beginning date of the first observation time (interpreted as the first packet with a time stamp)
	 * @return a {@link PDSDate} containing the packet time
	 */
	public PDSDate getBeginningDateTime() {
		return firstGranule.getBeginningObservationDateTime();
	}
	
	/**
	 * Return the ending date of the first observation time (interpreted as the last packet with a time stamp)
	 * @return a {@link PDSDate} containing the packet time
	 */
	public PDSDate getEndingDateTime() {
		return lastGranule.getEndingObservationDateTime();
	}

	//TEST: Required to get beginning/ending IET times...
	public PDSDate getBeginningTimeDateTime(){
		return firstGranule.getBeginningTimeDateTime();
	}
	public PDSDate getEndingTimeDateTime(){
		return lastGranule.getEndingTimeDateTime();
	}
	
	/**
	 * Return the number of granules in the RDR Product
	 * @return an <code>int</code> containing the count
	 */
	public long getGranuleCount() {
		return granuleCount;
	}

	/**
	 * Close the HDF group for this RDR Product
	 * @throws RtStpsException wraps any HDF exception
	 */
	public void close() throws RtStpsException {
		try {
			H5.H5Gclose(rdrGroup);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * Write attributes to the RDR Product
	 * @throws RtStpsException wraps any HDF exception
	 */
	private void writeAttributes() throws RtStpsException {

		HDFAttribute.writeString(rdrGroup, "Instrument_Short_Name", sensor.toString());

		HDFAttribute.writeString(rdrGroup, "N_Collection_Short_Name", collection.toString());

		HDFAttribute.writeString(rdrGroup, "N_Dataset_Type_Tag", dataSetType.toString());

		HDFAttribute.writeString(rdrGroup, "N_Processing_Domain", processingDomain.toString());
	}
	
	/**
	 * Read the attributes associated with the RDR Product 
	 * @throws RtStpsException wraps any HDF exception
	 */
	private void readAttributes() throws RtStpsException {

       
		String sensorStr = HDFAttribute.readString(rdrGroup, "Instrument_Short_Name"); //, sensor.toString());
	
		String collectionStr = HDFAttribute.readString(rdrGroup, "N_Collection_Short_Name"); //, collection.toString());

		String dataTypeSetStr = HDFAttribute.readString(rdrGroup, "N_Dataset_Type_Tag"); //, dataSetType.toString());

		String processingDomainStr = HDFAttribute.readString(rdrGroup, "N_Processing_Domain"); //, processingDomain.toString());
		
		System.out.println("Found attributes: " + sensorStr + " " + collectionStr + " " + dataTypeSetStr + " " + processingDomainStr);

		if (sensorStr != null) {
			sensor = Sensor.valueOf(sensorStr);
		} else {
			System.out.println("Failed to read Instrument_Short_Name");
		}
		
		if (collectionStr != null) {
			collection = Collection.myValueOf(collectionStr);
		} else {
			System.out.println("Failed to read N_Collection_Short_Name");
		}
		
		if (dataTypeSetStr != null) {
			dataSetType = DataSetType.valueOf(dataTypeSetStr);
		} else {
			System.out.println("Failed to read N_Dataset_Type_Tag");
		}
		
		
		if (processingDomainStr != null) {
			if (processingDomainStr.equals("int")) {  //FIXME
				processingDomain = FixedDomainDescription.iAndt;
			} else {
				processingDomain = FixedDomainDescription.valueOf(processingDomainStr);
			}
		} else {
			System.out.println("Failed to read N_Processing_Domain");
		}
		
	}
	

	/**
	 * Return true or false if there are more items in the RDR Product to read
	 * @return true or false
	 */ 
	@Override
	public boolean hasNext() {
		return (counter < numObjects);
	}

	/**
	 * Return either the next Granule or the Aggregate in a common object
	 * @return either a granule or the aggregate as a {@link CommonDataSetObject}
	 */
	@Override
	public CommonDataSetObject next() {
		CommonDataSetObject ga = null;
		
		try {
			String name[] = new String[1];

			long size = H5.H5Gget_objname_by_idx(rdrGroup, counter, name, 256L ) ;
			
			//System.out.println("I have found yea: " + name[0] + " Size: " + size);
			
			if (name[0].contains("Gran")) {
				ga = Granule.factory(rdrGroup, name[0]);
			} else {
				ga = Aggregate.factory(rdrGroup, name[0]);
			}
			
		} catch (HDF5LibraryException e) {
			throw new RtStpsRuntimeException(e);
		} catch (RtStpsException e) {
			throw new RtStpsRuntimeException(e);
		} 
		
		++counter;
		
		return ga;
	}

	@Override
	public void remove() {
		throw new RtStpsRuntimeException("Not implemented or supported.");
	}

	/**
	 * Get the granule designated by the granuleNumber. 
	 * @param granuleNumber  the granule with that number for example XXX-SCIENCE-RDR_Gran_0 would be retrieved if zero is given here
	 * @return the granule or null
	 * @throws RtStpsException 
	 */
	public Granule getGranule(int granuleNumber) throws RtStpsException {
		if (granuleNumber < 0) {
			throw new RtStpsRuntimeException("Illegal negative granule number");
		}
		if (granuleNumber >= this.granuleCount) {
			throw new RtStpsRuntimeException("Illegal granule number [" + granuleNumber + "], not enough granules [" + this.granuleCount + "]");
		}
		Granule ga = Granule.factory(rdrGroup, Granule.makeName(rdrName, granuleNumber));
		
		return ga;
	}

	public String toString() {
		return "Instrument Short Name: " + 
		this.getInstrument_Short_Name() + 
		" Collection Short Name: " + 
		this.getN_Collection_Short_Name() + 
		" Dataset Type Tag: " + 
		this.getN_Dataset_Type_Tag() + 
		" Instrument Short Name: " + 
		this.getN_Processing_Domain();
	}



}
