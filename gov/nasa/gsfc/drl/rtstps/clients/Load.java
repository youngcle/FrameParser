/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients;
import gov.nasa.gsfc.drl.rtstps.server.RtStpsServices;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * 
 */
public class Load
{
    public static void main(String args[])
    {
        if (args.length < 2)
        {
            System.err.println("Syntax: <StpsSetupFileName> <host> <server>");
            System.exit(-1);
        }


        //I hook up via RMI to the Stps server. Its full RMI URL will look
        //something like this: "//host.gsfc.nasa.gov/StpsServices"

        String target = "//" + args[1] + "/StpsServices";
        if (args.length == 3)
        {
            target += "." + args[2];
        }
        
        try
        {
            String setupFileName = args[0];
            java.io.File file = new java.io.File(setupFileName);
            int blen = (int)file.length();
            char[] cbuffer = new char[blen];
            BufferedReader r = new BufferedReader(new FileReader(file));
            int rlen = r.read(cbuffer,0,blen);
            if (blen != rlen) throw new java.io.IOException("Truncated file.");
            r.close();

            RtStpsServices server = (RtStpsServices)java.rmi.Naming.lookup(target);
            server.load(cbuffer);
            System.out.println("Ok");
        }
        catch (java.io.IOException jioe)
        {
            System.err.println(jioe);
        }
        catch (java.rmi.NotBoundException jrnbe)
        {
            System.err.println(jrnbe);
        }
    }
}
