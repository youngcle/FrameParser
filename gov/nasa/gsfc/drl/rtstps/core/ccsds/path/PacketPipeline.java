/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;
import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsNode;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketOutputTool;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketReceiver;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * This class handles a packet stream usually for one application id. Its
 * primary functions are to count status, check packet lengths, and check
 * the sequence counter.
 * 
 * 
 */
public class PacketPipeline extends RtStpsNode implements PacketReceiver,
        gov.nasa.gsfc.drl.rtstps.core.Sender, Cloneable
{
    /**
     * This is a class name for this STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "packet";

    private Setup setup;
    private Sequencer sequencer = null;
    private PacketReceiver output = null;
    private PacketOutputTool potool = null;
    private LongStatusItem packetsOut;
    private LongStatusItem discardedPackets;
    private LongStatusItem invalidLengthPackets;
    private LongStatusItem missingPackets;
    private LongStatusItem packetsWithFill;
    private LongStatusItem sequenceErrors;
    private LongStatusItem badLengthSample;


    /**
     * Create a PacketPipeline object.
     */
    public PacketPipeline()
    {
        super(CLASSNAME);
    }

    /**
     * Set up this stps node with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        String myname = element.getAttribute("label");
        super.setLinkName(myname);
        setup = new Setup(element);
        if (setup.checkSequenceCounter)
        {
            sequencer = new Sequencer();
        }

        packetsOut = new LongStatusItem("Packets Output");
        discardedPackets = new LongStatusItem("Discarded Packets");
        invalidLengthPackets = new LongStatusItem("Bad Lengths");
        badLengthSample = new LongStatusItem("Bad Length Sample");
        missingPackets = new LongStatusItem("Missing Packets");
        packetsWithFill = new LongStatusItem("Packets With Fill");
        sequenceErrors = new LongStatusItem("Sequence Errors");

        statusItemList = new java.util.ArrayList<StatusItem>(7);
        statusItemList.add(packetsOut);
        statusItemList.add(discardedPackets);
        statusItemList.add(invalidLengthPackets);
        statusItemList.add(badLengthSample);
        statusItemList.add(missingPackets);
        statusItemList.add(packetsWithFill);
        statusItemList.add(sequenceErrors);
    }

    /**
     * Add a Receiver to this sender's list of receivers.
     * @param r If the receiver is not of the expected type,
     *          then the method throws an StpsException.
     */
    public void addReceiver(gov.nasa.gsfc.drl.rtstps.core.Receiver r) throws RtStpsException
    {
        if (potool == null)
        {
            potool = new PacketOutputTool(getLinkName());
        }

        potool.addReceiver(r);
        output = potool.getOutput();
    }

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     */
    public void finishSetup(Configuration configuration) throws RtStpsException
    {
        output = potool.getOutput();
        potool = null;   //no longer needed
        if (output == null)
        {
            throw new RtStpsException(getLinkName() + " demands an output link.");
        }
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
    public void putPacket(Packet packet) throws RtStpsException
    {
        if (packet.isDeleted())
        {
            ++discardedPackets.value;
            return;
        }

        int appid = packet.getApplicationId();
        if (appid == Packet.IDLE_PACKET)
        {
            packet.setDeleted(true);
            ++discardedPackets.value;
            return;
        }

        int length = packet.getSize();
        if ((length < setup.minPacketSize) || (length > setup.maxPacketSize))
        {
            packet.getPacketAnnotation().isInvalidLength = true;
            badLengthSample.value = (long)length;

            if (setup.discardWrongLengthPackets)
            {
                ++discardedPackets.value;
                ++invalidLengthPackets.value;
                packet.setDeleted(true);
                return;
            }
        }

        if (setup.checkSequenceCounter)
        {
            int missing = sequencer.check(packet);
            missingPackets.value += (long)missing;
        }

        Packet.Annotation pa = packet.getPacketAnnotation();
        if (packet.isDeleted()) ++discardedPackets.value;
        if (pa.isPacketWithFill) ++packetsWithFill.value;
        if (pa.isInvalidLength) ++invalidLengthPackets.value;
        if (pa.hasSequenceError) ++sequenceErrors.value;
        ++packetsOut.value;

        output.putPacket(packet);
    }

    /**
     * Flush the pipeline.
     */
    public void flush() throws RtStpsException
    {
        output.flush();
    }

    private static final int UNINITIALIZED = -100;
    private static final int SEQUENCE_MASK = 0x3fff;

    /**
     * This class maintains the packet sequence count.
     */
    class Sequencer
    {
        private int expected = UNINITIALIZED;
        private int stepsize = 1;

        int check(Packet packet)
        {
            int missingPackets = 0;
            int actual = packet.getSequenceCounter();

            if (actual == expected)
            {
                expected += stepsize;
            }
            else if (expected == UNINITIALIZED)
            {
                expected = actual + 1;
            }
            else
            {
                missingPackets = (actual - expected) & SEQUENCE_MASK;
                packet.getPacketAnnotation().hasSequenceError = true;
                expected = actual + 1;
            }

            expected &= SEQUENCE_MASK;
            return missingPackets;
        }
    }

    public static class Setup
    {
        /**
         * The application id associated with this pipeline.
         */
        public int applicationId;

        /**
         * This is the maximum packet size in bytes. The pipeline will
         * mark packet annotation for packets with out-of-bounds lengths.
         * It may also delete wrong-length packets.
         */
        public int maxPacketSize = 8192;

        /**
         * This is the minimum packet size in bytes. The pipeline will
         * mark packet annotation for packets with out-of-bounds lengths.
         * It may also delete wrong-length packets.
         */
        public int minPacketSize = 15;

        /**
         * If true, the pipeline will discard any packet whose length is
         * less than the minimum packet size or greater than the maximum
         * packet size.
         */
        public boolean discardWrongLengthPackets = true;

        /**
         * If true, the pipeline checks the packet sequence counter and
         * marks packet annotation and status if it detects a sequence
         * error.
         */
        public boolean checkSequenceCounter = true;

        public Setup(org.w3c.dom.Element element) throws RtStpsException
        {
            applicationId = Convert.toInteger(element,"appid",applicationId,
                    0,2047);
            maxPacketSize = Convert.toInteger(element,"maxSize",
                    maxPacketSize,7);
            minPacketSize = Convert.toInteger(element,"minSize",
                    minPacketSize,7);
            checkSequenceCounter = Convert.toBoolean(element,
                    "checkSequenceCounter",checkSequenceCounter);
            discardWrongLengthPackets = Convert.toBoolean(element,
                    "discardWrongLengthPackets",discardWrongLengthPackets);
        }
    }
}

