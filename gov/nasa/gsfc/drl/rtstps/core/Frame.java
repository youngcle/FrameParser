/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * This class contains a single frame.
 * 
 */
public final class Frame extends Unit
{
    /**
     * If true, this frame is defined as a fill or idle frame. This is
     * often a CCSDS concept, but it can have meaning for non-CCSDS
     * frames too.
     */
    private boolean fillFrame = false;


    /**
     * Create a frame.
     * @param length the frame length in bytes
     */
    public Frame(int length)
    {
        super(length);
        frameAnnotation = new FrameAnnotation();
    }
    public Frame(byte[] indata)
    {
        super();
        data = indata;
        length = indata.length;
        frameAnnotation = new FrameAnnotation();
    }

    /**
     * Reset this object so that it can be used to contain another frame.
     */
    public final void reset()
    {
        frameAnnotation.reset();
        deleted = false;
        fillFrame = false;
    }

    /**
     * Is this a fill frame?
     */
    public final boolean isFillFrame()
    {
        return fillFrame;
    }

    /**
     * Mark this frame as a fill frame or not fill frame. By default,
     * a frame is not a fill frame.
     */
    public final void setFillFrame(boolean fill)
    {
        fillFrame = fill;
    }

    /**
     * Get debug information.
     */
    public String toString()
    {
        return "fill="+fillFrame+" deleted="+deleted+" "+frameAnnotation;
    }
}
