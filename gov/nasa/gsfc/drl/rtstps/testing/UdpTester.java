/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.testing;						
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 
 * 
 */
public class UdpTester
{
    private static int[] sizes = {1000, 2048, 4090, 16*1024, 400, 3000,
            20000, 2000, 8192, 5000};

    public static void main(String args[])
    {
        if (args.length != 3)
        {
            System.err.println("arguments: <host> <msDelay> <KbBufferSize");
            System.exit(1);
        }

        String host = args[0];
        long msdelay = Long.valueOf(args[1]).longValue();
        int kbSize = Integer.valueOf(args[2]).intValue();

        int datagramBufferSize = kbSize * 1024;
        int port = 4935;

        byte[] buffer = new byte[datagramBufferSize];
        for (int n = 0; n < datagramBufferSize; n++)
        {
            buffer[n] = (byte)n;
        }

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
            socket.setSendBufferSize(datagramBufferSize);
            System.out.println("Platform send buffer size " +
                    socket.getSendBufferSize());
        }
        catch (java.net.SocketException se)
        {
            se.printStackTrace();
            System.exit(2);
        }

        try
        {
            for (int k = 0; k < sizes.length; k++)
            {
                int plen = sizes[k];
                System.out.println("size="+plen);
                int v = 0;
                for (int m = 1; m < plen; m++)
                {
                    v += (int)buffer[m];
                }
                buffer[0] = (byte)(v & 0x0ff);

                packet.setLength(plen);
                socket.send(packet);
                if (msdelay > 0)
                {
                    try { Thread.sleep(msdelay); }
                    catch (InterruptedException ie) {}
                }
            }

            socket.close();
        }
        catch (java.io.IOException jioe)
        {
            jioe.printStackTrace();
            System.exit(2);
        }
    }
}
