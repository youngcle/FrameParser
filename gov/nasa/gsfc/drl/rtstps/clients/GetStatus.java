/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;
import gov.nasa.gsfc.drl.rtstps.server.RtStpsServices;
import gov.nasa.gsfc.drl.rtstps.server.StatusBlock;

/**
 * 
 */
public class GetStatus
    {
    private static long DELAYMILLIS = 10000L;

    public static void main(String args[])
    {
        String host = (args.length > 0)? args[0] : "localhost server";

        //I hook up via RMI to the RT-STPS server. Its full RMI URL will look
        //something like this: "//host.gsfc.nasa.gov/RtStpsServices"

        String target = "//" + host + "/RtStpsServices";
        if (args.length == 2)
        {
            target += "." + args[1];
        }
        
        int cycles = 0;

        try
        {
            RtStpsServices server = (RtStpsServices)java.rmi.Naming.lookup(target);

            while (true)
            {
                ++cycles;
                System.out.print("CYCLE=" + cycles);
                String cname = server.getConfigurationName();
                boolean running = server.isEnabled();
                if (running)
                {
                    System.out.print("  running ");
                    System.out.println(cname);
                }
                else if (cname != null)
                {
                    System.out.print("  loaded ");
                    System.out.println(cname);
                }
                else
                {
                    System.out.println("  idle");
                }

                /** Uncomment this section to use the StatusToString
				 * String[] status = server.getStatusToString();
				 *
				 * if (status != null) {
				 *	for (int i = 0; i < status.length; i++) {
				 *		System.out.println(status[i]);
				 *	}
				 * }
				 */
                
                // This is the original section and should work fine
                // for most users. If you uncomment the above
                // section, you should comment this one out
                //
                StatusBlock[] blocks = server.getStatus();
                if (blocks != null)
                {
                    for (int n = 0; n < blocks.length; n++)
                    {
                        StatusBlock sb = blocks[n];
                        StatusItem[] si = sb.getStatusItems();
                        System.out.println(sb);
                        for (int k = 0; k < si.length; k++)
                        {
                            System.out.println("    " + si[k]);
                        }
                    }
                }
                // End Origial Status Section

                Thread.sleep(DELAYMILLIS);
            }
        }
        catch (InterruptedException ie)
        {
            System.err.println(ie);
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
