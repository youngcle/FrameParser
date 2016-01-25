/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds;
import gov.nasa.gsfc.drl.rtstps.core.Receiver;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

/**
 * An STPS node that sends packets to receivers uses this utility to
 * construct the output object. The utility disguises the fact that the
 * output may be more than one receiver. It also guarantees that all
 * receivers accept packets.
 * 
 */
public class PacketOutputTool
{
    private PacketReceiver output = null;
    private PacketBroadcaster broadcaster = null;
    private String name;

    /**
     * Create the packet receiver tool.
     * @param name usually the client's name, used in error
     *          messages.
     */
    public PacketOutputTool(String name)
    {
        this.name = name;
    }

    /**
     * Add a PacketReceiver object to the output list.
     */
    public void addOutput(PacketReceiver r)
    {
        if (output == null)
        {
            output = r;
        }
        else if (broadcaster == null)
        {
            broadcaster = new PacketBroadcaster(name,output,r);
            output = broadcaster;
        }
        else
        {
            broadcaster.addReceiver(r);
        }
    }

    /**
     * Add a receiver to the output list.
     * @param r If the receiver is not of the expected type,
     *          then the method throws an exception.
     */
    public void addReceiver(Receiver r) throws RtStpsException
    {
        if (r instanceof PacketReceiver)
        {
            addOutput((PacketReceiver)r);
        }
        else
        {
            throw new RtStpsException(name + " sends packets to packet receivers; " +
                    r.getLinkName() + " is not a packet receiver.");
        }
    }

    /**
     * Get the constructed packet receiver.
     */
    public PacketReceiver getOutput()
    {
        return output;
    }

    public String toString()
    {
        return name;
    }
}
