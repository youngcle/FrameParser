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
 * A helper class for the TimeManagedPacketList, this marks the list of designated packets
 * as either open on front or beginning, open at the end, or complete enclosed or encapsulated
 * or open both on the front and end.
 * 
 *
 */
public class TimeSpanPacketList {
	private List<Packet> spanList = new LinkedList<Packet>();
	private TimeSpanType tst = TimeSpanType.Open;
	
	/**
	 * Create the list and mark it as either being open or enclosed in various ways
	 * @param firstIndex the first index is either -1 for open, or some index within the list
	 * @param endingIndex the last index is either -1 for open, or some index within the list
	 * @param packetList the packet list of interest
	 */
	public TimeSpanPacketList(int firstIndex, int endingIndex, List<Packet> packetList) {
		tst = TimeSpanType.Enclosed; // start off optimistically 
		if (firstIndex < 0) {
			firstIndex = 0;
			tst = TimeSpanType.BeginningTimeOpen;
		}
		if (endingIndex < 0) {
			endingIndex = packetList.size()-1;  // its EXCLUSIVE this below
			if (tst == TimeSpanType.BeginningTimeOpen) {
				tst = TimeSpanType.Open;  // it's open at the front and end...
			} else {
				tst = TimeSpanType.EndingTimeOpen;
			}
		}
		// If neither of those trips us up, then the packet list 
		// should container by time, a completely enclosing span as requested
		// originally in the TimeManagedPacketList class...
		
		// regardless, copy what we have into the spanList
		spanList = packetList.subList(firstIndex, endingIndex+1);
	}

	/**
	 * The size of the list of packets being spanned
	 * @return the size
	 */
	public int size() {
		return spanList.size();
	}
	
	/**
	 * The spanned list of packets
	 * @return the list of the packets in the span
	 */
	public List<Packet> getList() {
		return spanList;
	}
	
	/**
	 * The type of span, whether it open on one or both ends or enclosed
	 * @return a TimeSpanType
	 */
	public TimeSpanType getTimeSpanType() {
		return tst;
	}
}
