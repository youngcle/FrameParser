/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;

/**
 * This class handles the frame synchronizer pattern logic for the Frame
 * Synchronizer subsystem. The pattern must be unambiguous; a separate class
 * handles the rare ambiguous pattern. See AbstractSynchronizer for more
 * information.
 * 
 */
class Synchronizer extends AbstractSynchronizer
{
    /**
     * Create a Synchronizer.
     * @param pattern The synchronization pattern. The length may be 1 to 4
     *          bytes.
     * @param frameLength The expected frame length, which includes the sync
     *          pattern.
     */
    Synchronizer(byte[] pattern, int frameLength) throws FsException
    {
        super(pattern,frameLength);

        int x = ((int)pattern[0] << 8) | ((int)pattern[1] & 0x0ff);
        for (int n = 0; n < 8; n++)
        {
            int y = x & 0x0ff;
            if (s2[y] == NOSYNC)
            {
                s2[y] = n;
            }
            else
            {
                throw new FsException("Ambiguous sync pattern");
            }
            s2value[n] = (byte)y;
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

            //If I get here, I found a byte that might signal a pattern.
            //I test the surrounding bytes.

            if (syncLength > 2)
            {
                if (s3[bitIndex] != data[n+1]) continue;
                if (syncLength > 3 && s4[bitIndex] != data[n+2]) continue;
            }

            int q = n - 1;
            int a = s1mask[bitIndex] & data[q];
            if (a != s1[bitIndex]) continue;

            if (bitIndex > 0)  //bit shift
            {
                a = s5mask[bitIndex] & data[q+syncLength];
                if (a != s5[bitIndex]) continue;
            }

            loc = new Location(q, bitIndex);
            break;
        }

        return loc;
    }
}
