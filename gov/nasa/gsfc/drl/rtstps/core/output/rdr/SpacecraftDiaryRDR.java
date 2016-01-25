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

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

/**
 * Implements a special case of {@link RDR} for the SpacecraftDiary.
 * All attitude and ephemeris packets are accumulated until the
 * all other sensor data RDRs have been processed.  Then the packets
 * held in this class are used to build the SpacecraftAOS Diary.
 * 
 * This is the original implementation which attempted to correlate to the sensor granules after the sensor granule processing
 * It was used in special case in the code which may still be there but commented out.
 *
 */
@Deprecated
public class SpacecraftDiaryRDR extends RDR {
	private AllData allData;
	private DataProducts dataProds;
	private RDRName rdrName;
	private RDRAll rdrAll;
	private RDRProduct rdrProduct;
	private LinkedList<RawApplicationPackets> raps = new LinkedList<RawApplicationPackets>();
	private List<Packet> packetList = new LinkedList<Packet>();
	

	/**
	 * Construct a new SpacecraftDiary RDR object
	 * @param allData the AllData object
	 * @param dataProds te DataProducts object
	 * @param dev the Development domain
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	public SpacecraftDiaryRDR(AllData allData, DataProducts dataProds, FixedDomainDescription dev) throws RtStpsException {
		this.allData = allData;
		this.dataProds = dataProds;
		
		this.rdrName = RDRName.NPP_Ephemeris_and_Attitude;
		rdrAll = allData.createRDRAll(rdrName);
		
		rdrProduct = dataProds.createRDRProduct(RDRName.NPP_Ephemeris_and_Attitude,
												rdrName.getSensor(), 
												Collection.fromRDRName(rdrName),
												FixedDomainDescription.dev);
	}
	
	/**
	 * Accumulate packets in the SpacecraftRDR packetlist, all of them, they are processed at the end
	 * after all the other RDRs have been created.
	 * @param p an attitude and ephemeris packet
	 */
	public void put(Packet p) throws RtStpsException {
		
		// the packet is deep copied, it is allocated off a pool
		// which may hold previously copied packets...  they must be put
		// back on the pool when done...
		
		Packet pcopy = CopyPacket.deep(p, getPacketPool());
		
		packetList.add(pcopy);

	}
	
	/**
	 * This is not used by this class
	 */
	protected RawApplicationPackets createRawApplicationPackets(RDRName rdrName)  {
		throw new RtStpsRuntimeException("The method put is not used by the SpacecraftAOS Diary RawApplication class");
	}
	
	/**
	 * Get the RDRName
	 */
	public RDRName getRDRName() {
		return rdrName;
	}
	
	/**
	 * Get the RDRAll object
	 */
	public RDRAll getRDRAll() {
		return rdrAll;
	}
	
	/**
	 * Get the RDRProduct object
	 */
	public RDRProduct getRDRProduct() {
		return rdrProduct;
	}
	
	/**
	 * Get all the RawApplicationPacket objects created by this routine
	 */
	public List<RawApplicationPackets> getAllRawApplicationPackets() {
		return raps;
	}

	/**
	 * Get the current RawApplicationPacket object
	 */
	public RawApplicationPackets getCurrentRawApplicationPackets() {
		return raps.getFirst();
	}
	
	
	/**
	 * Not supported
	 */
	public void close() {
		throw new RtStpsRuntimeException("Close method without arguments, not used in SpacecraftDiaryRDR");
	}
	
	/**
	 * In the SpacecraftAOS Diary, all the work is done here.   It is assumed this is the last
	 * RDR close to be called.
	 * The granules of the other RDRs are used to determine the time-space of each SpacecraftDiary entry,
	 * the granule and RawApplicationPacket
	 * @param collection The collection associated wih this RDR
	 */
	public void close(java.util.Collection<RDR> collection) throws NullPointerException, HDF5Exception, RtStpsException {
		
		createSpacecraftDiaryRDR(collection.iterator().next()); // FIXME only one supported right now
				
		// once its all been created, close up the rdr all and product areas
		rdrAll.close();
		rdrProduct.close();
		
		// move all the packets to the pool...
		getPacketPool().flush(packetList);
	}
	

	/**
	 * Not supported
	 */
	protected void createGranule(RawApplicationPackets rap) {
		throw new RtStpsRuntimeException("createGranule not used in SpacecraftDiaryRDR");
	}
	
	/**
	 * Create the aggregrate for this RDR
	 * @throws RtStpsException any HDF exceptions are wrapped in an RtStpsException
	 */
	private void createAggregate() throws RtStpsException {
	
		Aggregate aggregate =  Aggregate.factory(rdrProduct.getBeginningGranuleId(),
												rdrProduct.getEndingGranuleId(),
												rdrProduct.getBeginningOrbit(),
												rdrProduct.getEndingOrbit(),
												rdrProduct.getBeginningDateTime(),
												rdrProduct.getEndingDateTime(),
												rdrProduct.getGranuleCount(),
												rdrName);
		
		rdrProduct.write(aggregate); // FIXME does this close the aggregate out?
	}

