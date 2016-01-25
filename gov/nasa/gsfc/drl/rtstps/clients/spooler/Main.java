/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients.spooler;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;

/**
 * 
 */
final class Main
{
    public static void main(String[] args)
    {
        final int blocksize = 2048;
        final int fileblocks = 100;
        final int pipeblocks = 32;
        final File tempFileDirectory = null;

        if (args.length != 3)
        {
            System.err.println("syntax: <inputPort> <host> <outputPort>");
            System.exit(1);
        }

        String host = args[1];
        int rport = 0;
        int wport = 0;
        try
        {
            rport = Integer.parseInt(args[0]);
            wport = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("port argument format error");
            System.exit(2);
        }

        DateFormat dateFormat = DateFormat.getDateInstance();

        ServerSocket ss = null;
        try
        {
            ss = new ServerSocket(rport,1);
        }
        catch (IOException sse)
        {
            System.err.println("Could not create server socket");
            sse.printStackTrace();
            System.exit(3);
        }

        System.out.println("Ready.");

        try
        {
            while (true)
            {
                Socket rsocket = ss.accept();
                Pipe pipe = new Pipe(pipeblocks,blocksize);
                FileSpool filespool = new FileSpool(fileblocks,tempFileDirectory);
                Signal signal = new Signal();

                Socket wsocket = null;
                SocketWriter writer = null;
                SocketReader reader = null;
                System.out.println("A data source connected to me. " +
                        dateFormat.format(new java.util.Date()));

                try
                {
                    wsocket = new Socket(host,wport);
                    writer = new SocketWriter(wsocket,signal,pipe,filespool);
                    reader = new SocketReader(rsocket,signal,pipe,filespool);
                    writer.start();
                    //I am purposely not running the reader as a new thread.
                    reader.run();
                }
                catch (UnknownHostException uhe)
                {
                    System.err.println("Unknown host " + host);
                    System.exit(5);
                }
                catch (IOException e)
                {
                    try { rsocket.close(); }
                    catch (IOException eee) { }
                    System.err.println(e.getMessage());
                }
                System.out.println("My input source has disconnected. " +
                        dateFormat.format(new java.util.Date()));
            }
        }
        catch (IOException rse)
        {
            rse.printStackTrace();
            System.exit(4);
        }
    }
}
