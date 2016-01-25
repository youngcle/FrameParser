/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.testing;						
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * 
 */
public class XR
{
    public static final void main(String[] args)
    {
        int bufferSize = 16*1024;
        try
        {
            if (args.length > 0)
            {
                bufferSize = Integer.parseInt(args[0]);
            }
            /*XR sd =*/ new XR(bufferSize);				
        }
        catch (Exception e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    private static final int PORT = 4001;

    public XR(int bufferSize) throws Exception
    {
        byte[] buffer = new byte[bufferSize];
        ServerSocket ss = new ServerSocket(PORT);
        Socket socket = ss.accept();

        //socket.setReceiveBufferSize(bufferSize);
        System.out.println("System read buffer size " +
                socket.getReceiveBufferSize());

        //socket.setTcpNoDelay(false);

        Thread.sleep(10000L);

        InputStream is = socket.getInputStream();
        is = new BufferedInputStream(is,bufferSize);
        int bytes = 0;
        int total = 0;
        do
        {
            bytes = is.read(buffer);
            boolean ok = true;
            if (bytes > 0) total += bytes;

            if (bytes > 1)
            {
                int x = 0x3a;
                for (int n = 1; n < bytes; n++)
                {
                    x ^= (int)buffer[n];
                }
                ok = ((byte)x == buffer[0]);
            }
            System.out.println("bytes="+bytes+" ok="+ok);
        }
        while (bytes >= 0);

        System.out.println("Total="+total);
        is.close();
        socket.close();
        ss.close();
    }
}
