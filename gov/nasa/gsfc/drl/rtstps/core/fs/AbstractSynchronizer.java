/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;

/**
 * This class handles the frame synchronizer pattern logic for the Frame
 * Synchronizer subsystem. It assumes there are no bit errors in the pattern.
 * It can detect bit slips of 1 or 2 bits. The pattern may be 16, 24, or 32
 * bits in length.
 * <p>
 * This class's search strategy is to take each byte in a input buffer and
 * treat it as an index into a 256-byte lookup table, which returns a "bit
 * index." The index will either be NOSYNC or a value indicating it is a sync
 * pattern candidate.
 * <p>
 * If a buffer contains a sync pattern of 16 or more bits, then we know that
 * we can search for known bytes in the input buffer. For example, the standard
 * CCSDS sync pattern is 0x1ACFFC1D (designated here as s1=1A, s2=CF, s3=FC,
 * s4=1D). If we find a CF (s2), then we have a sync pattern candidate. Since
 * the bits could be shifted, we could have up to 8 unique candidate patterns.
 * (Take CF and shift 1A into it 7 times, dropping CF's low-order bit each
 * time. If you shift 8 bits, the CF drops into the next byte.) As a
 * consequence, when we search the input buffer, we want to test each byte
 * against the 8 test patterns. To speed this up, we build a 256-byte lookup
 * table with 8 non-NOSYNC values, which are the bit shift counts. i.e. CF is
 * bit shift 0. Each input byte is an index into the table. So, for example,
 * if the input byte is 0x67, the table lookup would return bit index 1 (1ACF
 * shifted right one bit, i.e. (1ACF >> 1) & 0x0FF). Once we have a NOSYNC
 * value, we can test the surrounding bits to see if it is really a sync
 * pattern.
 * <p>
 * There are two catches to this plan.
 * <p>
 * (1) Permitting bit errors in the sync pattern really complicates matters.
 * Since in my experience operators rarely configure systems for sync pattern
 * bit errors, I omit it from this class because disallowing bit errors improves
 * performance. If bit error detection is required, I would suggest writing a
 * separate companion class that would replace this one when that option were
 * chosen.
 * <p>
 * (2) Although I have said that shifting down the pattern creates 8 unique
 * values, which is the case for 1ACFFC1D, this is not necessarily true for
 * general  patterns. For example, the absurd 0xFFFFFFFF would create only one
 * value. I call this an ambiguous pattern because the lookup value maps to
 * more than  one possible bit shift. I handle ambiguous patterns in a
 * separate class.
 * 
 */
abstract class AbstractSynchronizer
{
    protected static final int NOSYNC = -1;

    /**
     * s1 is the first pattern byte. When verifying the first byte, I usually
     * must mask off some leading bits. I use two arrays to hold the masks and
     * the expected values.
     */
    protected int[] s1 = new int[8];
    protected int[] s1mask = new int[8];

    /**
     * This is the 256-byte lookup table as described above. It is keyed to the
     * second pattern byte. The s2value array contains the 8 shifted s2-patterns,
     */
    protected int[] s2 = new int[256];
    protected byte[] s2value = new byte[8];

    /**
     * When the pattern length is 3 or 4 bytes, I know the expected third byte.
     */
    protected byte[] s3 = new byte[8];

    /**
     * When the pattern length is 4 bytes, I know the expected fourth byte.
     */
    protected byte[] s4 = new byte[8];

    /*
     * Like s1, the pattern's may be mixed with other data bits, so I need a
     * mask and an expected pattern.
     */
    protected int[] s5 = new int[8];
    protected int[] s5mask = new int[8];

    protected int syncLength = 4;
    protected int frameLength = 1024;
    protected int slippage = 0;


    /**
     * Create an AbstractSynchronizer.
     * @param pattern The synchronization pattern. The length may be 1 to 4
     *          bytes.
     * @param frameLength The expected frame length, which includes the sync
     *          pattern.
     */
    AbstractSynchronizer(byte[] pattern, int frameLength)
    {
        syncLength = pattern.length;
        this.frameLength = frameLength;

        int x = (int)pattern[0] & 0x0ff;
        int y = 0x0ff;
        for (int n = 0; n < 8; n++)
        {
            s1[n] = x;
            x >>>= 1;
            s1mask[n] = y;
            y >>>= 1;
        }

        for (int n = 0; n < 256; n++)
        {
            s2[n] = NOSYNC;
        }

        //The derivatives handle s2[] setup.

        if (syncLength >= 3)
        {
            x = ((int)pattern[1] << 8) | ((int)pattern[2] & 0x0ff);
            for (int n = 0; n < 8; n++)
            {
                s3[n] = (byte)x;
                x >>= 1;
            }
            if (syncLength == 4)
            {
                x = ((int)pattern[2] << 8) | ((int)pattern[3] & 0x0ff);
                for (int n = 0; n < 8; n++)
            {
                    s4[n] = (byte)x;
                    x >>= 1;
                }
            }
        }

        x = (int)pattern[pattern.length-1] << 8;
        y = 0x0ff00;
        for (int n = 0; n < 8; n++)
        {
            s5[n] = x & 0x0ff;
            x >>= 1;
            s5mask[n] = y & 0x0ff;
            y >>= 1;
        }
    }

