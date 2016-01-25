/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.AOS;

import gov.nasa.gsfc.drl.rtstps.core.*;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import java.nio.ByteBuffer;
import java.util.TreeMap;

/**
 * This class is a AOS, which is a CCSDS version AOS frame for cast. It is a unit
 * itself, but it also encapsulates a frame, which it promotes to a AOS.
 * 
 */
public final class AOS extends Unit
{
    static final int AOS_SYNC_MARK = 0x1ACFFC1D;

    static final int BIT_POS_AOS_SYNC_MARK = 0;
    static final int BIT_LENGTH_AOS_SYNC_MARK = 32;
    static final int BIT_MASK_AOS_SYNC_MARK = 0xffffffff;

    static final int BIT_POS_AOS_VERSION = 32;
    static final int BIT_LENGTH_AOS_VERSION = 2;
    static final int BIT_MASK_AOS_VERSION = 0x3;


    static final int BIT_POS_AOS_SPACECRAFTID = 34;
    static final int BIT_LENGTH_AOS_SPACECRAFTID = 8;

    static final int BIT_POS_AOS_VCDUID = 42;
    static final int BIT_LENGTH_AOS_VCDUID = 6;

    static final int BIT_POS_AOS_VCDUCOUNTER = 48;
    static final int BIT_LENGTH_AOS_VCDUCOUNTER = 24;


    static final int BIT_POS_AOS_FLAG_REPLAY = 72;
    static final int BIT_LENGTH_AOS_FLAG_REPLAY = 1;

    static final int BIT_POS_AOS_FLAG_IQ = 73;
    static final int BIT_LENGTH_AOS_FLAG_IQ = 2;

    static final int BIT_POS_AOS_FLAG_ENCRYPTED = 75;
    static final int BIT_LENGTH_AOS_FLAG_ENCRYPTED = 2;

    static final int BIT_POS_AOS_WORKINGMODE = 77;
    static final int BIT_LENGTH_AOS_WORKINGMODE = 3;

    static final int BIT_POS_AOS_VCDU_INSERTZONE = 80;
    static final int BIT_LENGTH_AOS_VCDU_INSERTZONE = 256;

    static final int BIT_POS_AOS_BPDU_HEADER = 336;
    static final int BIT_LENGTH_AOS_BPDU_HEADER = 16;

    static final int BIT_POS_AOS_BPDU_DATA_BODY = 352;
    static final int BIT_LENGTH_AOS_BPDU_DATA_BODY = 7312;

    static final int BIT_POS_AOS_CRC_CODE = 7664;
    static final int BIT_LENGTH_AOS_CRC_CODE = 16;

    static final int BIT_POS_AOS_RS_CODE = 7680;
    static final int BIT_LENGTH_AOS_RS_CODE = 512;



    private static final int FILL_MASK = 0x03f;
    private Frame frame = null;
    private int BPDUdataStartOffset = 44;
    private int BPDULength = 914;
    private int dataZoneStartOffset = 10;
    private int dataZoneEndOffset;
    private int trailerLength = 0;
    private int rsParityLength = 0;
    public static final int IQFLAG_I = 0x00;
    public static final int IQFLAG_Q = 0x11;



    /**
     * Create a AOSFrame with the characteristics defined by the setup. The
     * AOSFrame does not contain a frame at this point. You must defer creating
     * a AOSFrame with this constructor until after the Builder has created all
     * STPS nodes because AOSFrame needs special information from them such as
     * parity lengths.
     */
    public AOS(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        String spacecraftName = element.getAttribute("spacecraftAOS");
//        SpacecraftAOS spacecraftAOS =
//                (SpacecraftAOS)configuration.getSpacecrafts().get(spacecraftName);

//        if (spacecraftAOS == null)
//        {
//            throw new RtStpsException(element.getTagName() + " " +
//                    element.getAttribute("label") +
//                    " does not reference a defined spacecraftAOS.");
//        }

        TreeMap<String, RtStpsNode> nodes = configuration.getStpsNodes();

        /**
         * There is only one reed solomon node, so the node name is the same
         * as the class name.
         */
        String rsNodeName = ReedSolomonDecoder.CLASSNAME;
        ReedSolomonDecoder rs = (ReedSolomonDecoder)nodes.get(rsNodeName);
        if (rs != null)
        {
            rsParityLength = rs.getParityLength();
            trailerLength = rsParityLength;
        }

        boolean crcParityPresent = Convert.toBoolean(element,"crcParityPresent",false);
        if (crcParityPresent || (nodes.get("crc") != null))
        {
            trailerLength += 2;
        }

        boolean ocfPresent = Convert.toBoolean(element,"OCFpresent",false);
        if (ocfPresent) trailerLength += 4;

        dataZoneStartOffset = 10;  //sync pattern + VCDU header
//        if (spacecraftAOS.headerErrorControlPresent) dataZoneStartOffset += 2;
//        dataZoneStartOffset += spacecraftAOS.insertZoneLength;
    }

