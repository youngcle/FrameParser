/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketI;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PacketFactoryI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;


/**
 * Reads packets out of a known packet file, return an index to
 * a buffer that holds them using an Iterator interface.
 * 
 *
 */
public class FilePacketReader implements Iterator<PacketI>, PacketReaderI {
	private File inputFile;
	private FileInputStream fis;
	private int index = 0;
	private final static int bufSize = 1024*100;
	static byte[] dataBuf = new byte[bufSize];
	private int bufLength = 0;
	private PacketFactoryI factory;
	private int leftToProcess = 0;
	
	/**
	 * Given file name, open the file 
	 * @param fileName
	 * @throws IOException
	 */
	public FilePacketReader(PacketFactoryI factory, String fileName) throws RtStpsException {
		// open the input file
		inputFile = new File(fileName);
		
		try {
			fis = new FileInputStream(inputFile);
		
			//System.out.println("File size: " + fis.getChannel().size());
		
			bufLength = fis.read(dataBuf); // fill 'er up
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
		this.factory = factory;
	}
	
	/**
	 * Return a Packet constructed from an index into the buffer
	 * @return the next Packet
	 */
	@Override
	public PacketI next() {

		PacketI p;
		// really not sure what to do about this error case
		try {
			p = factory.make(index, dataBuf);
		} catch (Exception e) {
			return null;
		}

		// debugging, otherwise not used here...
		//int appid = getAppID();
		//System.out.println("APPID: " + appid);

		// update the index into the buffer
		index += getPacketSize();
		
		return p;
	}

	/**
	 * If there's space for a packet there's another... 
	 */
	@Override
	public boolean hasNext() {
		
		// first read more in if needed
		try {
			mayNeedToReadMore();
		} catch (IOException e) {
			return false;
		}
		// first if the end of the file is reached, there is nothing to do
		if (bufLength == -1) {
			return false;
		}
		// otherwise even if there's a fragment it should at least hold
		// a header, this is checked for below.  
		// If it holds a header (it should hold more) then if the
		// packet length is greater than what is in the buffer, we are done...
		// there was a final fragment and it cannot be processed...
		leftToProcess = bufLength - index;
		
		if (getPacketSize() > leftToProcess) {
			return false; // not enough data to make a packet
		}
		leftToProcess = 0;
		return true;  // there's enough...
	}



	@Override
	public void remove() {
		// not implemented...
	}
	
	public int getLeftOver() {
		//try {
		//	System.out.println("fis.available: " + fis.available());
		//} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		return leftToProcess;
	}
	
	private int getAppID() {
		int appid = (((dataBuf[index] << 8) & 0x0ff00) | (dataBuf[index+1] & 0x0ff));
		appid &= 0x7ff;
		return appid;
	}
	
	// this returns the length field of from the CCSDS header
	private int getPacketLength() {
		int packetLen = (((dataBuf[index+4] << 8) & 0x0ff00) | (dataBuf[index+5] & 0x0ff));
		packetLen &= 0x0ffff;
		return packetLen;
	}
	
	// this returns the full length of the packet by taking the headers length field
	// adding the header size to it and then adding 1... 
	private int getPacketSize() {
		return (Packet.PRIMARY_HEADER_LENGTH + getPacketLength() + 1);
	}
	
	// read more data into the buffer if needed, manage buffer if needed
	private void mayNeedToReadMore() throws IOException {
		int leftToProcess = bufLength - index;
		if (Packet.PRIMARY_HEADER_LENGTH > leftToProcess) {
			// without the full header its not possible to determine if
			// an entire packet is left in the buffer, so since that's  the case
			// copy any remaining bytes to the top of the buffer and read in more...
			readInMore();
		} else if (getPacketSize() > leftToProcess) {
			// this is the similar to the above case, in fact they could probably
			// be merged now that I sit here and type it up.  In this case there is
			// not enough remaining data to make up a full header but not enough to make 
			// a full packet... so copy and read in more
			//
			readInMore();
		}
	}
	
	private void readInMore() throws IOException {
		int j = 0;
		for (int i = index; i < bufLength; i++) {
			dataBuf[j++] = dataBuf[i];
		}
		// j is now past the old data...
		int amountToRead = bufSize - j; // this is the free space
		
		//System.out.print("Position: " + fis.getChannel().position() + " amountToRead: " + amountToRead);
		
		int amountRead = fis.read(dataBuf, j, amountToRead);
		
		//System.out.println(" amountRead: " + amountRead + " index: " + j);
		
		// if the end of file is hit in the read but there is still data in the buffer
		// then calculating bufLength must take that into account
		if (amountRead < 0) { // i.e. end of file has been reached
			bufLength = j;
		} else {
			bufLength = amountRead + j; // whatever is read plus whatever was old
		}
		index = 0; // back to the top...
		
		// it should really have at LEAST a CCSDS header in the buffer
		// so the length of the packet can be calculated... we'll check
		// that here and if this test fails, we are done...
		if (Packet.PRIMARY_HEADER_LENGTH > bufLength) {
			bufLength = -1;  // checked above
		}
	}

}
