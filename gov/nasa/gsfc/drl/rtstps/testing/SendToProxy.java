/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.testing;						
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 
 * 
 */
public class SendToProxy
{
    public static void main(String args[])
    {
        if (args.length == 0) return;

        int port = 5935;
        String host = "localhost";

        Socket socket = null;
        try
        {
            socket = new Socket(host,port);
        }
        catch (IOException se)
        {
            System.err.println(se.getMessage());
            System.exit(2);
        }

        //I compose the mesage (loadgo, etc) from the argument list.
        String msg = "";
        for (int n = 0; n < args.length; n++)
        {
            msg += args[n] + " ";
        }
        msg += '\n';    //required

        try
        {
            OutputStream o = socket.getOutputStream();
            OutputStreamWriter os = new OutputStreamWriter(o);
            BufferedWriter bw = new BufferedWriter(os);
            bw.write(msg);
            bw.flush();
            bw.write("quit\n");
            bw.close();
            socket.close();
        }
        catch (java.io.IOException jioe)
        {
            jioe.printStackTrace();
        }
    }
}
