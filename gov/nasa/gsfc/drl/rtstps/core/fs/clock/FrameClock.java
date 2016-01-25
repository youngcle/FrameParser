/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs.clock;

/**
 * This interface is the timer for the frame annotation timetag.
 * 
 */
public interface FrameClock
{
    /**
     * Start the clock. This is really only important for clocks that use
     * the current time.
     */
    public void start();

    /**
     * Get a timestamp. For some clocks you must call this method to advance
     * the time.
     */
    public long getTimeStamp();
}
