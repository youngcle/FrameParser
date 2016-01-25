/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * An RT-STPS node that sends units to receivers uses this utility to construct the
 * output object. The utility disguises the fact that the output may be more than
 * one receiver. It also guarantees that all receivers accept units.
 * 
 */
public class UnitOutputTool
{
    private UnitReceiver output = null;
    private UnitBroadcaster broadcaster = null;
    private String name;

    /**
     * Create the unit output tool.
     * @param name  usually the client's name, used in error
     *          messages.
     */
    public UnitOutputTool(String name)
    {
        this.name = name;
    }

    /**
     * Add a UnitReceiver to the output list.
     */
    public void addOutput(UnitReceiver ur)
    {
        if (output == null)
        {
            output = ur;
        }
        else if (broadcaster == null)
        {
            broadcaster = new UnitBroadcaster(name,output,ur);
            output = broadcaster;
        }
        else
        {
            broadcaster.addReceiver(ur);
        }
    }

    /**
     * Add a receiver to the output list.
     * @param r If the receiver is not of the expected type,
     *          then the method throws an exception.
     */
    public void addReceiver(Receiver r) throws RtStpsException
    {
        if (r instanceof UnitReceiver)
        {
            addOutput((UnitReceiver)r);
        }
        else
        {
            throw new RtStpsException(name + " sends units to unit receivers; " +
                    r.getLinkName() + " is not a unit receiver.");
        }
    }

    /**
     * Get the constructed unit receiver.
     */
    public UnitReceiver getOutput()
    {
        return output;
    }

    public String toString()
    {
        return name;
    }
}
