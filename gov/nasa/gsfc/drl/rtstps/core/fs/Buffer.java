/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;

/**
 * This class holds a byte array with a currency indicator that marks a bit
 * and byte location within the buffer.
 * 
 */
final class Buffer
{
    byte[] data;
    Location index = new Location(0,0);
    private int dataLength;      //actual data length which may be <= data.length
    private int remainingBytes;  //bytes yet to be processed

    /**
     * Create an empty Buffer. You must use setData() to initialize.
     */
    Buffer()
    {
    }

    /**
     * Create a buffer of the specified length.
     */
    Buffer(int length)
    {
        data = new byte[length];
        remainingBytes = dataLength = length;
    }

    /**
     * Change the buffer's array and length. The length must be less than
     * or equal to data.length.
     */
    public void setData(byte[] data, int length)
    {
        this.data = data;
        remainingBytes = dataLength = length;
        index.offset = 0;
        index.bit = 0;
    }

    /**
     * Get the buffer length.
     */
    final int getLength()
    {
        return dataLength;
    }

    /**
     * Get the number of bytes that are remaining.
     */
    final int getRemainingBytes()
    {
        return remainingBytes;
    }

    /**
     * Clear the buffer.
     */
    final void empty()
    {
        index.offset = dataLength;
        remainingBytes = 0;
    }

    /**
     * Append another buffer to this one. Beginning at the other buffer's
     * start index, enough bytes are appended to fill this buffer completely.
     * If the other buffer is too short, then this methods appends all of its
     * bytes. The currency indices of both buffers are updated.
     * @return the number of bytes copied
     */
    int append(Buffer buffer)
    {
        int length = buffer.remainingBytes;
        if (remainingBytes < length) length = remainingBytes;

        System.arraycopy(buffer.data, buffer.index.offset, data,
                    index.offset, length);

        buffer.advance(length);
        advance(length);

        return length;
    }

    /**
     * Advance the current location. The bit offset is unaffected.
     */
    final void advance(int bytes)
    {
        index.offset += bytes;
        remainingBytes = dataLength - index.offset;
    }

    /**
     * Set the buffer's current location.
     * @param loc A new location. loc is not saved; its contents are copied.
     */
    final void setLocation(Location loc)
    {
        index.offset = loc.offset;
        index.bit = loc.bit;
        remainingBytes = dataLength - index.offset;
    }

    /**
     * Set the current buffer location to the byte offset. The bit offset
     * is unaffected.
     */
    final void setLocation(int byteOffset)
    {
        index.offset = byteOffset;
        remainingBytes = dataLength - index.offset;
    }

    public String toString()
    {
        return index.toString() + " remainingBytes=" + remainingBytes;
    }
}
