/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

/**
 * This class defines setup information for a Path Service pipeline.
 * It is associated with one virtual channel and one spacecraft.
 * 
 */
public class PathServiceSetup
{
    /**
     * When the Path Service is unable to fill a packet in its entirety,
     * it will fill the remainder by repeatedly appending this fill byte.
     * (However, it will discard any packet that does not have a packet
     * header and at least one byte of real data.) It marks the packet
     * annotation for packets with fill data.
     */
    public byte packetFill = (byte)0x0c9;

    /**
     * If true, the Path Service will discard any packet with fill.
     */
    public boolean discardPacketsWithFill = false;

    /**
     * If true, the Path Service will discard idle packets.
     */
    public boolean discardIdlePackets = true;

    /**
     * This is the maximum rational packet size in bytes. If a packet size
     * exceeds this value, the Path Service assumes that either the data is
     * not really packet data or it is hopelessly lost in the current frame.
     * If the packet length fails this test, then the Path Service stops any
     * further processing for the current frame and resets itself to start
     * looking for a new packet in the next frame. It discards the failed
     * packet.
     */
    public int maxRationalPacketSize = 8192;


    public PathServiceSetup(org.w3c.dom.Element element)
            throws RtStpsException
    {
        maxRationalPacketSize = Convert.toInteger(element,
                "maxRationalPacketSize",maxRationalPacketSize);
        discardIdlePackets = Convert.toBoolean(element,"discardIdlePackets",
                discardIdlePackets);
        discardPacketsWithFill = Convert.toBoolean(element,
                "discardPacketsWithFill",discardPacketsWithFill);
        packetFill = (byte)Convert.toHexInteger(element,"fill",
                (int)packetFill);
    }
}
