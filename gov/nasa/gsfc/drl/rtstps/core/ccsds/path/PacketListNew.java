/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PacketPool;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PacketPoolBySize;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RtStpsRuntimeException;

import java.util.ArrayList;


/**
 * This class manages a sequential list of packets to be filled.
 * It reuses its Packet objects, so subsequent users must not cache them.
 * 
 * 
 */
final class PacketListNew
{
    private ArrayList<Packet> packetList = new ArrayList<Packet>();	
    private Packet currentPacket;
    private int currentPacketIndex = -1;
    private boolean isPartialCurrentPacket = true;
    
    
    private PacketPool packetPool = new PacketPoolBySize("PacketList", null);
	private long counter = 0L;

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
            // currentPacket = new Packet(packetLength);
        	// try to get a new one that's of this size that's been previously allocated
        	try {
				currentPacket = packetPool.get(packetLength);
			} catch (RtStpsException e) {
				throw new RtStpsRuntimeException(e);
			}
            packetList.add(currentPacket);
        }
        else
        {
        	// Three cases:  
        	// 1) The current packet is partially filled and of the size being requested,  send that on
        	// 2) The current packet is not of the correct size, so it will be reset but the data[] is actually larger
        	// 3) It not of the correct size, it will be reset but the data[] is smaller and so will be reallocated from the heap by packet.reset()
        	//
        	// Three ways to handle these:
        	// a) If its already the right size, just return it
        	// b) If its the wrong size, the data[] is larger, reset it and return it
        	// c) If its the wrong size, the data[] is smaller... Try to get it off the pool and return that instead, replacing its in the list
        	//
        	Packet p = (Packet)packetList.get(currentPacketIndex);
        	if (p.getPacketSize() == packetLength) {
        		// case a
        		currentPacket = p;
        	} else if (p.getData().length >= packetLength) {
        		// case b
        		currentPacket = p;
        		currentPacket.reset(packetLength);
        	} else {
        		// case c
        		try {
        			// hopefully come up with one of the right size that's been cached on the pool
        			// otherwise packetPool.get() will go to the heap any way...
        			Packet newP = packetPool.get(packetLength);
        			
        			// replace it on the list
        			packetList.set(currentPacketIndex, newP);
        			
        			// and put the old one on the pool
        			packetPool.put(p);
				} catch (RtStpsException e) {
					throw new RtStpsRuntimeException(e);
				}
        	}
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
    	// NOTE: the index is reset but the list size() will remain, in effect keeping the previous packets
    	// allocated in the list and they will then be reused next time the get() is called and the index
    	// is incremented. It will be point to the first now packet on the list.
    	
    	try {
			packetPool.flush(packetList); // put all the packet on the current list to the pool
		} catch (RtStpsException e) {
			throw new RtStpsRuntimeException(e);
		}
		
    	packetList.clear(); // clear the list completely
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
        
        if ((++counter  % 2000L) == 0) {
        	System.out.println("In PacketList -- " + this.packetPool.toString() + " -- PacketList size: " + packetList.size());
        	//packetPool.preen();
        }
        
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
