/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

public class UserBlockReader {
	private File hdfFile; // this one comes from the Java File API
	private RandomAccessFile in;
	private int userBlockSize;
	private byte[] userBlockBytes;
	
	public UserBlockReader(String filename, int userBlockSize) throws RtStpsException {
	//System.out.println("User Block name: " + filename + " User Block Size: " + userBlockSize);
		hdfFile = new File(filename);
		try {
			in = new RandomAccessFile(hdfFile, "r");
			in.seek(0);
			
		} catch (FileNotFoundException e) {
			throw new RtStpsException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		userBlockBytes = new byte[userBlockSize];
		
	}
	
	/**
	 * Get the UserBlock data and return it in a byte[] array.  This is likely to include zeros at the end
	 * which are not part of the real ASCII data values...
	 * @return a byte[] of the entire user block
	 * @throws RtStpsException wrapped IOException
	 */
	public byte[] readBytes() throws RtStpsException {
		try {
			//System.out.println("First byte: " + in.read());  // works
			//in.readFully(userBlockBytes, 0, userBlockSize); // does not work
			in.read(userBlockBytes); // works
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
	
		return userBlockBytes;
	}
	
	/**
	 * Get the UserBlock data and return it as a Java String.  Any zeros at the end are trimmed off.
	 * @return a XML as a String
	 * @throws RtStpsException wrapped IOException
	 */
	public String readString() throws RtStpsException {
		try {
			//in.readFully(userBlockBytes, 0, userBlockSize);  // does not work
			in.read(userBlockBytes);  // works
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
		// the exact size of the string data is less than or equal to the user block
		// so it likely has 0s at the end which need to be trimmed off before returning
		
		return new String(userBlockBytes, Charset.forName("US-ASCII")).trim();
	}
	 
	
	public void close() throws RtStpsException {
		try {
			in.close();
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
	}
}
