/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;
import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.Frame;
import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;
import gov.nasa.gsfc.drl.rtstps.core.FrameReceiver;
import gov.nasa.gsfc.drl.rtstps.core.Receiver;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsNode;
import gov.nasa.gsfc.drl.rtstps.core.Sender;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.AbstractService;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketOutputTool;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketReceiver;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;

import java.util.TreeMap;

import org.w3c.dom.Element;

/**
 * This class handles the first phase of the CCSDS Path Service. It performs
 * packet assembly from v2 CADUs and sends the packets to a PacketReceiver.
 * It does not sort packets, which it expects subsequent PacketReceivers to
 * do. It does identify idle packets, but it does no other packet functions
 * or verifications other than what it needs to do to assemble them.
 * <p>
 * This class receives frames as a FrameReceiver and decomposes each one's
 * packet zone into a stream of packets. You must set the PacketReceiver for
 * this class to operate successfully. The packet zone must include the
 * first header pointer in the first two bytes immediately followed by packet
 * data, which excludes any trailing bytes that are not packet parts.
 * 
 * 
 */
public class PathService extends AbstractService implements FrameReceiver,
        Sender, Cloneable
{
    /**
     * This is a class name for this STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "path";

    private static final int NO_PACKET_HEADER_IN_MPDU = 0x7ff;
    private static final int IDLE_VCDU = 0x07fe;

    private static final int FREE_STATE = 0;
    private static final int SPLIT_HEADER_STATE = 1;
    private static final int SPLIT_PACKET_STATE = 2;
    private int state = FREE_STATE;

    private PacketCaddy packetCaddy;
    private PathServiceSetup setup;
    private PacketZone packetZone;
    private SplitHeader splitHeader;
    private PacketList packetList;
    private int mpduStart;
    private int mpduEnd;
    private boolean frameHasBadFhp = false;
    private org.w3c.dom.NodeList pklinks;
    private PacketReceiver output = null;
    private PacketOutputTool potool = null;

    private LongStatusItem idleVcdus;
    private LongStatusItem badFirstHeaderPointers;
    private LongStatusItem troublesomeFrames;
    private LongStatusItem irrationalPacketLengths;
    private LongStatusItem createdPackets;
    private LongStatusItem discardedFragments;
    private LongStatusItem discardedFragmentByteCount;


    /**
     * Create a PathService object.
     */
    public PathService()
    {
        super(CLASSNAME);
    }

    /**
     * Set up this stps node with a configuration.
     */
    public void load(Element element, Configuration configuration)
            throws RtStpsException
    {
        super.load(element,configuration);
        setup = new PathServiceSetup(element);
        packetCaddy = new PacketCaddy(setup);
        packetZone = new PacketZone();
        splitHeader = new SplitHeader();
        packetList = new PacketList();
        pklinks = element.getElementsByTagName("pklink");

        idleVcdus = new LongStatusItem("Idle VCDUs");
        badFirstHeaderPointers = new LongStatusItem("Bad FHPs");
        troublesomeFrames = new LongStatusItem("Troublesome Frames");
        irrationalPacketLengths = new LongStatusItem("Irrational Packet Lengths");
        createdPackets = new LongStatusItem("Created Packets");
        discardedFragments = new LongStatusItem("Discarded Fragments");
        discardedFragmentByteCount = new LongStatusItem("Discarded Bytes");

        statusItemList.add(idleVcdus);
        statusItemList.add(badFirstHeaderPointers);
        statusItemList.add(troublesomeFrames);
        statusItemList.add(createdPackets);
        statusItemList.add(irrationalPacketLengths);
        statusItemList.add(discardedFragments);
        statusItemList.add(discardedFragmentByteCount);
    }

    /**
     * Add a Receiver to this sender's list of receivers.
     * @param receiver If the receiver is not of the expected type,
     *          then the method throws an StpsException.
     */
    public void addReceiver(Receiver receiver) throws RtStpsException
    {
        if (potool == null)
        {
            potool = new PacketOutputTool(getLinkName());
        }

        potool.addReceiver(receiver);
        output = potool.getOutput();
    }

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     */
    public void finishSetup(Configuration configuration) throws RtStpsException
    {
        super.finishSetup(configuration);

        TreeMap<String, RtStpsNode> stpsNodes = configuration.getStpsNodes();

        int length = pklinks.getLength();
        if (length > 0)
        {
            PacketRouter router = new PacketRouter(toString()+".PacketRouter",
                    statusItemList);
            addReceiver(router);

            for (int n = 0; n < length; n++)
            {
                Element pklink = (Element)pklinks.item(n);
                int appid = Convert.toInteger(pklink,"appid",0);
                String target = pklink.getAttribute("label");
                RtStpsNode stpsNode = (RtStpsNode)stpsNodes.get(target);

                if (stpsNode instanceof PacketReceiver)
                {
                    router.addPacketReceiver(appid,(PacketReceiver)stpsNode);
                }
                else
                {
                    pklinks = null;
                    throw new RtStpsException("path: " + target +
                            " is not a packet receiver.");
                }
            }
        }
        pklinks = null;

        output = potool.getOutput();
        potool = null;   //no longer needed
        if (output == null)
        {
            throw new RtStpsException(toString() + " demands an output link.");
        }
    }

    /**
     * Flush the pipeline.
     */
    public final void flush() throws RtStpsException
    {
        output.flush();
        packetList.flushAllData();
    }

    /**
     * Give a frame to this class for packet reassembly.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        if (!frame.isDeleted() && !frame.isFillFrame())
        {
            cadu.setFrame(frame);
            mpduStart = cadu.getdataZoneStartOffset();
            mpduEnd = cadu.getdataZoneEndOffset();

            //Check for frame sequence errors.
            sequencer.check(cadu);

            byte[] data = cadu.getData();
            FrameAnnotation frameAnnotation = cadu.getFrameAnnotation();

            //Make packets.
            boolean trouble = decomposeDataZone(data,frameAnnotation);

            if (trouble) ++troublesomeFrames.value;
            frameAnnotation.hasPacketDecompositionError = trouble;

            Packet[] packets = packetList.getList();
            if (packets != null)
            {
                createdPackets.value += packets.length;
                output.putPackets(packets);
                packetList.flushCompletedPackets();
            }
        }
    }

    /**
     * Decompose a packet zone into packets.
     * @param data An array of bytes that define the complete frame.
     * @param frameAnnotation The annotation for this frame.
     * @return true if there was any problem decomposing this frame.
     *      The method will do as much as possible with a frame even
     *      if it has problems. False = no frame problems.
     */
    private boolean decomposeDataZone(byte[] data,
            FrameAnnotation frameAnnotation)
    {
        boolean frameTrouble = false;
        packetZone.reload(data, mpduStart+2, mpduEnd);

         /**
          * I compute the first header pointer, which is also the length
          * of the first packet fragment.
          */
        int fhp = (int)data[mpduStart+1] & 0x0ff;
        fhp |= (int)data[mpduStart] << 8;
        fhp &= 0x07ff;
        

        frameHasBadFhp = (fhp != IDLE_VCDU) &&
                (fhp != NO_PACKET_HEADER_IN_MPDU) &&
                ((fhp + mpduStart + 2) > mpduEnd);

        frameAnnotation.hasBadFirstHeaderPointer = frameHasBadFhp;
        frameAnnotation.hasIdleVcdu = (fhp == IDLE_VCDU);

         /**
          * If there is a frame sequence error, then I must jettison any
          * partial packet or header that I may have stored because this new
          * frame cannot fit with the partial data that I have stored.
          */
        if (frameAnnotation.hasSequenceError && (state != FREE_STATE))
        {
            if (state == SPLIT_HEADER_STATE)
            {
                abortSplitHeader(0);
            }
            else
            {
                abortSplitPacket(0);
            }
        }

        /**
         * There are 3 states I can be in from frame to frame.
         * split packet: I have a partial packet with a complete header,
         * and I expect a subsequent fragment.
         * split header: I have a partial packet header, and I expect a
         * fragment that at least has the rest of the header.
         * free state: I have stored no pieces, and I expect to begin a
         * new packet assembly.
         */
        switch (state)
        {
            case FREE_STATE:
                if (fhp != 0)
                {
                     //I expect the first header pointer to be zero.
                    frameTrouble = discardUnexpectedPiece(fhp);
                }
                break;

            case SPLIT_HEADER_STATE:
                frameTrouble = doSplitHeaderScenario(fhp,frameAnnotation);
                if (!frameTrouble)
                {
                    if (packetCaddy.getBytesToFill() == 0)
                    {
                        packetList.setCurrentPacketIsComplete();
                        state = FREE_STATE;
                    }
                    else
                    {
                        state = SPLIT_PACKET_STATE;
                    }
                }
                break;

            case SPLIT_PACKET_STATE:
                frameTrouble = doSplitPacketScenario(fhp,frameAnnotation);
                if (packetCaddy.getBytesToFill() == 0)
                {
                    packetList.setCurrentPacketIsComplete();
                    state = FREE_STATE;
                }
                break;
        }

        if (frameTrouble) state = FREE_STATE;
        int bytes = packetZone.getRemainingByteCount();

         /**
          * Once I have handled the first piece, I do the rest in this frame.
          */
        while (bytes > 0)
        {
            /**
             * If I have a tiny piece remaining, I assume it's a partial
             * packet header. I store it and will get the rest from the next
             * frame.
             */
            if (bytes < Packet.PRIMARY_HEADER_LENGTH)
            {
                state = SPLIT_HEADER_STATE;
                splitHeader.begin(packetZone,frameAnnotation);
                bytes = 0;
            }

            else
            {
                int packetLength = packetZone.getWord(Packet.LENGTH_OFFSET) +
                        Packet.PRIMARY_HEADER_LENGTH + 1;

                if (packetLength > setup.maxRationalPacketSize)
                {
                    /**
                     * The packet length must be rational. Otherwise, I assume
                     * that this is not packet data or I am lost. I will
                     * discard everything and start fresh with the next frame.
                     */
                    resolveIrrationalPacket();
                    bytes = 0;
                    frameTrouble = true;
                }
                else
                {
                    //I get a packet to fill from the packet list.
                    Packet packet = packetList.get(packetLength);
                    packet.setFrameAnnotation(frameAnnotation);
                    packetCaddy.setPacket(packet);

                    if (bytes < packetLength)
                    {
                        /**
                         * The bytes left are not enough to fill this packet.
                         * I save what I got and will get the rest from the
                         * next frame.
                         */
                        state = SPLIT_PACKET_STATE;
                        packetCaddy.appendRestOfZone(packetZone);
                        bytes = 0;
                    }
                    else
                    {
                        /**
                         * I got enough bytes to create a complete packet.
                         */
                        state = FREE_STATE;
                        packetCaddy.finish(packetZone);
                        bytes -= packetLength;
                        packetList.setCurrentPacketIsComplete();
                    }
                }
            }
        }

        return frameTrouble;
    }

    /**
     * This method processes the first piece, which is the second half of a
     * split header, when this class is in the "split packet header" state.
     * @param fhp The first header pointer.
     * @param frameAnnotation Frame annotation from the current frame.
     * @return true if there was any problem decomposing this frame.
     *      The method will do as much as possible with a frame even
     *      if it has problems. False = no frame problems.
     */
    private boolean doSplitHeaderScenario(int fhp,
            FrameAnnotation frameAnnotation)
    {
        boolean frameTrouble = false;
        int headerToFill = splitHeader.getToFill();
        int firstPieceLength = fhp;

        if (fhp == NO_PACKET_HEADER_IN_MPDU)
        {
            /**
             * The first header pointer indicates that there is no packet
             * header in this mpdu, so it means the entire mpdu is an internal
             * packet piece. It indicates a fairly large packet that spans
             * more than two frames.
             */
            int packetDataLength = splitHeader.finish(packetZone);
            firstPieceLength = packetZone.getRemainingByteCount();

            if (packetDataLength > setup.maxRationalPacketSize)
            {
                /**
                 * The packet length must be rational. Otherwise, I assume
                 * that this is not packet data or I am lost. I will discard
                 * everything and start fresh with the next frame.
                 */
                resolveIrrationalPacket();
                frameTrouble = true;
            }
            else if (firstPieceLength <= packetDataLength)
            {
                /**
                 * The big piece fits this packet. I compute the packet
                 * length, get a packet to fill, and fill it as much as
                 * possible.
                 */
                int packetLength = packetDataLength +
                        Packet.PRIMARY_HEADER_LENGTH + 1;
                Packet packet = packetList.get(packetLength);
                packet.setFrameAnnotation(splitHeader.getFrameAnnotation());
                packetCaddy.setPacket(packet);
                packetCaddy.appendHeader(splitHeader);
                packetCaddy.appendRestOfZone(packetZone);
                packetCaddy.setAnotherFrameAnnotation(frameAnnotation);
            }
            else
            {
                /**
                 * Trouble. The whole mpdu should be in this packet, but the
                 * length says it isn't. I discard the partial header and the
                 * big piece because they do not belong together.
                 */
                abortSplitHeader(firstPieceLength);
                frameTrouble = true;
            }
        }

        else if (fhp == IDLE_VCDU)
        {
            /**
             * Trouble. I should never see this when I have a split header.
             * I discard everything. I consider an idle vcdu at the wrong
             * time to be a troublesome frame.
             */
            ++idleVcdus.value;
            abortSplitHeader(0);
            packetZone.clear();
            frameTrouble = true;
        }

        else if (frameHasBadFhp)
        {
            /**
             * This frame is completely useless if the first header pointer
             * points beyond the end of the packet data zone. It is a bad
             * frame, which I count. I must leave immediately (by clearing
             * the packet zone) since I cannot process this frame.
             */
            ++badFirstHeaderPointers.value;
            abortSplitHeader(packetZone.getRemainingByteCount());
            frameTrouble = true;
        }

        else if (headerToFill < firstPieceLength)
        {
            int packetDataLength = splitHeader.finish(packetZone) + 1;
            firstPieceLength -= headerToFill;

            if (packetDataLength == firstPieceLength)
            {
                /**
                 * The first piece in this frame exactly fills out the rest of
                 * the packet that I have assembled. Great, it's what I expect.
                 * I get a packet from the packet list and fill it up.
                 */
                int packetLength = packetDataLength + Packet.PRIMARY_HEADER_LENGTH;
                Packet packet = packetList.get(packetLength);
                packet.setFrameAnnotation(splitHeader.getFrameAnnotation());
                packetCaddy.setPacket(packet);
                packetCaddy.appendHeader(splitHeader);
                packetCaddy.finish(packetZone);
                packetCaddy.setAnotherFrameAnnotation(frameAnnotation);
            }
            else
            {
                /**
                 * Trouble. The piece size is not what it should be, which
                 * strongly suggests this piece does not belong with this
                 * header. I discard the partial header and the piece.
                 */
                abortSplitHeader(firstPieceLength);
                frameTrouble = true;
            }
        }
        else
        {
            /**
             * Trouble. The first piece is too small to finish the header,or
             * it is exactly the right size, which means the packet is ALL
             * header and no data. I skip the piece and discard the partial
             * header.
             */
            abortSplitHeader(firstPieceLength);
            frameTrouble = true;
        }

        return frameTrouble;
    }

    /**
     * This method processes the first frame piece, which is subsequent
     * packet piece, when this class is in the "split packet" state.
     * @param fhp The first header pointer.
     * @param frameAnnotation Frame annotation from the current frame.
     * @return true if there was any problem decomposing this frame.
     *      The method will do as much as possible with a frame even
     *      if it has problems. False = no frame problems.
     */
    private boolean doSplitPacketScenario(int fhp,
            FrameAnnotation frameAnnotation)
    {
        boolean frameTrouble = false;
        packetCaddy.setAnotherFrameAnnotation(frameAnnotation);
        int toFill = packetCaddy.getBytesToFill();
        int firstPieceLength = fhp;

        if (fhp == NO_PACKET_HEADER_IN_MPDU)
        {
            /**
             * The big piece does not complete this packet. I compute the
             * packet length, get a packet to fill, and fill it as much as
             * possible.
             */
            firstPieceLength = packetZone.getRemainingByteCount();
            if (toFill > firstPieceLength)
            {
                packetCaddy.appendRestOfZone(packetZone);
            }
            else if (toFill == firstPieceLength)
            {
                packetCaddy.finish(packetZone);
            }
            else
            {
                /**
                 * Trouble. The entire mpdu should fit in the packet, and it
                 * doesn't. I fill the packet with chaff and discard the piece.
                 */
                abortSplitPacket(firstPieceLength);
                frameTrouble = true;
            }
        }

        else if (fhp == IDLE_VCDU)
        {
            /**
             * Trouble. I should never see this when I have a split packet.
             * I create a short packet. I consider an idle vcdu at the wrong
             * time to be a troublesome frame.
             */
            packetCaddy.appendWaste();
            ++idleVcdus.value;
            packetZone.clear();
            frameTrouble = true;
        }

        else if (frameHasBadFhp)
        {
            /**
             * This frame is completely useless if the first header pointer
             * points beyond the end of the packet data zone. It is a bad
             * frame, which I count. I must leave immediately (by clearing
             * the packet zone) since I cannot process this frame. I do make
             * a short packet.
             */
            ++badFirstHeaderPointers.value;
            abortSplitPacket(packetZone.getRemainingByteCount());
            frameTrouble = true;
        }

        else if (firstPieceLength == toFill)
        {
            packetCaddy.finish(packetZone);
        }

        else
        {
            /**
             * Trouble. The first piece is not the correct size.
             * I fill the packet with chaff and discard the piece.
             */
            abortSplitPacket(firstPieceLength);
            frameTrouble = true;
        }

        return frameTrouble;
    }

    /**
     * I use this method when I must discard a packet header and the first
     * frame piece during the "split packet header" scenario. It discards
     * the fragments and accounts for them in status.
     */
    private void abortSplitHeader(int firstPieceLength)
    {
        ++discardedFragments.value;
        discardedFragmentByteCount.value += splitHeader.getFilledCount();
        splitHeader.clear();
        state = FREE_STATE;

        if (firstPieceLength > 0)
        {
            ++discardedFragments.value;
            discardedFragmentByteCount.value += firstPieceLength;
            packetZone.advance(firstPieceLength);
        }
    }

    /**
     * I use this method when I must discard the first packet fragment and
     * create a short packet during the "split packet" scenario.
     */
    private void abortSplitPacket(int firstPieceLength)
    {
        packetCaddy.appendWaste();
        state = FREE_STATE;

        if (firstPieceLength > 0)
        {
            ++discardedFragments.value;
            discardedFragmentByteCount.value += firstPieceLength;
            packetZone.advance(firstPieceLength);
        }
    }

    /**
     * I use this method when I encounter an unexpected piece, which I
     * discard, during the "free state" scenario.
     */
    private boolean discardUnexpectedPiece(int fhp)
    {
        boolean frameTrouble = true;
        state = FREE_STATE;

        if (fhp == IDLE_VCDU)
        {
            /**
             * An idle vcdu is acceptable in this state as it does not
             * disrupt packet assembly. I simply count it and clear the
             * packet zone since there is nothing to process in this
             * frame. It is not a problem frame.
             */
            ++idleVcdus.value;
            packetZone.clear();
            frameTrouble = false;
        }
        else if (frameHasBadFhp)
        {
            /**
             * This frame is completely useless if the first header
             * pointer points beyond the end of the packet data zone.
             * It is a bad frame, which I count. I must leave immediately
             * (by clearing the packet zone) since I cannot process this
             * frame.
             */
            ++badFirstHeaderPointers.value;
            ++discardedFragments.value;
            discardedFragmentByteCount.value +=
                    packetZone.getRemainingByteCount();
            packetZone.clear();
            frameTrouble = true;
        }
        else if (fhp == NO_PACKET_HEADER_IN_MPDU)
        {
             //This frame is all data -- one big piece.
            ++discardedFragments.value;
            discardedFragmentByteCount.value +=
                    packetZone.getRemainingByteCount();
            packetZone.clear();
            frameTrouble = true;
        }
        else if (fhp > 0)
        {
            /**
             * Since I do not have a packet assembly in progress,
             * I did not expect to see a packet piece. I discard it.
             */
            packetZone.advance(fhp);
            ++discardedFragments.value;
            discardedFragmentByteCount.value += fhp;
            frameTrouble = true;
        }

    return frameTrouble;
    }

    /**
     * A packet length must be rational. If it were not and the Path
     * Service tried to construct an irrational-length packet, then
     * up to 64 kb of good packet data could be lost. This method
     * cleans up after an irrational length is detected. It drops the
     * current frame and any saved data, and it resets to look for a
     * new packet starting with the next frame.
     */
    private void resolveIrrationalPacket()
    {
        ++irrationalPacketLengths.value;
        ++discardedFragments.value;

        discardedFragmentByteCount.value +=
                packetZone.getRemainingByteCount();

        packetZone.clear();
        splitHeader.clear();

        state = FREE_STATE;
    }
}
