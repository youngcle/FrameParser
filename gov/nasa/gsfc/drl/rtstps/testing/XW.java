/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.testing;						
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * 
 */
public class XW
{
    private static final int PORT = 4001;
    private static final int BSIZE = 8*1024;

    public static void main(String args[])
    {
        byte[] buffer = new byte[1024];
        int x = 0x3a;
        for (int n = 1; n < buffer.length; n++)
        {
            buffer[n] = (byte)n;
            x ^= (int)buffer[n];
        }
        buffer[0] = (byte)x;

        InetAddress address = null;
        try
        {
            address = InetAddress.getByName("localhost");
        }
        catch (UnknownHostException uhe)
        {
            System.err.println(uhe.getMessage());
            System.exit(3);
        }

        Socket socket = null;
        try
        {
            socket = new Socket(address,PORT);
            //socket.setTcpNoDelay(false);
            socket.setSendBufferSize(BSIZE);
            System.out.println("sendBufferSize="+socket.getSendBufferSize());
        }
        catch (Exception se)
        {
            System.err.println(se.getMessage());
            System.exit(2);
        }

        try
        {
            OutputStream o = socket.getOutputStream();
            int total = 0;

            for (int k = 0; k < 100; k++)
            {
                total += buffer.length;
                o.write(buffer,0,buffer.length);
                System.out.println("write "+k);
            }

            o.close();
            socket.close();
            System.out.println("Total="+total);
        }
        catch (java.io.IOException jioe)
        {
            jioe.printStackTrace();
            System.exit(2);
        }
    }
}