	/**
	 * Get the short name of the Product Identifier which is a ProductId
	 */
	public ProductIdentifiers getProductId() {
		return ProductIdentifiers.fromShortName(rdrName.getRDRStringName());
	}
	
	/**
	 * Create the spacecraft diary RDR by comparing the packet times stored in this packet list
	 * with those found in ONE sensor's RDR granule entries.  The packet times for the attitude and
	 * ephemeris must encapsulate the sensor granule times.
	 * - Iterate through the non-Diary RDR granules [only one supported now ] and process as follows:
	 * --- For each Granule in the sensor RDR get its beginning and end time
	 * --- Find the encapsulating this spacecraft diary packet list
	 * --- if no encapsulating time found, increment set number and continue
	 * --- With list of packets that encapsulate the sci granule, create the spacecraft diary granule and rawApp
	 * --- increment set number
	 * --- continue
	 * @param sensorRDR The sensor of interest
	 * @throws RtStpsException wraps any HDF exception
	 */
	private void createSpacecraftDiaryRDR(RDR sensorRDR) throws RtStpsException {
		//  - Iterate through the non-Diary RDR granules [only one supported now ] and process as follows:
		//  --- For each Granule in the sensor RDR get its beginning and end time
		//  --- Find the encapsulating this spacecraft diary packet list
		//  --- if no encapsulating time found, increment set number and continue
		//  --- With list of packets that encapsulate the sci granule, create the spacecraft diary granule and rawApp
		//  --- increment set number
		//  --- continue
		
		//System.out.println("We are working on SpacecraftAOS Diary RDR");
		
		// build a time managed packet list with all the Attitude and Emphemeris packets
		TimeManagedPacketList tmpl = new TimeManagedPacketList(this.packetList);
		//System.out.println("Time Managed List size: " + tmpl.size());
		
		// loop the granules of the sensor RDR already created...  and then
		for (Granule g : sensorRDR.getGranules()) {
			
			// try to return a span of the A&E packets for that granules time span
			TimeSpanPacketList tspl = tmpl.get(g.getBeginningIET(), g.getEndingIET());
			
			//System.out.println("Time span packet list: " + tspl.size());
			
			// create the raw application packet for the Diary with eresult
			RawApplicationPackets sdrap = createSpacecraftDiaryRawApplicationPackets(tspl, g.getGranuleNumber());
			
	
			// put the rap in the rap list, although this is not currently used
			raps.push(sdrap);
			
			// first write the  RawApp, this creates a dataspace the granule needs
			rdrAll.write(sdrap);
			
			// then create it's granule, it uses the RawApps dataspace...
			Granule sdg = createSpacecraftDiaryGranule(sdrap, g.getGranuleNumber());
			
			
			rdrProduct.write(sdg); // this also calls granule close...

			
			// then close the RawApp ... convoluted, nice to fix it one day...
			sdrap.close();
			//sdg.close();  // already closed above
		}
		
		// finally create the aggregate
		createAggregate();
	}

	/**
	 * Create the SpacecraftDiary granule
	 * @param rap the RawApplicationPackets object of interest
	 * @param granuleNumber the granule number
	 * @return the create Granule object
	 * @throws RtStpsException wraps any HDF exception
	 */
	private Granule createSpacecraftDiaryGranule(RawApplicationPackets rap, int granuleNumber) throws RtStpsException {
				

		Granule granule = Granule.factory(rap,
										1L,
										new GranuleId(SpacecraftId.npp, rap.getFirstTime(), 0 /** HACK **/, rap.getTimeSpan()),
										LEOAFlag.Off,
										RDR.DocumentName,
										rap.getPacketTypes(),
										rap.getPacketTypeCounts(),
										new ReferenceId(),
										granuleNumber, 
										rap.getDataSpace(), 
										rdrName);	
		
		return granule;
		
	}

	/**
	 * Create a SpacecraftDiaryRawApplicationPackets object
	 * @param tspl a list of packet, time ordered for a given span of time
	 * @param rapNumber the raw application packet set number
	 * @return the created SpacecraftDiaryRawApplicationPackets object
	 */
	private RawApplicationPackets createSpacecraftDiaryRawApplicationPackets(TimeSpanPacketList tspl, int rapNumber) {
		SpacecraftDiaryRawApplicationPackets sdrap = new SpacecraftDiaryRawApplicationPackets(SpacecraftId.npp, rapNumber, getPacketPool());
		
		System.out.println("SpacecraftDiary Granule_" + rapNumber + " is " + tspl.getTimeSpanType());
		
		sdrap.putAll(tspl.getList());
		//System.out.println("S/C Diary list size: " + sdrap.getPacketList().size());
		//System.out.println("S/C Diary Quality: " + tspl.getTimeSpanType());
		
		return sdrap;
	}


	

	
	
}
