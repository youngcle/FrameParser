/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.ArrayList;


/**
 * This class manages a sequential list of packets to be filled.
 * It reuses its Packet objects, so subsequent users must not cache them.
 * 
 * 
 */
final class PacketList
{
    private ArrayList<Packet> packetList = new ArrayList<Packet>();	
    private Packet currentPacket;
    private int currentPacketIndex = -1;
    private boolean isPartialCurrentPacket = true;
 

    /**
     * Get the next packet to fill from the packet list.
     * @param packetLength The length of the desired packet.
     * @return A packet to fill
     */
    Packet get(int packetLength)
    {
        ++currentPacketIndex;
        if (packetList.size() == currentPacketIndex)
        {
            currentPacket = new Packet(packetLength);
        	
        	
            packetList.add(currentPacket);
        }
        else
        {
        	
  
        	
        		currentPacket =(Packet)packetList.get(currentPacketIndex);
        		currentPacket.reset(packetLength);
        	
        }

        isPartialCurrentPacket = true;

        return currentPacket;
    }

    /**
     * Mark that the current packet, which was last obtained via get(), as a
     * completed one.
     */
    final void setCurrentPacketIsComplete()
    {
        isPartialCurrentPacket = false;
    }

    /**
     * Reset the packet list so it contains no full or partial packets.
     */
    final void flushAllData()
    {
 
        currentPacketIndex = -1;  // reset internal indexes and flags
        currentPacket = null;
    }

    /**
     * Remove all completed packets from the list but keep the last partial
     * one if it exists.
     */
    void flushCompletedPackets()
    {
        if (currentPacket == null) return;

        if (!isPartialCurrentPacket)
        {
            flushAllData();
        }
        else if (currentPacketIndex > 0)
        {
            /**
             * When the index > 0 and the last packet is partial, it means
             * I have a part-filled packet following a bunch of filled ones.
             * I want to put the partial one first and to forget about the
             * rest. I swap the first packet and part-filled packet positions.
             */
            Packet x = packetList.get(0);				
            packetList.set(0,currentPacket);
            packetList.set(currentPacketIndex,x);
            currentPacketIndex = 0;
            // NOTE: here again the index is reset but the underlying packet list is not touched beyond
            // the zero entry and the currentIndexEntry, so more may remain on the list and be reused
            
            // And now the hope is the get() takes care of finding better matches in terms of packet sizes
            // if the partial packets are in fact totally empty and not partially full of data... 
        }
        /**
         * The remaining case is a part-filled packet in the first position,
         * which is what I want, so I do nothing.
         */
        
        
    }

    /**
     * Get a list of completed packets in time order.
     * @return null if there are no completed packets in the list.
     */
    Packet[] getList()
    {
        Packet[] list = null;
        int completedPackets = currentPacketIndex;

        /**
         * I don't advance the packet index until I need a new packet, so
         * I must count the current packet if it is full.
         */
        if (!isPartialCurrentPacket) ++completedPackets;

        // NOTE: march through the list of USED packets, the list may contain more past this
        // so it would not work to call list.toArray()...
        
        if (completedPackets > 0)
        {
            list = new Packet[completedPackets];
            for (int n = 0; n < completedPackets; n++)
            {
                list[n] = (Packet)packetList.get(n);
            }
        }
        return list;
    }
}
