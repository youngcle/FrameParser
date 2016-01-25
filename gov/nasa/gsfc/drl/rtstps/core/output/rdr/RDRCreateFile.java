/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Create an HDF file with basics of the RDR structure.  Provide some methods to further
 * build out the RDR.
 *
 * Basic RDR & HDF file structure are from JPSS standard formats
 */
public class RDRCreateFile {
	private int hdfFile;
	private int propertyId;
	private final static int xmlSize = 1536;  // official size per RDR...
	private AllData allData;
	private DataProducts dataProducts;
	private static int count = 0;
	private PDSDate firstDateTime = null;
	private PDSDate lastDateTime = null;
	
	// used to be a special case, but is now a requirement for proper RDR file creation
	private SpacecraftDiaryRDR_MultipleGranule spacecraftDiaryRDR_MultipleGranule = null;
	
	// All other RDRs go here. The RDRName has all of the enumerations required to identify
	// an RDR, which shall be used as the HashMap's keys...
	private HashMap<RDRName, RDR> rdrs = new HashMap<RDRName, RDR>();

	//private WeightedRDRList rdrs = new WeightedRDRList();
	
	public static boolean timeBaseCalculation = false;
	
	private Stats stats = null;
	
	// For multiple RDR file outputs only; not used for single-RDR output:
	private boolean sensorGranuleNotFull = true;
	private boolean diarySpanNotComplete = true;

	// Status information
	public static final int IN_PROGRESS = 0;
	public static final int DATA_GRANULE_COMPLETE = 1;
	public static final int SPACECRAFT_DIARIES_COMPLETE = 2;
	public static final int ALL_COMPLETE = 3;

	// For tracking granule boundaries & boundary thresholds
	private long granuleTimeSpan = 0L;
	private long startBoundaryThreshold = -1;
	private long endBoundaryThreshold = -1;
	private long latestScienceGranuleEndBoundary = -1;
	private long latestDiaryGranuleEndBoundary = -1;

	// For determining the RDR file's absolute path and current filename
	private String destPath;
	private String filename;
	
