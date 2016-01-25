/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;
import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * This class holds a Packet and is responsible for filling it with data.
 * 
 */
final class PacketCaddy
{
    private Packet packet = null;
    private Packet.Annotation packetAnnotation = null;
    private int packetLength;
    private int index;
    private byte[] data;
    private int bytesToFill;
    private PathServiceSetup setup;

    /**
     * This is a clone of the current packet's frame annotation. Normally
     * a packet uses the annotation from the first frame it came in, so
     * I don't need this field, and it is often null. However, sometimes
     * a packet that comes from multiple frames has different quality from
     * different frames, which I must OR together, so I do this assembly
     * in the clone. I cannot change fields in the packet's frame annotation
     * because that object is very likely shared by other frames and packets.
     */
    private FrameAnnotation frameAnnotationCopy = null;

    /**
     *  Construct the PacketCaddy.
     * @param setup A configuration.
     */
    PacketCaddy(PathServiceSetup setup)
    {
        this.setup = setup;
    }

    /**
     * Get the number of bytes yet to fill in the current packet.
     */
    final int getBytesToFill()
    {
        return bytesToFill;
    }

    /**
     * Change the packet this caddy is filling.
     */
    void setPacket(Packet p)
    {
        packet = p;
        index = 0;
        bytesToFill = packetLength = packet.getSize();
        data = packet.getData();
        packetAnnotation = packet.getPacketAnnotation();
        frameAnnotationCopy = null;
    }

    /**
     * I absorb another frame's annotation (its quality) into this packet's
     * frame annotation.
     */
    void setAnotherFrameAnnotation(FrameAnnotation fa)
    {
        if (frameAnnotationCopy != null)
        {
            frameAnnotationCopy.addQuality(fa);
        }
        else if (!fa.isEqualQuality(packet.getFrameAnnotation()))
        {
            frameAnnotationCopy =
                    (FrameAnnotation)packet.getFrameAnnotation().clone();
            frameAnnotationCopy.addQuality(fa);
        }
    }

    /**
     * Move all remaining data in the packet zone to the end of the
     * current packet. The method updates all indices.
     * @return The number of bytes yet to fill in this packet after
     *      this move operation completes.
     */
    int appendRestOfZone(PacketZone zone)
    {
        int count = zone.moveRemainderTo(data,index);
        index += count;
        bytesToFill -= count;
        if (bytesToFill == 0 && frameAnnotationCopy != null)
        {
            packet.setFrameAnnotation(frameAnnotationCopy);
        }
        return bytesToFill;
    }

    /**
     * Finish filling the current packet by moving as much data as needed
     * from the packet zone. The method updates all indices.
     */
    void finish(PacketZone zone)
    {
        zone.moveTo(data,index,bytesToFill);
        packetAnnotation.goodByteCount = packetLength;
        bytesToFill = 0;

        if ((packet.getApplicationId() == Packet.IDLE_PACKET) &&
                setup.discardIdlePackets)
        {
            packet.setDeleted(true);
        }
        else if (frameAnnotationCopy != null)
        {
            packet.setFrameAnnotation(frameAnnotationCopy);
        }
    }

    /**
     * Copy the packet header to the beginning of the current packet.
     */
    void appendHeader(SplitHeader header)
    {
        byte[] h = header.getData();
        System.arraycopy(h, 0, data, 0, h.length);
        index += h.length;
        bytesToFill -= index;
    }

    /**
     * Finish filling the current packet repeatedly appending the waste
     * byte to the packet. The method updates the packet annotation too.
     */
    void appendWaste()
    {
        packetAnnotation.isPacketWithFill = true;
        packetAnnotation.goodByteCount = index;

        if (setup.discardPacketsWithFill)
        {
            packet.setDeleted(true);
        }

        if ((packet.getApplicationId() == Packet.IDLE_PACKET) &&
                setup.discardIdlePackets)
        {
            packet.setDeleted(true);
        }

        java.util.Arrays.fill(data, index, index+bytesToFill,
                setup.packetFill);

        index += bytesToFill;
        bytesToFill = 0;

        if (frameAnnotationCopy != null)
        {
            packet.setFrameAnnotation(frameAnnotationCopy);
        }
    }
}
