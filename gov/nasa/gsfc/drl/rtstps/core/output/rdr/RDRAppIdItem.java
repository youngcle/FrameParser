/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import java.nio.charset.Charset;



/**
 * Read of write the AppIdItem from or to a StaticHeader
 * 
 *
 */
public class RDRAppIdItem {

		private String name; // ultimately a char[16] 
		private int value; // Uint32 
		private int pktTrackerIndex; // Uint32
		private int pktsReserved; // Uint32 
		private int pktsReceived; // Uint32
		
		private final static int size = 32; // 32 bytes
	
		// dataset
		//int dataSet;
		private byte[] data;
		private int offset;
		private RDRName rdrName;
		private PacketName packetName;
		
		
		/**
		 * Constructor to create an RDRAppIdItem from passed in values, likely to be written to a memory buffer as 
		 * define by the write() method, and then eventually to an HDF file.  This then is the "write" side of this class.
		 * @param rdrName 
		 * @param appId the value field
		 * @param pktTrackerIndex the packet tracker index
		 * @param pktsReceived the packets received
		 */
		public RDRAppIdItem(RDRName rdrName, PacketName packetName, int pktTrackerIndex, int pktsReceived) {
			this.rdrName = rdrName;
			this.packetName = packetName;
			this.name = packetName.toString();
			System.out.println("APID List Name:"+this.name);
			this.value = packetName.getAppId();
			this.pktTrackerIndex = pktTrackerIndex;
			
			//FIXSET1
			// calculating the concept of reserved is not as trivial as this... so x'd out for now
			int reserved = rdrName.getGranuleSize() * packetName.getTotalGroupCount();
			System.out.println(packetName.toString());
			
			if (packetName.getAppId() == 825)
			{
			    reserved = 1152;
			}
			if(packetName.toString() == "ENG_HS")
			    reserved = 4;
			if (packetName.toString() == "CSW1")
			{
			    reserved = 8;
			    System.out.println("reserved for CSW1 is: " + reserved);
			}
			
			if (packetName.toString() == "CSW2")
			    reserved = 8;
			if (packetName.toString() == "CSW3")
			    reserved = 8;
			if (packetName.toString() == "CSW4")
			    reserved = 8;
			if (packetName.toString() == "CSW5")
			    reserved = 8;
			if (packetName.toString() == "CSW6")
			    reserved = 8;
			if (packetName.toString() == "CSW7")
			    reserved = 8;
			if (packetName.toString() == "CSW8")
			    reserved = 8;
			if (packetName.toString() == "CSW9")
			    reserved = 8;

			if (packetName.toString() == "CMW1")
			    reserved = 8;
			if (packetName.toString() == "CMW2")
			    reserved = 8;
			if (packetName.toString() == "CMW3")
			    reserved = 8;
			if (packetName.toString() == "CMW4")
			    reserved = 8;
			if (packetName.toString() == "CMW5")
			    reserved = 8;
			if (packetName.toString() == "CMW6")
			    reserved = 8;
			if (packetName.toString() == "CMW7")
			    reserved = 8;
			if (packetName.toString() == "CMW8")
			    reserved = 8;
			if (packetName.toString() == "CMW9")
			    reserved = 8;
			
			if (packetName.toString() == "SLW1")
			    reserved = 8;
			if (packetName.toString() == "SLW2")
			    reserved = 8;
			if (packetName.toString() == "SLW3")
			    reserved = 8;
			if (packetName.toString() == "SLW4")
			    reserved = 8;
			if (packetName.toString() == "SLW5")
			    reserved = 8;
			if (packetName.toString() == "SLW6")
			    reserved = 8;
			if (packetName.toString() == "SLW7")
			    reserved = 8;
			if (packetName.toString() == "SLW8")
			    reserved = 8;
			if (packetName.toString() == "SLW9")
			    reserved = 8;
			
			if (packetName.toString() == "SMW1")
			    reserved = 8;
			if (packetName.toString() == "SMW2")
			    reserved = 8;
			if (packetName.toString() == "SMW3")
			    reserved = 8;
			if (packetName.toString() == "SMW4")
			    reserved = 8;
			if (packetName.toString() == "SMW5")
			    reserved = 8;
			if (packetName.toString() == "SMW6")
			    reserved = 8;
			if (packetName.toString() == "SMW7")
			    reserved = 8;
			if (packetName.toString() == "SMW8")
			    reserved = 8;
			if (packetName.toString() == "SMW9")
			    reserved = 8;
			
			if (packetName.toString() == "SSW1")
			    reserved = 8;
			if (packetName.toString() == "SSW2")
			    reserved = 8;
			if (packetName.toString() == "SSW3")
			    reserved = 8;
			if (packetName.toString() == "SSW4")
			    reserved = 8;
			if (packetName.toString() == "SSW5")
			    reserved = 8;
			if (packetName.toString() == "SSW6")
			    reserved = 8;
			if (packetName.toString() == "SSW7")
			    reserved = 8;
			if (packetName.toString() == "SSW8")
			    reserved = 8;
			if (packetName.toString() == "SSW9")
			    reserved = 8;
			
			if (packetName.toString() == "CLW1")
			    reserved = 8;
			if (packetName.toString() == "CLW2")
			    reserved = 8;
			if (packetName.toString() == "CLW3")
			    reserved = 8;
			if (packetName.toString() == "CLW4")
			    reserved = 8;
			if (packetName.toString() == "CLW5")
			    reserved = 8;
			if (packetName.toString() == "CLW6")
			    reserved = 8;
			if (packetName.toString() == "CLW7")
			    reserved = 8;
			if (packetName.toString() == "CLW8")
			    reserved = 8;
			if (packetName.toString() == "CLW9")
			    reserved = 8;
			if (packetName.toString() == "EIGHT_S_SCI")
			    reserved = 4;
			if (packetName.getAppId() == 1290)
			    reserved = 4;
			
			
			
			
	
			System.out.println("Granule Size:"+rdrName.getGranuleSize());
			System.out.println("Group count:"+packetName.getTotalGroupCount());
			//if (rdrName == RDRName.VIIRS_Science) {
			//	this.pktsReserved = reserved * 2;  // times 2 is a HACK
			//} else {
			this.pktsReserved = reserved;
			//}
			
			//this.pktsReserved = pktsReceived;
			this.pktsReceived = pktsReceived;
			
			// doing this just ensures the entry in packet tracker has one empty entry for it...
			//if (pktsReceived == 0) {
			//	this.pktsReserved = 1;
			//}
			
			System.out.println("Packet [" + name + "] expected [" + 
								rdrName.getGranuleSize() * packetName.getTotalGroupCount() + 
								"] -- Received [" + 
								pktsReceived + "]");
		}
	
