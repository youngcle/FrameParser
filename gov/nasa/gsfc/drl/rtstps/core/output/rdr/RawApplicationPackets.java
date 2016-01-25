/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * An abstract class to support the creation of a raw application packet dataset in an HDF file for an RDR such as:
 * '/All_Data/VIIRS-SCIENCE-RDR/RawApplicationPackets0'
 * Note that several of these routines must be overridden by the implementation for a specific sensor, and that
 * the original version used a certain algorithm whose vestiges remain in this class.  Where possible this has been
 * more fully generilized with abstract methods and no implementation.
 * The dataset is created based on a timed holding of packets hung up in the object. (constructor, notFull, put)
 * The time is based on the clock time when the object was created and a given user argument of seconds in the future.
 * It assumed the object will be used immediately to put packets in it and then create the HDF dataset.
 * Once the time span has occurred, the accumulated packets may then be written to the HDF file,  
 * created in the RDR dataset structure. (writeRDR)
 * A static header is calculated from the packets given to the object, then the packets
 * themselves are written to the dataset.
 * The object should be explicitly closed by calling close or the HDF API will get a heap exception eventually.
 * 
 *
 */
public abstract class RawApplicationPackets {
	private int dataSpace = -1;
	private int dataSet = -1;

	//For co-temporal purposes
	private int dataSpaceTimesSC = 0;
	private int dataSetTimesSC = 0;
	
	private int setNum;
	private RDRName rdrName;
	private SpacecraftId satellite;

	// Costly as it writes by taking current snapshot first, but is incredibly thread-safe in return
	private CopyOnWriteArrayList<Packet> packetList = new CopyOnWriteArrayList<Packet>();

	private int readId = -1;
	private byte[] data = null;
	
	private StaticHeader staticHeader;
	
	private long firstTime = 0L; // these are packets times
	private long lastTime = 0L;
	
	private long firstSystemTime = 0L;  // this is a system time
	private long lastSystemTime = 0L;
	
	private String name;
	
	private int[] appIds = new int[2048];  // 2047 is always idle
	
	private int size = 0;
	
	PacketPool packetPool;
	private int allRDRId;
	
	long scanCounter = 0L;
	long scansPerGranule = 0L;
	private float percentMissingData = 0.0f; // technically this hard coded for FTs

	public float getPercentMissingData() {
		return percentMissingData;
	}

	public void setPercentMissingData(float percentMissingData) {
		this.percentMissingData = percentMissingData;
	}

	/**
	 * Constructor for creating an nth instance of a raw application data packet area
	 * @param satellite  the name of the spacecraft
	 * @param rdrName  the rdrName of the RDR dataset (i.e. VIIRS-SCIENCE-RDR)
	 * @param setNum  the set number
	 */
	public RawApplicationPackets(SpacecraftId satellite, RDRName rdrName, int setNum, PacketPool packetPool) {
		
		this.name = rdrName.getRDRStringName();
		
		this.satellite = satellite;
		this.rdrName = rdrName;
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
		
		this.setNum = setNum;
		
		this.packetPool = packetPool;
	}
	
	/**
	 * Constructor which attempts to read the RawApplicationPacket entry that pre-exists.
	 * The contents of the dataspace are read into a memory buffer... assuming it will fit.
	 * @param allRDRId  the rdrAll Groups id
	 * @param setNum the set number of raw entry
	 * @throws RtStpsException 
	 */
	public RawApplicationPackets(int allRDRId, int setNum) throws RtStpsException  {
		size = 0;
		this.allRDRId = allRDRId;
		try {
			name = "RawApplicationPackets_" + setNum ;
			readId = H5.H5Dopen( allRDRId, name);
			size = (int) H5.H5Dget_storage_size(readId);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e); 
		}
		//System.out.println("And the " + setNum + "th size is -- " + size);

