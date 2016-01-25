/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.nio.charset.Charset;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;


/**
 * Utility class for reading/writing HDF attributes
 * 
 *
 */
public class HDFAttribute {
	/**
	 * Read an HDF attribute string from the given data set
	 * @param dataSet an descriptor to the open data set
	 * @param attribute the name of the attribute in question that should be in the data set
	 * @return a string containing the attribute's value
	 * @throws RtStpsException wraps any HDF library exception
	 */
	public static String readString(int dataSet, String attribute) throws RtStpsException {
		int r = 0;

		// [A]: Declare IDs here so we can clean up on failure
		int attrId = -1;
		int theType = -1;
		int dataSpaceId = -1;
		int attrType = -1;

		try {
			// open the attribute
			attrId  = H5.H5Aopen_name(dataSet, attribute);

			// determine its size
			theType = H5.H5Aget_type(attrId);
			int size = H5.H5Tget_size(theType);
			++size;  // add 1 for the 0 at the end of the chars...

			// get the size of the space
			dataSpaceId = H5.H5Aget_space(attrId);

			long[] dims = new long[2]; // should be 1 x 1 array
			//long[] maxDims = new long[1];
			r = H5.H5Sget_simple_extent_dims(dataSpaceId, dims, null);

			// make a byte[] buffer to hold it
			byte[][] buf = new byte[(int) dims[0]][size];  // it should only be 1-D...

			// make the Type
			attrType = H5.H5Tcopy(HDF5Constants.H5T_C_S1);

			// set the size of the item there to the Type just created...
			r = H5.H5Tset_size(attrType, size);
			r = H5.H5Aread(attrId, attrType, buf);

			// clean up the ids...

			r = H5.H5Tclose(attrType);
			r = H5.H5Tclose(theType);
			r = H5.H5Sclose(dataSpaceId);
			r = H5.H5Aclose(attrId);
			
			// FIXME now go back, copy the byte array and lose the zero string terminator
			// It MAY be we could just ask for 1 less byte in the calls above.
			// The reason this needs to occur is the java routines process in the zero into the string and
			// we get a funny char in the result...
			//
			byte[] tmp = java.util.Arrays.copyOf(buf[0], size-1);
			
			// convert the ascii bytes value to Java char-codes
			return new String(tmp, Charset.forName("US-ASCII")).trim();
		} 
		catch (HDF5Exception e) {
			// [A]: Clean up on failure; we don't want to leave open HDF5 IDs
			if(attrType > -1){
				try{
					H5.H5Tclose(attrType);
				} catch(HDF5Exception f){ }
			}
			if(theType > -1){
				try{
					H5.H5Tclose(theType);
				} catch(HDF5Exception f){ }
			}
			if(dataSpaceId > -1){
				try{
					H5.H5Sclose(dataSpaceId);
				} catch(HDF5Exception f){ }
			}
			if(attrId > -1){
				try{
					H5.H5Aclose(attrId);
				} catch(HDF5Exception f){ }
			}
			throw new RtStpsException(e);
		}
	}
	
	/**
	public static String readString(int dataSet, int attributeIndex) throws HDF5LibraryException {
		int attrId  = H5.H5Aopen_idx(dataSet, attributeIndex);
		byte[] buf = new byte[256]; // hopefully more than enough
		H5.H5Aread(attrId, HDF5Constants.H5T_C_S1, buf);
		H5.H5Aclose(attrId);
		
		return new String(buf, Charset.forName("US-ASCII"));
	}
	**/
	
