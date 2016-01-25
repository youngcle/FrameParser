/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Implements the SpacecraftDiary RawApplication area which is a special case of most of
 * the other sensors supported by this package.  This class largely ignores or does not support
 * the infrastructure given by the RawApplicationPackets super class.  It does not accept packets
 * one at a time from the caller.  Instead it provides its own method to receive a list of packets
 * which make up one SpaceDiaryRawApplication area.
 * 
 *  Not being used
 */
public class SpacecraftDiaryRawApplicationPackets extends RawApplicationPackets {

	/**
	 * Constructor for creating an nth instance of a SpacecraftAOS Diary raw application data packet area
	 * @param satellite  the name of the spacecraft
	 * @param setNum  the set number
	 */
	public SpacecraftDiaryRawApplicationPackets(SpacecraftId satellite, int setNum, PacketPool packetPool) {
		super(satellite, RDRName.NPP_Ephemeris_and_Attitude, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
	}
	
	/**
	 * Constructor which attempts to read the RawApplicationPacket entry that pre-exists.
	 * The contents of the dataspace are read into a memory buffer... assuming it will fit.
	 * @param allRDRId  the rdrAll Groups id
	 * @param setNum the set number of raw entry
	 * @throws NullPointerException 
	 * @throws HDF5LibraryException 
	 * @throws RtStpsException 
	 */
	public SpacecraftDiaryRawApplicationPackets(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public SpacecraftDiaryRawApplicationPackets(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}

	/**
	 * This method is not used by the SpacecraftAOS Diary
	 */
	public boolean notFull(Packet p) {
		throw new RtStpsRuntimeException("The method not-Full is not used by the SpacecraftAOS Diary RawApplication class");
	}
	
	
	/**
	 * This method is not used by the SpacecraftAOS Diary
	 */
	public void put(Packet p) {
		throw new RtStpsRuntimeException("The method put is not used by the SpacecraftAOS Diary RawApplication class");

	}
	
	
	/**
	 * Put all the packets at once into this SpacecraftAOS Diary ApplicationPackets
	 * @param packets the list of packets
	 */
	public void putAll(List<Packet> packets) {
		//getPacketList().addAll(packets);
		
		// crud, can't seem to get away with not looping through the whole dang thing...
		for (Packet p : packets) {
			getPacketList().add(p);
			this.updateAppIdCounters(p.getApplicationId());
		}
		
		setFirstTime(packets.get(0).getTimeStamp(8));
		setLastTime(packets.get(packets.size()-1).getTimeStamp(8));
	}
	



}
