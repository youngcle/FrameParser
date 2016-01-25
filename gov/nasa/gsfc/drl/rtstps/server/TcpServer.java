/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * This is an RT-STPS server that receives telemetry through a TCP/IP socket.
 * <p>
 * You may pass one argument when invoking, the server name. The full server
 * name then becomes "RtStpsServices."+name. The default full server name is
 * "RtStpsServices.A".
 * <p>
 * <b>The site.properties file is processed if it is present for configuring the log and
 * it will take precedence over some of these option, even if they are present.</b>
 * <p>
 * The server understands the following system properties. You set them when
 * you run the server using the -D attribute as in "-Dport=5000".
 * <br>
 * -Dconfig=xmlConfigFileName
 * <br>A configuration file to be used until overridden by a loaded one. The
 * default name is "default.xml".  The configuration file controls the packet output
 * side of RT-STPS.
 * <br>
 * -Dport=4935
 * <br>The port number that the server reads for telemetry data. The
 * default port is 4935.
 * <br>
 * -DbufferSizeKb=8
 * The amount of data I accumulate before processing. The default is 8 kb.
 * <br>
 * -Dsetup=configurationDirectory
 * <br>The directory where local configuration files are found. If provided,
 * all files must be within the directory tree.
 * <br>
 * -Dlog.stdout
 * <br>
 * If specified, log messages are written to the standard output.
 * <br/><b>note: this is overridden by the presence of a site.properties file</b>
 * <br>
 * -Dlog.file=<i>file</i>
 * <br>
 * If specified, log messages are written to <i>file</i>.
 * <br/><b>note: this is overridden by the presence of a site.properties file</b>
 * <br>
 * -Dlog.server=<i>host:port:tmpDir</i>
 * <br>
 * If specified, log messages are sent to the NSLS server at host:port
 * (eg. localhost:3500) and to the temporary directory tmpDir when the NSLS
 * server is unavailable.
 * <br/><b>note: this is overridden by the presence of a site.properties file</b>
 * 
 * 
 */
public class TcpServer extends AbstractServer
{
    private int inputBufferSize = 4 * 1024;
    private int inputPort = 4935;

    /**
     * If a data source connects to this server and there is no loaded
     * configuration, the server will automatically load one of the two
     * following configurations. One will be null. When a user loads a
     * new configuration, it also becomes the new automatic default.
     */
    private String defaultConfigurationFileName = null;
    private char[] defaultConfigurationBuffer = null;
    
    InputStream input = null;
    //Socket dataSocket = null;
    ServerSocket serverSocket = null;


    /**
     * This is the main entry point for the RT-STPS server.
     */
    public static void main(String args[])
    {
        String serverName = (args.length == 0)? "A" : args[0];
        String defaultConfig = System.getProperty("config","default.xml");
        int port = 0;
        int bufferSize = 0;
        String propertyName = null;

        try
        {
            //Get the port number system property.
            propertyName = "port";
            String x = System.getProperty(propertyName,"4935");
            port = Integer.valueOf(x).intValue();

            //Get the input buffer size.
            propertyName = "bufferSizeKb";
            x = System.getProperty(propertyName,"8");
            bufferSize = 1024 * Integer.valueOf(x).intValue();
        }
        catch (NumberFormatException nfe)
        {
            System.err.println(propertyName + " property is not an integer.");
            System.exit(-3);
        }

        try
        {
            LocateRegistry.createRegistry(1099);
        }
        catch (java.rmi.RemoteException rre)
        {
            System.err.println("Could not create/export the RMI registry.");
            rre.printStackTrace();
            System.exit(-5);
        }

        String security = System.getProperty("security","1");
        if (security.equals("1") && System.getSecurityManager() == null)
        {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }

        try
        {
            TcpServer server = new TcpServer(defaultConfig,port,bufferSize);
            java.rmi.Naming.rebind("RtStpsServices." + serverName, server);
            server.log("Ready to serve.");
        }
        catch (Exception ex)
        {
            System.err.println("SERVER ERROR. " + ex);
            ex.printStackTrace();
            System.exit(-2);
        }
    }

    /**
     * Create a server.
     */
    public TcpServer(String defaultConfig, int port, int bufferSize) throws RemoteException
    {
        defaultConfigurationFileName = defaultConfig;
        inputPort = port;
        inputBufferSize = bufferSize;

        /**
         * This thread reads the sockets and gives the buffers to the
         * FrameSynchronizer object (if it exists), which is the head of the
         * pipeline.
         */
        runner = new Thread(this);
        runner.setDaemon(true);
        runner.setPriority(Thread.MAX_PRIORITY);
        runner.start();
    }