	/**
	 * Open the specified file, wipe out a file of the same name if it exists
	 * A user block space is created based on the rdrCount given.
	 * @param destPath the path of interest
	 * @param filename the file name of interest
	 * @param rdrCount the number of RDRs that will go in this file, used to create the space for the user block
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	public RDRCreateFile(String destPath, String filename, int rdrCount, long gtspan) throws RtStpsException {
		count++;
		int userBlockSize = xmlSize * rdrCount;
		
		// computes the next power of 2 the xml will fit in for the user block
		// which must be a power of two. 
		userBlockSize--;
		userBlockSize = (userBlockSize >>> 1) | userBlockSize;
		userBlockSize = (userBlockSize >>> 2) | userBlockSize;
		userBlockSize = (userBlockSize >>> 4) | userBlockSize;
		userBlockSize = (userBlockSize >>> 8) | userBlockSize;
		userBlockSize = (userBlockSize >>> 16) | userBlockSize;
		userBlockSize++; // userBlockSize is now the next highest power of 2.
		
		// set the user block size
		try {
			propertyId = H5.H5Pcreate (HDF5Constants.H5P_FILE_CREATE);
			H5.H5Pset_userblock(propertyId, userBlockSize);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}

		this.destPath = destPath;
		this.filename = filename;
		String pathFilename = destPath + File.separator + filename;
		this.granuleTimeSpan = gtspan;

		
		// Use java to check that the directory is there because the HDF
		// library has such nebulous error codes and messages.
		// In order for this to be true it must be a directory AND exist
		File dir = new File(destPath);
		File currentDir = new File(".");
		try {

			if (dir.exists() == false) {

				throw new RtStpsException("RDR write directory [" + destPath
						+ "] does not exist. Current directory ["
						+ currentDir.getCanonicalPath() + "]");

			}

			if (dir.isDirectory() == false) {
				throw new RtStpsException("RDR write directory [" + destPath
						+ "] is not a directory. Current directory ["
						+ currentDir.getCanonicalPath() + "]");
			}
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
		
		//System.out.println("Filename: " + pathFilename);
		
		// Create a new file. If file exists its contents will be overwritten.
		// At this time, it should be created with a temporary filename...
		try {
			hdfFile = H5.H5Fcreate (pathFilename, HDF5Constants.H5F_ACC_TRUNC, propertyId, HDF5Constants.H5P_DEFAULT);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
		
		//System.out.println("hdfFile = " + hdfFile);
		
		// create the AllData portion only i.e. "/All_Data"
		allData = new AllData(hdfFile);
		
		// create the DataProducts portion only i.e. "/Data_Products"
		dataProducts = new DataProducts(hdfFile);

	}
	
	public RDRCreateFile(Stats stats, String destPath, String filename, int rdrCount, long gtspan) throws RtStpsException {
	
	    	count++;
		this.stats = stats;
		
		int userBlockSize = xmlSize * rdrCount;
		
		// computes the next power of 2 the xml will fit in for the user block
		// which must be a power of two. 
		userBlockSize--;
		userBlockSize = (userBlockSize >>> 1) | userBlockSize;
		userBlockSize = (userBlockSize >>> 2) | userBlockSize;
		userBlockSize = (userBlockSize >>> 4) | userBlockSize;
		userBlockSize = (userBlockSize >>> 8) | userBlockSize;
		userBlockSize = (userBlockSize >>> 16) | userBlockSize;
		userBlockSize++; // userBlockSize is now the next highest power of 2.
		
		// set the user block size
		try {
			propertyId = H5.H5Pcreate (HDF5Constants.H5P_FILE_CREATE);
			H5.H5Pset_userblock(propertyId, userBlockSize);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}

		this.destPath = destPath;
		this.filename = filename;
		String pathFilename = destPath + File.separator + filename;
		this.granuleTimeSpan = gtspan; 
		
		// Use java to check that the directory is there because the HDF
		// library has such nebulous error codes and messages.
		// In order for this to be true it must be a directory AND exist
		File dir = new File(destPath);
		File currentDir = new File(".");
		try {

			if (dir.exists() == false) {

				throw new RtStpsException("RDR write directory [" + destPath
						+ "] does not exist. Current directory ["
						+ currentDir.getCanonicalPath() + "]");

			}

			if (dir.isDirectory() == false) {
				throw new RtStpsException("RDR write directory [" + destPath
						+ "] is not a directory. Current directory ["
						+ currentDir.getCanonicalPath() + "]");
			}
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
		
		/* Create a new file. If file exists its contents will be overwritten. */
		try {
			hdfFile = H5.H5Fcreate (pathFilename, HDF5Constants.H5F_ACC_TRUNC, propertyId, HDF5Constants.H5P_DEFAULT);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
		
		//System.out.println("hdfFile = " + hdfFile);
		
		// create the AllData portion only i.e. "/All_Data"
		allData = new AllData(hdfFile);
		
		// create the DataProducts portion only i.e. "/Data_Products"
		dataProducts = new DataProducts(hdfFile);

	}
	/**
	 * Get the RDR from the pool of created RDRs, or create a new one if its new
	 * @param anRdrName an RDR name as an {@link RDRName}
	 * @return an {@link RDR}
	 * @throws RtStpsException wraps an any HDF files
	 */
	private RDR getRDR(RDRName anRdrName) throws RtStpsException {
		// If RDR does not exist yet, create it using the RDRFactory
		RDR rdr = rdrs.get(anRdrName);
		if (rdr == null) {
			rdr = RDRFactory.createRDR(stats, anRdrName, allData, dataProducts, FixedDomainDescription.dev);
			rdr.setGranuleTimeSpan(this.granuleTimeSpan);
			rdrs.put(anRdrName, rdr);
		}
		
		return rdr;
	}

	/**
	 * Return the {@link AllData} object which is created through this class
	 * @return the AllData object
	 */
	public AllData getAllData() {
		return allData;
	}
	
