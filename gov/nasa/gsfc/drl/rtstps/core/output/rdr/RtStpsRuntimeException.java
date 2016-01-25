/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;


/**
 * Local customization of the RuntimeException mainly used in places
 * the checked RtStpsException is not allowed (such as Iterator). 
 * It is not meant to wrap other RuntimeExceptions but to wrap other
 * checked exceptions principally generated by the HDF library.
 * 
 * 
 *
 */
public class RtStpsRuntimeException extends RuntimeException {

	/**
	 * Default, starting point but currently unused...
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Wrap any Exception into a RtStpsRuntimeException
	 * @param e the Exception
	 */
	public RtStpsRuntimeException(Exception e) {
		super(e);
	}

	/**
	 * Make a new RtStpsRuntimeException with the String msg
	 * @param msg the message that goes into the exception
	 */
	public RtStpsRuntimeException(String msg) {
		super(msg);
	}


}