		/**
		 * Constructor to create an RDRAppIdItem from a memory buffer, likely read from an HDF file. This then is
		 * the "read" side of this class. Once created each field will be populated from the data in the supplied buffer
		 * and accessible by the methods below.  The data buffer is not synchronized to these fields however, any changes to it
		 * are NOT reflected here after the constructor has been called
		 * @param data byte array that holds the data
		 * @param offset the offset into the array
		 */
		public RDRAppIdItem(byte[] data, int offset) {
			this.data = data;
			this.offset = offset;
			readName();
			readValue();
			readPktTrackerIndex();
			readPktsReserved();
			readPkstReceived();
		}

		/**
		 * Write the contents of this object's fields to the specified data buffer, return the new offset
		 * @param data the data buffer to be written to
		 * @param offset  the offset into the buffer
		 * @return int the number of bytes written (its fixed at the defined HDF size)s
		 */
		public int write(byte[] data, int offset)  {
			//this.dataSet = dataSet;
			this.data = data;
			this.offset = offset;
			writeName();
			writeValue();
			writePktTrackerIndex();
			writePktsReserved();
			writePkstReceived();
			
			return RDRAppIdItem.size;
		}
		
		/**
		 * Return the name of packets in the PacketTracker
		 * @return the name in the <code>String</code>
		 */
		public String getName() {
			return this.name;
		}
		
		/**
		 * Return the app id value of the first packet in the packetTracker. For segmented
		 * packets this will be the first packet in the segmented group.
		 * @return the value as an <code>int</code>
		 */
		public int getValue() {
			return this.value;
		}
		
		/**
		 * Return the pktTrackerIndex
		 * @return the pktTrackerIndex as an <code>int</code>
		 */
		public int getPktTrackerIndex() {
			return this.pktTrackerIndex;
		}
		
		/**
		 * Return the pktsReserved field.  This is the count of the packet expected.
		 * For DRL it is the same as pktsReceived
		 * @return the pktsReserved as an <code>int</code>
		 */
		public int getPktsReserved() {
			return this.pktsReserved;
		}
		
