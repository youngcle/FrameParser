/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;
import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * This class contains a CCSDS packet header. I use this class to assemble
 * a packet header that is split across two frames.
 * 
 */
final class SplitHeader
{
    private byte[] header = new byte[Packet.PRIMARY_HEADER_LENGTH];
    private int index = 0;

    /**
     * This is the frame annotation from the frame that contributed the
     * first half of the split header. I will need it when I construct the
     * packet.
     */
    private FrameAnnotation frameAnnotation;


    /**
     * Discard any bytes contained in this packet header.
     */
    final void clear()
    {
        index = 0;
        frameAnnotation = null;
    }

    /**
     * Get the bytes in the packet header.
     */
    final byte[] getData()
    {
        return header;
    }

    /**
     * Get the number of bytes yet to fill in this split packet header.
     */
    final int getToFill()
    {
        return Packet.PRIMARY_HEADER_LENGTH - index;
    }

    /**
     * Get the number of bytes already filled.
     */
    final int getFilledCount()
    {
        return index;
    }

    /**
     * Get the frame annotation from the frame that contributed the first
     * half of the split header.
     */
    public FrameAnnotation getFrameAnnotation()
    {
        return frameAnnotation;
    }

    /**
     * Begin a split packet header by moving the remaining bytes from the
     * packet zone, which must be less that 6 bytes, to this split header
     * container.
     */
    final void begin(PacketZone zone, FrameAnnotation frameAnnotation)
    {
        this.frameAnnotation = frameAnnotation;
        index = zone.moveRemainderTo(header,0);
    }

    /**
     * Finish a split header by moving a few bytes from the packet zone to
     * end of this packet header.
     * @return The packet data length field from the packet header. (It does
     *          not include +7, which is the complete packet length.)
     */
    final int finish(PacketZone zone)
    {
        int toFill = Packet.PRIMARY_HEADER_LENGTH - index;
        zone.moveTo(header,index,toFill);

        int packetDataLength = (int)header[Packet.LENGTH_OFFSET] << 8;
        packetDataLength |= (int)header[Packet.LENGTH_OFFSET+1] & 0x0ff;
        return packetDataLength;
    }
}
