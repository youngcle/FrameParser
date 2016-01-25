/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients.spooler;

/**
 * This class contains a block of data. It's the way SocketReader sends
 * data to SocketWriter.
 * 
 */
final class Block
{
    private static int blockSize = 2048;
    private byte[] data;
    private int length = 0;
    private boolean empty = true;
    private boolean toggle = false;

    /**
     * Get the block size in bytes.
     */
    static int getBlockSize()
    {
        return blockSize;
    }

    /**
     * Set the block size. Use this once only before configuration.
     */
    static void setBlockSize(int size)
    {
        blockSize = size;
    }

    /**
     * Construct a block of the specified block size.
     */
    Block()
    {
        data = new byte[blockSize];
    }

    /**
     * Get the data within this block.
     */
    final byte[] getData()
    {
        return data;
    }

    /**
     * Mark this block as empty.
     */
    final void setEmpty(boolean e)
    {
        empty = e;
    }

    /**
     * Is this block empty?
     */
    final boolean isEmpty()
    {
        return empty;
    }

    /**
     * Mark this block as used.
     * @param toggle Set the toggle flag, which has special meaning.
     *      SocketReader uses it to tell SocketWriter to switch to
     *      either the pipe or the file spool to get its next block.
     * @param length The true block length, which may be shorter
     *      than the maximum block length.
     */
    void markUsed(boolean toggle, int length)
    {
        this.toggle = toggle;
        this.length = length;
        empty = false;
    }

    /**
     * Is the toggle flag on or off?
     */
    final boolean isToggled()
    {
        return toggle;
    }

    /**
     * Write this block's data to the output stream.
     * @return true if successful and false otherwise.
     */
    boolean write(java.io.OutputStream os)
    {
        boolean ok = true;
        try
        {
            os.write(data,0,length);
        }
        catch (java.io.IOException e)
        {
            System.err.println("write failure " + e.getMessage());
            ok = false;
        }
        return ok;
    }
}