		data = new byte[size];
		try {
			H5.H5Dread(readId, 
					HDF5Constants.H5T_STD_U8BE,
					HDF5Constants.H5S_ALL,
					HDF5Constants.H5S_ALL, 
					HDF5Constants.H5P_DEFAULT,
					data);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e); 
		}
		
	}
	
	public String getName() {
		// FIXME this should read the name out of the HDF...
		return name;
	}
	
	public long getScanCounter() {
		return scanCounter;
	}

	/**
	 * Constructor which attempts to read the RawApplicationPacket entry that pre-exists.
	 * This constructor is only used by Granule (indirectly) when it dereferences to the RawAppPackets of
	 * interest, through the factory method.  This constructor should only be used internally at this time.
	 * @param readId  the dataspace Id of the RawApp of interest
	 * @param setNum the set number of raw entry
	 * @param usedByGranuleOnly a flag (value not used) to differentiate and imply its intended use
	 * @throws RtStpsException 
	 */
	RawApplicationPackets(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException  {
		size = 0;
		this.readId = readId;
		try {
			size = (int) H5.H5Dget_storage_size(readId);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e); 
		}
		//System.out.println("And the " + setNum + "th size is -- " + size);

		data = new byte[size];
		try {
			H5.H5Dread(readId, 
					HDF5Constants.H5T_STD_U8BE,
					HDF5Constants.H5S_ALL,
					HDF5Constants.H5S_ALL, 
					HDF5Constants.H5P_DEFAULT,
					data);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e); 
		}
	}
	/**
	 * Constructor doesn't do anything but some minor set up -- it is used by RDRAlLRandomAccessReader only.
	 * After the constructor is called the read method must be called to actually open the RawApplicationPacket.
	 * @param readId  the dataspace Id of the RawApp of interest
	 * @param setNum the set number of raw entry
	 * @param specialCase a flag (value not used) to differentiate and imply its intended use
	 * @param usedByRandomAccessReaderOnly a flag (value not used) to differentiate and imply its intended use
	 * @throws RtStpsException 
	 */
	RawApplicationPackets(int allRDRId, int setNum, boolean specialCase, boolean usedByRandomAccessReaderOnly) throws RtStpsException  {
		this.size = 0;
		this.allRDRId = allRDRId;
		name = "RawApplicationPackets_" + setNum ;
	}
	
	/**
	 * Open the RawApplicationPacket entry that pre-exists.
	 * This should only be used by RDRAllRandomAccessReader
	 * @throws RtStpsException 
	 */
	public void open() throws RtStpsException  {
		try {
			readId = H5.H5Dopen( allRDRId, name);
			size = (int) H5.H5Dget_storage_size(readId);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e); 
		}

		//System.out.println("And the " + setNum + "th size is -- " + size);
		data = new byte[size];
		try {
			H5.H5Dread(readId, 
					HDF5Constants.H5T_STD_U8BE,
					HDF5Constants.H5S_ALL,
					HDF5Constants.H5S_ALL, 
					HDF5Constants.H5P_DEFAULT,
					data);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e); 
		}
		
	}
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Retrieve the static header in RDR, this is for a pre-existing RDR
	 * @return the StaticHeader object
	 */
	public StaticHeader getStaticHeader() {
		return new StaticHeader(data);
	}
	
	/*
	* Create a dataset that will hold the header info and packet storage
	* @param hdfFile integer handle to the HDF file
	* @param dataSetSize size of the static header to write to HDF file
	*/
	private void createDataSet(int hdfFile, int dataSetSize) throws RtStpsException {
		//System.out.println("hdfFile = " + hdfFile + " dateSetSize = " + dataSetSize);
		long totalDims[] = new long[1];
		totalDims[0] = dataSetSize;
		
		// If dataSpace and dataSet are already initialized to something, close them
		// first before initializing them to new dataspaces/datasets. We don't want to
		// create orphaned identifiers and cause a memory leak. This should be safe as long
		// as all RDRs that use this RAP write this to file before this RAP's packets are flushed
		if(dataSpace >= 0){
			try{
				H5.H5Sclose(dataSpace);
			}
			catch(HDF5LibraryException e){
				System.out.println("RAP dataspace " + dataSpace + " H5Sclose fail." +
							" Dataspace may be closed already.");
			}
		}
		if(dataSet >= 0){
			try{
				H5.H5Dclose(dataSet);
			}
			catch(HDF5LibraryException e){
				System.out.println("RAP dataset " + dataSet + " H5Dclose fail." +
							" Dataset may be closed already.");
			}	
		}

	    	// Create the data space with unlimited dimensions
		try {
			dataSpace = H5.H5Screate_simple (1, totalDims, null);
			dataSpaceTimesSC = 0;
		} catch (HDF5Exception e) {
			throw new RtStpsException(e);
		}  
		
		// was UNLIMITED... but no chunking?

	    	// Modify the dataset creation properties to enable chunking
	    	// chunkingProperties = H5.H5Pcreate (HDF5Constants.H5P_DATASET_CREATE);
	    	// H5.H5Pset_chunk (chunkingProperties, 1, chunkDims);
	    
	    	// Create the data set's name...
		String name = "/All_Data/" + rdrName.getRDRStringName() + "_All/RawApplicationPackets_" + setNum;
		
		try {
			//dataSet = H5.H5Dcreate(hdfFile, 
			//		name, 
			//		HDF5Constants.H5T_STD_U8BE, 
			//		dataSpace, 
			//		HDF5Constants.H5P_DEFAULT);

			// Then, create the actual RawApplicationPackets_N dataset in the HDF file:
			dataSet = H5.H5Dcreate(hdfFile, 
					name, 
					HDF5Constants.H5T_STD_U8BE, 
					dataSpace, 
					HDF5Constants.H5P_DEFAULT,
					HDF5Constants.H5P_DEFAULT,
					HDF5Constants.H5P_DEFAULT);
			dataSetTimesSC = 0;
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		} 

	}

	/**
	 * Determine if the RawApplicationPacket is full or not. Override this method
	 * to provide the implementation.
	 * @param p the packet to be added to the RawApplicationPacket
	 * @return true or false
	 * @throws RtStpsException 
	 */
	public abstract boolean notFull(Packet p) throws RtStpsException;
	
	/**
	 * Put a packet into the RawApplicationPacket after checking if it is full or not.  Override
	 * this method to provide the implementation.
	 * @param p packet to be written
	 * @throws RtStpsException 
	 */
	public abstract void put(Packet p) throws RtStpsException;
	
	/**
	 * Write the RDR dataset record to the HDF file. Keep in mind that specializations
	 * may override this function with their own version.
	 *
	 * @param hdfFile handle to the HDF file
	 * @return true if the write succeeds, false if it does not
	 * @throws RtStpsException 
	 */
	public boolean write(int hdfFile) throws RtStpsException {
		
		if (packetList.size() <= 0) return false;  // write nothing if no packets were collected
		//System.out.println("Working on RawApp: " + rdrName.getRDRStringName());
		//this.hdfFile = hdfFile;

		// Calculate the RDR file's static header, and give it all of the packets to track and
		// put into the RawApplicationPackets_N dataset
		staticHeader = new StaticHeader(satellite, rdrName, packetList);
		
		// Create a dataset that will hold the header info and packet storage
		createDataSet(hdfFile, staticHeader.getSize());
		
		// Write the header AND the packets to the dataset
		staticHeader.write(dataSet);
		
		return true;
	}
	
	/**
	 * Update the internal packet counters which are used in the creation of certain attributes for 
	 * example in the Granules.  
	 * Note: this method MUST be called by specializations of this class or the attributes will not
	 * be created properly.  See the VIIRS, ATMS and CRIS implementation for examples.
	 * 
	 * @param appId  the application identifier of interest
	 */
	public final void updateAppIdCounters(int appId) {
		//System.out.println("Counting appid = " + appId);
		this.appIds[appId]++;
		
	}
	
	public final String appIdCountersToString(int scans) {
		StringBuffer sb = new StringBuffer();
		boolean firstOne=true;
		for (int i = 0; i < 2048; i++) {
			if (appIds[i] != 0) {
				if (firstOne==false) {
					sb.append(", ");
				}
				sb.append(i);
				sb.append('[');
				sb.append(appIds[i]);
				sb.append(']');
				sb.append('[');
				sb.append((float)appIds[i]/(float)scans);
				sb.append(']');
				firstOne=false;
			}
		}
		return sb.toString();
	}
	/**
	 * Get the calculated application identifier counts
	 * @return an integer array of counts
	 */
	public final int[] getAppIdCounts() {
		return this.appIds;
	}
	
	/**
	 * Get the calculated packets types 
	 * @return a long array of packet types
	 */
	public final long[] getPacketTypes() {

		long[] packetTypeCounts;

		packetTypeCounts = new long[getTotalPacketCounts()];
		
		int packetTypesIndex = 0;
		for (int i = 0; i < this.appIds.length; i++) {
			if (this.appIds[i] != 0) {
				
				packetTypeCounts[packetTypesIndex] = this.appIds[i];
				++packetTypesIndex;
			}
		}
		
		return packetTypeCounts;
	}
	
	/**
	 * Return the packet types associated with each counter, these are string names
	 * @return an array of string names for the packets (see {@link PacketName}) that have counts
	 */
	public final String[] getPacketTypeCounts() {

		String[] packetTypes = new String[getTotalPacketCounts()];
		
		int packetTypesIndex = 0;
		for (int i = 0; i < this.appIds.length; i++) {
			if (this.appIds[i] != 0) {
				packetTypes[packetTypesIndex] = PacketName.fromAppId(i).toString();
				
				++packetTypesIndex;
			}
		}
		
		return packetTypes;
	}
	
	/**
	 * Return the total packet counts, summed
	 * @return the sum of all packet counts
	 */
	public final int getTotalPacketCounts() {

		int packetsCounted = 0;
		
		for (int i = 0; i < this.appIds.length; i++) {
			if (this.appIds[i] != 0) {
				++packetsCounted;
			}
		}
		
		return packetsCounted;
	}
	
	/**
	 * Get the first time of the first packet in this RawApplicationPackets that has a timestamp
	 * @return the 64-bit time in a signed long
	 */
	public final long getFirstTime() {
		return this.firstTime;
	}
	
	/**
	 * Get the first system time of the first packet in this RawApplicationPackets that has a timestamp
	 * @return the 64-bit time in a signed long
	 */
	public final long getFirstSystemTime() {
		return this.firstSystemTime;
	}
	
	/**
	 * Set the first time
	 * @param time the 64-bit time in a signed long
	 */
	public final void setFirstTime(long time) {
		this.firstTime = time;
	}
	
	/**
	 * Set the first system time
	 * @param time the 64-bit time in a signed long
	 */
	public final void setFirstSystemTime(long time) {
		this.firstSystemTime = time;
	}
	
	/**
	 * Get the last time of the last packet in this RawApplicationPackets that has a time stamp
	 * @return the 64-bit time in a signed long
	 */
	public final long getLastTime() {
		return lastTime;
	}
	
	/**
	 * Get the last system time of the last packet in this RawApplicationPackets that has a timestamp
	 * @return the 64-bit time in a signed long
	 */
	public final long getLastSystemTime() {
		return lastSystemTime;
	}
	
	/**
	 * Set the last time
	 * @param time the 64-bit time in a signed long
	 */
	public final void setLastTime(long time) {
		this.lastTime = time;
	}
	
	/**
	 * Set the last system time
	 * @param time the 64-bit time in a signed long
	 */
	public final void setLastSystemTime(long time) {
		this.lastSystemTime = time;
	}
	
	/**
	 * Return the internal packet list used to create the particular RawApplicationPackets
	 * This can be used when adding a packet to it.
	 * @return the List of Packet
	 */
	public final List<Packet> getPacketList() {
		return this.packetList;
	}
	/**
	 * Get the time space of the packets in this RawApplicationPacket.
	 * Note that if the first and last time are the same or this old many packets
	 * but only one has a timestamp, then this returns 1.
	 * 
	 * @return time space between the first and last packet, or 1 if they are the same
	 */
	public long getTimeSpan() {
		if (firstTime == lastTime) {
			return 1L;
		} else {
			// with a bit of luck this will avoid the sign issues...
			// first convert the long to a hex string... then...
			// Assumption the result will fit into the long as a positive
			// value...
			PDSDate lastTimePDS = new PDSDate(lastTime);
			PDSDate firstTimePDS = new PDSDate(firstTime);
			long lastTimeInMillis = lastTimePDS.getMicrosSinceEpoch();
			long firstTimeInMillis = firstTimePDS.getMicrosSinceEpoch();

			String lastTimeHexStr = Long.toHexString(lastTimeInMillis);
			String firstTimeHexStr = Long.toHexString(firstTimeInMillis);
			BigInteger lastTimeBig = new BigInteger(lastTimeHexStr, 16);
			BigInteger firstTimeBig = new BigInteger(firstTimeHexStr, 16);
			BigInteger timeSpanBig = lastTimeBig.subtract(firstTimeBig);
			return timeSpanBig.longValue();
		}
	}
	
	/**
	 * Get the HDF data set handle with the RawApplicationPackets
	 * @return the handle
	 */
	public int getDataSet() {
		return dataSet;
	}
	
	/** 
	 * Get the HDF data space associated with the RawApplicationPackets
	 * @return the handle to the data space as an <code>int</code>
	 */
	public int getDataSpace() {
		return dataSpace;
	}
	
	/**
	 * Close up the various HDF items.  If this is not called you will
	 * throw an out of heap exception eventually in HDF jars...
	 * 
	 * @throws RtStpsException 
	 */
	public void close() throws RtStpsException {
		try {
			//FIXME this dichotomy is part of an UGLY hack...
			if (readId < 0) {
				// these two sides are separate really, maybe they should not be in one object?
				packetPool.flush(packetList);  // critical or the pool is of no use...

				// H5.H5Pclose(chunkingProperties);
				// Partial granules may not have created these; if they are negative, don't close them
				if (dataSpace >= 0) {
					try{
						if(dataSpaceTimesSC == 0)
							H5.H5Sclose(dataSpace);
						dataSpaceTimesSC++;
					}
					catch(HDF5LibraryException e){
						System.out.println("Dataspace " + dataSpace + 
								" H5Sclose fail. Dataspace may be closed already.");
					}
				}
				if (dataSet >= 0) {
					try{
						if(dataSetTimesSC == 0)
							H5.H5Dclose(dataSet);
						dataSetTimesSC++;
					}
					catch(HDF5LibraryException e){
						System.out.println("Dataset " + dataSet + 
								" H5Dclose fail. Dataset may be closed already.");
					}
				}
			} 
			else {
				H5.H5Dclose(readId);
			}
		} 
		catch (HDF5LibraryException e) {
		    	return;
		}
	}
	
	public RDRName getRdrName() {
		return rdrName;
	}

	public void setRdrName(RDRName rdrName) {
		this.rdrName = rdrName;
	}

	// Get & set functions for this RawApplicationPackets' set number (setNum)
	public int getSetNum(){
		return setNum;
	}

	public void setSetNum(int newNum){
		this.setNum = newNum;
	}	
}
