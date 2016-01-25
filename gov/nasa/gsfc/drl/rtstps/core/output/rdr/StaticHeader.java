/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools.CopyPacketFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * Implements the StaticHeader for the RawApplication area, this class can be used to either create the
 * StaticHeader or read a pre-existing StaticHeader.
 * 
 * 
 *
 */
public class StaticHeader {
	// note: at the moment the Uint32s are being treated as signed ints, this halves the value
	// but it does not seem very likely this code will ever create anything even approaching those
	// sizes anyway... this could be a problem though, in which case they should all be longs and masked
	// when storing...
	
		private final static int HeaderSize = 72;  // fixed
		// static header portion of the static header
		// satellite char[4] array in header, addr 0
		// sensor char[16], addr 4
		// typeid char[16], addr 20
		private int numAppIds = 0; // Uint32, address 36
		private int apidListOffset = HeaderSize; // Uint32, 72 is the fixed value at this time, address 40
		private int pktTrackerOffset = 0; // Uint32, address 44
		private int apStorageOffset = 0; // Uint32, address 48
		private int nextPktPos = 0;  // Uint32, address 52
		private long startBoundary = 0L;  // int64, address 56
		private long endBoundary = 0L;  // int64, address 64
		// address end is 72
		
		// most of these are used in BUILDING the static header
		// but not used if the dataset has been read in and passed in
		// as a byte array... MAYBE 
		//
		private RDRAppIdList rdrList;
		private PacketTrackerList packetTracker;
		private ApStorageArea apStorageArea;
		
		private int dataSet;
		//private int packetSizeSum = 0;
		private SpacecraftId satellite;
		
		private RDRName rdrName;
		
		// To hold all the packets a RAP has collected; list of packets the static header needs to track
		// that go into a particular RawApplicationPackets_N dataset!
		private CopyOnWriteArrayList<Packet> packetList;
		
		private byte[] data;
		private int offset = 0;
		
		
		private Sensor sensor;
		private int bufSize = 0;
		
		/**
		 * Create a static header from an input packet list, used to write to the HDF
		 * @param satellite the name of the satellite
		 * @param rdrName the RDR name
		 * @param packetList the list of packets that the static header needs to track that go into a particular RawApplicationPackets area
		 */
		public StaticHeader(SpacecraftId satellite, RDRName rdrName, CopyOnWriteArrayList<Packet> packetList) {
			
			//this.dataSet = dataSet;
			this.satellite = satellite;
			this.rdrName = rdrName;	
			this.packetList = packetList;
			
	
			numAppIds = rdrName.getNumberOfAppIdsInRDR();
			
			// calculate the Ap Storage Area first (note: no longer of importance)
			apStorageArea = new ApStorageArea(packetList);
			
			packetTracker = new PacketTrackerList(rdrName, packetList);
			
			// then build the RDR list, it depends on the packetTracker list...
			rdrList = new RDRAppIdList(rdrName, packetList, packetTracker);
			
			pktTrackerOffset = HeaderSize + rdrList.getSize();
			
			apStorageOffset = HeaderSize + rdrList.getSize() + packetTracker.getSize();
			
			nextPktPos = this.getSize()-apStorageOffset; //fixed as per DRO analysis report.
			

			
		}

		/**
		 * Create a SequentialPacketReader which allows the packets in a particular RawApplicationPackets area
		 * to be read out sequentially using the information in the StaticHeader.
		 * @return a SequentialPacketReader
		 */
		public SequentialPacketReader createSequentialPacketReader() {
			
			return new SequentialPacketReader(new CopyPacketFactory(), this);
			
		}
		
		/**
		 * Create a RandomAccessPacketReader which allows the packet in a particular RawApplicationPackets area
		 * to be read out by random access using the information in the StaticHeader.
		 * @return a RandomAccessPacketReader
		 */
		public RandomAccessPacketReader createRandomAccessPacketReader() {
			return new RandomAccessPacketReader(new RandomAccessPacketFactory(), this);
		}
		
