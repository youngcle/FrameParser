/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients;
import gov.nasa.gsfc.drl.rtstps.server.RtStpsServices;

/**
 * A program to terminate the server.
 * 
 */
public class Kill
{
    public static void main(String args[])
    {
        String host = (args.length > 0)? args[0] : "localhost";

        String target = "//" + host + "/RtStpsServices";
        if (args.length == 2)
        {
            target += "." + args[1];
        }
        

        try
        {
            RtStpsServices server = (RtStpsServices)java.rmi.Naming.lookup(target);
            server.stopServer();
            System.exit(0);
        }
        catch (java.rmi.UnmarshalException uex)
        {
            //normal because the server probably won't answer.
            System.exit(0);
        }
        catch (Exception ex)
        {
            System.err.println("Lookup error. "+target);
            System.err.println(ex);
            System.exit(100);
        }
    }
}