	/**
	 * Write a string attribute value to the named string attribute in the data set
	 * @param dataSet a descriptor to the open data set
	 * @param name the name of the attribute in question that should be in the data set
	 * @param value the value of the attribute in question that should be in the data set
	 * @throws RtStpsException wraps any HDF library exception
	 */
	public static void writeString(int dataSet, String name, String value) throws RtStpsException {
		long attrDim[] = { 1, 1 };
		int attrId = -1;
		int attrType = -1;
		int attr = -1;
		int dimId = -1;

		try {
			attrId  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
			dimId = H5.H5Sset_extent_simple(attrId, 2, attrDim, null);
			attrType = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
		
			H5.H5Tset_size(attrType, value.length());
			attr = H5.H5Acreate(dataSet, name, attrType, attrId, HDF5Constants.H5P_DEFAULT);
			H5.H5Awrite(attr, attrType, value.getBytes(Charset.forName("US-ASCII"))); 
		
			// [A]: Close all opened HDF5 identifiers
			try {
				H5.H5Aclose(attr); 
			} catch (HDF5LibraryException e) {
				//throw new RtStpsException(e);
			}

			try {
				H5.H5Tclose(attrType);
			} catch (HDF5LibraryException e) {
				//throw new RtStpsException(e);
			}

			//try { /*dimId is not an HDF5 identifier...?*/
			//	H5.H5Sclose(dimId);
			//} catch (HDF5LibraryException e) {
			//	throw new RtStpsException(e);
			//}

			try {
				H5.H5Sclose(attrId); 
			} catch (HDF5LibraryException e) {
				//throw new RtStpsException(e);
			} 
		} 
		catch(HDF5Exception e){
			// [A]: Clean up on failure
			if(attr > -1){
				try{
					H5.H5Aclose(attr);
				} catch(HDF5Exception f){ }
			}
			if(attrType > -1){
				try{
					H5.H5Tclose(attrType);
				} catch(HDF5Exception f){ }
			}
			if(attrId > -1){
				try{
					H5.H5Sclose(attrId);
				} catch(HDF5Exception f){ }
			}
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * Write an array of string attribute values to the named string attribute in the data set
	 * @param dataSet a descriptor to the open data set
	 * @param name the name of the attribute in question that should be in the data set
	 * @param values an array of values of the attribute in question that should be in the data set
	 * @throws RtStpsException
	 */
	public static void writeStrings(int dataSet, String name, String[] values) throws RtStpsException {
		// [A]: Declare IDs here, so we can clean them up on failure
		int attrId = -1;
		int dimId = -1;
		int attrType = -1;
		int attr = -1;		

		if (values.length <= 0) {
			throw new RtStpsException("No values to write");
		}
		long attrDim[] = { (long) values.length, 1L };
		
		byte[][] bvalues = new byte[values.length][9];  // up to 8 letters including the terminating zero
		
		for (int i = 0; i < values.length; i++) {
			byte[] ascii = values[i].getBytes(Charset.forName("US-ASCII"));
			
			int length = ascii.length;
			if (length > 8) {
				length = 8;
			}
			for (int j = 0; j < length; j++) {
				bvalues[i][j] = ascii[j];
			}
		}
		try {
			attrId  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
			dimId = H5.H5Sset_extent_simple(attrId, 2, attrDim, null);
			attrType = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
			H5.H5Tset_size(attrType, 9);
			attr = H5.H5Acreate(dataSet, name, attrType, attrId, HDF5Constants.H5P_DEFAULT);
			H5.H5Awrite(attr, attrType, bvalues); 
	
			H5.H5Aclose(attr); 
			H5.H5Tclose(attrType);
			//H5.H5Sclose(dimId);
			H5.H5Sclose(attrId); 
		} 
		catch (HDF5Exception e) {
			// [A]: Clean up on failure
			if(attr > -1){
				try{
					H5.H5Aclose(attr);
				} catch(HDF5Exception f){ }
			}
			if(attrType > -1){
				try{
					H5.H5Tclose(attrType);
				} catch(HDF5Exception f){ }
			}
			if(attrId > -1){
				try{
					H5.H5Sclose(attrId);
				} catch(HDF5Exception f){ }
			}
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * Read an array of string attribute values to the named string attribute in the data set
	 * @param dataSet a descriptor to the open data set
	 * @param attribute the name of the attribute in question that should be in the data set
	 * @return an array of values of the attribute in question that should be in the data set
	 * @throws RtStpsException
	 */
	public static String[] readStrings(int dataSet, String attribute) throws RtStpsException {
		// [A]: Declare IDs here, so we can clean up on failure
		int attrId = -1;
		int attrType = -1;
		int theType = -1;
		int dataSpaceId = -1;

		try {
			// open the attribute
			attrId  = H5.H5Aopen_name(dataSet, attribute);
			
			// determine its size
			theType = H5.H5Aget_type(attrId);
			int size = H5.H5Tget_size(theType);
			++size;  // add 1 for the 0 at the end of the chars...
			
			// get the size of the space
			dataSpaceId = H5.H5Aget_space(attrId);
			
			long[] dims = new long[2];
			H5.H5Sget_simple_extent_dims(dataSpaceId, dims, null);
			
			// make a byte[] buffer to hold it
			byte[][] buf = new byte[(int) dims[0]][size];  // it should only be 1-D...
		
			// make the Type
			attrType = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
		
			// set the size of the item there to the Type just created...
			H5.H5Tset_size(attrType, size);
		
			H5.H5Aread(attrId, attrType, buf);
			
			// clean up the ids...
			H5.H5Tclose(theType);
			H5.H5Sclose(dataSpaceId);
			H5.H5Tclose(attrType);
			H5.H5Aclose(attrId);
		
			// convert the ascii bytes value to Java char-codes
			
			String[] outStrings  = new String[(int)dims[0]];
			for (int i = 0; i < (int)dims[0]; i++) {
				int count = 0;
				for (int j = 0; j < buf[i].length; j++) {
					if ((int)buf[i][j] == 0) break;
					++count;
				}
				outStrings[i] = new String(buf[i], 0, count, Charset.forName("US-ASCII"));
			}

			return outStrings;
		} 
		catch (HDF5Exception e) {
			// [A]: Clean up on failure
			if(theType > -1){
				try{
					H5.H5Tclose(theType);
				} catch(HDF5Exception f){ }
			}
			if(dataSpaceId > -1){
				try{
					H5.H5Sclose(dataSpaceId);
				} catch(HDF5Exception f){ }
			}
			if(attrType > -1){
				try{
					H5.H5Tclose(attrType);
				} catch(HDF5Exception f){ }
			}
			if(attrId > -1){
				try{
					H5.H5Aclose(attrId);
				} catch(HDF5Exception f){ }
			}
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * Read an HDF attribute unsigned long (64-bits) from the given data set. The return data type
	 * is signed in Java.  The user of this routine should either know that the information held
	 * in the attribute fits within the non-negative portion of the Java data type or know that 
	 * the item will be treated for example like a timestamp where sub-fields are taken to convert
	 * to other values.  If a full 64-bits is needed of unsigned values, then some other Java data
	 * type will have to returned (BigInteger?) to hold the value, and another method created in
	 * this utility class to support it.
	 * 
	 * @param dataSet an descriptor to the open data set
	 * @param attribute the name of the attribute in question that should be in the data set
	 * @return a long containing the attribute's value
	 * @throws RtStpsException wraps any HDF library exception
	 */
	public static Long readULong(int dataSet, String attribute) throws RtStpsException {
		// [A]: Declare IDs here, so we can clean up on failure
		int attrId = -1;
		int attrType = -1;
		int dataSpaceId = -1;
		int theType = -1;

		try {
			// open the attribute
			attrId  = H5.H5Aopen_name(dataSet, attribute);
			
			// determine its size
			theType = H5.H5Aget_type(attrId);
			int size = H5.H5Tget_size(theType);
			int order = H5.H5Tget_order(theType);
			
			// get the size of the space
			dataSpaceId = H5.H5Aget_space(attrId);
			
			long[] dims = new long[2];
			H5.H5Sget_simple_extent_dims(dataSpaceId, dims, null);
			
			// make a byte[] buffer to hold it
			byte[][] buf = new byte[(int) dims[0]][size];  // it should only be 1-D...
	
			// make the Type
			attrType = H5.H5Tcopy(theType); //HDF5Constants.H5T_NATIVE_ULLONG);
			
			printType(attrType);
	
			// set the size of the item there to the Type just created...
			H5.H5Tset_size(attrType, size);
	
			H5.H5Aread(attrId, attrType, buf);
			
			// clean up the ids...
			H5.H5Tclose(theType);
			H5.H5Sclose(dataSpaceId);
			H5.H5Tclose(attrType);
			H5.H5Aclose(attrId);
			
			// OK for some reason these are coming out reversed, in little endian...
			// SO flip them around...
			long result = 0l;
			
			// two choices, little or big
			if (order == HDF5Constants.H5T_ORDER_LE) {
				int shift = 0;
				for (int i = 0; i < size; i++) {
					result |= (buf[0][i] & 0x0ffL) << shift;
					shift += 8;
				}

			} else {
				// big endian 
				int shift = (size * 8) - 8;
				for (int i = 0; i < size; i++) {
					result |= (buf[0][i] & 0x0ffL) << shift;
					shift -= 8;
				}
			}
			
			return result;
		} 
		catch (HDF5Exception e) {
			// [A]: Clean up on failure
			if(theType > -1){
				try{
					H5.H5Tclose(theType);		
				} catch(HDF5Exception f){ }
			}
			if(dataSpaceId > -1){
				try{
					H5.H5Sclose(dataSpaceId);
				} catch(HDF5Exception f){ }
			}
			if(attrType > -1){
				try{
					H5.H5Tclose(attrType);
				} catch(HDF5Exception f){ }
			}
			if(attrId > -1){
				try{
					H5.H5Aclose(attrId);
				} catch(HDF5Exception f){ }
			}
			throw new RtStpsException(e);
		}
		
	}
	

	/**
	 * Write to an HDF attribute unsigned long (64-bits) to the given data set. The input data type
	 * is signed in Java.  However it is written to the <code>H5T_NATIVE_ULLONG</code> HDF data type.
	 * 
	 * @param dataSet an descriptor to the open data set
	 * @param name the name of the attribute in question that should be in the data set
	 * @param value a long containing the attribute's value
	 * @throws RtStpsException wraps any HDF library exception
	 */
	public static void writeULong(int dataSet, String name, long value) throws RtStpsException {
		// [A]: Declare IDs here so we can clean up on failure
		int attrId = -1;
		int attr = -1;		
	
		try {
		   long[] wrappedValue = new long[1];
		   wrappedValue[0] = value;
		   long attrDim[] = { 1L, 1L };
		   
		   attrId  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
		   int dimId = H5.H5Sset_extent_simple(attrId, 2, attrDim, null);
		   attr = H5.H5Acreate(dataSet, name, HDF5Constants.H5T_NATIVE_ULLONG, attrId, HDF5Constants.H5P_DEFAULT);
		   H5.H5Awrite(attr, HDF5Constants.H5T_NATIVE_ULLONG, wrappedValue); 
		   
			H5.H5Aclose(attr); 
			//H5.H5Sclose(dimId);
			H5.H5Sclose(attrId);
		} 
		catch (HDF5Exception e) {
			if(attr > -1){
				try{
					H5.H5Aclose(attr);
				} catch(HDF5Exception f){ }
			}
			if(attrId > -1){
				try{
					H5.H5Sclose(attrId);
				} catch(HDF5Exception f){ }
			}
			throw new RtStpsException(e);
		}
	}
	
	
	/**
	 * Read an HDF attribute set of unsigned long (64-bits) values from the given data set. The out data type
	 * is signed in Java.  However it is reading as if from the <code>H5T_STD_U64BE</code> HDF data type.
	 * This contradicts the data-type in writeULong method above but seems to work.
	 * 
	 * @param dataSet an descriptor to the open data set
	 * @param attribute the name of the attribute in question that should be in the data set
	 * @return a long array containing the attribute's value
	 * @throws RtStpsException wraps any HDF library exception
	 */
	public static long[] readULongs(int dataSet, String attribute) throws RtStpsException {
		// [A]: Declare IDs here so we can clean them up on failure
		int attrId = -1;
		int attrType = -1;
		int dataSpaceId = -1;
		int theType = -1;		

		try {
			// open the attribute
			attrId  = H5.H5Aopen_name(dataSet, attribute);
			
			// determine its size
			theType = H5.H5Aget_type(attrId);
			int size = H5.H5Tget_size(theType);
			int order = H5.H5Tget_order(theType);

			// get the size of the space
			dataSpaceId = H5.H5Aget_space(attrId);
			
			long[] dims = new long[2];
			H5.H5Sget_simple_extent_dims(dataSpaceId, dims, null);
			
			// make a byte[] buffer to hold it
			byte[][] buf = new byte[(int) dims[0]][size];  // it should only be 1-D...
		
			// make the Type
			attrType = H5.H5Tcopy(HDF5Constants.H5T_STD_U64BE);
		
			// set the size of the item there to the Type just created...
			H5.H5Tset_size(attrType, size);
		
			H5.H5Aread(attrId, attrType, buf);
			
			// clean up the ids...
			H5.H5Tclose(theType);
			H5.H5Sclose(dataSpaceId);
			H5.H5Tclose(attrType);
			H5.H5Aclose(attrId);
			
			long[] result = new long[(int)dims[0]];
			java.util.Arrays.fill(result, 0L);
			
			// two choices, little or big
			if (order == HDF5Constants.H5T_ORDER_LE) {
				for (int j = 0; j < (int)dims[0]; j++) {
					int shift = 0;
					for (int i = 0; i < 8; i++) {
						result[j] |= (buf[0][i] & 0x0ffL) << shift;
						shift += 8;
					}
				}

			} else {
				// big endian 
				for (int j = 0; j < (int)dims[0]; j++) {
					int shift = 56;
					for (int i = 0; i < 8; i++) {
						result[j] |= (buf[0][i] & 0x0ffL) << shift;
						shift -= 8;
					}
				}
			}
			
			return result;	
		} 
		catch (HDF5Exception e) {
			if(theType > -1){
				try{
					H5.H5Tclose(theType);		
				} catch(HDF5Exception f){ }
			}
			if(dataSpaceId > -1){
				try{
					H5.H5Sclose(dataSpaceId);
				} catch(HDF5Exception f){ }
			}
			if(attrType > -1){
				try{
					H5.H5Tclose(attrType);
				} catch(HDF5Exception f){ }
			}
			if(attrId > -1){
				try{
					H5.H5Aclose(attrId);
				} catch(HDF5Exception f){ }
			}
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * Write to an HDF attribute a set of unsigned long (64-bits) values to the given data set. The input data type
	 * is signed in Java.  However it is written to the <code>H5T_STD_U64BE</code> HDF data type.
	 * 
	 * @param dataSet an descriptor to the open data set
	 * @param name the name of the attribute in question that should be in the data set
	 * @param values an array of long containing the attribute's value
	 * @throws RtStpsException wraps any HDF library exception
	 */
	public static void writeULongs(int dataSet, String name, long[] values) throws RtStpsException {
		// [A] Declare IDs here so we can clean up on failure
		int attr = -1;
		int attrId = -1;

		if (values.length <= 0) {
			throw new RtStpsException("No values to write");
		}
		try {
			// copy the long values to a byte[] array, it's big endian in java land
			byte[][] bvalues = new byte[values.length][8];
	
			for (int i = 0; i < values.length; i++) {
				bvalues[i][0] = (byte)((values[i] >>> 56) & 0x00ffL);
				bvalues[i][1] = (byte)((values[i] >>> 48) & 0x00ffL);
				bvalues[i][2] = (byte)((values[i] >>> 40) & 0x00ffL);
				bvalues[i][3] = (byte)((values[i] >>> 32) & 0x00ffL);
				bvalues[i][4] = (byte)((values[i] >>> 24) & 0x00ffL);
				bvalues[i][5] = (byte)((values[i] >>> 16) & 0x00ffL);
				bvalues[i][6] = (byte)((values[i] >>> 8) & 0x00ffL);
				bvalues[i][7] = (byte)(values[i] & 0x00ffL);
			}
	
			long attrDim[] = { (long)values.length , 1L };
	
			attrId  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
			int dimId = H5.H5Sset_extent_simple(attrId, 2, attrDim, null);
			attr = H5.H5Acreate(dataSet, name, HDF5Constants.H5T_STD_U64BE, attrId, HDF5Constants.H5P_DEFAULT);
			H5.H5Awrite(attr, HDF5Constants.H5T_STD_U64BE, bvalues); 
	
			H5.H5Aclose(attr); 
			//H5.H5Sclose(dimId);
			H5.H5Sclose(attrId); 
		} 
		catch (HDF5Exception e) {
			if(attr > -1){
				try{
					H5.H5Aclose(attr);
				} catch(HDF5Exception f){ }
			}
			if(attrId > -1){
				try{
					H5.H5Sclose(attrId);
				} catch(HDF5Exception f){ }
			}
			throw new RtStpsException(e);
		}
	}
	/** neither of these seems to work, the array somehow is not copied correctly in their wrapper... or that is what it seems
	public static void writeULongs(int dataSet, String name, long[] values) throws NullPointerException, HDF5Exception {


		long attrDim[] = { 1L, (long)values.length };

		int attrId  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
		int dimId = H5.H5Sset_extent_simple(attrId, 2, attrDim, null);
		int attr = H5.H5Acreate(dataSet, name, HDF5Constants.H5T_STD_U64BE, attrId, HDF5Constants.H5P_DEFAULT);
		H5.H5Awrite(attr, HDF5Constants.H5T_STD_U64BE, values); 

		H5.H5Aclose(attr); 
		H5.H5Sclose(dimId);
		H5.H5Sclose(attrId); 
	}
	
	public static void writeULongs(int dataSet, String name, List<Long> values) throws NullPointerException, HDF5Exception {

		long[] _values = new long[values.size()];
		
		for (int i = 0; i < values.size(); i++) {
			_values[i] = values.get(i).longValue();
		}
		
		long attrDim[] = { 1, values.size() };

		int attrId  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
		int dimId = H5.H5Sset_extent_simple(attrId, 2, attrDim, null);
		int attr = H5.H5Acreate(dataSet, name, HDF5Constants.H5T_STD_U64BE, attrId, HDF5Constants.H5P_DEFAULT);
		H5.H5Awrite(attr, HDF5Constants.H5T_STD_U64BE, values); 

		H5.H5Aclose(attr); 
		H5.H5Sclose(dimId);
		H5.H5Sclose(attrId); 
	}
	 * @throws NullPointerException 
	 * @throws HDF5Exception 
	**/
	
	// FIXME these TWO methods should possibly IEEE something or other?
	
	/**
	 * Read an HDF attribute float from the given data set.  The item is actually read as a
	 * an HDF <code>H5T_NATIVE_ULLONG</code> date type, and then byte converted to a Java float.
	 * 
	 * @param dataSet an descriptor to the open data set
	 * @param attribute the name of the attribute in question that should be in the data set
	 * @return a float containing the attribute's value
	 * @throws RtStpsException wraps any HDF library exception
	 */
	public static Float readFloat(int dataSet, String attribute) throws RtStpsException {
		// [A] Declare IDs here so we can clean up on failure 		
		int attrId = -1;
		int attrType = -1;
		int dataSpaceId = -1;
		int theType = -1;

		try {
			// open the attribute
			attrId  = H5.H5Aopen_name(dataSet, attribute);
			
			// determine its size
			theType = H5.H5Aget_type(attrId);
			int size = H5.H5Tget_size(theType);
			int order = H5.H5Tget_order(theType);

			/**
			if (order == HDF5Constants.H5T_ORDER_BE) {
				System.out.println("Float big endian");
			} else if (order == HDF5Constants.H5T_ORDER_BE) {
				System.out.println("Float big endian");
			} else {
				System.out.println("Unknown order?");
			}
			**/
			// get the size of the space
			dataSpaceId = H5.H5Aget_space(attrId);
			
			long[] dims = new long[2];
			H5.H5Sget_simple_extent_dims(dataSpaceId, dims, null);
			
			// make a byte[] buffer to hold it
			byte[][] buf = new byte[(int) dims[0]][size];  // it should only be 1-D...
	
			// make the Type
			attrType = H5.H5Tcopy(HDF5Constants.H5T_NATIVE_FLOAT);
	
			// set the size of the item there to the Type just created...
			H5.H5Tset_size(attrType, size);
	
			H5.H5Aread(attrId, attrType, buf);
			
			// clean up the ids...
			H5.H5Tclose(theType);
			H5.H5Sclose(dataSpaceId);
			H5.H5Tclose(attrType);
			H5.H5Aclose(attrId);
			
			int ivalue = ( (int)buf[0][0] << 24 ) | 
							(((int)buf[0][1] << 16) & 0x00ff0000) | 
							(((int)buf[0][2] <<  8) & 0x0000ff00) | 
							((int)buf[0][3] & 0x000000ff) ;
			
			float f = Float.intBitsToFloat(ivalue);
			
			return f;
		} 
		catch (HDF5Exception e) {
			if(theType > -1){
				try{
					H5.H5Tclose(theType);
				} catch(HDF5Exception fe){ }
			}
			if(dataSpaceId > -1){
				try{
					H5.H5Sclose(dataSpaceId);
				} catch(HDF5Exception fe){ }
			}
			if(attrType > -1){
				try{
					H5.H5Tclose(attrType);
				} catch(HDF5Exception fe){ }
			}
			if(attrId > -1){
				try{
					H5.H5Aclose(attrId);
				} catch(HDF5Exception fe){ }
			}
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * Write an HDF attribute float to the given data set.  The item is actually written as a
	 * an HDF <code>H5T_NATIVE_ULLONG</code> date type.
	 * 
	 * @param dataSet an descriptor to the open data set
	 * @param name the name of the attribute in question that should be in the data set
	 * @throws RtStpsException wraps any HDF library exception
	 */
	public static void writeFloat(int dataSet, String name, float value) throws RtStpsException {
		// [A]: Declare IDs here so we can clean up on failure
		int attr = -1;
		int attrId = -1;

		try {
			float[] wrappedValue = new float[1];
			wrappedValue[0] = value;
	
			long attrDim[] = { 1, 1 };
	
			attrId  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
			int dimId = H5.H5Sset_extent_simple(attrId, 2, attrDim, null);
			attr = H5.H5Acreate(dataSet, name, HDF5Constants.H5T_NATIVE_FLOAT, attrId, HDF5Constants.H5P_DEFAULT);
			H5.H5Awrite(attr, HDF5Constants.H5T_NATIVE_FLOAT, wrappedValue); 
	
			H5.H5Aclose(attr); 
			//H5.H5Sclose(dimId);
			H5.H5Sclose(attrId); 
		} 
		catch (HDF5Exception e) {
			if(attr > -1){
				try{
					H5.H5Aclose(attr);
				} catch(HDF5Exception f){ }
			}
			if(attrId > -1){
				try{
					H5.H5Sclose(attrId);
				} catch(HDF5Exception f){ }
			}
			throw new RtStpsException(e);
		}
	}

	private static void printType(int attrType) {
		StringBuffer sb = new StringBuffer();
	     if (attrType == HDF5Constants.H5T_ALPHA_B16) {
	    	 sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_B32) { sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_B64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_B8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_F32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_F64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_I16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_I32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_I64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_I8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_U16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_U32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_U64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ALPHA_U8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ARRAY) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_BITFIELD) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_BKG_NO) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_BKG_YES) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_C_S1) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_COMPOUND) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CONV_CONV) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CONV_FREE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CONV_INIT) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_ASCII) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_ERROR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_10) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_11) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_12) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_13) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_14) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_15) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_2) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_3) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_4) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_5) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_6) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_7) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_CSET_RESERVED_9) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_DIR_ASCEND) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_DIR_DEFAULT) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_DIR_DESCEND) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ENUM) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_FLOAT) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_FORTRAN_S1) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_IEEE_F32BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_IEEE_F32LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_IEEE_F64BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_IEEE_F64LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEGER) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_B16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_B32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_B64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_B8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_F32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_F64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_I16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_I32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_I64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_I8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_U16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_U32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_U64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_INTEL_U8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_B16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_B32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_B64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_B8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_F32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_F64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_I16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_I32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_I64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_I8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_U16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_U32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_U64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_MIPS_U8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_B16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_B32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_B64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_B8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_CHAR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_DOUBLE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_FLOAT) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_HADDR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_HBOOL) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_HERR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_HSIZE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_HSSIZE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT_FAST16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT_FAST32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT_FAST64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT_FAST8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT_LEAST16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT_LEAST32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT_LEAST64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT_LEAST8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_INT8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_LDOUBLE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_LLONG) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_LONG) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_OPAQUE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_SCHAR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_SHORT) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UCHAR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT_FAST16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT_FAST32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT_FAST64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT_FAST8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT_LEAST16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT_LEAST32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT_LEAST64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT_LEAST8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT16) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT32) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT64) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_UINT8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_ULLONG) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_ULONG) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NATIVE_USHORT) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NCLASSES) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NO_CLASS) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NORM_ERROR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NORM_IMPLIED) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NORM_MSBSET) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NORM_NONE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NPAD) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_NSGN) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_OPAQUE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_OPAQUE_TAG_MAX) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ORDER_BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ORDER_ERROR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ORDER_LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ORDER_NONE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_ORDER_VAX) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_PAD_BACKGROUND) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_PAD_ERROR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_PAD_ONE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_PAD_ZERO) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_PERS_DONTCARE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_PERS_HARD) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_PERS_SOFT) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_REFERENCE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_SGN_2) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_SGN_ERROR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_SGN_NONE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_B16BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_B16LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_B32BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_B32LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_B64BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_B64LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_B8BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_B8LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_I16BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_I16LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_I32BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_I32LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_I64BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_I64LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_I8BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_I8LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_REF_DSETREG) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_REF_OBJ) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_U16BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_U16LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_U32BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_U32LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_U64BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_U64LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_U8BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STD_U8LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_ERROR) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_NULLPAD) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_NULLTERM) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_10) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_11) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_12) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_13) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_14) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_15) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_3) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_4) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_5) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_6) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_7) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_8) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_RESERVED_9) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STR_SPACEPAD) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_STRING) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_TIME) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_UNIX_D32BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_UNIX_D32LE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_UNIX_D64BE) {sb.append("");
	     } else if (attrType == HDF5Constants.H5T_UNIX_D64LE) {sb.append("H5T_UNIX_D64LE");
	     } else if (attrType == HDF5Constants.H5T_VARIABLE) {sb.append("H5T_VARIABLE");
	     } else if (attrType == HDF5Constants.H5T_VLEN) {sb.append("H5T_VLEN");
	     }
	}



}
