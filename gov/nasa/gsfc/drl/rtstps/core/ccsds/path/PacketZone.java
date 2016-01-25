/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds.path;

/**
 * This class contains the packet zone part of a VCDU. We use it in packet
 * reassembly to move bytes to packets and to maintain our current location
 * within the packet zone. It does not contain the first header pointer. This
 * class is reuseable.
 * 
 */
final class PacketZone
{
    private byte[] data;
    private int zoneStart;
    private int zoneEnd;
    private int index;
    private int remainingBytes;

    /**
     * Use this method to load a new packet zone into this class. It resets
     * all indicators.
     * @param data The byte array that contains the packet zone.
     * @param start Index of the beginning byte of the packet zone. Skip the
     *          first header pointer.
     * @param end Index of the last byte of the packet zone.
     */
    void reload(byte[] data, int start, int end)
    {
        this.data = data;
        zoneStart = start;
        zoneEnd = end;
        index = start;
        remainingBytes = zoneEnd - zoneStart + 1;
    }

    /**
     * Get the number of remaining bytes in the zone.
     */
    final int getRemainingByteCount()
    {
        return remainingBytes;
    }

    /**
     * Empty the zone.
     */
    final void clear()
    {
        remainingBytes = 0;
    }

    /**
     * Move some packet zone bytes (starting at the current zone index)
     * to a target buffer.
     */
    void moveTo(byte[] target, int start, int length)
    {
        System.arraycopy(data, index, target, start, length);
        index += length;
        remainingBytes -= length;
    }

    /**
     * Move all remaining bytes to a target buffer.
     * This method does not verify lengths.
     * @return The number of bytes copied
     */
    int moveRemainderTo(byte[] target, int start)
    {
        System.arraycopy(data, index, target, start, remainingBytes);
        index += remainingBytes;
        int copied = remainingBytes;
        remainingBytes = 0;
        return copied;
    }

    /**
     * Get a 16-bit word from the zone starting at a byte offset from the
     * current index.
     */
    final int getWord(int offset)
    {
        offset += index;
        int v = (int)data[offset] << 8;
        ++offset;
        v |= (int)data[offset] & 0x0ff;
        return v & 0x0ffff;
    }

    /**
     * Advance the current location.
     */
    final void advance(int bytes)
    {
        index += bytes;
        remainingBytes -= bytes;
    }

    public String toString()
    {
        return "index=" + index + " remainingBytes=" + remainingBytes;
    }
}
