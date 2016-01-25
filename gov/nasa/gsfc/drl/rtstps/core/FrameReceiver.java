/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * Any class that implements FrameReceiver accepts frames from a FrameSender.
 * 
 */
public interface FrameReceiver extends Receiver
{
    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException;

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException;

    /**
     * Flush the data pipeline.
     */
    public void flush() throws RtStpsException;

    /**
     * Get this receiver's name (for error messages).
     */
    public String getLinkName();
}
