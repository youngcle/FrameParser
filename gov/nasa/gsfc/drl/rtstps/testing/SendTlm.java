/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.testing;						
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * 
 */
public class SendTlm
{
    public static void main(String args[])
    {
        if (args.length != 4)
        {
            System.err.println("arguments: <datafile> <host> <msDelay> <KbBufferSize>");
            System.exit(1);
        }

        String datafile = args[0];
        String host = args[1];
        long msdelay = Long.valueOf(args[2]).longValue();
        int kbSize = Integer.valueOf(args[3]).intValue();

        int bufferSize = kbSize * 1024;
        int port = 4935;

        byte[] buffer = new byte[bufferSize];

        InetAddress address = null;
        try
        {
            address = InetAddress.getByName(host);
        }
        catch (UnknownHostException uhe)
        {
            System.err.println(uhe.getMessage());
            System.exit(3);
        }

        Socket socket = null;
        try
        {
            socket = new Socket(address,port);
        }
        catch (IOException se)
        {
            System.err.println(se.getMessage());
            se.printStackTrace();
            System.exit(2);
        }

        try
        {
            File file = new File(datafile);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream r = new BufferedInputStream(fis,bufferSize);
            OutputStream o = socket.getOutputStream();

            while (true)
            {
                int bytes = r.read(buffer,0,bufferSize);
                if (bytes == -1) break;

                o.write(buffer,0,bytes);

                if (msdelay > 0)
                {
                    try { Thread.sleep(msdelay); }
                    catch (InterruptedException ie) {}
                }
            }

            o.close();
            socket.close();
            r.close();
        }
        catch (java.io.IOException jioe)
        {
            jioe.printStackTrace();
            System.exit(2);
        }
    }
}
