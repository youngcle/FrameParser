/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketBroadcaster;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketReceiver;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * This class receives packets. It contains a map of PacketReceivers keyed by
 * packet application id. It sends a packet to a PacketReceiver based on the
 * matching application id. This class is almost always attached to a Path
 * Service node.
 * 
 * 
 */
final class PacketRouter implements PacketReceiver
{
    private String name;
    private TreeMap<Integer,PacketReceiver> output = new TreeMap<Integer,PacketReceiver>();
    private PacketReceiver deadletters = null;
    private LongStatusItem unrouteablePackets;
    private LongStatusItem sentPackets;
    private LongStatusItem idlePackets;
    private LongStatusItem deletedPackets;

    /**
     * Create a PacketRouter.
     */
    PacketRouter(String name, Collection<StatusItem> statusItemList)	
    {
        this.name = name;
        sentPackets = new LongStatusItem("Output Packets");
        unrouteablePackets = new LongStatusItem("Unrouteable Packets");
        idlePackets = new LongStatusItem("Idle Packets");
        deletedPackets = new LongStatusItem("Deleted Packets");
        statusItemList.add(sentPackets);
        statusItemList.add(unrouteablePackets);
        statusItemList.add(idlePackets);
        statusItemList.add(deletedPackets);
    }

    public String getLinkName()
    {
        return name;
    }

    /**
     * Add a packet receiver to this class' receiver list.
     */
    void addPacketReceiver(int applicationId, PacketReceiver pr)
    {
        Integer key = new Integer(applicationId);
        PacketReceiver pr0 = (PacketReceiver)output.get(key);

        if (pr0 == null)
        {
            output.put(key,pr);
        }
        else if (pr0 instanceof PacketBroadcaster)
        {
            PacketBroadcaster pb = (PacketBroadcaster)pr0;
            pb.addReceiver(pr);
        }
        else
        {
            PacketBroadcaster pb = new PacketBroadcaster(name,pr0,pr);
            output.put(key,pb);
        }
    }


    /**
     * Set a receiver to which this class sends unrouteable packets.
     * Otherwise, it discards unrouteable packets.
     */
    void setDeadLetterPath(PacketReceiver pp)
    {
        deadletters = pp;
    }

    /**
     * Give an array of packets to this PacketReceiver.
     */
    public void putPackets(Packet[] packets) throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException
    {
        for (int n = 0; n < packets.length; n++)
        {
            putPacket(packets[n]);
        }
    }

    /**
     * Give a packet to this PacketReceiver.
     */
    public void putPacket(Packet packet) throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException
    {
        int appid = packet.getApplicationId();

        if (appid == Packet.IDLE_PACKET)
        {
            packet.setDeleted(true);
            ++idlePackets.value;
            ++deletedPackets.value;
        }
        else if (packet.isDeleted())
        {
            ++deletedPackets.value;
        }
        else
        {
            PacketReceiver pp = (PacketReceiver)output.get(new Integer(appid));
            if (pp != null)
            {
                pp.putPacket(packet);
                ++sentPackets.value;
            }
            else if (deadletters != null)
            {
                deadletters.putPacket(packet);
                ++unrouteablePackets.value;
            }
            else
            {
                ++deletedPackets.value;
                ++unrouteablePackets.value;
            }
        }
    }

    /**
     * Flush the pipeline.
     */
    public void flush() throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException
    {
        Iterator<PacketReceiver> i = output.values().iterator();
        while (i.hasNext())
        {
            PacketReceiver pp = (PacketReceiver)i.next();
            pp.flush();
        }
    }

    public String toString()
    {
        return name + ".PacketRouter";
    }
}
