/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.xstps.pds;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.util.Iterator;

/**
 * This class checks packets for wrong lengths and collects pertinent
 * information about wrong-length-packets for Sorcerer. You set up this
 * class by either setting minimum and maximum valid lengths or by giving
 * this class a list of valid lengths.
 * 
 * 
 */
final class WrongLengthList
{
    public static final int MAP_SIZE = 64;
    private java.util.HashSet<Integer> packetSizeSet = null;				
    private java.util.LinkedList<Integer> wlList = new java.util.LinkedList<Integer>();	
    private int maxLength = 0;
    private int minLength = 0;
    private boolean useMinMax = true;
    private int packets = 0;

    /**
     * Set a minimum and maximum packet length.
     */
    void setMinMaxLengths(int min, int max) throws RtStpsException
    {
        if (min < 15 || max < 15)
        {
            throw new RtStpsException("sorcerer: Both minLength and maxLength must be >= 15.");
        }
        if (min > max)
        {
            throw new RtStpsException("sorcerer: maxLength must be >= minLength.");
        }

        minLength = min;
        maxLength = max;
        useMinMax = true;
    }

    /**
     * Get the number of packets with wrong lengths.
     */
    final int getPacketCount()
    {
        return packets;
    }

    /**
     * Add a valid packet length to the list of valid packet lengths. Using this
     * method will override any prior calls to setMinMaxLengths.
     */
    void addPacketLength(int length)
    {
        if (packetSizeSet == null)
        {
            packetSizeSet = new java.util.HashSet<Integer>(MAP_SIZE);	
        }
        packetSizeSet.add(new Integer(length));
        useMinMax = false;
    }

    /**
     * Check the packet for an invalid length.
     */
    boolean check(gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet packet)
    {
        boolean bad = false;
        int length = packet.getSize();

        if (useMinMax)
        {
            bad = (length < minLength) || (length > maxLength);
        }
        else if (packetSizeSet != null)
        {
            bad = !packetSizeSet.contains(new Integer(length));
        }

        if (bad)
        {
            wlList.add(new Integer(packet.getSequenceCounter()));
        }

        if (bad) ++packets;

        return bad;
    }

    /**
     * Write wrong-length information to the construction record.
     */
    void printCS(java.io.DataOutput out) throws java.io.IOException
    {
        int count = wlList.size();
        out.writeInt(count);

        Iterator<Integer> i = wlList.iterator();

        while (i.hasNext())
        {
            Integer sequenceNumber = (Integer)i.next();
            out.writeInt(sequenceNumber.intValue());
        }
    }
}
