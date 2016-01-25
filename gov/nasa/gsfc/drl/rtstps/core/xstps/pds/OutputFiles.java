/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.xstps.pds;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * This class manages the output files. It deals with naming conventions and
 * switching between data files, which it hides from its user.
 * 
 * 
 */
class OutputFiles
{
    private java.util.ArrayList<DSFile> fileList = new java.util.ArrayList<DSFile>(10);	
    private long bytesPerFile = Long.MAX_VALUE;
    private PacketKernel previousPacketKernel = new PacketKernel();
    private long bytesWritten = 0;
    private DSFile currentFile;
    private StringBuffer fileNameTemplate;
    private String path;
    private OutputStream out;
    private int[] xappid = new int[3];
    private int[] xspid = new int[3];

    /**
     * Create an OutputFiles object.
     * @param constructionRecordName The construction record file name
     * @param path The directory where it will put the data files
     */
    OutputFiles(StringBuffer constructionRecordName, String path) throws IOException
    {
        fileNameTemplate = new StringBuffer(constructionRecordName.toString());
        fileNameTemplate.setCharAt(35,'1');
        this.path = path;
        openFile();
    }

    /**
     * Set the number of bytes per file. By default, there is only one output
     * file.
     */
    final void setBytesPerFile(long length)
    {
        bytesPerFile = length;
    }

    /**
     * Get the directory where this class is putting data files.
     */
    final String getPath()
    {
        return path;
    }

    /**
     * Set the application id and spacecraft id for a specific application
     * index. The index is an array index (0,1,2) and not an application id.
     */
    final void setAppidSpid(int apindex, int appid, int spid)
    {
        xappid[apindex] = apindex;
        xspid[apindex] = spid;
    }

    /**
     * Get the number of created data files.
     */
    final int getFileCount()
    {
        return fileList.size();
    }

    /**
     * Open a file.
     */
    private void openFile() throws FileNotFoundException
    {
        String baseName = fileNameTemplate.toString();
        currentFile = new DSFile(path,baseName);
        FileOutputStream fos = new FileOutputStream(currentFile.file);
        out = new BufferedOutputStream(fos,8192);
    }

    /**
     * Write a packet to a data file.
     */
    void write(Packet packet, PacketKernel packetKernel, int apindex)
            throws IOException
    {
        long written = bytesWritten + packet.getSize();

        if ((written > bytesPerFile) && !previousPacketKernel.isEmpty() &&
            (packetKernel.comparePacketTime(previousPacketKernel) > 0))
        {
            out.close();
            fileList.add(currentFile);
            String s = fileNameTemplate.substring(34,36);
            int n = Integer.parseInt(s) + 1;
            s = Integer.toString(n);
            fileNameTemplate.replace(36-s.length(),36,s);
            openFile();
            written = packet.getSize();
        }

        currentFile.ap[apindex].store(packetKernel);

        out.write(packet.getData(),0,packet.getSize());
        bytesWritten = written;
        previousPacketKernel.copy(packetKernel);
    }

    /**
     * Close the current data file.
     */
    void close() throws IOException
    {
        out.close();
        if (currentFile.hasData())
        {
            fileList.add(currentFile);
        }
        else
        {
            currentFile.file.delete();
        }
    }

    /**
     * Write file information to the construction record.
     */
    void writeCS(DataOutput crecord) throws IOException
    {
        Iterator<DSFile> i = fileList.iterator();
        while (i.hasNext())
        {
            DSFile file = (DSFile)i.next();
            crecord.writeBytes(file.id);

            int appids = 0;
            if (file.ap[0].hasData) ++appids;
            if (file.ap[1].hasData) ++appids;
            if (file.ap[2].hasData) ++appids;
            crecord.writeInt(appids);

            for (int n = 0; n < appids; n++)
            {
                Ap a = file.ap[n];
                crecord.writeShort(xspid[n]);
                crecord.writeShort(xappid[n]);
                crecord.writeLong(a.start);
                crecord.writeLong(a.stop);
                crecord.writeInt(0);
            }
        }
    }

    /**
     * This class holds a small amount of information about an
     * application id.
     */
    class Ap
    {
        long start;
        long stop;
        boolean hasData = false;

        void store(PacketKernel pk)
        {
            stop = pk.getPacketTime();
            if (!hasData)
            {
                hasData = true;
                start = stop;
            }
        }
    }

    /**
     * This class holds information about one data file.
     */
    class DSFile
    {
        String id;
        Ap[] ap = new Ap[3];
        File file;  //complete file name spec

        DSFile(String directory, String filename)
        {
            file = new File(directory,filename);
            id = filename;
            ap[0] = new Ap();
            ap[1] = new Ap();
            ap[2] = new Ap();
        }

        boolean hasData()
        {
            return ap[0].hasData || ap[1].hasData || ap[2].hasData;
        }

        public String toString()
        {
            return id;
        }
    }
}
