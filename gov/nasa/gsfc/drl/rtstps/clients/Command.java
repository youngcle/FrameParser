/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients;
import gov.nasa.gsfc.drl.rtstps.server.RtStpsServices;

/**
 * 
 */
public class Command
    {
    public static void main(String args[])
    {
        if (args.length < 2)
        {
            System.out.println("Syntax: GO|STOP|UNLOAD|SHUTDOWN|ZERO <host> <server>");
            System.exit(-1);
        }

        //I hook up via RMI to the RT-STPS server. Its full RMI URL will look
        //something like this: "//host.gsfc.nasa.gov/RtStpsServices"

        String target = "rmi://" + args[1] + "/RtStpsServices";
        if (args.length == 3)
        {
            target += "." + args[2];
        }

        try
        {
            RtStpsServices server = (RtStpsServices)java.rmi.Naming.lookup(target);

            String command = args[0];

            if (command.equalsIgnoreCase("GO"))
            {
                server.setEnabled(true);
            }
            else if (command.equalsIgnoreCase("STOP"))
            {
                server.setEnabled(false);
            }
            else if (command.equalsIgnoreCase("UNLOAD"))
            {
                server.unload();
            }
            else if (command.equalsIgnoreCase("SHUTDOWN"))
            {
                server.setEnabled(false);
                server.unload();
            }
            else if (command.equalsIgnoreCase("ZERO"))
            {
                server.zeroStatus();
            }
            else
            {
                throw new java.io.IOException("Unknown command: " + command);
            }

            System.out.println("Ok");
        }
        catch (java.io.IOException jioe)
        {
            System.err.println(jioe);
        }
        catch (java.rmi.NotBoundException jrnbe)
        {
            System.err.println(jrnbe.getMessage());
        }
    }
}
