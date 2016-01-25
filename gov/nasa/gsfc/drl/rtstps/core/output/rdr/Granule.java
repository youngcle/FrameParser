/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.Version;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.text.ParseException;
import java.util.Date;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import java.lang.Thread;

/**
 * Create Granules dataset in the RDR/HDF file.  There is a one corresponding in this package to Granules and
 * RawApplicationPackets.  For example any XXX-SCIENCE_RDR_Gran_x should correspond to a RawApplicationsPacket_x
 * in the RDR/HDF file
 * 
 *
 */
public abstract class Granule extends CommonDataSetObject {
	private int dataSpace=-1;
	private int dataSet;
	
	private PDSDate beginningObservationDateTime;  // for IET calcs
	private PDSDate endingObservationDateTime; // for IET calcs
	private PDSDate beginningTimeDateTime; // for Beginning_Time calcs
	private PDSDate endingTimeDateTime; // for Ending_Time calcs
	
	private long orbit;
	private long beginningIET;
	private long endingIET;

	private Date creationDateTime;

	private GranuleId granuleId;
	private LEOAFlag leoaState;
	private String docName;
	private String[] packetTypes;
	private long[] packetTypeCounts;
	private ReferenceId referenceId;
	
	private int granuleNumber;
	private int dataSpaceOfRaw; 
	
	private RDRName rdrReferenceName;
	private float percentMissingData = 0.0f; // technically this hard coded for FTs
	private String granuleVersionStr = "A1"; // at the moment hardcoded
	private String granuleStatusStr = "N/A"; // also hardcoded
	private String softwareVersionStr;
	
	private String granuleName;
	private String beginningDateStr; // calculated if writing, or holds the string off the attributes if reading
	private String beginningTimeStr; // calculated if writing, or holds the string off the attributes if reading
	private String endingDateStr; // calculated if writing, or holds the string off the attributes if reading
	private String endingTimeStr; // calculated if writing, or holds the string off the attributes if reading
	private String creationDateStr; // calculated if writing, or holds the string off the attributes if reading
	private String creationTimeStr; // calculated if writing, or holds the string off the attributes if reading
	
	long scansPerGranule = 0L; 

	private String nl = System.getProperty("line.separator");
	RawApplicationPackets rap;

	/**
	 * Constructor for a Granule instance, the arguments are associated with the granule's attributes.
	 * An instance of this class in this packet is created by some other factory method.
	 * 
	 * @param beginningObservationDateTime the beginning date and time of the corresponding RawApplicationPackets area
	 * @param endingObservationDateTime the ending date and time of the corresponding RawApplicationPackets area
	 * @param orbit the orbit number of the pass
	 * @param granuleId the granuleId {@link GranuleId}
	 * @param leoaState the LEO state flag
	 * @param docName the document name of the specification controlling this granules construction
	 * @param packetTypes an array of packet types received in this granule
	 * @param packetTypeCounts the counts per type of the packets received
	 * @param referenceId the reference identifier which is a UUID {@link java.util.UUID}
	 * @param granuleNumber the granule number which corresponds to the RawApplicationPackets number in the RDR/HDF file
	 * @param dataSpaceOfRaw the HDF DataSpace handle of the RawApplicationPackets area associated with this granule
	 * @param rdrReferenceName the corresponds RDR name such VIIRS-SCIENCE-RDR
	 */
	@Deprecated
	public Granule(PDSDate beginningObservationDateTime,	
					PDSDate endingObservationDateTime,
					long orbit,
					GranuleId granuleId,
					LEOAFlag leoaState,
					String docName,
					String[] packetTypes,
					long[] packetTypeCounts,
					ReferenceId referenceId,
					int granuleNumber, 
					int dataSpaceOfRaw, 
					RDRName rdrReferenceName) {
		
		this.beginningObservationDateTime = beginningObservationDateTime;
		this.endingObservationDateTime = endingObservationDateTime;
		
		// format and create the String representations of these fields
		this.beginningDateStr = TimeFormat.formatPDSDate(beginningObservationDateTime).toString();
		this.beginningTimeStr = TimeFormat.formatPDSTime(beginningObservationDateTime).toString();;
		this.endingDateStr = TimeFormat.formatPDSDate(endingObservationDateTime).toString();
		this.endingTimeStr = TimeFormat.formatPDSTime(endingObservationDateTime).toString();

		this.beginningIET = beginningObservationDateTime.getMicrosSinceEpoch();
		this.endingIET = endingObservationDateTime.getMicrosSinceEpoch();
		this.orbit = orbit;
		this.creationDateTime = new Date();

		// format and great the String representations of these fields
		this.creationDateStr = TimeFormat.formatDate(creationDateTime).toString();
		this.creationTimeStr = TimeFormat.formatTime(creationDateTime).toString();
		
		this.granuleId = granuleId;
		this.leoaState = leoaState;
		this.docName = docName;
		this.packetTypes  = packetTypes;
		this.packetTypeCounts = packetTypeCounts;
		
		this.referenceId = referenceId;
		this.granuleNumber = granuleNumber;
		this.dataSpaceOfRaw = dataSpaceOfRaw;
		
		this.rdrReferenceName = rdrReferenceName;
		this.granuleName = makeName(rdrReferenceName, granuleNumber);
		
	}

