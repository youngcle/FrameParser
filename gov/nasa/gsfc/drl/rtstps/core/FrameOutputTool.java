/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * An RT-STPS node that sends frames to receivers uses this utility to construct
 * the output object. The utility disguises the fact that the output may be
 * more than one receiver. It also guarantees that all receivers accept frames.
 * 
 */
public class FrameOutputTool
{
    private FrameReceiver output = null;
    private FrameBroadcaster broadcaster = null;
    private String name;

    /**
     * Create the frame receiver tool.
     * @param name A name, which is usually the client's name, used in error
     *          messages.
     */
    public FrameOutputTool(String name)
    {
        this.name = name;
    }

    /**
     * Add a FrameReceiver object to the output list.
     */
    public void addOutput(FrameReceiver r)
    {
        if (output == null)
        {
            output = r;
        }
        else if (broadcaster == null)
        {
            broadcaster = new FrameBroadcaster(name,output,r);
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
        if (r instanceof FrameReceiver)
        {
            addOutput((FrameReceiver)r);
        }
        else
        {
            throw new RtStpsException(name + " sends frames to frame receivers; " +
                    r.getLinkName() + " is not a frame receiver.");
        }
    }

    /**
     * Get the constructed frame receiver.
     */
    public FrameReceiver getOutput()
    {
        return output;
    }

    public String toString()
    {
        return name;
    }
}
