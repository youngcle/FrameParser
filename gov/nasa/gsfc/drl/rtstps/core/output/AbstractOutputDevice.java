/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output;

/**
 * This is the base class for all RT-STPS devices. It should probably
 * have extended DataOutputStream instead of using containment, and
 * I no doubt someday do just that. I did it this way originally
 * because I wanted more flexibility on the nature of output devices.
 * 
 */
abstract class AbstractOutputDevice
{
    protected java.io.DataOutputStream output = null;

    final java.io.DataOutputStream getOutputStream()
    {
        return output;
    }

    abstract void shutdown() throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
}