	/**
	 * Return the {@link DataProducts} object which is created through this class
	 * @return the DataProducts object
	 */
	public DataProducts getDataProducts() {
		return dataProducts;
	}
	
	
	/**
	 * Close the HDF file completely, including any open RDRs, AllData and DataProducts
	 * @throws RtStpsException 

	 */
	public void close() throws RtStpsException {
		
		// first close all the normal RDRs
		for (RDR rdr : rdrs.values()) 
		{
		    	System.out.println("Closing: " + rdr.toString());
		    	rdr.close();
		}
		//spacecraftDiaryRDR_MultipleGranule.getAggregate()

		// Then close the spacecraft diary RDR if it is being
		// processed, since it is a special case and depends on 
		// the others
		if (spacecraftDiaryRDR_MultipleGranule != null) 
		{
		    	//System.out.println("diary RDR was null, inside conditional");
		    	//System.out.println(count);
			//try 
			{
			    //if (count == 1)
			    {
				spacecraftDiaryRDR_MultipleGranule.close();
				//FIXME: Why is the SD RDR being added again? AFTER it's been closed?
				//ANSWER: So that NPP atteph RDR is included in this RDR file's overall list of RDRs,
				//which is perused later during finalization of all files. It's okay if it's already
				//closed; nothing more is done to the atteph RDR object after this point
				rdrs.put(RDRName.NPP_Ephemeris_and_Attitude, spacecraftDiaryRDR_MultipleGranule);
			    }
			    //else
			    {
				//count--;
			    }
			    //spacecraftDiaryRDR_MultipleGranule.close(); //rdrs.values());
			} 
			//catch (HDF5LibraryException e) 
			{
			//	throw new RtStpsException(e);
			}
			
			// Finally ADD the S/C Diary RDR to the RDR list
			// this is needed by the routine that builds the file name
		}
		allData.close();
		dataProducts.close();
		
		try {
			try{
				H5.H5Pclose(propertyId);
			} catch(HDF5LibraryException e){
				System.out.println("RDRCreateFile propery ID " + propertyId + " may have been closed already.");
			}

			// Apparently, this only does what is expected if all other HDF5 identifiers
			// associated with it are already closed beforehand. Otherwise, the file isn't
			// really "closed" yet as other identifiers remain open, and this is dangerous.
			H5.H5Fclose(hdfFile);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
	}

	public void flushToDisk(){
		try{
			H5.H5Fflush(hdfFile, HDF5Constants.H5F_SCOPE_GLOBAL);
		}
		catch(HDF5LibraryException e){
			// Flush failed, but it's okay for now since H5Fclose will flush data as well
		}
		catch(HDF5Exception e){
			// Flush failed, but it's okay for now since H5Fclose will flush data as well
		}
	}

	/**
	 * Return a list of the RDRs created by this class
	 * @return a <code>List</code> of {@link RDR}s
	 */
	public List<RDR> getRDRs() {
		List<RDR> rdrList = new LinkedList<RDR>(rdrs.values());
		return rdrList;
		//System.out.println("rdrList size: " + rdrList.size());
		//return rdrs.values();
	}

	/**
	 * Get the first time of the first packet with a time stamp given to this class
	 * @return a {@link PDSDate} from that packet
	 */
	public PDSDate getFirstDateTime() {
		return firstDateTime;
	}

	/**
	 * Get the last time of the last packet with a time stamp given to this class
	 * @return a {@link PDSDate} from that packet
	 */
	public PDSDate getLastDataTime() {
		return lastDateTime;
	}

	/**
	 * Put a packet into this RDR file
	 * @param p a {@link Packet}
	 * @throws RtStpsException wraps any HDF exception
	 */
	public void put(Packet p) throws RtStpsException {
		// If packet is the 1st packet in a sequence, it's bound to have a timestamp (secondary header) no matter
		// the instrument! Record it as either the first or last timestamp we've encountered.
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			if (firstDateTime == null) {
				firstDateTime = new PDSDate(p.getTimeStamp(8));
			}
			lastDateTime = new PDSDate(p.getTimeStamp(8));
		}
		// Based on packet's application ID, obtain the correct RDRName RDR enumeration (assuming appid is supported)
		RDRName an_rdrName = RDRName.fromAppId(p.getApplicationId());
		
		if (an_rdrName == null) {
			if (stats == null) {
				System.out.println("Unsupported Packet w/App ID[" + p.getApplicationId() + "] -- no matching RDR, dropping packet...");
			} else {
				stats.drop.value++;
			}
		} else if (an_rdrName == RDRName.NPP_Ephemeris_and_Attitude) { 
			// FIXME spacecraft diary used to be a special case but is not one any longer...
			// first if the diary RDR has not been created, create one
			if (spacecraftDiaryRDR_MultipleGranule == null) {
				spacecraftDiaryRDR_MultipleGranule = new SpacecraftDiaryRDR_MultipleGranule(stats, allData, dataProducts, FixedDomainDescription.dev);
			}
			// then put the packet
			spacecraftDiaryRDR_MultipleGranule.put(p);
		} else {
			// all others; get the RDR object that handles this packet appID and give it the packet. If not yet 
			// initialized, the RDR object is created by the getRDR based on RDRName enumeration.
			RDR rdr = this.getRDR(an_rdrName);
			rdr.put(p);
		}
	}

