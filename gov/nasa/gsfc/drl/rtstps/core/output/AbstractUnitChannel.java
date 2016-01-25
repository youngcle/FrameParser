/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output;
import gov.nasa.gsfc.drl.rtstps.core.Frame;
import gov.nasa.gsfc.drl.rtstps.core.FrameReceiver;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.Unit;
import gov.nasa.gsfc.drl.rtstps.core.UnitReceiver;

/**
 * This generic output channel writes units to the output stream. It is
 * also a FrameReceiver, so it can writes frames to an output stream too.
 * 
 */
public abstract class AbstractUnitChannel extends AbstractChannel
        implements UnitReceiver, FrameReceiver, Cloneable
{
    /**
     * A constructor.
     */
    protected AbstractUnitChannel(String elementName)
    {
        super(elementName);
    }

    /**
     * Give a unit to this UnitReceiver.
     */
    public abstract void putUnit(Unit unit) throws RtStpsException;

    /**
     * Give an array of units to this UnitReceiver.
     */
    public final void putUnits(Unit[] units) throws RtStpsException
    {
        for (int n = 0; n < units.length; n++)
        {
            putUnit(units[n]);
        }
    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        putUnit(frame);
    }

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
        for (int n = 0; n < frames.length; n++)
        {
            putUnit(frames[n]);
        }
    }
}
