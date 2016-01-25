/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.sender;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This thread sends binary file data to a specified port on a target host.
 * 
 */
public class SenderThread extends Thread
{
    public static final long PROGRESS_SIZE = 4192L;
    public static final int BUFFERSIZE = 4192;
    private long msdelay = 0L;
    private OutputStream output;
    private InputStream input;
    private Socket socket;
    private int progress;
    private boolean finished = false;
    private boolean running = true;
    private Sender sender;

    public SenderThread(Sender parent, String host, int port, File file, int delay)
            throws IOException
    {
        sender = parent;
        msdelay = (long)delay;

        socket = new Socket(host,port);
        output = socket.getOutputStream();

        FileInputStream fis = new FileInputStream(file);
        input = new BufferedInputStream(fis,BUFFERSIZE);
    }

    final int getProgress()
    {
        return progress;
    }

    final boolean isDone()
    {
        return finished;
    }

    final void terminate()
    {
        running = false;
        if (msdelay > 0) interrupt();
    }

    public void run()
    {
        byte[] buffer = new byte[BUFFERSIZE];
        progress = 0;
        finished = false;
        long bytesRead = 0L;
        running = true;

        try
        {
            while (running)
            {
                int bytes = input.read(buffer,0,BUFFERSIZE);
                if (bytes == -1) break;

                bytesRead += (long)bytes;
                progress = (int)(bytesRead / PROGRESS_SIZE);

                output.write(buffer,0,bytes);

                if (msdelay > 0)
                {
                    Thread.sleep(msdelay);
                }
            }
        }
        catch (InterruptedException ie)
        {
            //System.out.println("Interrupted");
        }
        catch (final java.io.IOException ioe)
        {
            String msg = ioe.getMessage();
            if (msg == null) msg = "Transmission stopped.";
            final String mmsg = msg;
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    javax.swing.JOptionPane.showMessageDialog(sender, mmsg,
                            "Sender: Alert", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        finished = true;

        try
        {
            output.close();
            socket.close();
            input.close();
        }
        catch (java.io.IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
