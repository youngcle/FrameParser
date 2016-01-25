/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients.spooler;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * This class reads data from a socket.
 * 
 */
final class SocketReader extends Thread
{
    private Signal signal;
    private Socket socket;
    private Pipe pipe;
    private FileSpool file;
    private InputStream istream;

    SocketReader(Socket socket, Signal signal, Pipe pipe, FileSpool file)
            throws IOException
    {
        this.signal = signal;
        this.socket = socket;
        this.pipe = pipe;
        this.file = file;
        istream = socket.getInputStream();
        int blockSize = Block.getBlockSize();
        istream = new BufferedInputStream(istream,blockSize);
    }

    public void run()
    {
        boolean usingPipe = true;
        byte[] data = pipe.getWriteBuffer();

        while (true)
        {
            int bytes = 0;
            try
            {
                bytes = istream.read(data);
            }
            catch (IOException ioe1)
            {
                pipe.setEndOfStream(true);
                signal.send();
                System.err.println("Socket read error "+ ioe1.getMessage());
                break;

            }
            if (signal.isTerminated())
            {
                System.err.println("Writer terminated probably because of IO exception.");
                break;
            }

            if (bytes <= 0)
            {
                pipe.setEndOfStream(true);
                signal.send();
                break;
            }

            if (usingPipe)
            {
                boolean toggle = pipe.putWriteBuffer(bytes);
                usingPipe = !toggle;
                signal.send();
            }
            else
            {
                usingPipe = pipe.isEmpty();
                try
                {
                    file.putWriteBuffer(bytes,usingPipe);
                    signal.send();
                }
                catch (IOException ioe2)
                {
                    System.err.println("File write error "+ ioe2.getMessage());
                    pipe.setEndOfStream(true);
                    signal.send();
                    break;
                }
            }

            if (usingPipe)
            {
                data = pipe.getWriteBuffer();
            }
            else
            {
                data = file.getWriteBuffer();
            }

            yield();
        }

        try { istream.close(); } catch (IOException e001) { e001.printStackTrace(); }
        try { socket.close(); } catch (IOException e002) { e002.printStackTrace(); }
        try { file.closeWriter(); } catch (IOException e003) { e003.printStackTrace(); }
    }
}
