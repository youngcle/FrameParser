/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * 
 */
public class SocketDrain
{
    public static final void main(String[] args)
    {
        int bufferSize = 8192;
        try
        {
            if (args.length > 0)
            {
                bufferSize = Integer.parseInt(args[0]);
            }
            /*SocketDrain sd = */new SocketDrain(bufferSize);		
        }
        catch (Exception e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public SocketDrain(int bufferSize) throws Exception
    {
        byte[] buffer = new byte[32*1024];
        ServerSocket ss = new ServerSocket(4001,2);
        Socket socket = ss.accept();

        socket.setReceiveBufferSize(bufferSize);
        System.out.println("System read buffer size " +
                socket.getReceiveBufferSize());

        InputStream is = socket.getInputStream();
        int biggest = 0;
        int smallest = 999999;

        while (true)
        {
            int bytes = is.read(buffer);
            if (bytes > biggest) biggest = bytes;
            if (bytes == -1) break;
            if (bytes < smallest) smallest = bytes;
            Thread.sleep(25L);
        }

        System.out.println("biggest buffer = " + biggest);
        System.out.println("smallest buffer = " + smallest);
        
        is.close();
        socket.close();
        ss.close();
    }
}
