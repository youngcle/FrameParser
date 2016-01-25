/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;
import java.util.Iterator;

/**
 * RtStpsNodes that send frames to FrameReceivers use this class to broadcast
 * frames to more than one FrameReceiver. See FrameOutputTool, which you can
 * use to construct the broadcaster.
 * 
 */
public class FrameBroadcaster extends Broadcaster implements FrameReceiver
{
    /**
     * Create a FrameBroadcaster with an initial two target receivers.
     * @param name A name, which is usually the client's name, used in error
     *          messages.
     * @param fr1 The first frame receiver.
     * @param fr2 The second frame receiver.
     */
    public FrameBroadcaster(String name, FrameReceiver fr1, FrameReceiver fr2)
    {
        super(name,fr1,fr2);
    }

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
        Iterator<Receiver> i = output.iterator();
        while (i.hasNext())
        {
            FrameReceiver fr = (FrameReceiver)i.next();
            fr.putFrames(frames);
        }
    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        if (!frame.isDeleted())
        {
            Iterator<Receiver> i = output.iterator();
            while (i.hasNext())
            {
                FrameReceiver fr = (FrameReceiver)i.next();
                fr.putFrame(frame);
            }
        }
    }
}
