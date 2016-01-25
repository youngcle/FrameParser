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
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * NO ANNOTATION, GENERIC OUTPUT CHANNEL.
 * This generic output channel writes units to the output stream. It is
 * also a FrameReceiver, so it can writes frames to an output stream too.
 * It does not attach annotation.
 * 
 */
public final class UnitChannel extends AbstractUnitChannel
        implements UnitReceiver, FrameReceiver, Cloneable
{
    /**
     * This is a class name for this RT-STPS node type. It is not the link name,
     * which is the name of one particular object.
     */
    public static final String CLASSNAME = "UNIT_NONE";

    /**
     * The constructor.
     */
    public UnitChannel()
    {
        super(CLASSNAME);
    }

    public void setOutput(String deviceType,String userLabel) throws RtStpsException {


        unitType = "NONE";
        annotationOption = "NO";

        if (deviceType.equals("file"))
        {
            device = new FileDevice(unitType,userLabel);
        }
        else if (deviceType.equals("socket"))
        {

            try {
                device = new SocketDevice(null);
            } catch (Exception e) {
                //System.out.println("No Simulcast connection available. Please ensure Simulcast server is running.");
                //System.out.println("failed to create socket device: " + e.toString());
            }
        }

        if (device != null)
        {
            output = device.getOutputStream();
        }

        count = new LongStatusItem("Output");
        outputErrorCount = new LongStatusItem("Errors");
        dicardedCount = new LongStatusItem("Discarded");

        statusItemList = new java.util.ArrayList<StatusItem>(3);
        statusItemList.add(count);
        statusItemList.add(outputErrorCount);
        statusItemList.add(dicardedCount);

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