		/**
		 * Used to parse out of a static header read in as part of reading a pre-existing HDF file.
		 * Note that the input data buffer has been read by some other entity and passed to this routine
		 * which cracks it apart.
		 * @param data containing the static header
		 */
		public StaticHeader(byte[] data) {
			this.data = data; // save a pointer to the original input, not sure this is truly a good idea
			//satellite = SpacecraftId.valueOf(readSatelliteString(0, data));
			
			//sensor = Sensor.valueOf(readSensorString(5, data));
			
			
			numAppIds = (((int)data[36] << 24) & 0xff000000) | // 36, 32 bits
						(((int)data[37] << 16) & 0x00ff0000) | 
						(((int)data[38] <<  8) & 0x0000ff00) | 
						 ((int)data[39] & 0x000000ff);
			
			pktTrackerOffset = (((int)data[44] << 24) & 0xff000000) | // 44, 32 bits
			  					(((int)data[45] << 16) & 0x00ff0000) | 
			  					(((int)data[46] <<  8) & 0x0000ff00) | 
			  					((int)data[47] & 0x000000ff);
			
			apStorageOffset = (((int)data[48] << 24) & 0xff000000) | // 48, 32 bits
							  (((int)data[49] << 16) & 0x00ff0000) | 
							  (((int)data[50] <<  8) & 0x0000ff00) | 
							   ((int)data[51] & 0x000000ff);
			
			nextPktPos = (((int)data[52] << 24) & 0xff000000) | // 52, 32 bits
			  			 (((int)data[53] << 16) & 0x00ff0000) | 
			  			 (((int)data[54] <<  8) & 0x0000ff00) | 
			  			 ((int)data[55] & 0x000000ff);
			
			
			//System.out.println("Number of APID list items [" + ((this.pktTrackerOffset - this.apidListOffset)/32) + "]");
			// as it stand now the nextPktPos should equal the data.length -- TESTING
			if (nextPktPos != data.length) {
				System.out.println(">> Warning: there is a nextPktPos[" + nextPktPos + "] data.length [" + data.length + "] discrepency!!!");
			}
		}
		
	

		/**
		 * Return a pointer to the original input data used for reading out the contents of the StaticHeader read
		 * out of the RawApplicationPackets area.
		 * @return a byte array of the data
		 */
		public byte[] getData() {
			return this.data;
		}
		
		/**
		 * Return the ApStorageOffset value
		 * @return the offset
		 */
		public int getApStorageOffset() {
			return apStorageOffset;
		}
		
		/**
		 * Return the total size of the StaticHeader which is the fixed sized of the StaticHeader area plus the RDR list size
		 * and the packet tracker size.
		 * @return the sum of the sizes
		 */
		public int getTotalHeaderSize() {
			return HeaderSize + rdrList.getSize() + packetTracker.getSize();
		}

		/**
		 * Return the packet store size
		 * @return the size
		 */
		public int getPacketStoreSize() {
			return this.apStorageArea.getSize();
		}
		
		/**
		 * Return the size of the StaticHeader, the RDR list size, the packet tacker size and the packet store size itself
		 * @return the sum of all the sizes
		 */
		public int getSize() {
			return getTotalHeaderSize() + getPacketStoreSize();
		}

		/**
		 * Return the AppIdList offset in the StaticHeader area
		 * @return the offset
		 */
		public int getAppIdListOffset() {
			return this.apidListOffset;
		}
		
		/**
		 * Return the packet tracker offset in the StaticHeader area
		 * @return the offset
		 */
		public int getPktTrackerOffset() {
			return this.pktTrackerOffset;
		}
		
		public int getNextPktPos() {
			return this.nextPktPos;
		}
		/**
		 * Write from the various sources of input the StaticHeader and the packet storage area to the RawApplicationPackets
		 * based on the HDF DataSet handle.
		 * @param dataSet the HDF handle to the RawApplicationPackets
		 * @throws RtStpsException wrapped HDF or other exceptions
		 */
		public void write(int dataSet) throws RtStpsException {
			// write static header fields
			this.dataSet = dataSet; // FIXME maybe, this could be passed into the constructor...
			
			//System.out.println("static header size: " + this.getSize());
			int neededSize = this.getSize();
			neededSize = ((neededSize / 1024) * 1024) + 1024;
			
			//System.out.println("static header size: " + this.getSize() + " Rounded up: " + neededSize + " bufsize: " + bufSize);
			if (neededSize > bufSize ) { 
				this.data = new byte[neededSize /* this.getSize() */ ]; // must be dynamically calculated depending on inputs
				bufSize = neededSize;
			}
		
			writeSatellite();
		
			writeSensor();
		
			writeTypeID();
		
			writeNumAppIds();
		
			writeApidListOffset();
		
			writePktTrackerOffset();
		
			writeApStorageOffset();
		
			writeNextPktPos();
		
			writeStartBoundary();  //FIXME must be called first
		
			writeEndBoundary();
		
			offset = rdrList.write(data, offset);
		
			offset = packetTracker.write(data, offset);
		
			// writeApplicationStorageArea();
		
			offset = apStorageArea.write(data, offset);
		
			writeToHDF();
			
			//this.data = null;
			
		}

