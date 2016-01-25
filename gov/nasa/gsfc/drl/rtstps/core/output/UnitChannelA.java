/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output;
import gov.nasa.gsfc.drl.rtstps.core.FrameReceiver;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.Unit;
import gov.nasa.gsfc.drl.rtstps.core.UnitReceiver;

/**
 * PRE-ANNOTATION, GENERIC OUTPUT CHANNEL.
 * This generic output channel writes units to the output stream. It is
 * also a FrameReceiver, so it can writes frames to an output stream too.
 * It writes frame annotation immediately before each unit.
 * 
 */
public final class UnitChannelA extends AbstractUnitChannel
        implements UnitReceiver, FrameReceiver, Cloneable
{
    /**
     * This is a class name for this RT-STPS node type. It is not the link name,
     * which is the name of one particular object.
     */
    public static final String CLASSNAME = "UNIT_BEFORE";

    /**
     * The constructor.
     */
    public UnitChannelA()
    {
        super(CLASSNAME);
    }

    /**
     * Give a unit to this UnitReceiver.
     */
    public void putUnit(Unit unit) throws RtStpsException
    {
        if (!unit.isDeleted())
        {
            if (consecutiveErrors == CONSECUTIVE_PERMITTED_ERRORS)
            {
                ++dicardedCount.value;
            }
            else
            {
                try
                {
                    writeFrameAnnotation(unit);
                    output.write(unit.getData(),unit.getStartOffset(),
                            unit.getSize());
                    ++count.value;
                    consecutiveErrors = 0;
                }
                catch (java.io.IOException e)
                {
                    ++outputErrorCount.value;
                    ++consecutiveErrors;
                }
            }
        }
    }
}