    /**
     * Set the allowable bit slip. The value must be 0, 1, or 2.
     * The default is 0.
     */
    final void setSlip(int slip) throws IllegalArgumentException
    {
        if (slip < 0 || slip > 2)
        {
            throw new IllegalArgumentException("0 <= slip <= 2");
        }
        slippage = slip;
    }

    /**
     * Check if sync is present at the buffer's current index. Do not slip.
     * The entire pattern must be able to fit in the buffer at the current
     * location.
     * @param buffer The input buffer.
     * @return true if sync was present and false otherwise.
     */
    final boolean checkSync(Buffer buffer)
    {
        //The bit index is the shifted-down, second-byte bit shift.
        //If it is not what I expect, then the pattern is not at the current
        //location. Otherwise I have a candidate, and I must test the other
        //bytes.
        int keyByte = buffer.index.offset + 1;
        return testSync(buffer.data,keyByte,buffer.index.bit);
    }

    /**
     * Test if sync is present. Do not slip.
     * The entire pattern must be able to fit in the buffer at the  location.
     * @param data The input data array.
     * @param keyByte data array index of key sync byte to be tested
     * @param bitIndex bit shift of keyByte
     * @return true if sync was present and false otherwise.
     */
    final boolean testSync(byte[] data, int keyByte, int bitIndex)
    {
        if (data[keyByte] != s2value[bitIndex]) return false;

        if (syncLength > 2)
        {
            //I directly test s3 and s4 for equality with longer sync patterns.
            byte d = data[keyByte+1];
            if (s3[bitIndex] != d) return false;
            if (syncLength > 3)
            {
                d = data[keyByte+2];
                if (s4[bitIndex] != d) return false;
            }
        }

        //The first and last segments of the pattern may be mixed with data
        //bits when there is a bit shift. I must mask them before testing.

        int a = (int)data[keyByte-1] & s1mask[bitIndex];
        if (a != s1[bitIndex]) return false;

        if (bitIndex > 0)
        {
            //The s5 test only applies when there is bit shift.
            a = (int)data[keyByte+syncLength-1] & s5mask[bitIndex];
            if (a != s5[bitIndex]) return false;
        }

        return true;
    }

    /**
     * Test bit slip at the buffer's current index. If it detects slip,
     * it changes the buffer's current index. If N is the current buffer
     * index, then array locations N-1 through N+syncLength should exist.
     * @param buffer The buffer to be tested
     * @return true if a slipped sync was detected and false otherwise.
     */
    final boolean checkSlip(Buffer buffer)
    {
        boolean slip = false;

        //Test for 1-bit slippage, which every slip check will do at least.
        if (slippage > 0)
        {
            slip = testSlip(buffer,1);
        }

        //If no sync detected and slippage is 2 bits, do the 2nd bit now.
        if (!slip && slippage == 2)
        {
            slip = testSlip(buffer,2);
        }

        return slip;
    }

    /**
     * Test for an exact bit position slip at the buffer's current index.
     * That is, if you test for 2-bit slippage, it does not check 1-bit slip.
     * If it detects slip, it changes the buffer's current index. If N is the
     * current buffer index, then array locations N-1 through N+syncLength
     * should exist.
     * @param buffer The buffer to be tested
     * @return true if a slipped sync was detected and false otherwise. It
     *          returns false if there is an unslipped pattern at the buffer's
     *          current location.
     */
    private boolean testSlip(Buffer buffer, int slip)
    {
        int keyByte = buffer.index.offset + 1;
        int bitIndex = buffer.index.bit;

        //Is the slip within the same byte? Testing long slip.
        if (bitIndex < 8 - slip)
        {
            int n = bitIndex + slip;
            boolean sync = testSync(buffer.data,keyByte,n);
            if (sync)
            {
                buffer.index.bit = n;
                return true;
            }
        }
        else
        {
            //The frame may have slipped into the next byte.
            int n = bitIndex + slip - 8;
            boolean sync = testSync(buffer.data,keyByte+1,n);
            if (sync)
            {
                buffer.index.bit = n;
                buffer.advance(1);
                return true;
            }
        }

        //Is the slip within the same byte? Testing short slip.
        if (bitIndex >= slip)
        {
            int n = bitIndex - slip;
            boolean sync = testSync(buffer.data,keyByte,n);
            if (sync)
            {
                buffer.index.bit = n;
                return true;
            }
        }
        else
        {
            //The frame may have slipped into the previous byte.
            int n = bitIndex - slip + 8;
            boolean sync = testSync(buffer.data,keyByte-1,n);
            if (sync)
            {
                buffer.index.bit = n;
                buffer.advance(-1);
                return true;
            }
        }
        return false;
    }

    /**
     * Search for sync in the data array.
     * @param data The data array.
     * @param dataLength The actual length of the data array, which may be
     *          less than data.length.
     * @param start A start byte in data[].
     * @param end An end byte in data[]. It cannot detect partial sync, so
     *          it will adjust the end byte if necessary to be no greater
     *          than dataLength-syncLength.
     * @return A location in data where the first sync pattern was detected
     *          between start and end. It returns null if it did not detect
     *          sync.
     */
    abstract Location search(byte[] data, int dataLength, int start, int end);
}
