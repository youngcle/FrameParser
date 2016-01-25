/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.LinkedList;
import java.util.List;

/**
 * The time managed list takes any number of packets as input.  It assumes the packets are in time order.
 * A span of time is then used to request packets from the list.  The returned packet either match or encapsulate
 * the request.
 * If the request can not be met, four conditions may occur:
 * 1 - the timespan cannot be fulfilled for the beginning time, but can for the ending time
 * 2 - the timespan cannot be fulfilled for the ending time, but can for the beginning time
 * 4 - the timespan cannot be fulfilled for either time
 * 5 - the timespan can be fulfilled for both times
 * 
 * One the request has been processed the list requested is returned along with the return code status as described above...
 * 
 * 
 *
 */
public class TimeManagedPacketList {

	private List<Packet> packetList = new LinkedList<Packet>();
	
	/**
	 * Create a TimeManagedPacketList from a list of input packets
	 * @param packets the input packets
	 */
	public TimeManagedPacketList(List<Packet> packets) {
		packetList.addAll(packets);
	}

	/**
	 * The size of the internal list of packets
	 * @return the number of packets
	 */
	public int size() {
		return packetList.size();
	}
	/**
	 * Given packet times in IET format as a 64-bit quantity, find the encapsulating list of packets from the 
	 * internal list.
	 * The the beginning packet found must be either of the same time as the specified beginningTime below, 
	 * or before it.
	 * And the ending packet found must be either of the same time or after it.   These should be the closest 
	 * before/after times in the list.
	 * 
	 * @param beginningTimeIET the 64-bit beginning packet time in IET format
	 * @param endingTimeIET the 64-bit ending packet time in IET format
	 * @return a TimeSpanPacketList object that contains the list of packets that meet the criteria in some way as specified by the code
	 */
	public TimeSpanPacketList get(long beginningTimeIET, long endingTimeIET) {
		//System.out.println("Beginning time: " + beginningTimeIET + " Ending Time: " + endingTimeIET);
		int firstIndex = getIndexOnOrBefore(beginningTimeIET);
		int endingIndex = getIndexOnOrAfter(endingTimeIET);
		
		//System.out.println("First Index: " + firstIndex + " Ending Index: " + endingIndex);
		
		return new TimeSpanPacketList(firstIndex, endingIndex, packetList);
	}
	
	// calculate aspects of the list
	private int getIndexOnOrAfter(long endingTimeIET) {
		// the loop selects the an exact packet time match or the first timestamp after
		// it in the packet list
		// the index is returned, or -1 means it was not found
		//
		if (packetList.size() == 0) {
			return -1;
		}
		for (int i = 0; i < packetList.size(); i++) {
			Packet p = packetList.get(i);
			long packetTime = p.getTimeStamp(8);
			int result = compare(PDSDate.getMicrosSinceEpoch(packetTime), endingTimeIET);
			if (result == 0) {
				// the packeTime is equal to the endingTime so this is exactly what we want
				return i;
		    } else if (result > 0) {
		    	// the packet is after the endingTime, stop and return this index
		    	return i;
		    }
			// otherwise it is before, so keep going
		}
		// if we get here, we go to the end of the list and every packet's time is before
		// endingTime, this means we failed ...
		return -1;
	}
	
	// calculate aspects of the list #2
	private int getIndexOnOrBefore(long beginningTimeIET) {

		// the loop selects the earlier time before the time in the packet
		// list goes past the asked for time.   That earlier time is going
		// to then either match the packet time exactly or be before it
		

		if (packetList.size() == 0) {
			return -1;
		}
		for (int i = 0; i < packetList.size(); i++) {
			Packet p = packetList.get(i);
			long packetTime = p.getTimeStamp(8);
			int result = compare(PDSDate.getMicrosSinceEpoch(packetTime), beginningTimeIET);
			if (result == 0) {
				// the packeTime is equal to the beginningTime so this exactly what we want
				return i;
		    } else if (result > 0) {
				// the packetTime is after the beginningTime...
				// well we don't have anything to do really but return the index previous
		    	// to this one which should be an earlier time stamped packet.
		    	// It could also be -1 if this is packet 0.
				return i-1;
			} 
			// otherwise the packet time is before the beginning time so keep going
		}
		
		// if we get this far, all the packets must be before beginningTime
		// so the last packet in the list will do, return that index
		return (packetList.size()-1);
	}
	// compare two 64-bit IET packet times, similar to Comparator interface
	// -1 -- firstTime <  secondTime, 0 -- they are equal, 1 -- first is greater than second
	// NOTE: I could just implement Comparator as well but not sure of the need/benefit...
	private int compare(long firstTimeIET, long secondTimeIET) {
		if (firstTimeIET < secondTimeIET) {
			return -1;
		}
		if (firstTimeIET > secondTimeIET) {
			return 1;
		}
		
		return 0;
	}
}
