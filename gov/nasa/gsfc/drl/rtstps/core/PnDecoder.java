/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * This class performs pseudo-noise encoding/decoding on frames or blocks
 * of data. Pseudo-Noise is also known as bit transition density. This
 * class uses the standard CCSDS polynomial: x**8 + x**7 + x**5 + x**3 + 1.
 * 
 */
public class PnDecoder extends FrameSenderNode implements FrameReceiver,
        Sender, Cloneable
{
    /**
     * This is a class name for this RT-STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "pn";

    /**
     * This is the default encoding table, which is CCSDS-recommended.
     * It is a 255 byte array. Gerald Grebowsky of GSFC gave me the table
     * in 1996.
     */
    private static final int[] table = {
        0xff, 0x48, 0x0e, 0xc0, 0x9a, 0x0d, 0x70, 0xbc, 0x8e, 0x2c, 0x93,
        0xad, 0xa7, 0xb7, 0x46, 0xce, 0x5a, 0x97, 0x7d, 0xcc, 0x32, 0xa2,
        0xbf, 0x3e, 0x0a, 0x10, 0xf1, 0x88, 0x94, 0xcd, 0xea, 0xb1, 0xfe,
        0x90, 0x1d, 0x81, 0x34, 0x1a, 0xe1, 0x79, 0x1c, 0x59, 0x27, 0x5b,
        0x4f, 0x6e, 0x8d, 0x9c, 0xb5, 0x2e, 0xfb, 0x98, 0x65, 0x45, 0x7e,
        0x7c, 0x14, 0x21, 0xe3, 0x11, 0x29, 0x9b, 0xd5, 0x63, 0xfd, 0x20,
        0x3b, 0x02, 0x68, 0x35, 0xc2, 0xf2, 0x38, 0xb2, 0x4e, 0xb6, 0x9e,
        0xdd, 0x1b, 0x39, 0x6a, 0x5d, 0xf7, 0x30, 0xca, 0x8a, 0xfc, 0xf8,
        0x28, 0x43, 0xc6, 0x22, 0x53, 0x37, 0xaa, 0xc7, 0xfa, 0x40, 0x76,
        0x04, 0xd0, 0x6b, 0x85, 0xe4, 0x71, 0x64, 0x9d, 0x6d, 0x3d, 0xba,
        0x36, 0x72, 0xd4, 0xbb, 0xee, 0x61, 0x95, 0x15, 0xf9, 0xf0, 0x50,
        0x87, 0x8c, 0x44, 0xa6, 0x6f, 0x55, 0x8f, 0xf4, 0x80, 0xec, 0x09,
        0xa0, 0xd7, 0x0b, 0xc8, 0xe2, 0xc9, 0x3a, 0xda, 0x7b, 0x74, 0x6c,
        0xe5, 0xa9, 0x77, 0xdc, 0xc3, 0x2a, 0x2b, 0xf3, 0xe0, 0xa1, 0x0f,
        0x18, 0x89, 0x4c, 0xde, 0xab, 0x1f, 0xe9, 0x01, 0xd8, 0x13, 0x41,
        0xae, 0x17, 0x91, 0xc5, 0x92, 0x75, 0xb4, 0xf6, 0xe8, 0xd9, 0xcb,
        0x52, 0xef, 0xb9, 0x86, 0x54, 0x57, 0xe7, 0xc1, 0x42, 0x1e, 0x31,
        0x12, 0x99, 0xbd, 0x56, 0x3f, 0xd2, 0x03, 0xb0, 0x26, 0x83, 0x5c,
        0x2f, 0x23, 0x8b, 0x24, 0xeb, 0x69, 0xed, 0xd1, 0xb3, 0x96, 0xa5,
        0xdf, 0x73, 0x0c, 0xa8, 0xaf, 0xcf, 0x82, 0x84, 0x3c, 0x62, 0x25,
        0x33, 0x7a, 0xac, 0x7f, 0xa4, 0x07, 0x60, 0x4d, 0x06, 0xb8, 0x5e,
        0x47, 0x16, 0x49, 0xd6, 0xd3, 0xdb, 0xa3, 0x67, 0x2d, 0x4b, 0xbe,
        0xe6, 0x19, 0x51, 0x5f, 0x9f, 0x05, 0x08, 0x78, 0xc4, 0x4a, 0x66,
        0xf5, 0x58};

    private int syncLength = 4;



    /**
     * A null constructor.
     */
    public PnDecoder()
    {
        /**
         * There is only one PnDecoder object, so the class name is the same as
         * the link/object name.
         */
        super(CLASSNAME,CLASSNAME);
    }

    /**
     * Set up this RT-STPS node with a configuration. PN does not have an element.
     * It is created in the Frame Sync, so this required method does nothing.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
    }

    /**
     * Set the sync pattern length. The default length is 4 bytes.
     */
    public void setSyncLength(int length)
    {
        syncLength = length;
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
                decode(frame.getData(), syncLength, frame.getSize()-1);
            }
        }
        output.putFrames(frames);
    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        if (!frame.isDeleted())
        {
            decode(frame.getData(), syncLength, frame.getSize()-1);
        }
        output.putFrame(frame);
    }

    /**
     * This method adds or removes pseudo-noise encoding (PN or bit transition
     * density encoding) from a data block. The polynomial is the CCSDS-
     * recommended one.
     * This method both encodes and decodes; the operation depends on the
     * original state of the data: encoded or decoded.
     * @param data The byte array to be encoded or decoded.
     * @param startByte The data index of the starting byte.
     * @param endByte The data index of the last decoded byte.
     */
    public static void decode(byte[] data, int startByte, int endByte)
    {
        int k = 0;
        for (int n = startByte; n <= endByte; n++)
        {
            int d = (int)data[n];
            d ^= table[k];
            data[n] = (byte)d;
            if (++k == 255) k = 0;
        }
    }
}