		public String readSatelliteString() {
			String satellite = new String(data, 0, 4, Charset.forName("US-ASCII") ).trim();
			return satellite;
		}
		
		/**
		 * Write the satellite name to the StaticHeader data buffer, converting it to ASCII first
		 */
		private void writeSatellite() {
			
			//System.out.println("Satellite = " + satellite);
			
			byte[] ascii = satellite.toString().getBytes(Charset.forName("US-ASCII"));
			
			
			//System.out.println("ascii length = " + ascii.length);
			
			//System.out.println("data length = " + data.length);
			data[offset++] = ascii[0];
			data[offset++] = ascii[1];
			data[offset++] = ascii[2];
			data[offset++] = 0;

		}

		public String readSensorString() {
			String sensor = new String(data, 4, 16, Charset.forName("US-ASCII")) .trim();
			return sensor;
		}
		
		/**
		 * Write the sensor name to the StaticHeader data buffer, converting it to ASCII first
		 */
		private void writeSensor() {
			//System.out.println("rdrName.getSensor: " + rdrName.getSensor().toString());
			
			byte[] ascii = rdrName.getSensor().toString().getBytes(Charset.forName("US-ASCII"));
			
			//System.out.println("Sensor length: " + ascii.length);
			
			for (int i = 0; i < 16; i++) {
				data[offset+i] = 0;
			}
			int copyLen = ascii.length;
			if (copyLen > 16) copyLen = 16;
			
			for (int i = 0; i < copyLen; i++) {
				data[offset + i] = ascii[i];
			}
			offset += 16;
		}

		public String readTypeIDString() {
			String typeID = new String(data, 20, 16, Charset.forName("US-ASCII")) .trim();
			return typeID;
		}
		
		/**
		 * Write the type id name to the StaticHeader data buffer, converting it to ASCII first
		 */
		private void writeTypeID() {
			
			//System.out.println("rdrName.getTypeID: " + rdrName.getTypeID().toString());
			
			byte[] ascii = rdrName.getTypeID().toString().getBytes(Charset.forName("US-ASCII"));
			
			
			//System.out.println("TypeId length: " + ascii.length);
			
			for (int i = 0; i < 16; i++) {
				data[offset+i] = 0;
			}
			int copyLen = ascii.length;
			if (copyLen > 16) copyLen = 16;
			
			for (int i = 0; i < copyLen; i++) {
				data[offset + i] = ascii[i];
			}
			offset += 16;
		}
		
		public int readNumAppIds() {
			int numAppIds = ( (int)data[36] << 24 ) | 
								(((int)data[37] << 16) & 0x00ff0000) | 
								(((int)data[38] <<  8) & 0x0000ff00) | 
								((int)data[39] & 0x000000ff) ;
			return numAppIds;
		}
		/**
		 * Write the number of application IDs to the StaticHeader data buffer
		 */
		private void writeNumAppIds() {
			//numAppIds = rdrName.getNumberOfAppIdsInRDR();
			data[offset++] = (byte)((numAppIds >>> 24) & 0x0ff);
			data[offset++] = (byte)((numAppIds >>> 16) & 0x0ff);
			data[offset++] = (byte)((numAppIds >>> 8) & 0x0ff);
			data[offset++] = (byte)(numAppIds & 0x0ff);
		
		}
		
		
		public long readApidListOffset() {
			int apidListOffset = ( (int)data[40] << 24 ) | 
							(((int)data[41] << 16) & 0x00ff0000) | 
							(((int)data[42] <<  8) & 0x0000ff00) | 
							((int)data[43] & 0x000000ff) ;
			return apidListOffset;
		}
		/**
		 * Write the application ID list offset to the StaticHeader data buffer
		 */
		private void writeApidListOffset() {
			
			data[offset++] = (byte)((apidListOffset >>> 24) & 0x0ff);
			data[offset++] = (byte)((apidListOffset >>> 16) & 0x0ff);
			data[offset++] = (byte)((apidListOffset >>> 8) & 0x0ff);
			data[offset++] = (byte)(apidListOffset & 0x0ff);
			
		}
		
