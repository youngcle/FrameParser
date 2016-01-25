/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients.spooler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * A file fifo.
 * 
 * 
 */
final class FileSpool
{
    private int maxBlocks = 10000;
    private KeyList keyList = new KeyList();
    private FWriter writer;
    private FReader reader;
    private File file;

    /**
     * Create a FileSpool.
     * @param maxBlocks The "fall behind" threshold. The writer is
     *          signalled if the reader falls behind by more than this
     *          number of blocks.
     * @param tempFileDirectory Where the file resides. If null, it
     *          uses the system default temp directory.
     */
    FileSpool(int maxBlocks, File tempFileDirectory) throws IOException
    {
        this.maxBlocks = maxBlocks;
        file = File.createTempFile("rt-stps",null,tempFileDirectory);
        file.deleteOnExit();
        writer = new FWriter(file,keyList);
        reader = new FReader(file,writer,keyList);
    }

    /**
     * Get a buffer to be used for writing to the spool.
     */
    final byte[] getWriteBuffer()
    {
        return writer.getWriteBuffer();
    }

    /**
     * Put the write-buffer into the spool.
     * @param length the true buffer length, which may be less than its
     *          allocated block length.
     * @param toggle If true, the reader should switch to the pipe after
     *          processing this block.
     * @return true if the reader has fallen so far behind that the
     *          maximum block quota has been exceeded.
     */
    boolean putWriteBuffer(int length, boolean toggle) throws IOException
    {
        int windex = writer.putWriteBuffer(length,toggle);
        return (windex - reader.getIndex()) > maxBlocks;
    }

    /**
     * Read the next block from the temporary file.
     * @return null if no block is available.
     */
    final Block read() throws IOException
    {
        return reader.read();
    }

    void closeWriter() throws IOException
    {
        writer.close();
        System.out.println("I have stopped writing to the spool file. Closed.");
        reader.writerClosed(writer.getIndex());
    }

    void closeReader() throws IOException
    {
        reader.close();
        System.out.println("I have finished reading from the spool file. Closed.");
        file.delete();
        System.out.println("I deleted the spool file.");
    }

    class Key
    {
        int index;
        int length;
        boolean toggle;

        Key(int index, int length, boolean toggle)
        {
            this.index = index;
            this.length = length;
            this.toggle = toggle;
        }

        public String toString()
        {
            return "Key n="+index+" len="+length+" toggle="+toggle;
        }
    }

    class KeyList extends LinkedList<Key>				
    {
        synchronized Key getFirstKey()
        {
            Key key = null;
            if (!isEmpty())
            {
                key = (Key)removeFirst();
            }
            return key;
        }

        final synchronized void addKey(Key key)
        {
            add(key);
        }
        private static final long serialVersionUID = 1;			
    }

    class FWriter
    {
        private int windex = 0;
        private byte[] wdata;
        private OutputStream wstream;
        private KeyList keyList;

        FWriter(File file, KeyList keyList) throws IOException
        {
            int length = Block.getBlockSize();
            wdata = new byte[length];
            this.keyList = keyList;
            wstream = new FileOutputStream(file);
        }

        final int getIndex()
        {
            return windex;
        }

        final byte[] getWriteBuffer()
        {
            return wdata;
        }

        int putWriteBuffer(int length, boolean toggle) throws IOException
        {
            wstream.write(wdata);
            wstream.flush();
            if ((length < wdata.length) || toggle)
            {
                Key key = new Key(windex,length,toggle);
                keyList.addKey(key);
            }
            ++windex;
            return windex;
        }

        void close() throws IOException
        {
            wstream.close();
        }
    }

    class FReader
    {
        private int rindex = 0;
        private Block rblock = new Block();
        private InputStream rstream;
        private FWriter writer;
        private KeyList keyList;
        private Key firstKey = null;
        private boolean writingFinished = false;
        private int residueBlocksCounter = 0;
        private int residueBlocks = 0;
        private int percentMilestone = 10;

        FReader(File file, FWriter writer, KeyList keylist)
                throws FileNotFoundException
        {
            this.writer = writer;
            keyList = keylist;
            rstream = new FileInputStream(file);
        }

        final int getIndex()
        {
            return rindex;
        }

        void writerClosed(int writeBlockCount)
        {
            writingFinished = true;
            residueBlocks = writeBlockCount - rindex;
            if (residueBlocks > 0)
            {
                System.out.println("I still must send " + residueBlocks +
                        " blocks to the target.");
            }
        }

        Block read() throws IOException
        {
            if (rindex == writer.getIndex()) return null;

            if (writingFinished && residueBlocks > 0)
            {
                ++residueBlocksCounter;
                int percent = 100 * residueBlocksCounter / residueBlocks;
                if (percent >= percentMilestone)
                {
                    System.out.print(" "+percent+"% ");
                    percentMilestone += 10;
                    if (percentMilestone > 100) System.out.println();
                }
            }

            byte[] data = rblock.getData();
            int bytes = rstream.read(data);

            if (bytes != data.length)
            {
                throw new IOException("Read error on spool file.");
            }

            if (firstKey == null)
            {
                firstKey = keyList.getFirstKey();
            }
            if ((firstKey != null) && (rindex == firstKey.index))
            {
                rblock.markUsed(firstKey.toggle,firstKey.length);
                firstKey = null;
            }
            else
            {
                rblock.markUsed(false,bytes);
            }

            ++rindex;
            return rblock;
        }

        void close() throws IOException
        {
            rstream.close();
        }
    }
}
