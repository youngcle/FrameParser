/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;

/**
 * This class handles the frame synchronizer pattern logic for the Frame
 * Synchronizer subsystem. It handles ambiguous sync patterns. See the
 * AbstractSynchronizer class description for more information.
 * 
 */
class XSynchronizer extends AbstractSynchronizer
{
    private int[] ambiguousS2 = new int[8];
    private int[][] s2plus = new int[8][8];

    /**
     * Create an XSynchronizer.
     * @param pattern The synchronization pattern. The length may be 1 to 4
     *          bytes.
     * @param frameLength The expected frame length, which includes the sync
     *          pattern.
     */
    XSynchronizer(byte[] pattern, int frameLength)
    {
        super(pattern,frameLength);

        int x = ((int)pattern[0] << 8) | ((int)pattern[1] & 0x0ff);
        for (int n = 0; n < 8; n++)
        {
            int y = x & 0x0ff;
            if (s2[y] == NOSYNC)
            {
                s2[y] = n;
                ambiguousS2[n] = 0;
            }
            else
            {
                int k = s2[y];
                int m = ambiguousS2[k];
                s2plus[k][m] = n;
                ++ambiguousS2[k];
            }
            x >>= 1;
        }
    }

    /**
     * Search for sync in the data array.
     * @param data The data array that may contain frames.
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
    Location search(byte[] data, int dataLength, int start, int end)
    {
        Location loc = null;

        int maxEnd = dataLength - syncLength;
        ++start;  //I am keying on the 2nd sync byte.
        if (end > maxEnd) end = maxEnd;

        //I test every byte in the buffer, exitting as soon as I find sync.
        for (int n = start; n <= end; n++)
        {
            int v = (int)data[n] & 0x0ff;
            int bitIndex = s2[v];
            if (bitIndex == NOSYNC) continue;

            loc = testSearch(data,n,bitIndex);
            if (loc != null) break;

            if (ambiguousS2[bitIndex] > 0)
            {
                int ambiguousBits = ambiguousS2[bitIndex];
                for (int b = 0; b < ambiguousBits; b++)
                {
                    loc = testSearch(data,n,s2plus[bitIndex][b]);
                    if (loc != null) return loc;
                }
            }
        }

        return loc;
    }

    /**
     * I test a candidate sync pattern.
     */
    private Location testSearch(byte[] data, int byteOffset, int bitIndex)
    {
        if (syncLength > 2)
        {
            if (s3[bitIndex] != data[byteOffset+1]) return null;
            if (syncLength > 3 && s4[bitIndex] != data[byteOffset+2])
                return null;
        }

        int q = byteOffset - 1;

        if (bitIndex == 0)  //no bit shift
        {
            if ((byte)s1[0] != data[q]) return null;
        }
        else
        {
            int a = s1mask[bitIndex] & data[q];
            if (a != s1[bitIndex]) return null;

            a = s5mask[bitIndex] & data[q+syncLength];
            if (a != s5[bitIndex]) return null;
        }

        return new Location(q, bitIndex);
    }
}
