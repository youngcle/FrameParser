/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.LinkedList;
import java.util.List;

/**
 * The basic guts of building the RDR HDF infrastructure. This class should be overridden
 * for each specific RDR sensor including the SpacecraftAOS Diary in use.
 * 
 * This is the class that provides utilities for creating XXX-RDR/XXX-RDR_Gran_n datasets and
 * their corresponding XXX-RDR_All/RawApplicationPackets_n
 */
public abstract class RDR {
	private RDRName rdrName;
	private RDRAll rdrAll;
	private RDRProduct rdrProduct;
	private LinkedList<RawApplicationPackets> raps = new LinkedList<RawApplicationPackets>();
	
	// To keep a handle on the latest full RAP dataspace (for multi-RDR only)
	private RawApplicationPackets lastRAP = null;

	// To keep track of granule boundaries and thresholds (for multi-RDR only)
	private long earliestFirstTime = 0L;
	private long currentFirstTime = 0L;
	private long startBoundaryThreshold = 0L;
	private long endBoundaryThreshold = 0L;
	private long lastGranuleEndBoundary = 0L;
	private long granuleTimeSpan = 0L;

	private int depth = 20;
	//private int[] appIds = new int[2048];  // 2047 is always idle
	private int setNum = 0; //-1; // UGLY
	private List<Granule> granuleList; // = new LinkedList<Granule>();  // temp possibly instead of re-reading HDF
	public static String DocumentName = "D34862-02_NPOESS-CDFCB-X-Vol-II_D_20090603_I1.5.0.pdf"; // default

	private Aggregate aggregate;
	private PacketPool packetPool;
	
	/**
	 * Only used by SpacecraftDiary RDR
	 */
	@Deprecated
	RDR() {
		
	}
	
	/**
	 * Build an RDR around the name, and other items
	 * @param rdrName the RDR name
	 * @param allData the AllData object
	 * @param dataProds the DataProducts object
	 * @param dev the Development domain
	 * @throws RtStpsException wraps various HDF exceptions
	 */
	public RDR(Stats stats, RDRName rdrName, AllData allData, DataProducts dataProds, FixedDomainDescription dev) throws RtStpsException {
		this.rdrName = rdrName;
		rdrAll = allData.createRDRAll(rdrName);
		
		rdrProduct = dataProds.createRDRProduct(rdrName,
							rdrName.getSensor(), 
							Collection.fromRDRName(rdrName),
							FixedDomainDescription.dev);
		// packetPool = new PacketPoolBySize(rdrName.toString(), stats);
		packetPool = new PacketPoolHeap(rdrName.toString(), stats);
		
		// Make an initial RawApplicationPacket dataspace
		// FIXME Make sure that subclasses properly implement the createRawApplicationPackets function, 
		// complete with a getRaps().push()
		RawApplicationPackets rap = this.createRawApplicationPackets(rdrName);
  
		//FIXME push() here is anomalous! ALL implementations of createRawApplicationPackets(rdrName) already calls push(rap)!
		// This causes TWO handles to the same RAP dataspace to be stored in the raps LinkedList, and causes HDF5-DIAG errors
		// later when the same RAP dataspace is closed MORE THAN ONCE!
		// getRaps().push(rap);
		this.setDepth(rdrName.getDepth());
	}

	/**
	 * Build an RDR around the name, and other items. Also creates a starting Granule using the
	 * provided RawApplicationPackets dataspace.
	 * @param rdrName the RDR name
	 * @param allData the AllData object
	 * @param dataProds the DataProducts object
	 * @param dev the Development domain
	 * @param RAP the initial RawApplicationPackets dataspace
	 * @throws RtStpsException wraps various HDF exceptions
	 */
	public RDR(Stats stats, RDRName rdrName, AllData allData, DataProducts dataProds, FixedDomainDescription dev, RawApplicationPackets RAP) throws RtStpsException {
		this.rdrName = rdrName;
		rdrAll = allData.createRDRAll(rdrName);
		
		rdrProduct = dataProds.createRDRProduct(rdrName,
							rdrName.getSensor(), 
							Collection.fromRDRName(rdrName),
							FixedDomainDescription.dev);
		
		packetPool = new PacketPoolHeap(rdrName.toString(), stats);
		this.setDepth(rdrName.getDepth());

		// Write and create a granule for the given RawApplicationPackets:
		if(RAP.getSetNum() != this.getSetNum())
			RAP.setSetNum(this.getSetNum());
		if(rdrAll.write(RAP)){
			createGranule(RAP);
			lastRAP = RAP;
		}
		else{
			System.out.println("Could not pre-load " + RAP.getRdrName() + " with the given raw application packets.");
		}

		RawApplicationPackets rap = this.createRawApplicationPackets(rdrName);
  
		//FIXME push() here is anomalous! All implementations of createRawApplicationPackets(rdrName) already calls push(rap)!
		// This causes TWO handles to the same RAP dataspace to be stored in the raps LinkedList and causes HDF5-DIAG errors
		// later when the same RAP dataspace is closed MORE THAN ONCE!
		//getRaps().push(rap);
	}

