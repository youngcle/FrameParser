/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds;
import gov.nasa.gsfc.drl.rtstps.core.Receiver;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.util.Iterator;

/**
 * StpsNodes that send packets to PacketReceivers use this class to broadcast
 * packets to more than one PacketReceiver.
 * 
 */
public class PacketBroadcaster extends gov.nasa.gsfc.drl.rtstps.core.Broadcaster
        implements PacketReceiver
{
    /**
     * Create a PacketBroadcaster.
     */
    public PacketBroadcaster(String name, PacketReceiver pr1, PacketReceiver pr2)
    {
        super(name,pr1,pr2);
    }

    /**
     * Give an array of packets to this PacketReceiver.
     */
    public void putPackets(Packet[] packets) throws RtStpsException
    {
        Iterator<Receiver> i = output.iterator();
        while (i.hasNext())
        {
            PacketReceiver pr = (PacketReceiver)i.next();
            pr.putPackets(packets);
        }
    }

    /**
     * Give a packet to this PacketReceiver.
     */
    public void putPacket(Packet packet) throws RtStpsException
    {
        if (!packet.isDeleted())
        {
            Iterator<Receiver> i = output.iterator();
            while (i.hasNext())
            {
                PacketReceiver pr = (PacketReceiver)i.next();
                pr.putPacket(packet);
            }
        }
    }
}
