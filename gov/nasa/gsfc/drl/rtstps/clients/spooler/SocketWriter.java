/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients.spooler;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class writes data to a socket.
 * 
 */
final class SocketWriter extends Thread
{
    private Signal signal;
    private Pipe pipe;
    private FileSpool file;
    private OutputStream ostream;
    private Socket socket;

    SocketWriter(Socket socket, Signal signal, Pipe pipe, FileSpool file)
            throws IOException
    {
        this.socket = socket;
        this.signal = signal;
        this.pipe = pipe;
        this.file = file;
        ostream = socket.getOutputStream();
        int blockSize = Block.getBlockSize();
        ostream = new BufferedOutputStream(ostream,blockSize);
    }

    public void run()
    {
        boolean usingPipe = true;
        Block block = null;
        boolean ok = true;  //When false, I discard output.

        while (true)
        {
            if (usingPipe)
            {
                block = pipe.read();
                if (block == null)
                {
                    if (pipe.isEndOfStream()) break;
                    try
                    {
                        signal.receive();
                        continue;
                    }
                    catch (InterruptedException pie)
                    {
                        break;
                    }
                }
                if (ok) ok = block.write(ostream);
                usingPipe = !block.isToggled();
                pipe.release();
            }
            else
            {
                try
                {
                    block = file.read();
                    if (block == null)
                    {
                        if (pipe.isEndOfStream()) break;
                        try
                        {
                            signal.receive();
                            continue;
                        }
                        catch (InterruptedException pie)
                        {
                            break;
                        }
                    }
                    if (ok) ok = block.write(ostream);
                    usingPipe = block.isToggled();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            //yield();
        }

        signal.terminate();
        try { ostream.flush(); } catch (IOException ec) { ec.printStackTrace(); }
        try { ostream.close(); } catch (IOException ec) { ec.printStackTrace(); }
        try { socket.close(); } catch (IOException ec) { ec.printStackTrace(); }
        try { file.closeReader(); } catch (IOException ec) { ec.printStackTrace(); }
        System.out.println("I disconnected my output socket.");
    }
}
