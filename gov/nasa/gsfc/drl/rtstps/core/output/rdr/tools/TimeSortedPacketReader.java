/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketI;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class TimeSortedPacketReader implements Iterator<PacketI> {
	private PacketReaderI packetReader;
	//private TreeSet<DateWrappedPacket> timeSortedPackets = new TreeSet<DateWrappedPacket>(new PacketDateComparator());
	
	private TreeSet<TimeWrappedPacket> timeSortedPackets = new TreeSet<TimeWrappedPacket>(new PacketTimeComparator());

	
	private int maxPacketCount = 10000;
	private int halfFull = maxPacketCount / 2;
	private Date currentDateTime;
	private long currentTime = 0l;
	private int currCount = 0;
	
	TimeSortedPacketReader(PacketReaderI packetReader) throws RtStpsException {
		this.packetReader = packetReader;
		
		// read the packets until a first time is found... 
		// adjust all those read with this time and insert them into 
		// the tree...
		// then if the list is not full, keep adding them.
		
		readUntilFirstTimeFound();
		
		System.out.println("timeSortedPacket size = " + timeSortedPackets.size());
		
		// if the the max packet count has not been reached, fill out the rest of the list
		while (packetReader.hasNext() && (timeSortedPackets.size() < maxPacketCount)) {

			Packet p = (Packet)packetReader.next();
	
			//timeSortedPackets.add(new DateWrappedPacket(p));
			timeSortedPackets.add(new TimeWrappedPacket(p));
			
		}
		
		System.out.println(" timeSortedPacket size = " + timeSortedPackets.size());
	}
	
	// if the incoming packet don't start with a timestamp
	// find one that does and then go back and apply that time
	// to the ones read in already, this is a little bit of fudge
	// but we do not know when we'll get lock and thus the stream of
	// packets could easily (and likely) not start with a packet that
	// has a timestamp
	private void readUntilFirstTimeFound() throws RtStpsException {
		List<Packet> packets = new LinkedList<Packet>();
		
		
		boolean timeDone = false;
		
		while (!timeDone && (timeSortedPackets.size() < this.maxPacketCount) && packetReader.hasNext()) {

			Packet p = (Packet)packetReader.next();
			packets.add(p);
			
			if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
				
				PDSDate pt = new PDSDate(p.getTimeStamp(8));
				
				// update the current time
				//currentDateTime = pt.getDate();
				currentTime = pt.getMicrosSinceEpoch();
				
				// subtract the list size from the time to go back in time, this helps give those packets previous to this one a unique time
				if (packets.size() > 1) {
					currentTime -= packets.size();
				}
				
				// now that a time has been found, go through the list
				// of already read packets and create a DateWrappedPacket for each
				// and put them into the date-time sorted list
				for (Packet ptmp : packets) {
					//timeSortedPackets.add(new DateWrappedPacket(ptmp));
					timeSortedPackets.add(new TimeWrappedPacket(ptmp));
				}
				
				
				timeDone = true;
			} 
			
			
		}
		if (timeDone != true) {
			throw new RtStpsException("No first time found!");
		}
	}

	@Override
	public boolean hasNext() {
		// if the time sort list is not empty, then return true, otherwise false
		//System.out.println("Size: " + timeSortedPackets.size());
		if (timeSortedPackets.size() < this.halfFull) { 
			while ((timeSortedPackets.size() < this.maxPacketCount) && packetReader.hasNext()) {

				Packet p = (Packet)packetReader.next();
	
				//timeSortedPackets.add(new DateWrappedPacket(p));
				timeSortedPackets.add(new TimeWrappedPacket(p));
			
			}
		}
		return (timeSortedPackets.size() != 0);
	}

	@Override
	public PacketI next() {
		// if the packetReader is not empty, read one and put it into the sorted list
		// read off the sorted list and return it...
		//DateWrappedPacket dwPacket = timeSortedPackets.first();
		TimeWrappedPacket dwPacket = timeSortedPackets.pollFirst();


		return dwPacket.getPacket();
	}

	@Override
	public void remove() {
		// not implemented...
		
	}
	
	private class DateWrappedPacket {
		private Date dateTime;
		private Packet packet;
		DateWrappedPacket(Packet p) {
			if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
				
				PDSDate pt = new PDSDate(p.getTimeStamp(8));
				
				// update the current time
				TimeSortedPacketReader.this.currentDateTime = pt.getDate();
				
			} 
			// use the current time
			this.dateTime = TimeSortedPacketReader.this.currentDateTime;
			
			this.packet = p;
		}
		
		Packet getPacket() {
			return packet;
		}
		Date getDate() {
			return dateTime;
		}
	}
	
	private class PacketDateComparator implements Comparator<DateWrappedPacket> {

		@Override
		public int compare(DateWrappedPacket p0, DateWrappedPacket p1) {
					
			return p0.getDate().compareTo(p1.getDate());
		}
	}

	
	private class TimeWrappedPacket {
		private long time;
		private Packet packet;
		TimeWrappedPacket(Packet p) {
			if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
				
				PDSDate pt = new PDSDate(p.getTimeStamp(8));
				
				// update the current time
				
				TimeSortedPacketReader.this.currentTime = pt.getMicrosSinceEpoch() * 1000l;  // make more precision than there is to make the times unique, hopefully
				//System.out.println("resetting currenTime = " + currentTime);
			}
			// use the current time
			
			
			this.time = TimeSortedPacketReader.this.currentTime + currCount;
			
			//System.out.println("currenTime = " + time);
			//TimeSortedPacketReader.this.currCounter++;  // increment to ensure all are unique
			
			currCount = (++currCount) % 1000;  // this is just a rolling counter to uniquify the times which don't very that much between packets
			
			this.packet = p;
		}
		
		Packet getPacket() {
			return packet;
		}
		long getTime() {
			return time;
		}
	}
	
	private class PacketTimeComparator implements Comparator<TimeWrappedPacket> {

		@Override
		public int compare(TimeWrappedPacket p0, TimeWrappedPacket p1) {
			if (p0.getTime() == p1.getTime()) return 0;
			if (p0.getTime() < p1.getTime()) return -1;
			
			return 1;
			//return p0.getTime().compareTo(p1.getTime());
		}
	}
	

}
