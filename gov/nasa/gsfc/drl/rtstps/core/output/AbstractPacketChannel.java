/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketReceiver;

/**
 * This packet output channel writes packets to the output stream.
 * 
 */
public abstract class AbstractPacketChannel extends AbstractChannel
        implements PacketReceiver, Cloneable
{
    /**
     * A constructor.
     */
    protected AbstractPacketChannel(String elementName)
    {
        super(elementName);
    }

    /**
     * Give a packet to this PacketReceiver.
     */
    public abstract void putPacket(Packet packet) throws RtStpsException;

    /**
     * Give an array of packets to this PacketReceiver.
     */
    public final void putPackets(Packet[] packets) throws RtStpsException
    {
        for (int n = 0; n < packets.length; n++)
        {
            putPacket(packets[n]);
        }
    }

    /**
     * Format the packet annotation into a 32-bit integer and write it to the
     * data output stream. The integer has the following format:
     * <pre>
     * bit 17       1= packet has invalid length, which is outside the configured
     *              minimum and maximum packet length for this packet stream.
     * bit 16       1= this packet could not be constructed in its entirety,
     *              and so it has appended fill data.
     * bits 15-0    The number of "good" bytes in this packet. For complete
     *              packets, it is the packet length. For packets with fill,
     *              it is the index of the first fill byte.
     * </pre>
     */
    protected void writePacketAnnotation(Packet packet)
            throws java.io.IOException
    {
        Packet.Annotation pa = packet.getPacketAnnotation();
        int x = pa.goodByteCount;
        if (pa.isInvalidLength) x |= 0x020000;
        if (pa.isPacketWithFill) x |= 0x010000;
        output.writeInt(x);
    }
}