	/**
	 * Constructor for a Granule instance, the arguments are associated with the granule's attributes.
	 * An instance of this class in this packet is created by some other factory method.
	 * 
	 * 
	 * @param rap the corresponding RawApplicationPackets area
	 * @param orbit the orbit number of the pass
	 * @param granuleId the granuleId {@link GranuleId}
	 * @param leoaState the LEO state flag
	 * @param docName the document name of the specification controlling this granules construction
	 * @param packetTypes an array of packet types received in this granule
	 * @param packetTypeCounts the counts per type of the packets received
	 * @param referenceId the reference identifier which is a UUID {@link java.util.UUID}
	 * @param granuleNumber the granule number which corresponds to the RawApplicationPackets number in the RDR/HDF file
	 * @param dataSpaceOfRaw the HDF DataSpace handle of the RawApplicationPackets area associated with this granule
	 * @param rdrReferenceName the corresponds RDR name such VIIRS-SCIENCE-RDR
	 */
	public Granule(RawApplicationPackets rap,
					long orbit,
					GranuleId granuleId,
					LEOAFlag leoaState,
					String docName,
					String[] packetTypes,
					long[] packetTypeCounts,
					ReferenceId referenceId,
					int granuleNumber, 
					int dataSpaceOfRaw, 
					RDRName rdrReferenceName) {
		this.rap = rap;
	
		this.beginningObservationDateTime = getBeginningObservationDateTime(rap); // goes towards IET
		this.endingObservationDateTime = getEndingObservationDateTime(rap); // goes towards IET
		
		// FIXME format and create the String representations of these fields
		this.beginningDateStr = TimeFormat.formatPDSDate(beginningObservationDateTime).toString();
		
		// FIXME Not clear if the date should be based on the leap calc'd times or not
		this.endingDateStr = TimeFormat.formatPDSDate(endingObservationDateTime).toString();

		// FIXME Calls to granule boundary calculators are hard-coded here per instrument. Make these static somewhere?
		if(rap.getRdrName()==RDRName.VIIRS_Science)
		{
			long ietTime = LeapDate.getMicrosSinceEpoch(rap.getFirstTime());
			this.beginningIET=VIIRSGranule.getStartBoundary(ietTime);
			this.endingIET=VIIRSGranule.getEndBoundary(ietTime);
		}
		else if(rap.getRdrName()==RDRName.NPP_Ephemeris_and_Attitude)
		{
			long ietTime = LeapDate.getMicrosSinceEpoch(rap.getFirstTime());
			this.beginningIET=SpacecraftDiaryGranule.getStartBoundary(ietTime);
			this.endingIET=SpacecraftDiaryGranule.getEndBoundary(ietTime);
		}
		else if(rap.getRdrName()==RDRName.ATMS_Science)
		{
			long ietTime = LeapDate.getMicrosSinceEpoch(rap.getFirstTime());
			this.beginningIET=ATMSGranule.getStartBoundary(ietTime);
			this.endingIET=ATMSGranule.getEndBoundary(ietTime);
		}
		else if(rap.getRdrName()==RDRName.CRIS_Science)
		{
			long ietTime = LeapDate.getMicrosSinceEpoch(rap.getFirstTime());
			this.beginningIET=CRISGranule.getStartBoundary(ietTime);
			this.endingIET=CRISGranule.getEndBoundary(ietTime);
		}
		else if(rap.getRdrName()==RDRName.OMPS_NPScience)
		{
			long ietTime = LeapDate.getMicrosSinceEpoch(rap.getFirstTime());
			this.beginningIET=RONPSGranule.getStartBoundary(ietTime);
			this.endingIET=RONPSGranule.getEndBoundary(ietTime);
		}
		else if(rap.getRdrName()==RDRName.OMPS_TCScience)
		{
			long ietTime = LeapDate.getMicrosSinceEpoch(rap.getFirstTime());
			this.beginningIET=ROTCSGranule.getStartBoundary(ietTime);
			this.endingIET=ROTCSGranule.getEndBoundary(ietTime);
		}
		else if(rap.getRdrName()==RDRName.OMPS_LPScience)
		{
			long ietTime = LeapDate.getMicrosSinceEpoch(rap.getFirstTime());
			this.beginningIET=ROLPSGranule.getStartBoundary(ietTime);
			this.endingIET=ROLPSGranule.getEndBoundary(ietTime);
		}
		else
		{
			this.beginningIET = beginningObservationDateTime.getMicrosSinceEpoch();
			this.endingIET = endingObservationDateTime.getMicrosSinceEpoch();
		}
		
		this.orbit = orbit;
		
		// TEST: These need the beginning/ending IETs to be calculated first.
		// According to CDFCB, Beginning_Time and Ending_Time attributes are the IET boundaries
		// converted to UTC strings!
		this.beginningTimeDateTime = getBeginningTimeDateTime();  // goes to beginningTimeStr
		this.endingTimeDateTime = getEndingTimeDateTime();  // goes to endingTimeStr

		// FIXME LPEATE wants the original time TRUNCATED to the nearest 100th...
		this.beginningTimeStr = TimeFormat.formatPDSTime(beginningTimeDateTime).toString();
		this.endingTimeStr = TimeFormat.formatPDSTime(endingTimeDateTime).toString();

		this.creationDateTime = new Date();
		// format and great the String representations of these fields
		this.creationDateStr = TimeFormat.formatDate(creationDateTime).toString();
		this.creationTimeStr = TimeFormat.formatTime(creationDateTime).toString();
		
		this.granuleId = granuleId;
		this.leoaState = leoaState;
		this.docName = docName;
		this.packetTypes  = packetTypes;
		this.packetTypeCounts = packetTypeCounts;
		//System.out.println("PacketTypes size: " + packetTypes.length + " packetTypeCounts: " + packetTypeCounts.length);
		this.referenceId = referenceId;
		this.granuleNumber = granuleNumber;
		this.dataSpaceOfRaw = dataSpaceOfRaw;
		if (dataSpaceOfRaw < 0) {
			System.out.println("About to write null granule, halt.");
			for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			    System.out.println(ste + "\n");
			}
			return;
		}
		this.rdrReferenceName = rdrReferenceName;
		