	/**
	 * Checks the Granule dataset first, then attempts to put the packet. Packet is only put into the granule if granule is
	 * not yet full.
	 * @param p a {@link Packet}
	 * @throws RtStpsException wraps any HDF exception
	 * @return int the appropriate status number. If returns > 0, packet is NOT put into the granule
	 */
	public int checkThenPut(Packet p) throws RtStpsException{
		// If packet is the 1st packet in a sequence, it has a timestamp (secondary header) no matter
		// the instrument! Record it as either the first or last timestamp we've encountered.
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			if (firstDateTime == null) {
				firstDateTime = new PDSDate(p.getTimeStamp(8));
			}
			lastDateTime = new PDSDate(p.getTimeStamp(8));
		}

		// Based on packet's application ID, obtain the correct RDRName enumeration
		RDRName an_rdrName = RDRName.fromAppId(p.getApplicationId());
		if (an_rdrName == null) {
			if (stats == null) {
				System.out.println("Unsupported Packet w/App ID[" + p.getApplicationId() + "] -- no matching RDR, dropping packet...");
			} else {
				stats.drop.value++;
			}
		} 
		else if (an_rdrName == RDRName.NPP_Ephemeris_and_Attitude) { 
			// First, if the SpacecraftAOS Diary RDR has not been created, create one. Then, put the packet.
			if (spacecraftDiaryRDR_MultipleGranule == null) {
				spacecraftDiaryRDR_MultipleGranule = new SpacecraftDiaryRDR_MultipleGranule(stats, allData, dataProducts, FixedDomainDescription.dev);
				spacecraftDiaryRDR_MultipleGranule.setGranuleTimeSpan(this.granuleTimeSpan);
			}
			
			// If SpacecraftAOS Diary time span is already greater than or equal to the data granule's, return immediately.
			if(!this.diarySpanNotComplete)
				return SPACECRAFT_DIARIES_COMPLETE;

			// [A]: By default, do not simply allow in new SD granules upon current one's completion...
			// If true, this will cause the creation of additional RAPs/Granule datasets within the same RDR file
			boolean createNew = false;

			// [A]: Only allow in more SD granules if the latest SD granule end boundary does not exceed our science
			// granules' end boundary threshold yet (meaning that SD granules do not fully encapsulate science granules yet)
			long lastGranuleEndBoundary = spacecraftDiaryRDR_MultipleGranule.getLastGranuleEndBoundary();
			if( this.endBoundaryThreshold <= 0L ){
				// Insufficient science packets to know start/end thresholds; let data flow for now
				createNew = true;
			}
			else if( lastGranuleEndBoundary > 0L && lastGranuleEndBoundary < this.endBoundaryThreshold ){
				// [A]: As long as the latest SD granule's end boundary doesn't exceed our science granule
				// end boundary threshold yet, let the spacecraft diary data keep flowing in! 
				createNew = true;
			}

			// Attempt to put packet into the RDR, and return appropriate status
			this.diarySpanNotComplete = spacecraftDiaryRDR_MultipleGranule.checkThenPut(p, createNew);
			if(this.diarySpanNotComplete)
				return IN_PROGRESS;
			else
				return SPACECRAFT_DIARIES_COMPLETE;
		} 
		else {
			// Do not attempt to put more packets if sensor granule is already full; just return.
			if(this.sensorGranuleNotFull == false)
				return DATA_GRANULE_COMPLETE;

			// all others; get the RDR object that handles this packet appID and give it the packet. 
			RDR rdr = this.getRDR(an_rdrName);

			// If true, this will cause the creation of additional RAPs/Granule datasets within the same RDR file
			boolean createNew = false;

			// Obtain start/end boundary thresholds if they're still at default values
			// [A] Note: both boundaries are always calculated at the same time.	
			if(this.startBoundaryThreshold <= 0L){
				this.startBoundaryThreshold = rdr.getBoundaryThreshold(0);
				this.endBoundaryThreshold = rdr.getBoundaryThreshold(1);	
			}
			
			// Only allow creation of new granules if most recent granule end boundary encountered is less than 
			// the end boundary threshold for this RDR file (and if granule boundary threshold is NOT at default).
			long lastGranuleEndBoundary = rdr.getLastGranuleEndBoundary();
			if( lastGranuleEndBoundary > 0L && lastGranuleEndBoundary < this.endBoundaryThreshold ){
				createNew = true;
			}

			this.sensorGranuleNotFull = rdr.checkThenPut(p, createNew);
			if(this.sensorGranuleNotFull)
				return IN_PROGRESS;
			else
				return DATA_GRANULE_COMPLETE;
		}