    /**
     * This CADU constructor is incomplete because it omits key elements,
     * such as parity length. Some services do not care about this because
     * they simply want the CADU to interpret the CADU header and nothing
     * more. This constructor is adequate for that purpose.
     */
    public AOS()
    {
    }

    /**
     * Set this CADU's frame, which promotes the frame to a CADU and makes
     * this class fully functional. You may use the same Cadu object with
     * different frames.
     */
    public void setFrame(Frame frame)
    {
        this.frame = frame;
        data = frame.getData();
        frameAnnotation = frame.getFrameAnnotation();
        startOffset = 0;
        length = frame.getSize();
        deleted = frame.isDeleted();
        dataZoneEndOffset = frame.getSize() - trailerLength - 1;
    }

    /**
     * Get the CADU's Reed Solomon parity length.
     */
    public final int getReedSolomonParityLength()
    {
        return rsParityLength;
    }

    /**
     * Get the data zone start offset in bytes from the frame's start.
     */
    public final int getdataZoneStartOffset()
    {
        return dataZoneStartOffset;
    }

    /**
     * Get the data zone end offset in bytes from the frame's start.
     */
    public final int getdataZoneEndOffset()
    {
        return dataZoneEndOffset;
    }

    /**
     * Get the frame's CCSDS frame version number.
     */
    public int getVersion()
    {
        //must be 1 for CADUs.
        return ((int)data[4] >> 6) & 3;
    }

    /**
     * Get the frame's spacecraft ID.
     */
    public int getSpacecraft()
    {
        int pos = BIT_POS_AOS_SPACECRAFTID/8;

        int mod = BIT_POS_AOS_SPACECRAFTID%8;

        int count = BIT_LENGTH_AOS_SPACECRAFTID/8;

        int AOS_SPACECRAFT_ID = 0;

        AOS_SPACECRAFT_ID = ((int)data[pos] & 0x3f)<<2 | ((int)data[pos+1] & 0xc0)>>6;

        return AOS_SPACECRAFT_ID ;

//
//
//        int p = ((int)data[4] & 0x03f) << 2;
//        return p | (((int)data[5] >> 6) & 3);
    }

    /**
     * Get the frame's virtual channel number.
     */
    public int getVirtualChannel()
    {
        int pos = BIT_POS_AOS_VCDUID/8;

        int mod = BIT_POS_AOS_VCDUID%8;

        int count = BIT_LENGTH_AOS_SPACECRAFTID/8;

        int AOS_VCDUID = 0;

        AOS_VCDUID = ((int)data[pos] & 0x3f);

        return AOS_VCDUID ;
    }

    /**
     * Determine if this is a fill (idle) frame.
     */
    public boolean isFillFrame()
    {
        return getVirtualChannel() == FILL_MASK;
    }

    /**
     * Get this frame's sequence number.
     */
    public int getSequenceCount()
    {
        int s = ((int)data[6] & 0x0ff) << 16;
        s |= ((int)data[7] & 0x0ff) << 8;
        s |= ((int)data[8] & 0x0ff);
        return s;
    }

    /**
     * Get this frame's header error control word.
     */
    public int getHeaderErrorControlWord()
    {
        int h = ((int)data[10] << 8) & ((int)data[11] & 0x0ff);
        return h & 0x0ffff;
    }

    /**
     * Get the frame's I Q flag
     *
     *
     *
     *
     * */
    public int getIQFlag(){
        return 0;
    }


    public byte[] getBPDUData(){
        byte[] BPDUData = new byte[BPDULength];
        int pos = BIT_POS_AOS_BPDU_DATA_BODY/8;

        ByteBuffer source = ByteBuffer.wrap(data);
        source.position(pos);
        source.get(BPDUData);

        return BPDUData;
    }


    /**
     * Set this cadu's frame annotation.
     */
    public void setFrameAnnotation(FrameAnnotation a)
    {
        frameAnnotation = a;
        frame.setFrameAnnotation(a);
    }

    /**
     * Mark this cadu as deleted or not deleted.
     */
    public void setDeleted(boolean d)
    {
        deleted = d;
        frame.setDeleted(d);
    }
}