	/**
	 * Put a packet into the RDR.
	 * @param p the Packet of interest
	 * @throws RtStpsException wraps any HDF or other exceptions
	 */
	public void put(Packet p) throws RtStpsException {
		
		RawApplicationPackets rap = this.getCurrentRawApplicationPackets();
		
		if(rap.getPercentMissingData() < 0)
		{
		    System.out.println("rap.getName(): " + rap.getName());
		    System.out.println("rap.getPercentMissingData(): " + rap.getPercentMissingData());
		}

		// Each RAP subclass (e.g. VIIRS_RDR_MultiGranule) have their own implementation of notFull()
		if (rap.notFull(p)) {
			rap.put(p);
		}  
		else {
			// Current RawApplicationPackets dataset is considered full already, so write to the HDF file...
			if (rdrAll.write(rap)) { 
			    	System.out.println("rap.getName(): " + rap.getName());
			    	System.out.println("rap.getPercentMissingData(): " + rap.getPercentMissingData());

				// now create the corresponding Granule dataset ((RDR Name)_Gran_N) for this RAP dataset, as
				// it must refer back to the RAP dataspace...
				this.createGranule(rap);

				// This RAP is now full and written to the HDF file, so we can close it.
				rap.close();
				// Take it off our List of RAP objects, as we are done with it...
				getRaps().pop(); 
			}

			// start a new rap and put the packet we have in there...
			// need to start a new rap if there are more packet to read...
			rap = this.createRawApplicationPackets(rdrName);
			rap.notFull(p);
			rap.put(p);			

			if ((getSetNum() % 100) == 0) {
				System.out.println("Passing Creation of Set " + getSetNum());
			}
		}
	}
	
	/**
	 * Same functionality as put(Packet p), but returns the boolean result of
	 * RawApplicationPackets.notFull(Packet p) to assist with the creation of
	 * multiple RDR files from a single pass. On false return, a new
	 * RawApplicationPackets object is NOT created, and the packet is NOT put
	 * anywhere!
	 * @param p the Packet of interest
	 * @param createNew boolean indicating whether to create new RawApplicationPackets when current one is full.
	 * @throws RtStpsException wraps any HDF or other exceptions
	 */
	public synchronized boolean checkThenPut(Packet p, boolean createNew) throws RtStpsException{
		// [M]: It is possible that this function get invoked multiple times after this RDR
		// has already stopped accepting data to create more RAPs/Granules (e.g. SpacecraftAOS
		// Diary). In this case, getCurrentRAP may return an exception if this RDR no longer
		// has any working/open RAPs in its "raps" linked list
		RawApplicationPackets rap = null;
		try{
			rap = this.getCurrentRawApplicationPackets();
		}
		catch(Exception e){
			System.out.println("RawApplicationPackets is already full and ready for closing");
			return false;
		}

		// [A]: Initial assumption is that granule is not full yet
		boolean granuleNotFull = true;

		if(rap.getPercentMissingData() < 0)
		{
		    System.out.println("rap.getName(): " + rap.getName());
		    System.out.println("rap.getPercentMissingData(): " + rap.getPercentMissingData());
		}

		// Each RAP subclass (e.g. VIIRS_RDR_MultiGranule) have their own implementation of notFull()
		granuleNotFull = rap.notFull(p);

		if (granuleNotFull) {
			// Current RawApplicationPackets is not yet full. It is safe to put the packet in.
			rap.put(p);
			
			// FIXME: When/how often does the RAP's first time has to be polled to get boundaries ASAP?
			// If we have no boundary info yet, record some from the current RAP if possible.
			// RAP only records time/boundary info once the packet is actually put, so this has to be after rap.put(p)
			long rapFirstTime = rap.getFirstTime();

			// Calculate granule boundary thresholds if they're still at default values
			if(earliestFirstTime <= 0L && rapFirstTime > 0L){
				earliestFirstTime = rapFirstTime;	
				startBoundaryThreshold = GranuleBoundaryCalculator.getBoundary(rap, 0);
				//endBoundaryThreshold = GranuleBoundaryCalculator.getBoundary(rap, 1);
				endBoundaryThreshold = GranuleBoundaryCalculator.getAdjustedEndBoundary(rap, this.granuleTimeSpan);
			}

			// Record this as the latest RAP first time we've seen, and update latest granule end boundary seen
			if(rapFirstTime > currentFirstTime){
				currentFirstTime = rapFirstTime;
				lastGranuleEndBoundary = GranuleBoundaryCalculator.getBoundary(rap, 1);
			}
		}  
		else {
			// Make sure RDR and RAP's setNums match before writing to file
			if(rap.getSetNum() != this.getSetNum()){
				rap.setSetNum(this.getSetNum());
			}

			// Current RAP dataset is full already, so create a granule for it and write to the HDF file...
			if (rdrAll.write(rap)) {
			   	System.out.println("rap.getName(): " + rap.getName());
			   	System.out.println("rap.getPercentMissingData(): " + rap.getPercentMissingData());

				// now create the granule because it must refer back to the RAP dataspace...
				this.createGranule(rap);
			}
	
			// Preserve the most recently completed RAP as the new lastRAP; close the previous lastRAP.
			if(lastRAP != null)
				lastRAP.close();
			lastRAP = getRaps().pop();

			// If createNew is true, create new RawApplicationPackets dataset
			if(createNew){
				rap = this.createRawApplicationPackets(rdrName);
				granuleNotFull = rap.notFull(p);
				rap.put(p);

				// Update RAP time/granule boundary information
				long rapFirstTime = rap.getFirstTime();
				if(rapFirstTime > currentFirstTime){
					currentFirstTime = rapFirstTime;
					lastGranuleEndBoundary = GranuleBoundaryCalculator.getBoundary(rap, 1);
				}
			}
			else{
				rap = null;
			}
		}
		return granuleNotFull;
	}
	
