/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

/**
 * This class represents Frame Synchronizer subsystem exceptions.
 * 
 * 
 */
public class FsException extends RtStpsException
{
    FsException(String text)
    {
        super(text);
    }
    private static final long serialVersionUID = 1L;			
}
