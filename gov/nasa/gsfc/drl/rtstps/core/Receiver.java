/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * This is a base interface to FrameReceiver, PacketReceiver, and
 * UnitReceiver. A Receiver object accepts frames, packets, or units from
 * a Sender.
 * 
 */
public interface Receiver
{
    /**
     * Flush the data pipeline.
     */
    public void flush() throws RtStpsException;

    /**
     * Get this receiver's name (for error messages).
     */
    public String getLinkName();
}