		public RDRAppIdList readRDRAppIdList() {
			
			return new RDRAppIdList((int)readApidListOffset(), data, (int)readPktTrackerOffset());
		}

		public long readPktTrackerOffset() {
			int pktTrackerOffset = ( (int)data[44] << 24 ) | 
									(((int)data[45] << 16) & 0x00ff0000) | 
									(((int)data[46] <<  8) & 0x0000ff00) | 
									((int)data[47] & 0x000000ff) ;
			return pktTrackerOffset;
		}
		
		/**
		 * Write the packet tracker offset to the StaticHeader data buffer
		 */
		private void writePktTrackerOffset() {
			
			data[offset++] = (byte)((pktTrackerOffset >>> 24) & 0x0ff);
			data[offset++] = (byte)((pktTrackerOffset >>> 16) & 0x0ff);
			data[offset++] = (byte)((pktTrackerOffset >>> 8) & 0x0ff);
			data[offset++] = (byte)(pktTrackerOffset & 0x0ff);
			
		}

		public long readApStorageOffset() {
			int apStorageOffset = ( (int)data[48] << 24 ) | 
									(((int)data[49] << 16) & 0x00ff0000) | 
									(((int)data[50] <<  8) & 0x0000ff00) | 
									((int)data[51] & 0x000000ff) ;
			return apStorageOffset;
		}
		
		/**
		 * Write the ap storage offset to the StaticHeader data buffer
		 */
		private void writeApStorageOffset() {
			//System.out.println("Creating storage offset at offset: " + offset + " value of it: " + apStorageOffset);
			data[offset++] = (byte)((apStorageOffset >>> 24) & 0x0ff);
			data[offset++] = (byte)((apStorageOffset >>> 16) & 0x0ff);
			data[offset++] = (byte)((apStorageOffset >>> 8) & 0x0ff);
			data[offset++] = (byte)(apStorageOffset & 0x0ff);
	
			//System.out.println("offset now = " + offset);
			
		}

		public long readNextPktPos() {
			int nextPktPos = ( (int)data[52] << 24 ) | 
									(((int)data[53] << 16) & 0x00ff0000) | 
										(((int)data[54] <<  8) & 0x0000ff00) | 
										((int)data[55] & 0x000000ff) ;
			return nextPktPos;
		}
		
		/**
		 * Write the next packet position to the StaticHeader data buffer
		 */
		private void writeNextPktPos() {
			data[offset++] = (byte)((nextPktPos >>> 24) & 0x0ff);
			data[offset++] = (byte)((nextPktPos >>> 16) & 0x0ff);
			data[offset++] = (byte)((nextPktPos >>> 8) & 0x0ff);
			data[offset++] = (byte)(nextPktPos & 0x0ff);

		}

		public long readStartBoundary() {
			long startBoundary = 0L;
			int loffset = 56;
			startBoundary =  ((long)data[loffset++] << 56) & 0xff00000000000000l;
			startBoundary |= ((long)data[loffset++] << 48) & 0x00ff000000000000l;
			startBoundary |= ((long)data[loffset++] << 40) & 0x0000ff0000000000l;
			startBoundary |= ((long)data[loffset++] << 32) & 0x000000ff00000000l;
			startBoundary |= ((long)data[loffset++] << 24) & 0x00000000ff000000l;
			startBoundary |= ((long)data[loffset++] << 16) & 0x0000000000ff0000l;
			startBoundary |= ((long)data[loffset++] <<  8) & 0x000000000000ff00l;
			startBoundary |=  (long)data[loffset++]        & 0x00000000000000ffl;
			
			return startBoundary;
		}
		
