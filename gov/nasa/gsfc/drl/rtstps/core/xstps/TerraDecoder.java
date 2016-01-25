/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.xstps;
import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.Frame;
import gov.nasa.gsfc.drl.rtstps.core.FrameReceiver;
import gov.nasa.gsfc.drl.rtstps.core.FrameSenderNode;
import gov.nasa.gsfc.drl.rtstps.core.PnDecoder;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.Sender;

/**
 * This class is a special Terra RT-STPS node. It removes Terra's internal PN
 * encoding, which is inside a CADU, starting with the first byte beyond the
 * VCDU header and ending with the byte just before the Reed Solomon parity.
 * 
 */
public final class TerraDecoder extends FrameSenderNode implements
        FrameReceiver, Sender, Cloneable
{
    /**
     * This is a class name for this RT-STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "terra_decoder";

    /**
     * These fields are the start and end byte offsets of the PN-encoded
     * region with the EOS Terra frame.
     */
    private static final int FIRST_BYTE = 10;
    private static final int LAST_BYTE = 1024 - 128;

    /**
     * Create a Terra decoder node.
     */
    public TerraDecoder()
    {
        super(CLASSNAME);
    }

    /**
     * Set up this RT-STPS node with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        String myname = element.getAttribute("label");
        super.setLinkName(myname);
    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        if (!frame.isDeleted() && !frame.isFillFrame())
        {
            PnDecoder.decode(frame.getData(),FIRST_BYTE,LAST_BYTE);
            output.putFrame(frame);
        }
    }

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
        for (int n = 0; n < frames.length; n++)
        {
            putFrame(frames[n]);
        }
    }
}