	/**
	* Closes all active RawApplicationPackets. Does NOT create granules NOR write to the file!
	* Mainly for emergency purposes in case something fails.
	*/
	public void closeAllRaps() throws RtStpsException{
		// Close current RAP
		try{
			RawApplicationPackets rap = this.getCurrentRawApplicationPackets();
			rap.close();
			getRaps().pop();
		}
		catch(java.util.NoSuchElementException e){
			//Do nothing; this only means that this RDR is not working on any RAPs right now.
		}
		
		// Close previous RAP, if not already closed
		if(lastRAP != null){
			lastRAP.close();
			lastRAP = null;
		}

		// Close remaining RAPs remaining...
		for (RawApplicationPackets rap : raps) {
			rap.close();
		}
	}

	/**
	 * Override this in the specific specific sensor RawApplicationPacket class
	 * @param rdrName the RDE name
	 * @return the specific RawApplicationPackets for the sensor in the super class
	 */
	protected abstract RawApplicationPackets createRawApplicationPackets(RDRName rdrName);
	
	/**
	 * Get the RDR name
	 * @return the RDRName
	 */
	public RDRName getRDRName() {
		return rdrName;
	}
	
	/**
	 * Get the RDRAll object
	 * @return the RDRAll object
	 */
	public RDRAll getRDRAll() {
		return rdrAll;
	}
	
	/**
	 * Get the RDRProduct
	 * @return the RDRProduct
	 */
	public RDRProduct getRDRProduct() {
		return rdrProduct;
	}
	
	/**
	 * Return a list of RawApplicationsPacket objects that have been created in processing
	 * @return the List of RawApplicationPackets
	 */
	public List<RawApplicationPackets> getAllRawApplicationPackets() {
		return getRaps();
	}

	/**
	 * Get the first (active) RawApplicationsPacket that have been created in processing
	 * @return the first RawApplicationPackets
	 */
	public RawApplicationPackets getCurrentRawApplicationPackets() {
		
		//System.out.println("Raps size: " + raps.size());
		return getRaps().getFirst();
	}

	/**
	* Get this RDR's start or end boundary threshold.
	* @param startEnd 0 for start boundary, anything else for end boundary.
	* @return the specified boundary threshold (Calculated CDS time -> IET boundary based on instrument)
	*/
	public long getBoundaryThreshold(int startEnd){
		if( startEnd == 0 )
			return this.startBoundaryThreshold;
		else
			return this.endBoundaryThreshold;
	}

