/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.xstps.pds;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * This class accumulates information about one application id.
 * 
 */
final class Appid
{
    private PacketKernel firstPacket;
    private PacketKernel lastPacket;
    private GapList gapList;
    private FillList fillList;
    private WrongLengthList wrongLengthList;
    private int reedSolomonCorrectedPackets = 0;
    private long firstByte;
    private long totalBytes = 0L;
    private int packets = 0;
    private int vcids;
    private int vcid1;
    private int vcid2 = -1;
    private int spid;
    private int id;
    private int timeOffset;
    private boolean discardBadLengthPackets = true;
    private boolean isQuicklookTypeEDS = false;
    private boolean hasCucSecondaryHeaderTime = false;

    /**
     * Create an application id object.
     * @param element a related XML element
     * @param defaultSpid a default spacecraft id, which may be overridden
     *          by a field in the element.
     * @param discardBadLengthPackets true=discard bad length packets
     * @param isQuicklookEDS true=this is a quicklook EDS
     */
    Appid(org.w3c.dom.Element element, int defaultSpid, boolean discardBadLengthPackets,
            boolean isQuicklookEDS) throws RtStpsException
    {
        firstPacket = new PacketKernel();
        lastPacket = new PacketKernel();
        wrongLengthList = new WrongLengthList();
        fillList = new FillList();

        this.discardBadLengthPackets = discardBadLengthPackets;
        this.isQuicklookTypeEDS = isQuicklookEDS;

        id = Convert.toInteger(element,"id",0,0,2047);
        vcid1 = Convert.toInteger(element,"vcid",0,0,63);
        vcid2 = Convert.toInteger(element,"vcid2",-1,-1,63);
        if (vcid2 == -1)
        {
            vcid2 = 0;
            vcids = 1;
        }
        else
        {
            vcids = 2;
        }

        spid = Convert.toInteger(element,"spid",defaultSpid,0);

        timeOffset = Convert.toInteger(element,"timeOffset",6,6);
        hasCucSecondaryHeaderTime = Convert.toBoolean(element,"CUCtime",false);

        int stepsize = Convert.toInteger(element,"stepsize",1);
        if (stepsize == 0) stepsize = 1;
        gapList = new GapList(stepsize);

        int min = Convert.toInteger(element,"minLength",-1);
        int max = Convert.toInteger(element,"maxLength",-1);

        if ((min != -1) || (max != -1))
        {
            wrongLengthList.setMinMaxLengths(min,max);
        }

        org.w3c.dom.NodeList nodes = element.getElementsByTagName("packetLength");
        for (int n = 0; n < nodes.getLength(); n++)
        {
            org.w3c.dom.Element e = (org.w3c.dom.Element)nodes.item(n);
            int len = Convert.toInteger(e,"length",0,15);
            wrongLengthList.addPacketLength(len);
        }
    }

    /**
     * Get the total number of fill bytes that are appended to short packets.
     */
    final long getTotalFillBytes()
    {
        return fillList.getTotalBytes();
    }

    /**
     * Get the number of packets that had an incorrect length. (deleted or not)
     */
    final int getTotalPacketsWithBadLength()
    {
        return wrongLengthList.getPacketCount();
    }

    /**
     * Get the number of gaps in sequence count.
     */
    final int getTotalGaps()
    {
        return gapList.getGapCount();
    }

    /**
     * Get the number of packets from Reed Solomon-corrected frames.
     */
    final int getReedSolomonCorrectedPacketCount()
    {
        return reedSolomonCorrectedPackets;
    }

    /**
     * Get the spacecraft id.
     */
    final int getSpacecraftId()
    {
        return spid;
    }

    /**
     * Get the application id.
     */
    final int getId()
    {
        return id;
    }

    /**
     * Store a packet in this application ID.
     * @return false if the packet should be discarded
     */
    boolean putPacket(Packet packet, PacketKernel packetKernel, long offset)
    {
        boolean bad = wrongLengthList.check(packet);
        if (bad && discardBadLengthPackets) return false;

        byte[] data = packet.getData();

        /**
         * If I'm doing a quicklook-flag EDS, then I skip packets with the
         * flag turned off.
         */
        if (isQuicklookTypeEDS)
        {
            int flags = (timeOffset == 6)? 14 : 6;
            if (data[flags] == 0) return false;
        }

        //I load the packet kernel with the current packet.
        packetKernel.set(packet,hasCucSecondaryHeaderTime,timeOffset);
        ++packets;

        if (packets == 1)
        {
            firstPacket.copy(packetKernel);
            firstByte = offset;
        }

        if (!isQuicklookTypeEDS && !lastPacket.isEmpty())
        {
            gapList.check(packet,packetKernel,lastPacket,offset);
        }

        //This line must follow the gap check.
        lastPacket.copy(packetKernel);

        fillList.check(packet,offset);

        if (packet.getFrameAnnotation().isRsCorrected)
        {
            ++reedSolomonCorrectedPackets;
        }

        totalBytes += packet.getSize();
        return true;
    }

    /**
     * Write information to the construction record file.
     */
    void printCS(java.io.DataOutput crecord) throws java.io.IOException
    {
        crecord.writeShort(spid);
        crecord.writeShort(id);
        crecord.writeLong(firstByte);
        crecord.writeInt(vcids);
        crecord.writeInt(vcid1 | (spid << 6));
        if (vcids == 2) crecord.writeInt(vcid2 | (spid << 6));

        gapList.printCS(crecord);
        fillList.printCS(crecord);
        wrongLengthList.printCS(crecord);

        crecord.writeLong(firstPacket.getPacketTime());
        crecord.writeLong(lastPacket.getPacketTime());
        crecord.writeLong(firstPacket.getEshTime());
        crecord.writeLong(lastPacket.getEshTime());
        crecord.writeInt(reedSolomonCorrectedPackets);
        crecord.writeInt(packets);
        crecord.writeLong(totalBytes);
        crecord.writeLong(0L);
    }
}
