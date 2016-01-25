/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.xstps.pds;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.Iterator;

/**
 * This class checks for packet sequence gaps. It collects gap information.
 * 
 * 
 */
class GapList
{
    private Sequencer sequencer;
    private java.util.LinkedList<Item> gaplist;

    /**
     * This class contains information about one gap.
     */
    class Item
    {
        PacketKernel prePacket = new PacketKernel();
        PacketKernel postPacket = new PacketKernel();
        long datasetOffset = 0;
        int expected = 0;
        int missing = 0;

        Item(int missing, PacketKernel current, PacketKernel previous, long datasetOffset)
        {
            expected = sequencer.getExpectedNextCount();
            this.missing = missing;
            prePacket.copy(previous);
            postPacket.copy(current);
            this.datasetOffset = datasetOffset;
        }
    }

    /**
     * Create a gap list.
     * @param stepsize A non-zero sequence step size.
     */
    GapList(int stepsize)
    {
        sequencer = new Sequencer(stepsize);
        gaplist = new java.util.LinkedList<Item>();
    }

    /**
     * Get the number of detected gaps.
     */
    final int getGapCount()
    {
        return gaplist.size();
    }

    //I have a difficult bug in that printCS occasionally throws ConcurrentModificationException.
    //This should be impossible because the loop does not modify gaplist, and processing is single
    //threaded, so no one else could change it because there is no one else.
    //I am synchronizing to collect more information.

    /**
     * Check a packet for a gap between it and the previous packet.
     * @return true if a gap was detected
     */
    boolean check(Packet packet, PacketKernel current, PacketKernel previous,
            long datasetOffset)
    {
        boolean seeGap = false;
        int missing = sequencer.check(packet);
        if (missing > 0)
        {
            synchronized (this)
            {
                Item g = new Item(missing,current,previous,datasetOffset);
                gaplist.add(g);
                seeGap = true;
            }
        }
        return seeGap;
    }

    /**
     * Write gap information to the construction record.
     */
    synchronized void printCS(java.io.DataOutput out) throws java.io.IOException
    {
        int count = gaplist.size();
        out.writeInt(count);

        Iterator<Item> i = gaplist.iterator();

        while (i.hasNext())
        {
            Item gap = (Item)i.next();
            out.writeInt(gap.expected);
            out.writeLong(gap.datasetOffset);
            out.writeInt(gap.missing);
            out.writeLong(gap.prePacket.getPacketTime());
            out.writeLong(gap.postPacket.getPacketTime());
            out.writeLong(gap.prePacket.getEshTime());
            out.writeLong(gap.postPacket.getEshTime());
        }
    }
}