	/**
	* Get the latest granule end boundary encountered.
	* @return latest granule end boundary encountered.
	*/
	public long getLastGranuleEndBoundary(){
		return this.lastGranuleEndBoundary;
	} 

	/**
	* Get the latest RawApplicationPackets dataset.
	* @return latest RawApplicationPackets dataset.
	*/
	public RawApplicationPackets getLastRawApplicationPackets(){
		// If current RAP exists, we return it as the latest RAP
		try{
			RawApplicationPackets latestRAP = this.getCurrentRawApplicationPackets();
			if(latestRAP != null){
				System.out.println("Returning the currently active RawApplicationPackets");
				return latestRAP;
			}
		}
		catch(java.util.NoSuchElementException e){
			System.out.println("No active RawApplicationPackets dataset exists;" +
						" returning last completed RawApplicationPackets instead");
		}

		// If there is no current RAP being worked on, return the last completed RAP instead
		return this.lastRAP;
	}
	
	/**
	 * Finish up any local cleanup and close the rdrAll and rdrProduct...
	 * @throws RtStpsException wraps any HDF or other exceptions
	 */
	public void close() throws RtStpsException {
		finish();
		
		// After all writes & granule creations are successful, close all open RAPs
		for (RawApplicationPackets rap : raps) {
			rap.close();
		}

		// Close the previous rap, if not already closed.
		if(lastRAP != null){
			lastRAP.close();
			lastRAP = null;
		}

		// close rdrAll and rdrProducts		
		packetPool.drain();  // send all the packets back to the heap
		rdrAll.close();
		rdrProduct.close();
	}
	
	/**
	 * Used to create the granule dataset (e.g. Data_Products/XXX-SCIENCE-RDR/XXX-SCIENCE-RDR_Gran_0) associated with the RawApplicationPacket structure, this method
	 * should be sufficient for most science sensors except for the SpacecraftAOS Diary which is unique
	 * and has its own implementation.
	 * @param rap the RawApplicationPackets associated with a particular sensor
	 * @throws RtStpsException wraps any HDF or other exceptions
	 */
	protected void createGranule(RawApplicationPackets rap) throws RtStpsException {

		// no packets, do nothing...
		if (rap.getTotalPacketCounts() <= 0){
		    System.out.println("rap.getTotalPacketCounts() is: " + rap.getTotalPacketCounts());
		    return;
		}
		
		// FIXME: Time spans for supported instruments are currently hard-coded here. Make these static somewhere?
		long timeSpan = 0L;
		String granSensor = rdrName.getSensor().toString();
		String granTypeID = rdrName.getTypeID().toString();

		if(granSensor.equals("VIIRS") && granTypeID.equals("SCIENCE")){
			timeSpan = 85350000L;
		}
		else if(granSensor.equals("ATMS") && granTypeID.equals("SCIENCE")){
			timeSpan = 31997000L;
		}
		else if(granSensor.equals("CRIS") && granTypeID.equals("SCIENCE")){
			timeSpan = 31997000L;
		}
		else if(granSensor.equals("OMPS-LP") && granTypeID.equals("SCIENCE")){
			timeSpan = 37437000L;
		}
		else if(granSensor.equals("OMPS-NP") && granTypeID.equals("SCIENCE")){
			timeSpan = 37405000L;
		}
		else if(granSensor.equals("OMPS-TC") && granTypeID.equals("SCIENCE")){
			timeSpan = 37405000L;
		}
		else if(granSensor.equals("SPACECRAFT") && granTypeID.equals("DIARY")){
			timeSpan = 20000000L;
		}
		else{
			timeSpan = rap.getTimeSpan();	
		}

		// HACK doesn't matter, as all granules now use correct post-launch NPP base time
		GranuleId granId = new GranuleId(SpacecraftId.npp, LeapDate.getMicrosSinceEpoch(rap.getFirstTime()), 0 /** HACK **/, timeSpan);
		
		System.out.println("rdrName inside createGranule: " + rdrName);
		System.out.println("Granule's timeSpan is: " + timeSpan);
		System.out.println("granuleID of this Granule: " + granId);
		
		//rap.
		if(rap.getDataSpace() < 0)
		{    
		    System.out.println("Dataspace was < 0, not writing granule.");
		    return;
		}
		Granule granule = Granule.factory(rap, 
						  1L,
						  new GranuleId(SpacecraftId.npp, 
							  LeapDate.getMicrosSinceEpoch(rap.getFirstTime()), 
							  0 /** HACK **/, timeSpan),
						  LEOAFlag.Off,
						  DocumentName ,
						  rap.getPacketTypes(),
						  rap.getPacketTypeCounts(),
						  new ReferenceId(),
						  getSetNum(), 
						  rap.getDataSpace(), 
						  rdrName);
		//granuleList.add(granule);
		
		rdrProduct.write(granule); // this also calls granule close...

		// This MUST be here or in any specialized implementation, to maintain correct numbering of
		// RawApplicationPackets and Granule datasets!
		this.nextSetNum();
	}
	