		this.granuleName = makeName(rdrReferenceName, granuleNumber);
		
		this.percentMissingData=rap.getPercentMissingData();	
		
	}

	/**
	 * Read the Granule out of the HDF file and fill in the various attributes and items in it for
	 * these access methods
	 * @param groupId the HDF group handle
	 * @param granuleName  the name of the granule
	 * @throws ParseException 
	 */
	public Granule(int groupId, String granuleName) throws RtStpsException {
		this.granuleName = granuleName;
		String[] pieces = granuleName.split("_");
		this.granuleNumber = Integer.parseInt(pieces[2]);
		this.rdrReferenceName = RDRName.fromRDRNameStr(pieces[0]);
		
		//System.out.println("GranuleName = " + granuleName + " -- pieces: " + java.util.Arrays.toString(pieces) + " -- Gran#:" + granuleNumber + " RefName: " + rdrReferenceName);
		
		try {
			dataSet = H5.H5Dopen(groupId, granuleName);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		
		//System.out.println("Granule Dopen: " + dataSet);
		readAttributes();
	}

	// NOTE -- these should only be called in this order...
	public abstract PDSDate getBeginningObservationDateTime(RawApplicationPackets rap);	
	public abstract PDSDate getEndingObservationDateTime(RawApplicationPackets rap);
	public abstract PDSDate getBeginningTimeDateTime();
	public abstract PDSDate getEndingTimeDateTime();
	abstract long getScansPerGranule();

	/**
	 * Static builder to build Granule names that could be in the HDF
	 * @param rdrName the RDR of interest (CRIS-SCIENCE-RDR)
	 * @param granuleNumber the granule number
	 * @return a String that would look something like CRIS-SCIENCE-RDR_Gran_#
	 */
	public static String makeName(RDRName rdrName, int granuleNumber) {
		StringBuffer sb = new StringBuffer();
		sb.append(rdrName.getRDRStringName());
		sb.append("_Gran_");
		sb.append(granuleNumber);
		return sb.toString();
	}
	
	public String getName() {
		return granuleName;
	}

	public PDSDate getBeginningObservationDateTime() { 
		return beginningObservationDateTime;
	}

	public PDSDate getEndingObservationDateTime() {
		return endingObservationDateTime;
	}

	public long getOrbit() {
		return orbit;
	}
	public long getBeginningIET() {
		return beginningIET;
	}
	public Date getCreationDateTime() {
		return creationDateTime;
	}

	public long getEndingIET() {
		return endingIET;
	}
	public GranuleId getGranuleId() {
		return granuleId;
	}
	public LEOAFlag getLeoaState() {
		return leoaState;
	}
	public String getDocName() {
		return docName;
		
	}
	public String[] getPacketTypes() {
		return packetTypes;
	}
	public long[] getPacketTypeCounts() {
		return packetTypeCounts;
	}
	public ReferenceId getReferenceId() {
		return referenceId;
	}
	public int getGranuleNumber() {
		return granuleNumber;
	}
	
	public int getDataSpaceOfRaw() {
		return dataSpaceOfRaw;
	}
	
	public float getPercentMissingData() {
		return this.percentMissingData;
	}
	public String getGranuleVersionStr() {
		return this.granuleVersionStr;
	}
	public String getgranuleStatusStr() {
		return this.granuleStatusStr;
	}
	public String getSoftwareVersion() {
		return this.softwareVersionStr;
	}

	public String getBeginning_Date() {
		return this.beginningDateStr;
	}
	public String getBeginning_Time() {
		return this.beginningTimeStr;
	}
	public String getEnding_Date() {
		return this.endingDateStr;
	}
	public String getEnding_Time() {
		return this.endingTimeStr;
	}
	public long getN_Beginning_Orbit_Number() {
		return this.getOrbit();
	}
	public long getN_Beginning_Time_IET() {
		return this.getBeginningIET();
	}
	public String getN_Creation_Date() {
		return  this.creationDateStr;
	}
	public String getN_Creation_Time() {
		return this.creationTimeStr;
	}
	public long getN_Ending_Time_IET() {
		return this.getEndingIET();
	}
	public GranuleId getN_Granule_ID() {
		return this.getGranuleId();
	}
	public String getN_Granule_Status() {
		return this.getgranuleStatusStr();
	}
	public String getN_Granule_Version() {
		return this.getGranuleVersionStr();
	}
	public LEOAFlag getN_LEOA_Flag() {
		return this.getLeoaState();
	}
	public String getN_NPOESS_Document_Ref() {
		return this.getDocName();
	}

	public String getN_Packet_Type() {
		return java.util.Arrays.toString(this.getPacketTypes());
	}
	public String getN_Packet_Type_Count() {
		return java.util.Arrays.toString(this.getPacketTypeCounts());
	}
	
	public float getN_Percent_Missing_Data() {
		return this.getPercentMissingData();
	}

	public ReferenceId getN_Reference_ID() {
		return this.getReferenceId();
	}
	public String getN_Software_Version() {
		return this.getSoftwareVersion();
	}
	public void write(int hdfFile, int rootGroup, int rdrGroup) throws RtStpsException {
		
		String name = rdrReferenceName.getRDRStringName();
		
		long     dims[] = { 1L };  /// 1 ref equals size of 1? {(long)granuleSize };
		// long dimsU[] = { HDF5Constants.H5S_UNLIMITED };  apparently this has to be 'chunked' in order set it as unlimited, which we are not using
		
		//System.out.println("Granule size=" + dims[0]);
		
		try {
			dataSpace = H5.H5Screate_simple(1, dims, null);

			// build a granule
			// for example: /Data_Products/VIIRS-SCIENCE-RDR/VIIRS-SCIENCE-RDR_Gran_0
			//
			String granPath =  "/Data_Products/" + name + "/" + granuleName; // name + "_Gran_" + granuleNumber;
		
			//System.out.println("GranPath = " + granPath);
			
			dataSet = H5.H5Dcreate( hdfFile,
						granPath,
						HDF5Constants.H5T_STD_REF_DSETREG,
						dataSpace,
						HDF5Constants.H5P_DEFAULT );
		
		} catch (HDF5LibraryException e) {
			//throw new RtStpsException(e);
			System.out.println("HDF5LibraryException occurred during granule write.");
			return;
		} catch (HDF5Exception e) {
			System.out.println("HDF5Exception occurred during granule write.");
			throw new RtStpsException(e);
		}
		// find the ref to what the granule will point to...
		// for example: /All_Data/VIIRS-SCIENCE-RDR_All/VIIRS-RawApplicationPacket_0
		//
		String rawPath = "/All_Data/" + name + "_All/RawApplicationPackets_" + granuleNumber;
		
		//System.out.println("RawPath = " + rawPath);
		
		byte[] ref;
		try {
			ref = H5.H5Rcreate(hdfFile,
						rawPath,
						HDF5Constants.H5R_DATASET_REGION,
						dataSpaceOfRaw);
		
			//System.out.println("REF size during creation: " + ref.length);
			// write the Ref to the granule
			H5.H5Dwrite(dataSet,
						HDF5Constants.H5T_STD_REF_DSETREG,
						HDF5Constants.H5S_ALL,
						HDF5Constants.H5S_ALL,
						HDF5Constants.H5P_DEFAULT,
						ref,
						false);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
		writeAttributes();
	}
	
	/**
	 * Return the RDR_All RDRAllReader that the reference points to in the Aggregate
	 * @return an RDRAlLReader for the RDR_All
	 */
	public RawApplicationPackets getReferencedRawApplicationPackets() throws RtStpsException {
		
		byte[] ref = new byte[12];
		
		//System.out.println("Ref size during read -- " + ref.length);
		
		try {
			H5.H5Dread(dataSet,
						HDF5Constants.H5T_STD_REF_DSETREG,
						HDF5Constants.H5S_ALL,
						HDF5Constants.H5S_ALL,
						HDF5Constants.H5P_DEFAULT,
						ref);

			int rawAppsId = H5.H5Rdereference(dataSet, HDF5Constants.H5R_DATASET_REGION, ref);

			// note: rawApps must be closed by the RawApplicationPackets method close
			return RawApplicationPacketsFactory.make(rawAppsId, rdrReferenceName, granuleNumber);  
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 
	
	}
	private void readAttributes() throws RtStpsException {
		
		beginningDateStr = HDFAttribute.readString(dataSet, "Beginning_Date"); 
		beginningTimeStr = HDFAttribute.readString(dataSet, "Beginning_Time");
		
		this.beginningObservationDateTime = TimeFormat.createPDSDateTime(beginningDateStr, beginningTimeStr);
	
		endingDateStr = HDFAttribute.readString(dataSet, "Ending_Date"); 
		endingTimeStr = HDFAttribute.readString(dataSet, "Ending_Time"); 
		
		this.endingObservationDateTime = TimeFormat.createPDSDateTime(endingDateStr, endingTimeStr);
		
		orbit = HDFAttribute.readULong(dataSet, "N_Beginning_Orbit_Number"); //, orbit);
		beginningIET = HDFAttribute.readULong(dataSet, "N_Beginning_Time_IET"); //, beginningIET);
		creationDateStr = HDFAttribute.readString(dataSet, "N_Creation_Date"); 
		creationTimeStr = HDFAttribute.readString(dataSet, "N_Creation_Time"); 
		
		this.creationDateTime = TimeFormat.createDateTime(creationDateStr, creationTimeStr);

		endingIET = HDFAttribute.readULong(dataSet, "N_Ending_Time_IET"); //, endingIET);
		String granuleIdStr = HDFAttribute.readString(dataSet, "N_Granule_ID"); //, granuleId.toString());
		granuleId = new GranuleId(granuleIdStr);
		
		granuleStatusStr  = HDFAttribute.readString(dataSet, "N_Granule_Status"); //, "N/A");
		granuleVersionStr  = HDFAttribute.readString(dataSet, "N_Granule_Version"); //, "A1");
		String leoAFlagStr = HDFAttribute.readString(dataSet, "N_LEOA_Flag"); //, leoaState.toString());
		leoaState = LEOAFlag.valueOf(leoAFlagStr);
		docName = HDFAttribute.readString(dataSet, "N_NPOESS_Document_Ref"); //, docName);
		packetTypes = HDFAttribute.readStrings(dataSet, "N_Packet_Type"); //, packetTypes);	
		packetTypeCounts = HDFAttribute.readULongs(dataSet, "N_Packet_Type_Count"); //, packetTypeCounts);
		percentMissingData  = HDFAttribute.readFloat(dataSet, "N_Percent_Missing_Data"); //, 0.0f);
		String refIdStr = HDFAttribute.readString(dataSet, "N_Reference_ID"); //, referenceId.toString());
		referenceId = new ReferenceId(refIdStr);
		
		softwareVersionStr = HDFAttribute.readString(dataSet, "N_Software_Version"); //, Version.getVersion());
	}
	
	private void writeAttributes() throws RtStpsException {
		
		HDFAttribute.writeString(dataSet, "Beginning_Date", this.beginningDateStr);
		HDFAttribute.writeString(dataSet, "Beginning_Time", this.beginningTimeStr);
		HDFAttribute.writeString(dataSet, "Ending_Date", this.endingDateStr);
		HDFAttribute.writeString(dataSet, "Ending_Time", this.endingTimeStr);
		HDFAttribute.writeULong(dataSet, "N_Beginning_Orbit_Number", orbit);
		HDFAttribute.writeULong(dataSet, "N_Beginning_Time_IET", beginningIET);
		HDFAttribute.writeString(dataSet, "N_Creation_Date", this.creationDateStr);
		HDFAttribute.writeString(dataSet, "N_Creation_Time", this.creationTimeStr);
		
		HDFAttribute.writeULong(dataSet, "N_Ending_Time_IET", endingIET);
		HDFAttribute.writeString(dataSet, "N_Granule_ID", granuleId.toString());
		HDFAttribute.writeString(dataSet, "N_Granule_Status", "N/A");
		HDFAttribute.writeString(dataSet, "N_Granule_Version", "A1");
		HDFAttribute.writeString(dataSet, "N_LEOA_Flag", leoaState.toString());
		HDFAttribute.writeString(dataSet, "N_NPOESS_Document_Ref", docName);

		HDFAttribute.writeStrings(dataSet, "N_Packet_Type", packetTypes);				
		HDFAttribute.writeULongs(dataSet, "N_Packet_Type_Count", packetTypeCounts);
		
		HDFAttribute.writeFloat(dataSet, "N_Percent_Missing_Data", percentMissingData);

		HDFAttribute.writeString(dataSet, "N_Reference_ID", referenceId.toString());
		HDFAttribute.writeString(dataSet, "N_Software_Version", Version.getVersion());
	}
	
	public void close() throws RtStpsException {
		
		try {
			if (dataSpace >= 0) {
				try{
					H5.H5Sclose(dataSpace); // write side only
				}
				catch(HDF5LibraryException e){
					System.out.println("Granule dataspace " + dataSpace 
						+ " may have been closed already.");
				}
			}

			try{
				H5.H5Dclose(dataSet);
			}
			catch(HDF5LibraryException e){
				System.out.println("Granule dataset " + dataSet 
					+ " may have been closed already.");
			}
		} catch (Exception e) {
			System.out.println("Exception occurred during Granule close.");
			//throw new RtStpsException(e);
			return;
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("==> Granule: " + this.getName());
		sb.append(nl);
		sb.append("    Beginning_Date: " +  this.getBeginning_Date());
		sb.append(nl);
		sb.append("    Beginning_Time: " + this.getBeginning_Time() );
		sb.append(nl);
		sb.append("    Ending_Date: " + this.getEnding_Date());
		sb.append(nl);
		sb.append("    Ending_Time: " +  this.getEnding_Time());
		sb.append(nl);
		sb.append("    N_Beginning_Orbit_Number: " +  this.getN_Beginning_Orbit_Number());
		sb.append(nl);
		sb.append("    N_Beginning_Time_IET: " + this.getN_Beginning_Time_IET() );
		sb.append(nl);
		sb.append("    N_Creation_Date: " +  this.getN_Creation_Date());
		sb.append(nl);
		sb.append("    N_Creation_Time: " +  this.getN_Creation_Time());
		sb.append(nl);
		sb.append("    N_Ending_Time_IET: " +  this.getN_Ending_Time_IET());
		sb.append(nl);
		sb.append("    N_Granule_ID: " +  this.getN_Granule_ID());
		sb.append(nl);
		sb.append("    N_Granule_Status: " +  this.getN_Granule_Status());
		sb.append(nl);
		sb.append("    N_Granule_Version: " +  this.getN_Granule_Version());
		sb.append(nl);
		sb.append("    N_LEOA_Flag: " +  this.getN_LEOA_Flag());
		sb.append(nl);
		sb.append("    N_NPOESS_Document_Ref: " +  this.getN_NPOESS_Document_Ref());
		sb.append(nl);
		sb.append("    N_Packet_Type: " +  this.getN_Packet_Type());
		sb.append(nl);
		sb.append("    N_Packet_Type_Count: " +  this.getN_Packet_Type_Count());
		sb.append(nl);
		sb.append("    N_Percent_Missing_Data: " +  this.getN_Percent_Missing_Data());
		sb.append(nl);
		sb.append("    N_Reference_ID: " +  this.getN_Reference_ID());
		sb.append(nl);
		sb.append("    N_Software_Version: " + this.getN_Software_Version());
		sb.append(nl);
		return sb.toString();
	}

	/**
	 * Factory for a Granule instance, the arguments are associated with the granule's attributes.
	 * An instance of this class in this packet is created by some other factory method.
	 * 
	 * @param rap the corresponding RawApplicationPackets area
	 * @param orbit the orbit number of the pass
	 * @param granuleId the granuleId {@link GranuleId}
	 * @param leoaState the LEO state flag
	 * @param docName the document name of the specification controlling this granules construction
	 * @param packetTypes an array of packet types received in this granule
	 * @param packetTypeCounts the counts per type of the packets received
	 * @param referenceId the reference identifier which is a UUID {@link java.util.UUID}
	 * @param granuleNumber the granule number which corresponds to the RawApplicationPackets number in the RDR/HDF file
	 * @param dataSpaceOfRaw the HDF DataSpace handle of the RawApplicationPackets area associated with this granule
	 * @throws RtStpsException thrown if the RDRName is not supported
	 */

	public static Granule factory(RawApplicationPackets rap,
								long orbit,
								GranuleId granuleId, 
								LEOAFlag off, 
								String documentName,
								long[] packetTypes,
								String[] packetTypeCounts, 
								ReferenceId referenceId, 
								int setNum, 
								int dataSpace,
								RDRName rdrName) throws RtStpsException {
		
		Granule retGran = null;
		
		if  (rdrName == RDRName.VIIRS_Science) {
			retGran = new VIIRSGranule(rap, // VIIRSGranule or VIIRSGranule_PacketTimes
					orbit, 
					granuleId, 
					off, 
					documentName, 
					packetTypeCounts, 
					packetTypes, 
					referenceId, 
					setNum,  
					dataSpace);
		} else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude) {
			retGran = new SpacecraftDiaryGranule(rap, 
					orbit, 
					granuleId, 
					off, 
					documentName, 
					packetTypeCounts, 
					packetTypes, 
					referenceId, 
					setNum,  
					dataSpace);
		} else if (rdrName == RDRName.ATMS_Science) {
			retGran = new ATMSGranule(rap, 
					orbit, 
					granuleId, 
					off, 
					documentName, 
					packetTypeCounts, 
					packetTypes, 
					referenceId, 
					setNum,  
					dataSpace);
		} else if (rdrName == RDRName.CRIS_Science) {
			retGran = new CRISGranule(rap, 
					orbit, 
					granuleId, 
					off, 
					documentName, 
					packetTypeCounts, 
					packetTypes, 
					referenceId, 
					setNum,  
					dataSpace);
		} else if (rdrName == RDRName.OMPS_LPScience) {
			retGran = new ROLPSGranule(rap, 
					orbit, 
					granuleId, 
					off, 
					documentName, 
					packetTypeCounts, 
					packetTypes, 
					referenceId, 
					setNum,  
					dataSpace);
		} else if (rdrName == RDRName.OMPS_NPScience) {
		    retGran = new RONPSGranule(rap, 
					orbit, 
					granuleId, 
					off, 
					documentName, 
					packetTypeCounts, 
					packetTypes, 
					referenceId, 
					setNum,  
					dataSpace);
		} else if (rdrName == RDRName.OMPS_TCScience) {
		    retGran = new ROTCSGranule(rap, 
			    		orbit, 
			    		granuleId, 
			    		off, 
			    		documentName, 
			    		packetTypeCounts, 
			    		packetTypes, 
			    		referenceId, 
			    		setNum,  
			    		dataSpace);
		} else {	
			throw new RtStpsException("Unable to create aggregate for RDR type [" + 
							rdrName.toString() + "] -- unknown aggregate type");
		}

		return retGran;
	}
	/**
	 * Factory method to create a granule instance to read the Granule out of the HDF file and fill in the various attributes and items in it for
	 * these access methods
	 * @param groupId the HDF group handle
	 * @param granuleName  the name of the granule
	  * @throws RtStpsException thrown if the RDRName type from the granuleName is not supported
	 */
	public static Granule factory(int groupId, String granuleName) throws RtStpsException {
		
		Granule retGran = null;
		
		String[] pieces = granuleName.split("_");
		
		RDRName rdrName = RDRName.fromRDRNameStr(pieces[0]);	
		
		if  (rdrName == RDRName.VIIRS_Science) {
			retGran = new VIIRSGranule_PacketTimes(groupId, granuleName); //  VIIRSGranule or VIIRSGranule_PacketTimes
		} else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude) {
			retGran = new SpacecraftDiaryGranule(groupId, granuleName);
		} else if (rdrName == RDRName.ATMS_Science) {
			retGran = new ATMSGranule(groupId, granuleName);
		} else if (rdrName == RDRName.CRIS_Science) {
			retGran = new CRISGranule(groupId, granuleName);
		} else {
			throw new RtStpsException("Unable to create granule for RDR type (original input string) [" + 
							granuleName + "] -- unknown granule type");
		}

		return retGran;
	}
}