		/**
		 * Write the start boundary to the StaticHeader data buffer
		 */
		private void writeStartBoundary() {
			Packet firstPacket = getFirstPacketWithTimestamp();  
			// we don't check for non-null result... assume that's fatal issue right now that needs to be checked
			
			//System.out.println("First packet end boundary: " + PDSDate.getMicrosSinceEpoch(firstPacket.getTimeStamp(8)));
			
			//startBoundary = PDSDate.getMicrosSinceEpoch(firstPacket.getTimeStamp(8));
			long ietTime = 0L;
			/*if ((rdrName == RDRName.VIIRS_Science) ||  (rdrName == RDRName.NPP_Ephemeris_and_Attitude)) {
				
				LPEATEDate lpd = new LPEATEDate(firstPacket.getTimeStamp(8));
				ietTime = lpd.getMicrosSinceEpoch();
				
			} else {
				ietTime = PDSDate.getMicrosSinceEpoch(firstPacket.getTimeStamp(8));
			}
			
			startBoundary = ietTime;*/
			ietTime = LeapDate.getMicrosSinceEpoch(firstPacket.getTimeStamp(8));
			if (rdrName == RDRName.VIIRS_Science)
			{
			    	startBoundary=VIIRSGranule.getStartBoundary(ietTime); 
			    	System.out.println("In write startBoundary:"+ietTime+" "+startBoundary);
			}
			else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude)
			{
				startBoundary=SpacecraftDiaryGranule.getStartBoundary(ietTime); 
				System.out.println("In write startBoundary:"+ietTime+" "+startBoundary);  
                  
			}
			else if (rdrName == RDRName.CRIS_Science)
			{
				startBoundary=CRISGranule.getStartBoundary(ietTime); 
				System.out.println("In write startBoundary for CRIS:"+ietTime+" "+startBoundary);  
                  
			}
			else if (rdrName == RDRName.ATMS_Science)
			{
				startBoundary=ATMSGranule.getStartBoundary(ietTime); 
				System.out.println("In write startBoundary for ATMS:"+ietTime+" "+startBoundary);  
			}
			else
			{
				startBoundary = ietTime;
			}
			
			// VIIRS partial test
			//if (rdrName == RDRName.VIIRS_Science) {
			//	startBoundary = 1422168873750000L; // was just ietTime
			//}
			
