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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 
 * 
 */
public class SendTlmUDP
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

        int datagramBufferSize = kbSize * 1024;
        int port = 4935;

        byte[] buffer = new byte[datagramBufferSize];

        InetAddress address = null;
        try
        {
            address = InetAddress.getByName(host);
        }
        catch (UnknownHostException uhe)
        {
            uhe.printStackTrace();
            System.exit(3);
        }

        DatagramPacket packet = new DatagramPacket(buffer,buffer.length,address,port);
        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket();
        }
        catch (java.net.SocketException se)
        {
            se.printStackTrace();
            System.exit(2);
        }

        try
        {
            File file = new File(datafile);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream r = 
                    new BufferedInputStream(fis,datagramBufferSize);

            while (true)
            {
                int bytes = r.read(buffer,0,datagramBufferSize);
                if (bytes == -1) break;
                
                packet.setLength(bytes);
                socket.send(packet);
                
                if (msdelay > 0)
                {
                    try { Thread.sleep(msdelay); }
                    catch (InterruptedException ie) {}
                }
            }

            socket.close();
            r.close();
        }
        catch (IOException jioe)
        {
            jioe.printStackTrace();
            System.exit(2);
        }
    }
}
