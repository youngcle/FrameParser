/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * This class contains packet status.
 * 
 */
public final class PacketStatus
{
    /**
     * Number of packets passed through this pipeline.
     */
    public long packetsOut = 0;

    /**
     * Number of discarded packets. Some packets that might be discarded
     * could be packets with fill or packets with invalid lengths.
     */
    public long discardedPackets = 0;

    /**
     * Number of short packets, which are packets that have appended
     * fill data.
     */
    public long packetsWithFill = 0;

    /**
     * Number of packets with invalid lengths.
     */
    public long invalidLengthPackets = 0;

    /**
     * Number of packet sequence errors discovered in this pipeline.
     */
    public long sequenceErrors = 0;

    /**
     * Number of missing packets. This is calculated from the packet
     * sequence errors.
     */
    public long missingPackets = 0;

    /**
     * Set all counters to zero.
     */
    public void clear()
    {
        packetsOut = 0;
        discardedPackets = 0;
        packetsWithFill = 0;
        invalidLengthPackets = 0;
        sequenceErrors = 0;
        missingPackets = 0;
    }

    /**
     * Count packet status.
     */
    void count(Packet packet)
    {
        Packet.Annotation pa = packet.getPacketAnnotation();
        if (packet.isDeleted()) ++discardedPackets;
        if (pa.isPacketWithFill) ++packetsWithFill;
        if (pa.isInvalidLength) ++invalidLengthPackets;
        if (pa.hasSequenceError) ++sequenceErrors;
        ++packetsOut;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer(300);
        sb.append("packetsOut=");
        sb.append(packetsOut);
        sb.append(" discardedPackets=");
        sb.append(discardedPackets);
        sb.append(" packetsWithFill=");
        sb.append(packetsWithFill);
        sb.append(" invalidLengthPackets=");
        sb.append(invalidLengthPackets);
        return sb.toString();
    }
}