			data[offset++] = (byte)((startBoundary >>> 56) & 0x0ffL);
			data[offset++] = (byte)((startBoundary >>> 48) & 0x0ffL);
			data[offset++] = (byte)((startBoundary >>> 40) & 0x0ffL);
			data[offset++] = (byte)((startBoundary >>> 32) & 0x0ffL);
			data[offset++] = (byte)((startBoundary >>> 24) & 0x0ffL);
			data[offset++] = (byte)((startBoundary >>> 16) & 0x0ffL);
			data[offset++] = (byte)((startBoundary >>> 8) & 0x0ffL);
			data[offset++] = (byte)(startBoundary & 0x0ffL);


		}
		
		// there an assumption there will be one, otherwise this ultimately will result in a 
		// null pointer exception where used...
		
		private Packet getFirstPacketWithTimestamp() {
			for (Packet p : packetList) {
				if (p.hasSecondaryHeader()) {
					return p;
				}
			}
			return null;
		}

		public long readEndBoundary() {
			long endBoundary = 0L;
			int loffset = 64;
			endBoundary =  ((long)data[loffset++] << 56) & 0xff00000000000000l;
			endBoundary |= ((long)data[loffset++] << 48) & 0x00ff000000000000l;
			endBoundary |= ((long)data[loffset++] << 40) & 0x0000ff0000000000l;
			endBoundary |= ((long)data[loffset++] << 32) & 0x000000ff00000000l;
			endBoundary |= ((long)data[loffset++] << 24) & 0x00000000ff000000l;
			endBoundary |= ((long)data[loffset++] << 16) & 0x0000000000ff0000l;
			endBoundary |= ((long)data[loffset++] <<  8) & 0x000000000000ff00l;
			endBoundary |=  (long)data[loffset++]        & 0x00000000000000ffl;
			
			return endBoundary;
		}

		/**
		 * Write the end boundary to the StaticHeader data buffer
		 */
		private void writeEndBoundary() {
			
			Packet lastPacket = getLastPacketWithTimestamp();
			// we don't check for non-null result... assume that's fatal issue right now that needs to be checked
			//System.out.println("Last packet end boundary: " + PDSDate.getMicrosSinceEpoch(lastPacket.getTimeStamp(8)));
			long ietTime = 0L;
			
			/*if ((rdrName == RDRName.VIIRS_Science) || (rdrName == RDRName.NPP_Ephemeris_and_Attitude)) {
				
				LPEATEDate lpd = new LPEATEDate(lastPacket.getTimeStamp(8));
				ietTime = lpd.getMicrosSinceEpoch();
				
				
				if (rdrName == RDRName.VIIRS_Science) {
					ietTime += 1400000L; // 1350000L worked for the 1st set, this works for the 2nd
				} else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude) {
					ietTime += 1000000L;
				}

			} else {
				ietTime = PDSDate.getMicrosSinceEpoch(lastPacket.getTimeStamp(8)) + 1L; // non-inclusive
			}
			
			endBoundary = ietTime;*/
			ietTime = LeapDate.getMicrosSinceEpoch(lastPacket.getTimeStamp(8)); //Why +1l?
			if (rdrName == RDRName.VIIRS_Science)
			{
				endBoundary=VIIRSGranule.getEndBoundary(ietTime);
				System.out.println("In write endBoundary:" + ietTime + " " + endBoundary);
			}
			else if (rdrName == RDRName.NPP_Ephemeris_and_Attitude)
			{
				endBoundary=SpacecraftDiaryGranule.getEndBoundary(ietTime); 
				System.out.println("In write endBoundary:"+ietTime+" "+ endBoundary);  
			}
			else if (rdrName == RDRName.CRIS_Science)
			{
				endBoundary=CRISGranule.getEndBoundary(ietTime); 
				System.out.println("In write endBoundary:"+ietTime+" "+ endBoundary);  
			}
			else if (rdrName == RDRName.ATMS_Science)
			{
				endBoundary=ATMSGranule.getEndBoundary(ietTime); 
				System.out.println("In write endBoundary:"+ietTime+" "+ endBoundary);  
			}
			else
			{
				endBoundary=ietTime;
			}
			
			// VIIRS partial test
			//if (rdrName == RDRName.VIIRS_Science) {
			//	endBoundary = 1422168959100000L;
			//}
			
			data[offset++] = (byte)((endBoundary >>> 56) & 0x0ffL);
			data[offset++] = (byte)((endBoundary >>> 48) & 0x0ffL);
			data[offset++] = (byte)((endBoundary >>> 40) & 0x0ffL);
			data[offset++] = (byte)((endBoundary >>> 32) & 0x0ffL);
			data[offset++] = (byte)((endBoundary >>> 24) & 0x0ffL);
			data[offset++] = (byte)((endBoundary >>> 16) & 0x0ffL);
			data[offset++] = (byte)((endBoundary >>> 8) & 0x0ffL);
			data[offset++] = (byte)(endBoundary & 0x0ffL);
			
	
		}

		/**
		private void writeApplicationStorageArea() throws HDF5LibraryException, NullPointerException {
			System.out.println("Counter basedd Offset into storage area: " + offset);
			for (Packet p : packetList) {
				System.out.println("p.getSize()" + p.getSize());
				writeBytes(p.getData(), p.getSize());
			}
			
		}

		private void writeBytes(byte[] from, int size) {
			for (int i = 0; i < size; i++) {
				this.data[offset++] = from[i];
			}
		}
		 * @throws RtStpsException 
		**/
		
		
		// there an assumption there will be one, otherwise this ultimately will result in a 
		// null pointer exception where used...
		
		private Packet getLastPacketWithTimestamp() {
			int end = packetList.size() - 1;
			
			// for larger granules this could be worth it
			for (int i = end; i >= 0; i--) {
				
				Packet p = packetList.get(i);
				if (p.hasSecondaryHeader()) {
					return p;
				}
			}
			return null;
		}

		/**
		 * Write the entire filled out data buffer to the HDF file 
		 * at the specified DataSet handle using the data buffer array
		 * @exception wraps any HDF exceptions in an RtStpsException
		 */
		private void writeToHDF() throws RtStpsException {
			try {
				H5.H5Dwrite(dataSet,
					     HDF5Constants.H5T_STD_U8BE,
					     HDF5Constants.H5S_ALL, 
					     HDF5Constants.H5S_ALL, 
						 HDF5Constants.H5P_DEFAULT,
						 data);
			} catch (HDF5LibraryException e) {
				throw new RtStpsException(e);
			}
		}



}
