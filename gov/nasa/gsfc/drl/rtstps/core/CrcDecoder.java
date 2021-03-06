/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;
import gov.nasa.gsfc.drl.rtstps.core.fs.FrameSynchronizer;

import java.util.TreeMap;

/**
 * This class performs CRC decoding on frames or blocks of data.
 * 
 */
public final class CrcDecoder extends FrameSenderNode implements FrameReceiver,
        Sender, Cloneable
{
    /**
     * This is a class name for this RT-STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "crc";

    private static final int[] highCRC = {
        0x000, 0x010, 0x020, 0x030, 0x040, 0x050, 0x060, 0x070, 0x081, 0x091,
        0x0a1, 0x0b1, 0x0c1, 0x0d1, 0x0e1, 0x0f1, 0x012, 0x002, 0x032, 0x022,
        0x052, 0x042, 0x072, 0x062, 0x093, 0x083, 0x0b3, 0x0a3, 0x0d3, 0x0c3,
        0x0f3, 0x0e3, 0x024, 0x034, 0x004, 0x014, 0x064, 0x074, 0x044, 0x054,
        0x0a5, 0x0b5, 0x085, 0x095, 0x0e5, 0x0f5, 0x0c5, 0x0d5, 0x036, 0x026,
        0x016, 0x006, 0x076, 0x066, 0x056, 0x046, 0x0b7, 0x0a7, 0x097, 0x087,
        0x0f7, 0x0e7, 0x0d7, 0x0c7, 0x048, 0x058, 0x068, 0x078, 0x008, 0x018,
        0x028, 0x038, 0x0c9, 0x0d9, 0x0e9, 0x0f9, 0x089, 0x099, 0x0a9, 0x0b9,
        0x05a, 0x04a, 0x07a, 0x06a, 0x01a, 0x00a, 0x03a, 0x02a, 0x0db, 0x0cb,
        0x0fb, 0x0eb, 0x09b, 0x08b, 0x0bb, 0x0ab, 0x06c, 0x07c, 0x04c, 0x05c,
        0x02c, 0x03c, 0x00c, 0x01c, 0x0ed, 0x0fd, 0x0cd, 0x0dd, 0x0ad, 0x0bd,
        0x08d, 0x09d, 0x07e, 0x06e, 0x05e, 0x04e, 0x03e, 0x02e, 0x01e, 0x00e,
        0x0ff, 0x0ef, 0x0df, 0x0cf, 0x0bf, 0x0af, 0x09f, 0x08f, 0x091, 0x081,
        0x0b1, 0x0a1, 0x0d1, 0x0c1, 0x0f1, 0x0e1, 0x010, 0x000, 0x030, 0x020,
        0x050, 0x040, 0x070, 0x060, 0x083, 0x093, 0x0a3, 0x0b3, 0x0c3, 0x0d3,
        0x0e3, 0x0f3, 0x002, 0x012, 0x022, 0x032, 0x042, 0x052, 0x062, 0x072,
        0x0b5, 0x0a5, 0x095, 0x085, 0x0f5, 0x0e5, 0x0d5, 0x0c5, 0x034, 0x024,
        0x014, 0x004, 0x074, 0x064, 0x054, 0x044, 0x0a7, 0x0b7, 0x087, 0x097,
        0x0e7, 0x0f7, 0x0c7, 0x0d7, 0x026, 0x036, 0x006, 0x016, 0x066, 0x076,
        0x046, 0x056, 0x0d9, 0x0c9, 0x0f9, 0x0e9, 0x099, 0x089, 0x0b9, 0x0a9,
        0x058, 0x048, 0x078, 0x068, 0x018, 0x008, 0x038, 0x028, 0x0cb, 0x0db,
        0x0eb, 0x0fb, 0x08b, 0x09b, 0x0ab, 0x0bb, 0x04a, 0x05a, 0x06a, 0x07a,
        0x00a, 0x01a, 0x02a, 0x03a, 0x0fd, 0x0ed, 0x0dd, 0x0cd, 0x0bd, 0x0ad,
        0x09d, 0x08d, 0x07c, 0x06c, 0x05c, 0x04c, 0x03c, 0x02c, 0x01c, 0x00c,
        0x0ef, 0x0ff, 0x0cf, 0x0df, 0x0af, 0x0bf, 0x08f, 0x09f, 0x06e, 0x07e,
        0x04e, 0x05e, 0x02e, 0x03e, 0x00e, 0x01e};

    private static final int[] lowCRC = {
        0x000, 0x021, 0x042, 0x063, 0x084, 0x0a5, 0x0c6, 0x0e7, 0x008, 0x029,
        0x04a, 0x06b, 0x08c, 0x0ad, 0x0ce, 0x0ef, 0x031, 0x010, 0x073, 0x052,
        0x0b5, 0x094, 0x0f7, 0x0d6, 0x039, 0x018, 0x07b, 0x05a, 0x0bd, 0x09c,
        0x0ff, 0x0de, 0x062, 0x043, 0x020, 0x001, 0x0e6, 0x0c7, 0x0a4, 0x085,
        0x06a, 0x04b, 0x028, 0x009, 0x0ee, 0x0cf, 0x0ac, 0x08d, 0x053, 0x072,
        0x011, 0x030, 0x0d7, 0x0f6, 0x095, 0x0b4, 0x05b, 0x07a, 0x019, 0x038,
        0x0df, 0x0fe, 0x09d, 0x0bc, 0x0c4, 0x0e5, 0x086, 0x0a7, 0x040, 0x061,
        0x002, 0x023, 0x0cc, 0x0ed, 0x08e, 0x0af, 0x048, 0x069, 0x00a, 0x02b,
        0x0f5, 0x0d4, 0x0b7, 0x096, 0x071, 0x050, 0x033, 0x012, 0x0fd, 0x0dc,
        0x0bf, 0x09e, 0x079, 0x058, 0x03b, 0x01a, 0x0a6, 0x087, 0x0e4, 0x0c5,
        0x022, 0x003, 0x060, 0x041, 0x0ae, 0x08f, 0x0ec, 0x0cd, 0x02a, 0x00b,
        0x068, 0x049, 0x097, 0x0b6, 0x0d5, 0x0f4, 0x013, 0x032, 0x051, 0x070,
        0x09f, 0x0be, 0x0dd, 0x0fc, 0x01b, 0x03a, 0x059, 0x078, 0x088, 0x0a9,
        0x0ca, 0x0eb, 0x00c, 0x02d, 0x04e, 0x06f, 0x080, 0x0a1, 0x0c2, 0x0e3,
        0x004, 0x025, 0x046, 0x067, 0x0b9, 0x098, 0x0fb, 0x0da, 0x03d, 0x01c,
        0x07f, 0x05e, 0x0b1, 0x090, 0x0f3, 0x0d2, 0x035, 0x014, 0x077, 0x056,
        0x0ea, 0x0cb, 0x0a8, 0x089, 0x06e, 0x04f, 0x02c, 0x00d, 0x0e2, 0x0c3,
        0x0a0, 0x081, 0x066, 0x047, 0x024, 0x005, 0x0db, 0x0fa, 0x099, 0x0b8,
        0x05f, 0x07e, 0x01d, 0x03c, 0x0d3, 0x0f2, 0x091, 0x0b0, 0x057, 0x076,
        0x015, 0x034, 0x04c, 0x06d, 0x00e, 0x02f, 0x0c8, 0x0e9, 0x08a, 0x0ab,
        0x044, 0x065, 0x006, 0x027, 0x0c0, 0x0e1, 0x082, 0x0a3, 0x07d, 0x05c,
        0x03f, 0x01e, 0x0f9, 0x0d8, 0x0bb, 0x09a, 0x075, 0x054, 0x037, 0x016,
        0x0f1, 0x0d0, 0x0b3, 0x092, 0x02e, 0x00f, 0x06c, 0x04d, 0x0aa, 0x08b,
        0x0e8, 0x0c9, 0x026, 0x007, 0x064, 0x045, 0x0a2, 0x083, 0x0e0, 0x0c1,
        0x01f, 0x03e, 0x05d, 0x07c, 0x09b, 0x0ba, 0x0d9, 0x0f8, 0x017, 0x036,
        0x055, 0x074, 0x093, 0x0b2, 0x0d1, 0x0f0};

    private int highStart = 0x0ff;
    private int lowStart = 0x0ff;
    private int firstData;
    private int lastData;
    private int parityStart;

    /**
     * If true, include the sync pattern in the CRC calculation.
     */
    private boolean includeSyncPattern = false;

    /**
     * If true, discard a frame with a bad CRC.
     */
    private boolean discardBadFrames = true;

    /**
     * This is the byte offset from the frame start to the first byte
     * of CRC parity, which is 2 bytes. In general, the CRC parity follows
     * the frame data but precedes any Reed Solomon parity. If zero, the
     * software calculates the value.
     */
    private int offsetToParity = 0;



    /**
     * Create a CRC decoder.
     */
    public CrcDecoder()
    {
        /**
         * There is only one CrcDecoder object, so the class name is the same
         * as the link/object name.
         */
        super(CLASSNAME,CLASSNAME);
    }

    /**
     * Set up this RT-STPS node with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        includeSyncPattern = Convert.toBoolean(element,"includeSyncPattern",
                includeSyncPattern);

        discardBadFrames = Convert.toBoolean(element,"discardBadFrames",
                discardBadFrames);

        offsetToParity = Convert.toInteger(element,"offsetToParity",
                offsetToParity);

        int seed = Convert.toHexInteger(element,"startSeed",0x0ffff) & 0x0ffff;
        highStart = seed >>> 8;
        lowStart = seed & 0x0ff;
    }

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     */
    public void finishSetup(Configuration configuration) throws RtStpsException
    {
        super.finishSetup(configuration);

        TreeMap<String, RtStpsNode> nodes = configuration.getStpsNodes();
        //There is only one FS, so its node name is its class name.
        String fsNodeName = FrameSynchronizer.CLASSNAME;
        FrameSynchronizer fs = (FrameSynchronizer)nodes.get(fsNodeName);
        if (fs == null)
        {
            throw new RtStpsException("The FrameSynchronizer node is missing.");
        }

        firstData = includeSyncPattern? 0 : fs.getSyncPatternLength();

        if (offsetToParity > 0)
        {
            parityStart = offsetToParity;
        }
        else
        {
            int rsParityLength = 0;
            //There is only one RS, so its node name is its class name.
            String rsNodeName = ReedSolomonDecoder.CLASSNAME;
            ReedSolomonDecoder rs = (ReedSolomonDecoder)nodes.get(rsNodeName);
            if (rs != null)
            {
                rsParityLength = rs.getParityLength();
            }

            parityStart = fs.getFrameLength() - 2 - rsParityLength;
        }

        lastData = parityStart - 1;
    }

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
        for (int n = 0; n < frames.length; n++)
        {
            Frame frame = frames[n];
            if (!frame.isDeleted())
            {
                decode(frame);
            }
        }

        output.putFrames(frames);
    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        if (!frame.isDeleted()) decode(frame);
        output.putFrame(frame);
    }

    /**
     * Decode the frame. The method marks the frame's annotation.
     * @return true if it computed a CRC error
     */
    private boolean decode(Frame frame)
    {
        int highParity = highStart;
        int lowParity = lowStart;
        byte[] data = frame.getData();

        for (int d = firstData; d <= lastData; d++)
        {
            int k = (int)data[d] ^ highParity;
            k &= 0x0ff;
            highParity = lowParity ^ highCRC[k];
            lowParity = lowCRC[k];
        }

        boolean failure = ((byte)highParity != data[parityStart]) ||
                ((byte)lowParity != data[parityStart+1]);

        frame.getFrameAnnotation().hasCrcError = failure;
        frame.setDeleted(failure && discardBadFrames);

        return failure;
    }
}