		return IN_PROGRESS;
	}

	public String getDestPath(){
		return this.destPath;
	}

	public String getFilename(){
		return this.filename;
	}

	public void writeAttributes(Date creationDateAndTime, 
					Origin distributor, 
					MissionName missionName, 
					Origin datasetSource, 
					PlatformShortName platformShortname) throws RtStpsException {
		StringBuffer dsb = TimeFormat.formatDate(creationDateAndTime);
		StringBuffer tsb = TimeFormat.formatTime(creationDateAndTime);
		
		HDFAttribute.writeString(hdfFile, "Distributor", distributor.toString());
		HDFAttribute.writeString(hdfFile, "Mission_Name", missionName.toString());
		HDFAttribute.writeString(hdfFile, "N_Dataset_Source", datasetSource.toString());
		HDFAttribute.writeString(hdfFile, "N_HDF_Creation_Date", dsb.toString());
		HDFAttribute.writeString(hdfFile, "N_HDF_Creation_Time", tsb.toString());
		HDFAttribute.writeString(hdfFile, "Platform_Short_Name", platformShortname.toString());
	}
	
	/**
	* Function that tells if RDR file is complete: sensor granule(s) are full, and SpacecraftAOS Diary
	* time span is already greater than or equal to sensor granules' time span.
	* @return true if RDR file is complete, false otherwrise.
	*/
	public boolean isComplete(){
		return ( (!diarySpanNotComplete) && (!sensorGranuleNotFull) );
	}

	/**
	* Function that tells if RDR file already has spacecraft diary RDR datasets
	* @return true if SpacecraftAOS Diary datasets exist, false otherwrise.
	*/
	public boolean hasSpacecraftDiary(){
		if( this.spacecraftDiaryRDR_MultipleGranule != null)
			return true;
		else
			return false;
	}

	/**
	* Get spacecraftDiaryRDR_MultipleGranule's latest completed RawApplicationPackets.
	*/
	public RawApplicationPackets getCurrentSpacecraftDiaryRAP(){
		return this.spacecraftDiaryRDR_MultipleGranule.getLastRawApplicationPackets();
	}

	/**
	* Creates a co-temporal SpacecraftAOS Diary granule pre-loaded with the provided
	* RawApplicationPackets
	*/
	public void createTransitioningSpacecraftDiaryRDR(RawApplicationPackets sdrap) throws RtStpsException{
		if(spacecraftDiaryRDR_MultipleGranule == null){
			spacecraftDiaryRDR_MultipleGranule = new SpacecraftDiaryRDR_MultipleGranule(stats, allData, dataProducts, FixedDomainDescription.dev, sdrap);
			spacecraftDiaryRDR_MultipleGranule.setGranuleTimeSpan(this.granuleTimeSpan);
		}
		else{
			System.out.println("First SpacecraftAOS Diary granule already exists!");
		}		
	}

	/**
	* Closes all open RawApplicationPacket objects for the given packet's App ID
	*/
	public void closeAllRaps() throws RtStpsException{
		for (RDR rdr : rdrs.values()) {
		    	rdr.closeAllRaps();
		} 
	}

	/**
	* Sets the desired granule time span for RDR files
	* @param timespan Desired granule time span (in microseconds) for RDR files
	*/
	public void setTimeSpan(long timespan){
		this.granuleTimeSpan = timespan;
	}
}
