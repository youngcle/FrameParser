/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients.spooler;

/**
 * SocketReader uses this FIFO queue of Blocks to send data to the
 * SocketWriter.
 * 
 */
final class Pipe
{
    private int writeIndex = 0;
    private int readIndex = 0;
    private boolean eos = false;
    private Block[] blockArray;

    /**
     * Create a pipe of 32 blocks. Each block is 2048 bytes long.
     */
    Pipe()
    {
        this(32,2048);
    }

    /**
     * Create a custom pipe.
     * @param blockCount The maximum number of blocks in the pipe.
     * @param blockSizeBytes The size of each block in bytes.
     */
    Pipe(int blockCount, int blockSizeBytes)
    {
        Block.setBlockSize(blockSizeBytes);
        blockArray = new Block[blockCount];
        for (int n = 0; n < blockArray.length; n++)
        {
            blockArray[n] = new Block();
        }
    }

    /**
     * Mark end-of-stream. SocketReader tells SocketWriter that
     * no more data will follow.
     */
    final void setEndOfStream(boolean eos)
    {
        this.eos = eos;
    }

    /**
     * Is this the end of stream?
     */
    final boolean isEndOfStream()
    {
        return eos;
    }

    /**
     * Is the pipe empty?
     */
    final boolean isEmpty()
    {
        return (readIndex == writeIndex) &&
                blockArray[writeIndex].isEmpty();
    }

    /**
     * Get a buffer to be used for writing to the pipe.
     * @return null if no block is available.
     */
    byte[] getWriteBuffer()
    {
        Block block = blockArray[writeIndex];
        return block.isEmpty()? block.getData() : null;
    }

    /**
     * Put the write-buffer into the pipe.
     * @param length the true buffer length, which may be less than its
     *          allocated block length.
     * @return true if this is the last available buffer in the pipe.
     */
    boolean putWriteBuffer(int length)
    {
        int x = writeIndex + 1;
        if (x == blockArray.length) x = 0;
        boolean isNextBlockUsed = !blockArray[x].isEmpty();
        Block current = blockArray[writeIndex];
        current.markUsed(isNextBlockUsed,length);
        writeIndex = x;
        return isNextBlockUsed;
    }

    /**
     * Get a filled block from the pipe.
     * @return the block or null if none are available.
     */
    Block read()
    {
        Block block = blockArray[readIndex];
        return block.isEmpty()? null : block;
    }

    /**
     * Return the last read block to the free block pool.
     */
    void release()
    {
        blockArray[readIndex].setEmpty(true);
        ++readIndex;
        if (readIndex == blockArray.length) readIndex = 0;
    }
}
