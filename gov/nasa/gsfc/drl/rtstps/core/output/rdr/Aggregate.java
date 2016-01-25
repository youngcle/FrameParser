/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Create or read the <code>Aggregrate</code> metadata from an RDR file.  The RDR file is specified
 * by an input handle, and assumed to be opened outside of this class.  An Aggregate consists of certain
 * attributes that contain among other things {@link Granule} start/stop times, counts and other items.  The Aggregate
 * itself references the items in the HDF and is defined the JPSS metadata documentation.
 * 
 * 
 *
 */
public abstract class Aggregate extends CommonDataSetObject {
	
	private int dataSpace = -1;
	private int dataSet = -1;
	//private int rdrAllGroup  = -1; // for deref only
	private GranuleId beginningGranuleId;
	private GranuleId endingGranuleId;
	private long beginningOrbit;
	private long endingOrbit;

	private RDRName rdrReferenceName;
	
	private String aggregateName; 
	private long granuleCount;
	
	private String begTimeStr;  // for writing these fields are constructed from input object, for read they contain the contents of attributes
	private String endDateStr;
	private String endTimeStr;
	private String begDateStr;
	
	private PDSDate beginningDateTime;  // these items are constructed internally from the attributes for reading
	private PDSDate endingDateTime;

	
	/**
	 * Constructor for reading a pre-existing <code>Aggregate</code>.
	 * @param rdrGroup  the RDR group on the HDF the Aggregrate is in such as /Data_Products/XXX-RDR
	 * @param aggregateName the aggregate name in the group like: SPACECRAFT-DIARY-RDR_Aggr
	 * @throws RtStpsException 
	 */
	public Aggregate(int rdrGroup, String aggregateName) throws RtStpsException {
		
		this.aggregateName = aggregateName;
		String[] aggFields = aggregateName.split("_");
		this.rdrReferenceName = RDRName.fromRDRNameStr(aggFields[0]);
		
		// System.out.println("Aggregate RDRName  = " + rdrReferenceName);
		
		try {
			dataSet = H5.H5Dopen(rdrGroup, aggregateName);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		
		//System.out.println("Aggregate Dopen: " + dataSet);
		
		readAttributes();
	}
	

	/**
	 * Constructor for reading a pre-existing <code>Aggregate</code>.
	 * @param rdrGroup  the RDR group on the HDF the Aggregrate is in such as /Data_Products/XXX-RDR
	 * @param rdrName the rdr name like: SPACECRAFT-DIARY-RDR
	 * @throws RtStpsException 
	 */
	public Aggregate(int rdrGroup, RDRName rdrName) throws RtStpsException {
		
		this.rdrReferenceName = rdrName;
		this.aggregateName = rdrReferenceName.getRDRStringName() + "_Aggr";
		
		//System.out.println("AggregateName = " + aggregateName);
		
		try {
			dataSet = H5.H5Dopen(rdrGroup, aggregateName);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		
		//System.out.println("Aggregate Dopen: " + dataSet);
		
		readAttributes(); 
	}
	
	

	/**
	 * Constructor for creating a new <code>Aggregate</code> in the RDR.  It takes several arguments.
	 * 
	 * @param beginningGranuleId  The first <code>Granule</code> that constitutes this <code>Aggregate</code>
	 * @param endingGranuleId  The last <code>Granule</code> that constitutes this <code>Aggregate</code>
	 * @param beginningOrbit The first orbit that constitutes this <code>Aggregate</code>
	 * @param endingOrbit The last orbit that constitutes this <code>Aggregate</code>
	 * @param beginningDateTime The beginning date and time of the <code>Granules</code> in spacecraft time
	 * @param endingDateTime The ending date and time of the <code>Granules</code> in spacecraft time
	 * @param granuleCount The number of <code>Granules in the <code>Aggregate</code>
	 * @param rdrReferenceName The RDR name associated with this <code>Aggregate</code>
	 */
	public Aggregate(GranuleId beginningGranuleId,
			 		GranuleId endingGranuleId,
			 		long beginningOrbit,
			 		long endingOrbit,
			 		PDSDate beginningDateTime,
					PDSDate endingDateTime,
					long granuleCount,
					RDRName rdrReferenceName) {
		
		this.beginningGranuleId = beginningGranuleId;
		this.beginningOrbit = beginningOrbit;
		this.endingGranuleId = endingGranuleId;
		this.endingOrbit = endingOrbit;
		
		// FIXME: Should filename timestamps be granule start/stop times, or observation start/stop times?
		//this.beginningDateTime = LeapDate.removeLeap(beginningDateTime);
		//this.endingDateTime = LeapDate.removeLeap(endingDateTime);
		this.beginningDateTime = beginningDateTime;
		this.endingDateTime = endingDateTime;		


		this.begDateStr = TimeFormat.formatPDSDate(this.beginningDateTime).toString();
		this.begTimeStr = TimeFormat.formatPDSTime(this.beginningDateTime).toString();
		
		this.endDateStr = TimeFormat.formatPDSDate(this.endingDateTime).toString();
		this.endTimeStr = TimeFormat.formatPDSTime(this.endingDateTime).toString();
		
		this.rdrReferenceName = rdrReferenceName;
		
		this.granuleCount = granuleCount;
	
		this.aggregateName = rdrReferenceName.getRDRStringName() + "_Aggr";
	}
	



	/**
	 * Return the RDR_All RDRAllReader that the reference points to in the Aggregate
	 * @return an RDRAlLReader for the RDR_All
	 */
	public RDRAllReader getReferencedRDRAll() throws RtStpsException {
		
		byte[] ref = new byte[1024];
		
		try {
			H5.H5Dread(dataSet,
						HDF5Constants.H5T_STD_REF_OBJ,
						HDF5Constants.H5S_ALL,
						HDF5Constants.H5S_ALL,
						HDF5Constants.H5P_DEFAULT,
						ref);
			
			
			int rdrAllGroup = H5.H5Rdereference(dataSet, HDF5Constants.H5R_OBJECT, ref);
		
			return new RDRAllReader(rdrAllGroup, rdrReferenceName, false);  // note: rdrAllGroup must be closed by the RDRAllReader method close
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
	
	}
	/**
	 * Returns the beginning <code>Granule</code> identifier
	 * @return the GranuleId
	 */
	public final GranuleId getBeginningGranuleId() {
		return beginningGranuleId;
	}
	
	/**
	 * Returns the beginning orbit
	 * @return the beginning orbit as a long
	 */
	public final long getBeginningOrbit() {
		return beginningOrbit;		
	}
	
	/**
	 * Returns the ending <code>Granule</code> identifier
	 * @return the GranuleId
	 */
	public final GranuleId getEndingGranuleId() {
		return endingGranuleId;
	}
	
	/**
	 * Returns the ending orbit
	 * @return the ending orbit as a long
	 */
	public final long getEndingOrbit() {
		return endingOrbit;		
	}
	
	/**
	 * Returns the beginning date and time of the first <code>Granule</code> in this <code>Aggregate</code> as a SpacecraftAOS PDS formated date and time.
	 * @return returns the date/time in a <code>PDSDate<code>
	 */
	public final PDSDate getBeginningDateTime() {
		return beginningDateTime;		
	}
	
	/**
	 * Returns the formatted date <code>String</code> of the BeginningDateTime as follows: yyyyMMdd
	 * @return the <code>String</code> containing the formatted date
	 */
	public final String getBeginningDateFormatted() {
		return this.begDateStr;		
	}
		
	/**
	 * Returns the formatted time <code>String</code> of the BeginningDateTime as follows: HHmmss.SSS
	 * @return the <code>String</code> contained the formatted time
	 */
	public final String getBeginningTimeFormatted() {
		return this.begTimeStr;		
	}
	
	/**
	 * Returns the end date and time of the last <code>Granule</code> in this <code>Aggregate</code> as a SpacecraftAOS PDS formated date and time.
	 * @return returns the date/time in a <code>PDSDate</code>
	 */
	public final PDSDate getEndingDateTime() {
		return endingDateTime;		
	}
	
	/**
	 * Returns the formatted date <code>String</code> of the EndingDateTime as follows: yyyyMMdd
	 * @return the <code>String</code> containing the formatted date
	 */
	public final String getEndingDateFormatted() {
		return this.endDateStr;		
	}
	
	/**
	 * Returns the formatted time <code>String</code> of the EndingDateTime as follows: HHmmss.SSS
	 * @return the <code>String</code> contained the formatted time
	 */
	public final String getEndingTimeFormatted() {
		return this.endTimeStr;
	}
	
	/**
	 * Returns the <code>Granule</code> count, the number of <code>Granules</code> that make up the <code>Aggregate</code>
	 * @return the <code>Granule</code> count as a long
	 */
	public final long getGranuleCount() {
		return granuleCount;
	}
	
	/**
	 * Write this <code>Aggregrate</code> to the HDF files according to the RDR format.  In particular it
	 * creates the /Data_Products/XXX-XXXX-RDR/XXX-XXXX-RDR_Aggr structure.
	 * 
	 * @param hdfFile the handle from the HDF file to the root of the HDF structure
	 * @throws RtStpsException Wraps any exceptions thrown by the HDF library
	 */
	public void write(int hdfFile) throws RtStpsException {
		
		try {
			String name = rdrReferenceName.getRDRStringName();
		
			long     dims[] = { 1L };

			dataSpace = H5.H5Screate_simple(1, dims, null);
		
			// build an aggregate
			// for example: /Data_Products/VIIRS-SCIENCE-RDR/VIIRS-SCIENCE-RDR_Aggr
			//
			aggregateName = name + "_Aggr";
			String aggrPath =  "/Data_Products/" + name + "/" + aggregateName;  
		
			//System.out.println("AggrPath = " + aggrPath);
			
			// [A]: If dataSet is already initialized to something, close it first!
			if(dataSet > -1){
				try{
					H5.H5Dclose(dataSet);
				} catch (HDF5Exception e){
					System.out.println("Aggregate dataset " + dataSet + " may be closed already...");
				}
			}
			dataSet = H5.H5Dcreate( hdfFile,
						aggrPath,
						HDF5Constants.H5T_STD_REF_OBJ,
						dataSpace,
						HDF5Constants.H5P_DEFAULT );
			
			
			// find the ref to what the aggregate will point to...
			// for example: /All_Data/VIIRS-SCIENCE-RDR_All
			//
			String allPath = "/All_Data/" + name + "_All";
			
			//System.out.println("AllPath = " + allPath);
			
			byte[] ref = H5.H5Rcreate(hdfFile,
							allPath,
							HDF5Constants.H5R_OBJECT,
							-1);
			
			
			
			H5.H5Dwrite(dataSet,
					HDF5Constants.H5T_STD_REF_OBJ,
					HDF5Constants.H5S_ALL,
					HDF5Constants.H5S_ALL,
					HDF5Constants.H5P_DEFAULT,
					ref);
			
			writeAttributes();
		
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} catch (HDF5Exception e) {
			throw new RtStpsException(e);
		}

	}
	
	/**
	 * Write the various attributes associated with the <code>Aggregate</code>
	 * @throws RtStpsException Wraps any exceptions thrown by the HDF library
	 */
	private void writeAttributes() throws RtStpsException {
		HDFAttribute.writeString(dataSet, "AggregateBeginningDate", getBeginningDateFormatted());
		HDFAttribute.writeString(dataSet, "AggregateBeginningGranuleID", beginningGranuleId.toString());
		HDFAttribute.writeULong(dataSet, "AggregateBeginningOrbitNumber", getBeginningOrbit());
		HDFAttribute.writeString(dataSet, "AggregateBeginningTime", getBeginningTimeFormatted());
		HDFAttribute.writeString(dataSet, "AggregateEndingDate", getEndingDateFormatted());
		HDFAttribute.writeString(dataSet, "AggregateEndingGranuleID", endingGranuleId.toString());
		HDFAttribute.writeULong(dataSet, "AggregateEndingOrbitNumber", getEndingOrbit());
		HDFAttribute.writeString(dataSet, "AggregateEndingTime",  getEndingTimeFormatted());
		HDFAttribute.writeULong(dataSet, "AggregateNumberGranules", getGranuleCount());
	}
	
	private void readAttributes() throws RtStpsException {
		
		
		begDateStr = HDFAttribute.readString(dataSet, "AggregateBeginningDate");
		
		String tmp2 = HDFAttribute.readString(dataSet, "AggregateBeginningGranuleID");
		this.beginningGranuleId = new GranuleId(tmp2);
		
		this.beginningOrbit = HDFAttribute.readULong(dataSet, "AggregateBeginningOrbitNumber");
		
		
		begTimeStr = HDFAttribute.readString(dataSet, "AggregateBeginningTime");
		endDateStr = HDFAttribute.readString(dataSet, "AggregateEndingDate");
		
		this.beginningDateTime = TimeFormat.createPDSDateTime(begDateStr, begTimeStr);
		
		String tmp6 = HDFAttribute.readString(dataSet, "AggregateEndingGranuleID");
		this.endingGranuleId = new GranuleId(tmp6);
		
		this.endingOrbit = HDFAttribute.readULong(dataSet, "AggregateEndingOrbitNumber");
		endTimeStr = HDFAttribute.readString(dataSet, "AggregateEndingTime");
		
		this.endingDateTime = TimeFormat.createPDSDateTime(endDateStr, endTimeStr);
		
		this.granuleCount = HDFAttribute.readULong(dataSet, "AggregateNumberGranules");
	}


	/**
	 * Close the RDR/HDF Aggregate structure after calling the write method above.   If this method is not
	 * called the HDF will not be complete and the resulting Aggregate may not be correct or even exist in the
	 * file.
	 * 
	 * @throws RtStpsException Wraps any exceptions thrown by the HDF library
	 */
	public void close() throws RtStpsException {
		try {
			//if (rdrAllGroup >= 0) {  // NOT DONE HERE but in RDRAllReader.close()
			//	H5.H5Gclose(rdrAllGroup);
			//}
			if (dataSpace >= 0) { // write side only
				try{
					H5.H5Sclose(dataSpace);
				} catch(HDF5Exception e){
					System.out.println("Aggregate dataspace " + dataSpace + " may have been closed already.");
				}
			}

			try{
				H5.H5Dclose(dataSet);
			} catch(HDF5Exception e){
				System.out.println("Aggregate dataset " + dataSet + " may have been closed already.");
			}
		} catch (Exception e) {
			System.out.println("Exception occurred during Aggregate close!");
			throw new RtStpsException(e);
		}
	}


	@Override
	public String getName() {
		return aggregateName;
	}


	/**
	 * Factory method for building specific sensor aggregates
	 * @param beginningGranuleId  The first <code>Granule</code> that constitutes this <code>Aggregate</code>
	 * @param endingGranuleId  The last <code>Granule</code> that constitutes this <code>Aggregate</code>
	 * @param beginningOrbit The first orbit that constitutes this <code>Aggregate</code>
	 * @param endingOrbit The last orbit that constitutes this <code>Aggregate</code>
	 * @param beginningDateTime The beginning date and time of the <code>Granules</code> in spacecraft time
	 * @param endingDateTime The ending date and time of the <code>Granules</code> in spacecraft time
	 * @param granuleCount The number of <code>Granules in the <code>Aggregate</code>
	 * @param rdrReferenceName The RDR name associated with this <code>Aggregate</code>
	 * @return an instance of the proper aggregate using the rdrName
	 * @throws RtStpsException thrown if the RDRName is not supported
	 */
	public static Aggregate factory(GranuleId beginningGranuleId,
									GranuleId endingGranuleId, 
									long beginningOrbit,
									long endingOrbit, 
									PDSDate beginningDateTime,
									PDSDate endingDateTime, 
									long granuleCount, 
									RDRName rdrName) throws RtStpsException {
		
		Aggregate retAgg = null;
		if (rdrName == RDRName.VIIRS_Science) {
			retAgg = new VIIRSAggregate(beginningGranuleId, endingGranuleId, beginningOrbit, endingOrbit, beginningDateTime, endingDateTime, granuleCount);
		} else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude) {
			retAgg = new SpacecraftDiaryAggregate(beginningGranuleId, endingGranuleId, beginningOrbit, endingOrbit, beginningDateTime, endingDateTime, granuleCount);
		} else if (rdrName == RDRName.ATMS_Science) {
			retAgg = new ATMSAggregate(beginningGranuleId, endingGranuleId, beginningOrbit, endingOrbit, beginningDateTime, endingDateTime, granuleCount);
		} else if (rdrName == RDRName.CRIS_Science) {
			retAgg = new CRISAggregate(beginningGranuleId, endingGranuleId, beginningOrbit, endingOrbit, beginningDateTime, endingDateTime, granuleCount);
		} else if (rdrName == RDRName.OMPS_LPScience) {
			retAgg = new ROLPSAggregate(beginningGranuleId, endingGranuleId, beginningOrbit, endingOrbit, beginningDateTime, endingDateTime, granuleCount);
		} else if (rdrName == RDRName.OMPS_TCScience) {
			retAgg = new ROTCSAggregate(beginningGranuleId, endingGranuleId, beginningOrbit, endingOrbit, beginningDateTime, endingDateTime, granuleCount);
		} else if (rdrName == RDRName.OMPS_NPScience) {
			retAgg = new RONPSAggregate(beginningGranuleId, endingGranuleId, beginningOrbit, endingOrbit, beginningDateTime, endingDateTime, granuleCount);
		} else {
			throw new RtStpsException("Unable to create aggregate for RDR type [" + rdrName.toString() + "] -- unknown aggregate type");
		}
		
		return retAgg;
	}
	
	/**
	 * Factory method for building specific sensor aggregates
	 * @param rdrGroup  the RDR group on the HDF the Aggregrate is in such as /Data_Products/XXX-RDR
	 * @param rdrName the rdr name like: SPACECRAFT-DIARY-RDR
	 * @throws RtStpsException thrown if the RDRName is not supported
	 */
	public static Aggregate factory(int rdrGroup, RDRName rdrName) throws RtStpsException {

		Aggregate retAgg = null;
		if (rdrName == RDRName.VIIRS_Science) {
			retAgg = new VIIRSAggregate(rdrGroup);
		} else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude) {
			retAgg = new SpacecraftDiaryAggregate(rdrGroup);
		} else if (rdrName == RDRName.ATMS_Science) {
			retAgg = new ATMSAggregate(rdrGroup);
		} else if (rdrName == RDRName.CRIS_Science) {
			retAgg = new CRISAggregate(rdrGroup);
		} else if (rdrName == RDRName.OMPS_LPScience) {
			retAgg = new ROLPSAggregate(rdrGroup);
		} else if (rdrName == RDRName.OMPS_NPScience) {
			retAgg = new RONPSAggregate(rdrGroup);
		} else if (rdrName == RDRName.OMPS_TCScience) {
			retAgg = new ROTCSAggregate(rdrGroup);
		} else {
			throw new RtStpsException("Unable to create aggregate for RDR type [" + rdrName.toString() + "] -- unknown aggregate type");
		}

		return retAgg;
	}
	/**
	 * Factory method for building specific sensor aggregates
	 * @param rdrGroup  the RDR group on the HDF the Aggregrate is in such as /Data_Products/XXX-RDR
	 * @param aggregateName the aggregate name in the group like: SPACECRAFT-DIARY-RDR_Aggr
	 * @throws RtStpsException thrown if the RDRName is not supported
	 */
	public static Aggregate factory(int rdrGroup, String aggregateName) throws RtStpsException {

		Aggregate retAgg = null;
		
		
		String[] aggFields = aggregateName.split("_");
		RDRName rdrName = RDRName.fromRDRNameStr(aggFields[0]);
		
		if  (rdrName == RDRName.VIIRS_Science) {
			retAgg = new VIIRSAggregate(rdrGroup);
		} else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude) {
			retAgg = new SpacecraftDiaryAggregate(rdrGroup);
		} else if (rdrName == RDRName.ATMS_Science) {
			retAgg = new ATMSAggregate(rdrGroup);
		} else if (rdrName == RDRName.CRIS_Science) {
			retAgg = new CRISAggregate(rdrGroup);
		} else if (rdrName == RDRName.OMPS_LPScience) {
			retAgg = new ROLPSAggregate(rdrGroup);
		} else if (rdrName == RDRName.OMPS_NPScience) {
			retAgg = new RONPSAggregate(rdrGroup);
		} else if (rdrName == RDRName.OMPS_TCScience) {
			retAgg = new ROTCSAggregate(rdrGroup);
		} else {
			throw new RtStpsException("Unable to create aggregate for RDR type (original input string) [" + aggregateName + "] -- unknown aggregate type");
		}

		return retAgg;
	}

}