		/**
		 * Return the pktsReceived field. This is the count of the packet expected.
		 * For DRL it is the same as pktsReserved.
		 * @return the pktsReceived as an <code>int</code>
		 */
		public int getPktsReceived() {
			return this.pktsReceived;
		}
		
		/**
		 * Return the AppIdItem's size which is fixed
		 * @return the size as an <code>int</code>
		 */	
		public static int getHDFSize() {
			return RDRAppIdItem.size;
		}
		
		/**
		 * Write the name (the name associate with the application ID).
		 * It is copied to a buffer.
		 */
		private void writeName() {
			byte[] ascii = name.getBytes(Charset.forName("US-ASCII"));
			for (int i = 0; i < 16; i++) {
				data[offset+i] = 0;
			}
			int copyLen = ascii.length;
			if (copyLen > 16) {
				copyLen = 16;
			}
			for (int i = 0; i < copyLen; i++) {
				data[offset+i] = ascii[i];
			}
			offset += 16;
		}
		
		/**
		 * Write the value, it is copied to a buffer.
		 */
		private void writeValue()  {
			
			data[offset++] = (byte)((value >>> 24) & 0x0ff);
			data[offset++] = (byte)((value >>> 16) & 0x0ff);
			data[offset++] = (byte)((value >>> 8) & 0x0ff);
			data[offset++] = (byte)(value & 0x0ff);

		}
		
		/**
		 * Write the pktTrackerIndex, it is copied to a buffer.
		 */
		private void writePktTrackerIndex()  {
			
			data[offset++] = (byte)((pktTrackerIndex >>> 24) & 0x0ff);
			data[offset++] = (byte)((pktTrackerIndex >>> 16) & 0x0ff);
			data[offset++] = (byte)((pktTrackerIndex >>> 8) & 0x0ff);
			data[offset++] = (byte)(pktTrackerIndex & 0x0ff);

		}
		
		/**
		 * Write the pktsReserved, it is copied to a buffer.
		 */
		private void writePktsReserved() {
			
			data[offset++] = (byte)((pktsReserved >>> 24) & 0x0ff);
			data[offset++] = (byte)((pktsReserved >>> 16) & 0x0ff);
			data[offset++] = (byte)((pktsReserved >>> 8) & 0x0ff);
			data[offset++] = (byte)(pktsReserved & 0x0ff);

		}
		
		/**
		 * Write the pktsReceived, it is copied to a buffer.
		 */
		private void writePkstReceived() {
			
			data[offset++] = (byte)((pktsReceived >>> 24) & 0x0ff);
			data[offset++] = (byte)((pktsReceived >>> 16) & 0x0ff);
			data[offset++] = (byte)((pktsReceived >>> 8) & 0x0ff);
			data[offset++] = (byte)(pktsReceived & 0x0ff);
			
	
		}
		
		/**
		 * Read the name from a buffer to an internal field
		 */
		private void readName() { 
			this.name = new String(data, offset, 16, Charset.forName("US-ASCII")).trim();
			offset += 16;
		}
		
		/**
		 * Read the value from a buffer to an internal field
		 */
		private void readValue() {
			
			value = ((data[offset++] << 24) & 0xff000000) |
					((data[offset++] << 16) & 0x00ff0000) |
					((data[offset++] << 8) & 0x0000ff00) |
					(data[offset++] & 0x000000ff);

		}
		
		/**
		 * Read the pktTrackerIndex from a buffer to an internal field
		 */
		private void readPktTrackerIndex() {
				
			pktTrackerIndex = ((data[offset++] << 24) & 0xff000000) |
							  ((data[offset++] << 16) & 0x00ff0000) |
							  ((data[offset++] << 8) & 0x0000ff00) |
							  (data[offset++] & 0x000000ff);
		}
		
		/**
		 * Read the pktsReserved from a buffer to an internal field
		 */
		private void readPktsReserved() {
				
			pktsReserved = ((data[offset++] << 24) & 0xff000000) |
			  ((data[offset++] << 16) & 0x00ff0000) |
			  ((data[offset++] << 8) & 0x0000ff00) |
			  (data[offset++] & 0x000000ff);

		}
		
		/**
		 * Read the pktsReceived from a buffer to an internal field
		 */
		private void readPkstReceived() {
			
			pktsReceived = ((data[offset++] << 24) & 0xff000000) |
			  ((data[offset++] << 16) & 0x00ff0000) |
			  ((data[offset++] << 8) & 0x0000ff00) |
			  (data[offset++] & 0x000000ff);
	
		}
		
}
