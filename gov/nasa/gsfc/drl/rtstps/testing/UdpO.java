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
public class UdpO
{
    private static final int PORT = 4001;
    private static final int BSIZE = 32*1024;
    private static final int BLOCKSIZE = 5000;

        public static void main(String args[])
    {
        byte[] buffer = new byte[BLOCKSIZE];
        for (int n = 0; n < buffer.length; n++)
        {
            buffer[n] = (byte)n;
        }

        InetAddress address = null;
        try
        {
            address = InetAddress.getByName("localhost");
        }
        catch (UnknownHostException uhe)
        {
            uhe.printStackTrace();
            System.exit(3);
        }

        DatagramPacket packet = new DatagramPacket(buffer,buffer.length,
                address,PORT);
        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket();
            socket.setSendBufferSize(BSIZE);
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
            for (int k = 0; k < 25; k++)
            {
                buffer[0] = (byte)k;

                packet.setLength(BLOCKSIZE);
                socket.send(packet);
                System.out.println("send "+k);
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
