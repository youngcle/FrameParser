/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.testing;						

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 
 * 
 */
//This is a UDP Receiver.
public class UdpR
{
    private static final int PORT = 4001;
    private static final int BUFFERSIZE = 32*1024;
    private static final int BLOCKSIZE = 5000;

    public static void main(String args[])
    {
        UdpR r = new UdpR();
        r.go();
    }

    public void go()
    {
        byte[] buffer = new byte[BLOCKSIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        DatagramSocket socket = null;

        try
        {
            socket = new DatagramSocket(PORT);
            socket.setReceiveBufferSize(BUFFERSIZE);
            System.out.println("socket platform receive size " +
                socket.getReceiveBufferSize());
            Thread.sleep(10000L);
        }
        catch (Exception se)
        {
            se.printStackTrace();
            return;
        }


        while (true)
        {
            try
            {
                packet.setLength(BLOCKSIZE);
                socket.receive(packet);
                InetAddress a = packet.getAddress();
                System.out.println("host="+a.getHostAddress());
            }
            catch (java.io.IOException ioe)
            {
                ioe.printStackTrace();
                break;
            }

            System.out.println("k="+(int)buffer[0] + " len="+packet.getLength());
        }

        socket.close();
    }
}
