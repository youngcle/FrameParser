/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.xstps.pds;
import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * This RT-STPS node is a java version of the Sorcerer program. It creates Terra
 * and Aqua PDS or EDS files.
 * 
 * 
 */
public final class PdsOutput extends gov.nasa.gsfc.drl.rtstps.core.RtStpsNode
        implements gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketReceiver, Cloneable
{
    /**
     * This is the class name for this RT-STPS node type, which is also the
     * element name. It is not necessarily the link name, which is the name
     * of one particular object.
     */
    public static final String CLASSNAME = "sorcerer";

    //When a flush is issued, I generate the construction record. The command thread
    //performs the flush, which is a different thread then the data processing thread.
    //This creates synchronization issues here because I maintain lists of information
    //such as gap lists, which get concurrent modification errors if packets arrive
    //while I am creating the construction record. I use the following flag with
    //synchronization to ignore any post-flush data.
    private boolean flushActive = false;

    //Users have complained that sometimes the PDS files from one pass do not all have
    //the same timestamp in their file names. This is natural because each instance of
    //this class is responsible for creating its own PDS, so files from the same pass
    //could have times as much as one second apart. Some users do not like this. To
    //silence them, I define a static variable for the time that the first-up loads
    //and all others use. I reset it to null at flush time so that it will be ready
    //for the next pass.
    private static String commonTimestamp = null;

    private Appid[] apList;
    private int totalPackets = 0;
    private long totalBytes = 0L;
    private OutputFiles output;
    private int major = 0;
    private int minor = 0;
    private int spid = 42;
    private boolean test = false;
    private boolean isPDS = true;
    private StringBuffer constructionRecordName;
    private boolean isQuicklookTypeEDS = false;
    private LongStatusItem packetsWritten;
    private PacketKernel firstPacket;
    private PacketKernel lastPacket;
    private PacketKernel current;

    /**
     * A null constructor.
     */
    public PdsOutput()
    {
        super(CLASSNAME);
    }

    /**
     * Configure from an XML document. You cannot assume that any other stps
     * nodes have been created.
     */
    public void load(org.w3c.dom.Element element,
            Configuration configuration) throws RtStpsException
    {
        firstPacket = new PacketKernel();
        lastPacket = new PacketKernel();
        current = new PacketKernel();

        statusItemList = new java.util.ArrayList<StatusItem>(3);	
        packetsWritten = new LongStatusItem("Packets Written");
        statusItemList.add(packetsWritten);

        String name = element.getAttribute("label");
        super.setLinkName(name);

        isQuicklookTypeEDS = Convert.toBoolean(element,"QuicklookEDS",isQuicklookTypeEDS);

        String spds = element.getAttribute("type");
        if (spds.length() > 0) isPDS = spds.equals("PDS");

        major = Convert.toInteger(element,"major",major,0,255);
        minor = Convert.toInteger(element,"minor",minor,0,255);
        spid = Convert.toInteger(element,"spid",spid);
        test = Convert.toBoolean(element,"test",test);

        org.w3c.dom.NodeList nodes = element.getElementsByTagName("appid");
        int appidCount = nodes.getLength();

        if (appidCount < 1 || appidCount > 3)
        {
            throw new RtStpsException("sorcerer: There must be 1-3 appid elements in Sorcerer.");
        }

        boolean discardBadLengthPackets = Convert.toBoolean(element,
                "discardBadLengthPackets",true);

        apList = new Appid[appidCount];

        for (int n = 0; n < appidCount; n++)
        {
            org.w3c.dom.Element e = (org.w3c.dom.Element)nodes.item(n);
            apList[n] = new Appid(e,spid,discardBadLengthPackets,isQuicklookTypeEDS);
        }

        int kbPerFile = Convert.toInteger(element,"KBperFile",0,0);
        long bytesPerFile = 1024L * (long)kbPerFile;

        constructionRecordName = createFileName(element);
        String path = element.getAttribute("path");

        try
        {
            output = new OutputFiles(constructionRecordName,path);
            if (bytesPerFile > 0) output.setBytesPerFile(bytesPerFile);
            for (int n = 0; n < appidCount; n++)
            {
                output.setAppidSpid(n,apList[n].getId(),apList[n].getSpacecraftId());
            }
        }
        catch (java.io.IOException ioe)
        {
            throw new RtStpsException(ioe);
        }
    }

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     */
    public void finishSetup(Configuration configuration) throws RtStpsException
    {
        flushActive = false;
    }

    /**
     * Create a PDS/EDS file name for the construction record.
     */
    private StringBuffer createFileName(org.w3c.dom.Element element) throws RtStpsException
    {
        java.text.DecimalFormat dfspid = new java.text.DecimalFormat("000");
        java.text.DecimalFormat dfapid = new java.text.DecimalFormat("0000");
        StringBuffer id = new StringBuffer(40);

        id.append(isPDS? 'P' : 'E');

        for (int n = 0; n < apList.length; n++)
        {
            Appid ap = apList[n];
            id.append(dfspid.format((long)ap.getSpacecraftId()));
            id.append(dfapid.format((long)ap.getId()));
        }

        if (apList.length < 3) id.append("AAAAAAA");
        if (apList.length == 1) id.append("AAAAAAA");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyDDDHHmmss");
        String gmt = element.getAttribute("create");
        if ((gmt == null) || (gmt.length() == 0))
        {
            if (commonTimestamp == null)
            {
               commonTimestamp = sdf.format(new java.util.Date());
            }
            gmt = commonTimestamp;
        }
        else
        {
            try
            {
                sdf.parse(gmt);
            }
            catch (java.text.ParseException pe)
            {
                throw new RtStpsException(pe);
            }
        }
        id.append(gmt);

        //dataset counter
        int n = Convert.toInteger(element,"datasetCounter",0,0,9);
        id.append(Character.forDigit(n,10));

        id.append("00");
        id.append(isPDS? ".PDS" : ".EDS");
        return id;
    }

    /**
     * Give an array of packets to this PacketReceiver.
     */
    public void putPackets(Packet[] packets) throws RtStpsException
    {
        for (int n = 0; n < packets.length; n++)
        {
            putPacket(packets[n]);
        }
    }

    /**
     * Give a packet to this PacketReceiver.
     */
    public synchronized void putPacket(Packet packet) throws RtStpsException
    {
        if (flushActive) return;

        ++packetsWritten.value;

        //First I identify to which appid the packet belongs.
        Appid ap = null;
        int apindex = 0;
        int a = packet.getApplicationId();
        for (int n = 0; n < apList.length; n++)
        {
            if (apList[n].getId() == a)
            {
                ap = apList[n];
                apindex = n;
                break;
            }
        }

        //If ap==null, then I don't recognize the appid.
        if (ap != null)
        {
            //I store packet information in its appid. I load "current"
            //with "packet" in ap and not here because ap knows critical
            //information about how the packet is formed.
            boolean save = ap.putPacket(packet,current,totalBytes);

            if (save)
            {
                ++totalPackets;
                if (totalPackets == 1)
                {
                    //ap loaded current.
                    firstPacket.copy(current);
                }

                lastPacket.copy(current);
                totalBytes += packet.getSize();

                try
                {
                    output.write(packet,current,apindex);
                }
                catch (java.io.IOException ioe)
                {
                    throw new RtStpsException(ioe);
                }
            }
        }
    }

    /**
     * The session is over. Create the construction record file.
     */
    public void flush() throws RtStpsException
    {
        commonTimestamp = null;  //reset for next pass.

        //I synchronize so that I do not do this while inside putPacket.
        synchronized (this)
        {
            flushActive = true;
        }

        try
        {
            output.close(); //close the last data file

            /**
             * If the total packets is zero, then the session was shut down
             * without processing any packets. I skip the creation of the
             * construction record.
             */
            if (totalPackets == 0) return;

            File file = new File(output.getPath(),constructionRecordName.toString());
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos,8192);
            DataOutputStream crecord = new DataOutputStream(bos);

            crecord.writeByte(major);
            crecord.writeByte(minor);

            int type = 2;
            if (isPDS) type = 1;
            else if (isQuicklookTypeEDS) type = 3;
            crecord.writeByte(type);

            crecord.writeByte(0);
            crecord.writeBytes(constructionRecordName.substring(0,36));
            crecord.writeByte(test? 1 : 0);
            crecord.writeByte(0);
            crecord.writeLong(0L);

            crecord.writeShort(1);
            crecord.writeLong(firstPacket.getEshTime());  //session start
            crecord.writeLong(lastPacket.getEshTime());  //session stop

            long totalFillBytes = 0;
            int wrongLengthPackets = 0;
            int totalGaps = 0;
            int reedSolomonCorrectedPackets = 0;

            for (int n = 0; n < apList.length; n++)
            {
                Appid ap = apList[n];
                totalFillBytes += ap.getTotalFillBytes();
                wrongLengthPackets += ap.getTotalPacketsWithBadLength();
                totalGaps += ap.getTotalGaps();
                reedSolomonCorrectedPackets += ap.getReedSolomonCorrectedPacketCount();
            }

            crecord.writeLong(totalFillBytes);
            crecord.writeInt(wrongLengthPackets);
            crecord.writeLong(firstPacket.getPacketTime());
            crecord.writeLong(lastPacket.getPacketTime());
            crecord.writeLong(firstPacket.getEshTime());
            crecord.writeLong(lastPacket.getEshTime());
            crecord.writeInt(reedSolomonCorrectedPackets);
            crecord.writeInt(totalPackets);
            crecord.writeLong(totalBytes);
            crecord.writeInt(totalGaps);
            crecord.writeLong(lastPacket.getEshTime());
            crecord.writeLong((long)apList.length);

            for (int n = 0; n < apList.length; n++)
            {
                apList[n].printCS(crecord);
            }

            int files = output.getFileCount() + 1;
            crecord.writeInt(files);
            crecord.writeBytes(constructionRecordName.toString());
            for (int n = 0; n < 7; n++)
            {
                crecord.writeInt(0);
            }
            output.writeCS(crecord);
            crecord.close();
        }
        catch (java.io.IOException ioe)
        {
            throw new RtStpsException(ioe);
        }
    }
}