	/**
	 * Build the Aggregate and other wise clean up ...
	 * @throws RtStpsException
	 */
	private void finish() throws RtStpsException {
	    	System.out.println("finishing: " + this.rdrName);

		if( getRaps().size() > 0 ){
			RawApplicationPackets rap = this.getCurrentRawApplicationPackets();
			
			// Make sure RDR and RAP's setNums match before writing to file
			if(rap.getSetNum() != this.getSetNum()){
				rap.setSetNum(this.getSetNum());
			}

			if (rdrAll.write(rap)) {
				this.createGranule(rap);
			} else {
				System.out.println("RDR processing has incomplete granule, skipping...");
			}
			rap.close();
			getRaps().pop();
		}

		GranuleId beginningGranuleId = rdrProduct.getBeginningGranuleId();
		GranuleId endingGranuleId = rdrProduct.getEndingGranuleId();
		
		// if no granules were processed, just quietly leave and move on...
		if ((beginningGranuleId == null) || (endingGranuleId == null)) return;
		
		/* OLD: Aggregate timestamps (and consequently, filename) based on packet start/end times
		aggregate =  Aggregate.factory(beginningGranuleId,
						endingGranuleId,
						rdrProduct.getBeginningOrbit(),
						rdrProduct.getEndingOrbit(),
						rdrProduct.getBeginningDateTime(),
						rdrProduct.getEndingDateTime(),
						rdrProduct.getGranuleCount(),
						rdrName);
		*/
		// NEW: Aggregate timestamps (and consequently, filename) based on granule start/end boundaries
		aggregate =  Aggregate.factory(beginningGranuleId,
						endingGranuleId,
						rdrProduct.getBeginningOrbit(),
						rdrProduct.getEndingOrbit(),
						rdrProduct.getBeginningTimeDateTime(),
						rdrProduct.getEndingTimeDateTime(),
						rdrProduct.getGranuleCount(),
						rdrName);

		// Write the aggregate to the file, and close it
		rdrProduct.write(aggregate); 
	}

	/**
	 * Get the product identifier. ProductIdentifiers.java has a straightforward way of
	 * correctly identifying the products' instrument source based on RDRName.
	 * @return the ProductIdentifiers
	 */
	public ProductIdentifiers getProductId() {
		return ProductIdentifiers.fromShortName(rdrName.getRDRStringName());
	}

	/**
	 * The list of RawApplicationPackets as LinkedList
	 * @return LinkedList of RawApplicationPackets
	 */
	final LinkedList<RawApplicationPackets> getRaps() {
		return raps;
	}

	/**
	 * A list of the created granule objects
	 * @return List of Granule
	 */
	@Deprecated
	final List<Granule> getGranules() {
		// NOTE: this will return null at at this time
		// if you do not want to return null, create the list for the field above
		// and uncomment the list.add() where the granules are created...
		// it was turned off due to space (heap) issues for some RDRs and is not currently
		// being used by any active classes...
		return granuleList;  
	}
	
	/**
	 * Return the aggregate object that was created if this RDR was closed.
	 * @return Aggregate instance
	 */
	final Aggregate getAggregate() {
		return aggregate;
	}

	/**
	 * Increment the set number
	 */
	final void nextSetNum() {
		this.setNum++;
	}

	/**
	 * Get the current set number
	 * @return the set number in an int
	 */
	final int getSetNum() {
		return setNum;
	}

	/**
	 * The counting depth of RawApplicationPacket, this is associated with an old implementation and probably 
	 * should be deprecated
	 * @param depth the new depth
	 */
	final void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * The current depth...
	 * @return the depth in an int
	 */
	final int getDepth() {
		return depth;
	}
	
	final PacketPool getPacketPool() {
		return packetPool;
	}

	public void setGranuleTimeSpan(long gtspan){
		this.granuleTimeSpan = gtspan;
	}
}
