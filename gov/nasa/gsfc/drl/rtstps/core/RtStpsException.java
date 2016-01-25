/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * This class represents an RT-STPS error.
 * 
 * 
 */
public class RtStpsException extends Exception implements 
        java.io.Serializable
{
    /**
     * Create an RT-STPS exception with a specific message.
     */
    public RtStpsException(String message)
    {
        super(message);
    }
    
    /**
     * Change an HDF5LibraryException exception to an RT-STPS exception.
     */
    public RtStpsException(HDF5LibraryException e)
    {
        super(e.getMinorError(e.getMinorErrorNumber()), e);
    }

    /**
     * Change an exception to an RT-STPS exception.
     */
    public RtStpsException(Exception e)
    {
        super(e.getMessage(), e);
    }
    
    /**
     * Change an exception to an RT-STPS exception.
     */
    public RtStpsException(String message, Exception e)
    {
        super(message, e);
    }
    
    private static final long serialVersionUID = 1L;			
}