    /**
     * Load a configuration.
     */
    public void load(char[] configuration) throws java.rmi.RemoteException
    {
        super.load(configuration);
        defaultConfigurationBuffer = configuration;
        defaultConfigurationFileName = null;
    }

    /**
     * Load a configuration from a file that is local to the server.
     */
    public void loadFile(String filename) throws java.rmi.RemoteException
    {
        super.loadFile(filename);
        defaultConfigurationFileName = filename;
        defaultConfigurationBuffer = null;
    }

    /**
     * This thread method gets data from the socket and sends it down the
     * pipeline if it exists. It discards the data if the pipeline is null.
     */
    public void run()
    {
        byte[] buffer = new byte[inputBufferSize];
        ServerSocket serverSocket = null;

        try
        {
            //The server socket listens for a data transmission client.
            serverSocket = new ServerSocket(inputPort,0);
        }
        catch (java.io.IOException se)
        {
            log("RT-STPS fatal error creating server socket. "+se.getMessage());
            
            System.exit(-1);
        }

        while (running)
        {
            Socket dataSocket = null;

            try
            {
                dataSocket = serverSocket.accept();
                input = dataSocket.getInputStream();
                input = new BufferedInputStream(input,inputBufferSize);
            }
            catch (java.io.IOException e)
            {
                log("Error reading server socket. "+ e.getMessage());
                
                System.exit(-2);
            }

            if (fs == null)
            {
                log("Auto-loading default configuration.");
                try
                {
                    if (defaultConfigurationFileName != null)
                    {
                        loadFile(defaultConfigurationFileName);
                    }
                    else
                    {
                        load(defaultConfigurationBuffer);
                    }
                    setEnabled(true);
                }
                catch (RemoteException ee)
                {
                    
                    System.exit(-4);
                }
            }

            int bytesRead = -1;
            int consecutiveErrors = 0;

		synchronized(readlock)
		{
		    do
		    {
			try
			{
			    if (input != null)
			    {
				bytesRead = input.read(buffer);
				consecutiveErrors = 0;
				if ((bytesRead > 0) && (fs != null))
				{
				    fs.putBuffer(buffer, bytesRead);
				}
			    }
			    else
			    {
				bytesRead = -1;
			    }
			}
			catch (java.io.IOException ior)
			{
			    log("RT-STPS error reading data socket. I will try again. "+ ior.getMessage());
			    ++consecutiveErrors;
			    bytesRead = (consecutiveErrors >= 4)? -1 : 0;
			}
			catch (gov.nasa.gsfc.drl.rtstps.core.RtStpsException se)
			{
			    log("Data read error. "+se.getMessage());
			    bytesRead = -1;
			}
		    }
		    while (running && (bytesRead >= 0));

		    try 
		    {
			if (input != null)
			{
			    input.close();
			    input = null;
			}
		    } 
		    catch (java.io.IOException e1) 
		    {
			e1.printStackTrace();
		    }
		    try 
		    {
			dataSocket.close();
		    } catch (java.io.IOException e2) {
			e2.printStackTrace();
		    }
		    try 
		    {
			if (fs != null) 
			{
			    fs.flush();
			} 
			else 
			{
			    System.out.println("Shutting down pipeline -- FS is already null, skipping fs.flush()");
			}
		    } 
		    catch (gov.nasa.gsfc.drl.rtstps.core.RtStpsException e3) 
		    {
			e3.printStackTrace();
		    }
		    state.unload();
		    fs = null;
		}
            log("I shut down the pipeline.");
        }

	try {
		serverSocket.close();
	} catch (java.io.IOException ssio) {
		ssio.printStackTrace();
	}
        log("I stop now.");
        
    }
    
    private static final long serialVersionUID = 1L;

    public void stopData() throws RemoteException
    {
	//System.out.println("stopData called.");
	try 
	{
	    //System.out.println("in try of stopData.");
	    if(input != null)
	    {
		//System.out.println("Closing input.");
	    	input.close();
	    	input = null;		
	    }
	    
	}
	catch (IOException e)
	{
	    System.out.println("Exception in stopData().");
	}
    }
    
}
