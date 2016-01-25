/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * The packet tracker item is part of the header in the RawApplicationPackets area.  This class manages ne of these items,
 * it can be either to create a PacketTrackerItem or it can be used to read one from a pre-existing file.
 * 
 *
 */
public class PacketTrackerItem {

		// HDF fields
		private long obsTime = 0L; // int64
		private int sequenceNumber = 0; // int32
		private int pktTrkr_size = 0; // int32 
		private int hdfOffset = 0; // int32 
		private int fillPercent = 0; // int32
		
		private final static int size = 24; // hdf memory size, 24 bytes
		
		// local fields
		private int count = 0; // this is not in the HDF but used to help build the RDRList section
		private int appId = 2047; // also not in the HDF but used to help build the RDRList section
		
		private byte[] data;
		private int offset;
		private static long recentTime = 0l; // MUST BE static ... FIXME?
		private Packet recentFirstPacket;
		

		public PacketTrackerItem() {
			this.obsTime = 0;
			this.sequenceNumber = 0;
			this.pktTrkr_size = 0;
			this.hdfOffset = -1;
			this.fillPercent = 0;
		}
		
		public PacketTrackerItem(Packet p, int offset) {
			this.obsTime = getObsTime(p);
			this.sequenceNumber = p.getSequenceCounter();
			//System.out.println("pkt trak -- apid = " + p.getApplicationId() + " seq = " + p.getSequenceCounter() + " size = " + p.getPacketSize());
			this.pktTrkr_size = p.getPacketSize(); 
			this.hdfOffset = offset;
			this.fillPercent = 0;
		}
		
