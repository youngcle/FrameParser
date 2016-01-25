/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * A Sender sends a type of unit (packets, frames, or units) to one or more
 * receivers.
 * 
 */
public interface Sender
{
    /**
     * Add a Receiver to this sender's list of receivers.
     * @param receiver If the receiver is not of the expected type,
     *          then the method throws an RtStpsException.
     */
    public void addReceiver(Receiver receiver) throws RtStpsException;
}
