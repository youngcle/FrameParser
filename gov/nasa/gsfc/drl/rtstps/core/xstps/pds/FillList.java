/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.xstps.pds;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.Iterator;

/**
 * This class looks for packets with fill data and collects pertinent
 * information about them for Sorcerer.
 * 
 * 
 */
class FillList
{
    private java.util.LinkedList<Item> flist = new java.util.LinkedList<Item>();	
    private long totalBytes = 0L;

    /**
     * This class contains information about one packet with fill.
     */
    class Item
    {
        long datasetOffset = 0;
        int sequenceCount = 0;
        int location = 0;
    }

    /**
     * Check a packet for fill.
     * @param packet the packet to be checked
     * @param datasetOffset A byte offset into the current dataset for
     *          this packet
     * @return true if this packet has fill
     */
    boolean check(Packet packet, long datasetOffset)
    {
        boolean hasFill = packet.getPacketAnnotation().isPacketWithFill;
        if (hasFill)
        {
            Item fill = new Item();
            fill.sequenceCount = packet.getSequenceCounter();
            fill.location = packet.getPacketAnnotation().goodByteCount;
            fill.datasetOffset = datasetOffset;
            flist.add(fill);
            totalBytes += (packet.getSize() - fill.location);
        }
        return hasFill;
    }

    /**
     * Get the number of packets with fill in this fill list.
     */
    final int getLength()
    {
        return flist.size();
    }

    /**
     * Get the total number of fill bytes.
     */
    final long getTotalBytes()
    {
        return totalBytes;
    }

    /**
     * Print fill-related information to the construction record.
     */
    void printCS(java.io.DataOutput out) throws java.io.IOException
    {
        int count = flist.size();
        out.writeInt(count);

        Iterator<Item> i = flist.iterator();

        while (i.hasNext())
        {
            Item fill = i.next();
            out.writeInt(fill.sequenceCount);
            out.writeLong(fill.datasetOffset);
            out.writeInt(fill.location);
        }

        out.writeLong(totalBytes);
    }
}
