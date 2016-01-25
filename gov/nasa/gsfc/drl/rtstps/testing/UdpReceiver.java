/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.testing;						

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * 
 * 
 */
public class UdpReceiver
{
    private int datagramBufferSize = 32 * 1024;
    private int port = 4935;

    public static void main(String args[])
    {
        UdpReceiver r = new UdpReceiver();
        r.go();
    }

    public void go()
    {
        boolean testChecksum = true;
        boolean running = true;
        byte[] buffer = new byte[datagramBufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        DatagramSocket socket = null;

        try
        {
            socket = new DatagramSocket(port);
            socket.setReceiveBufferSize(datagramBufferSize);
            System.out.println("socket platform receive size " +
                socket.getReceiveBufferSize());
        }
        catch (java.net.SocketException se)
        {
            se.printStackTrace();
            running = false;
        }

        while (running)
        {
            try
            {
                packet.setLength(datagramBufferSize);
                socket.receive(packet);
            }
            catch (java.io.IOException ioe)
            {
                ioe.printStackTrace();
                running = false;
            }

            System.out.println(packet.getLength());

            if (testChecksum)
            {
                byte[] data = packet.getData();
                int len = packet.getLength();
                int v = 0;
                for (int n = 1; n < len; n++)
                {
                    v += (int)data[n];
                }
                if (data[0] != (byte)(v & 0x0ff))
                {
                    System.out.println("checksum error");
                }
            }
        }

        socket.close();
    }
}