		private long getObsTime(Packet p) {
			long obsTime = 0l;
			//if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			if (p.hasSecondaryHeader()) {

				// first case is ideal:  it's true for many packets ...
				// with a timestamp
				LeapDate lpd = new LeapDate(p.getTimeStamp(8));
				recentTime  = lpd.getMicrosSinceEpoch();
				recentFirstPacket = p;
				obsTime = recentTime;
			} else if (inSegmentGroup(p)) {  //FIXME this does not work
				// otherwise it might be a middle or last packet in a segment group w/out TS
				// in which case we may be within a current grouping... if so
				// then the time of that grouping is used...
				obsTime = recentTime;
			} else {
				// otherwise it does not have a timestamp and there was no group it was
				// a part of.
				// In that case there no time to give it.  This could occur for a variety
				// of reasons.  For example at the start of reception, sync is likely not
				// to occur precisely on segmented group boundaries
				obsTime = recentTime;  // was 0L  FIXME it always falls through to here if the first test fails
			}
			return obsTime;
		}

		private boolean inSegmentGroup(Packet currentPacket) {
			if (recentFirstPacket == null) {
				return false;
			}
			PacketName recentFirstPacketName = PacketName.fromAppId(recentFirstPacket.getApplicationId());
			PacketName currentPacketName = PacketName.fromAppId(currentPacket.getApplicationId());
			
			// if its a segmented packet, they should be same name...
			if (recentFirstPacketName != currentPacketName) {
				return false;
			}
			
			// now test that the sequence count of the current packet is within the recent packets
			// sequence count range...
			int recentFirstPacketSeqCount = recentFirstPacket.getSequenceCounter();
			int currentPacketSeqCount = currentPacket.getSequenceCounter();
			
			int packetsInGroup = recentFirstPacketName.getTotalGroupCount();
			
			int maxSeqCountForRecent = (recentFirstPacketSeqCount + packetsInGroup) % 16384;
			
			// short of the counter completely wrapping to recentFirtPacket count and all those packets between recent
			// and one we have being missed...
			// it seems very likely if this is true that the packet is in the sequence count
			return (currentPacketSeqCount < maxSeqCountForRecent);
		}

		/**
		 * Construct a new packet tracker item with the input values.  Once constructed the values may be
		 * written to a memory buffer using the write method.  This is intended to be used to write an HDF
		 * structure, so this is the "write" side of the class.
		 * @param obsTime the observation time
		 * @param sequenceNumber the sequence number
		 * @param size the size of the packet tracker
		 * @param hdfOffset the offset in the header
		 * @param fillPercent the fill percentage which is fixed for DRL/field terminal at this time
		 */
		@Deprecated
		public PacketTrackerItem(long obsTime, int sequenceNumber, int size, int hdfOffset, int fillPercent) {
			this.obsTime = obsTime;
			this.sequenceNumber = sequenceNumber;
			this.pktTrkr_size = size; // careful!!
			this.hdfOffset = hdfOffset;
			this.fillPercent = fillPercent;
		}

		
		
		/**
		 * Given an already filled memory buffer, retrieve values at the given offset to build the packet tracker
		 * items.  The items can then be retrieved using the "read" methods below.  Hence this is the read side of
		 * the class.  Note, the two sides are not synchronized in anyway, nor would changes in the data buffer passed
		 * in here be reflected in the objects values if it were to change.
		 * @param data
		 * @param offset
		 */
		public PacketTrackerItem(byte[] data, int offset) {
			this.data = data;
			this.offset = offset;
			readObsTime();
			readSequenceNumber();
			readSize();
			readOffset();
			readFillPercent();
		}
		
	

		/**
		 * Set the observation time
		 * @param obsTime the observation time in 64-bits IET Time format
		 */
		@Deprecated
		public void setObsTime(long obsTime) {
			this.obsTime = obsTime;
		}
		/**
		 * Set the sequence number
		 * @param sequenceNumber the sequence number
		 */
		@Deprecated
		public void setSequenceNumber(int sequenceNumber) {
			this.sequenceNumber = sequenceNumber;
		}
		
		/**
		 * Set the packet tracker size
		 * @param size the size 
		 */
		@Deprecated
		public void setSize(int size) {
			this.pktTrkr_size = size; // careful!!
		}
		
		/**
		 * Add to the packet tracker size
		 * @param size  this is used when...
		 */
		@Deprecated
		public void addSize(int size) {
			this.pktTrkr_size += size; // careful!!
		}
		
		/**
		 * Set the offset in the header
		 * @param hdfOffset the offset in bytes
		 */
		@Deprecated
		public void setOffset(int hdfOffset) {
			this.hdfOffset = hdfOffset;
		}
		
		/**
		 * Set the fill percentage
		 * @param fillPercent the fill percentage
		 */
		@Deprecated
		public void setFillPercent(int fillPercent) {
			this.fillPercent = fillPercent;
		}
		
		/**
		 * Set the count
		 * @param count
		 */
		@Deprecated
		public void setCount(int count) {
			this.count = count;
		}
		
		/**
		 * Add to the count
		 * @param count
		 */
		@Deprecated
		public void addCount(int count) {
			this.count += count;
		}
		
		/**
		 * Set the application id of the packet tracker
		 * @param appId
		 */
		@Deprecated
		public void setAppId(int appId) {
			this.appId = appId;
		}

		// READ or retrievel side
		
		/**
		 * Get the observation time
		 * @return the observation time in 64 bits
		 */
		public long getObsTime() { return this.obsTime; }
		
		/**
		 * Get the sequence number
		 * @return the sequence number in an int
		 */
		public int getSequenceNumber() { return this.sequenceNumber; }
		
		/**
		 * Get the size of the pkt tracker
		 * @return the size
		 */
		public int getSize() { return this.pktTrkr_size; }
		
		/**
		 * Get the header offset
		 * @return the offset in bytes
		 */
		public int getOffset() { return this.hdfOffset; }
		
		/**
		 * Get the fill percentage
		 * @return the fill percentage
		 */
		public int getFillPercent() { return this.fillPercent; }
		
		/**
		 * Get the packet application ID
		 * @return the application ID in an <code>int</code>
		 */
		public int getAppId() { return this.appId; }
		
		/**
		 * Get the count
		 * @return the count
		 */
		public int getCount() { return count; }
		
		/**
		 * The size is fixed, retrieve it for other reasons
		 * @return the size in bytes
		 */
		public static int getHDFSize() { 
			return PacketTrackerItem.size;
		}
		
		/**
		 * Write the newly constructed PacketTrackerItem information to the data array given
		 * @param data the byte array the packet tracker information should be packed into
		 * @param offset the offset in bytes into the array
		 * @return the size of the item which is fixed
		 */
		public int write(byte[] data, int offset) {
			//this.dataSet = dataSet;
			this.data = data;
			this.offset = offset;
			writeObsTime();
			writeSequenceNumber();
			writeSize();
			writeOffset();
			writeFillPercent();
			
			return PacketTrackerItem.size;
		}

		/**
		 * Serialize the observation time into the data array
		 */
		private void writeObsTime() {
			
			data[offset++] = (byte)((obsTime >>> 56) & 0x0ffL);
			data[offset++] = (byte)((obsTime >>> 48) & 0x0ffL);
			data[offset++] = (byte)((obsTime >>> 40) & 0x0ffL);
			data[offset++] = (byte)((obsTime >>> 32) & 0x0ffL);
			data[offset++] = (byte)((obsTime >>> 24) & 0x0ffL);
			data[offset++] = (byte)((obsTime >>> 16) & 0x0ffL);
			data[offset++] = (byte)((obsTime >>> 8) & 0x0ffL);
			data[offset++] = (byte)(obsTime & 0x0ffL);
		
		}
		
		/**
		 * Serialize the sequence number into the data array
		 */
		private void writeSequenceNumber() {
			
			data[offset++] = (byte)((sequenceNumber >>> 24) & 0x0ff);
			data[offset++] = (byte)((sequenceNumber >>> 16) & 0x0ff);
			data[offset++] = (byte)((sequenceNumber >>> 8) & 0x0ff);
			data[offset++] = (byte)(sequenceNumber & 0x0ff);
	
		}
		
		/**
		 * Serialize the size into the data array
		 */
		private void writeSize() {
			data[offset++] = (byte)((pktTrkr_size >>> 24) & 0x0ff);
			data[offset++] = (byte)((pktTrkr_size >>> 16) & 0x0ff);
			data[offset++] = (byte)((pktTrkr_size >>> 8) & 0x0ff);
			data[offset++] = (byte)(pktTrkr_size & 0x0ff);
		}
		
		/**
		 * Serialize the offset into the data array
		 */
		private void writeOffset() {
			data[offset++] = (byte)((hdfOffset >>> 24) & 0x0ff);
			data[offset++] = (byte)((hdfOffset >>> 16) & 0x0ff);
			data[offset++] = (byte)((hdfOffset >>> 8) & 0x0ff);
			data[offset++] = (byte)(hdfOffset & 0x0ff);
			
		}
		
		/**
		 * Serialize the fill percent into the data array
		 */
		private void writeFillPercent() {
			data[offset++] = (byte)((fillPercent >>> 24) & 0x0ff);
			data[offset++] = (byte)((fillPercent >>> 16) & 0x0ff);
			data[offset++] = (byte)((fillPercent >>> 8) & 0x0ff);
			data[offset++] = (byte)(fillPercent & 0x0ff);
		}
		
		/**
		 * Read the observation time from a data buffer passed in the in "read" constructor
		 */
		private void readObsTime() {
			
			obsTime = (((long)data[offset++] << 56) &   0xff00000000000000L) |
						(((long)data[offset++] << 48) & 0x00ff000000000000L) |
						(((long)data[offset++] << 40) & 0x0000ff0000000000L) |
						(((long)data[offset++] << 32) & 0x000000ff00000000L) |
						(((long)data[offset++] << 24) & 0x00000000ff000000L) |
						(((long)data[offset++] << 16) & 0x0000000000ff0000L) |
						(((long)data[offset++] << 8) &  0x000000000000ff00L) |
						((long)data[offset++] & 0x00ffL);
		
		}
		
		/**
		 * Read the sequence number from a data buffer passed in the in "read" constructor
		 */
		private void readSequenceNumber() {
			
			sequenceNumber = ((data[offset++] << 24) & 0xff000000) |
			((data[offset++] << 16) & 0x00ff0000) |
			((data[offset++] << 8) & 0x0000ff00) |
			(data[offset++] & 0x000000ff);
	
		}
		
		/**
		 * Read the size from a data buffer passed in the in "read" constructor
		 */
		private void readSize() {
			
			pktTrkr_size = ((data[offset++] << 24) & 0xff000000) |
			((data[offset++] << 16) & 0x00ff0000) |
			((data[offset++] << 8) & 0x0000ff00) |
			(data[offset++] & 0x000000ff);
			
		}
		
		/**
		 * Read the offset from a data buffer passed in the in "read" constructor
		 */
		private void readOffset() {
			
			hdfOffset = ((data[offset++] << 24) & 0xff000000) |
			((data[offset++] << 16) & 0x00ff0000) |
			((data[offset++] << 8) & 0x0000ff00) |
			(data[offset++] & 0x000000ff);
			
		}
		
		/**
		 * Read the fill percentage from a data buffer passed in the in "read" constructor
		 */
		private void readFillPercent() {
		
			fillPercent = ((data[offset++] << 24) & 0xff000000) |
			((data[offset++] << 16) & 0x00ff0000) |
			((data[offset++] << 8) & 0x0000ff00) |
			(data[offset++] & 0x000000ff);
		}
		

		
}
