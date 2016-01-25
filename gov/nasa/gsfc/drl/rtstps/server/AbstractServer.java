/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.server;

import gov.nasa.gsfc.drl.rtstps.core.Builder;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsNode;
import gov.nasa.gsfc.drl.rtstps.core.fs.FrameSynchronizer;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

/**
 * This is an abstract RT-STPS server. It supports only one pipeline thread.
 * 
 * 
 */
public abstract class AbstractServer extends java.rmi.server.UnicastRemoteObject
        implements RtStpsServices, Runnable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected FrameSynchronizer fs = null;
    protected boolean running = true;
    protected Thread runner;
    protected StatusBlock[] statusBlocks = null;
    protected ServerState state;
	protected class ReadLock {
	    public ReadLock() {}
	}
    protected ReadLock readlock = new ReadLock();
    private Builder builder;
    private Logger logger;
    private String xmlDirectory;

    /**
     * Create an RT-STPS server. This class contains core server functions.
     * Note that it now processes the optional site.properties file in setting
     * up the logger.
     */
    protected AbstractServer() throws RemoteException
    {
        super();

        System.out.println(gov.nasa.gsfc.drl.rtstps.Version.getVersion());

        //Identify the configuration file directory.
        xmlDirectory = System.getProperty("setup","config");

        //The logger records all events.
        logger = new Logger();

        //The server state tracks load/unloaded and stop/go.
        state = new ServerState();

        //The builder creates pipelines.
        try
        {
            builder = new Builder();
        }
        catch (gov.nasa.gsfc.drl.rtstps.core.RtStpsException stpse)
        {
            stpse.printStackTrace();
            System.exit(0);
        }

        //This class/thread captures text commands from port 5935 and calls
        //this server's load and unload methods.
        /*ProxyThread proxy = */ new ProxyThread(this,5935);		
    }

    /**
     * Log a message.
     */
    public final void log(String message)
    {
        logger.print(message);
    }

    /**
     * Close the log.
     */
    /*									
    public final void closeLog()
    {
        logger.close();
    }
    */

    /**
     * Get the name of the currently loaded configuration.
     * @return null if none is loaded.
     */
    public synchronized String getConfigurationName() throws RemoteException
    {
        String name = null;
        if (state.isLoaded())
        {
            name = builder.getConfiguration().getName();
        }
        return name;
    }

    /**
     * Is the server loaded and enabled?
     */
    public boolean isEnabled() throws RemoteException
    {
        return state.isEnabled();
    }

    /**
     * Enable or disable data processing.
     */
    public synchronized void setEnabled(boolean enable) throws RemoteException
    {
        if (enable)
        {
            log("Go");
            state.go();
        }
        else
        {
            log("Stop");
            state.stop();
        }
    }

    /**
     * Unload a configuration.
     */
    public synchronized void unload() throws RemoteException
    {	
	if (state.isLoaded())
	{
	    stopData();
	    //System.out.println("After stopData().");
	    synchronized(readlock) 
	    {
		//System.out.println("Within synchronized loop in unload.");
		log("Unload " + getConfigurationName());
		try
		{ 
		    state.unload();
		    // Shutdown the FrameSynchronizer if it's still open and running:
		    if(fs != null)
		    	fs.shutdown();
		}
		catch (gov.nasa.gsfc.drl.rtstps.core.RtStpsException stpse)
		{
		    log("Unload error " + stpse.getMessage());
		    //throw new RemoteException("RT-STPS unload error",stpse);
		    try {
				H5.H5close();
			} catch (HDF5LibraryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		finally
		{
		    fs = null;
		}
	    }
	}
	else
	{
	    log("Unload commanded, but the server is not loaded.");
	}
    }
    
    public abstract void stopData() throws RemoteException;
  

    /**
     * Load a configuration.
     */
    public synchronized void load(String configuration) throws RemoteException
    {
        log("Load configuration as a string");
        build(new java.io.StringReader(configuration));
    }

    /**
     * Load a configuration.
     */
    public synchronized void load(char[] configuration) throws RemoteException
    {
        log("Load configuration (from Viewer) as a character array");
        build(new java.io.CharArrayReader(configuration));
    }

    /**
     * Load a configuration from a file that is local to the server.
     */
    public synchronized void loadFile(String filename) throws RemoteException
    {
        log("Load " + filename);
        File setupFile = new File(xmlDirectory,filename);
        BufferedReader r = null;
        try
        {
            r = new BufferedReader(new FileReader(setupFile));
        }
        catch (java.io.FileNotFoundException fnfe)
        {
            log("Load error " + fnfe.getMessage());
            throw new RemoteException("RT-STPS load file error",fnfe);
        }
        build(r);
    }

    /**
     * Build a pipeline from a configuration.
     */
    private void build(java.io.Reader r) throws RemoteException
    {
        //The server must be unloaded first.
        if (state.isLoaded())
        {
            unload();
        }

        try
        {
            //The builder constructs a pipeline from the configuration.
            org.xml.sax.InputSource setup = new org.xml.sax.InputSource(r);
            String root = System.getProperty("root","file://localhost/");
            if (!root.endsWith("/")) root += "/";
            setup.setSystemId(root);
            fs = builder.create(setup);
            r.close();

            //I count the number of status blocks.
            TreeMap<String, RtStpsNode> nodes = builder.getConfiguration().getStpsNodes();
            Iterator<RtStpsNode> i = nodes.values().iterator();
            int count = 1;  //1 for the server status
            while (i.hasNext())
            {
                RtStpsNode node = (RtStpsNode)i.next();
                if (node.getStatusItems() != null) ++count;
            }

            //I create a status block array. The server state is first.
            statusBlocks = new StatusBlock[count];
            statusBlocks[0] = state.getStatusBlock();

            //I put all status blocks into the array.
            count = 1;
            i = nodes.values().iterator();
            while (i.hasNext())
            {
                RtStpsNode node = (RtStpsNode)i.next();
                if (node.getStatusItems() != null)
                {
                    statusBlocks[count++] = new StatusBlock(node);
                }
            }

            //Update the state.
            state.load(builder.getConfiguration().getName());
        }
        catch (gov.nasa.gsfc.drl.rtstps.core.RtStpsException stpse)
        {
            log("Load error " + stpse.getMessage());
            stpse.printStackTrace();
            throw new RemoteException("RT-STPS load error",stpse);
        }
        catch (java.io.IOException ioe)
        {
            log("Load error " + ioe.getMessage());
            throw new RemoteException("RT-STPS load error",ioe);
        }
    }

    /**
     * Stop the RT-STPS server.
     */
    public void stopServer() throws RemoteException
    {
        running = false;
        try { runner.join(3000L); }
        catch(InterruptedException ie1) {  }
        log("Someone stopped the server.");
        System.exit(0);
    }

    /**
     * Set all clearable status counters to zero.
     */
    public synchronized void zeroStatus() throws RemoteException
    {
        if (statusBlocks != null)
        {
            for (int n = 0; n < statusBlocks.length; n++)
            {
                statusBlocks[n].clear();
            }
            log("Zero status counts.");
        }
    }

    /**
     * Get the current status from the last-used pipeline. Within a session,
     * the number and identity of the status blocks and the number and
     * identity of the contained status items will not change. Only the
     * status item values may change. However, when a session changes and
     * a new configuration is loaded, then all of these elements will change.
     * @return If no configuration has been loaded into the server, then
     * this method returns null. Otherwise it returns the status of the
     * last created pipeline, even post-session.
     */
    public synchronized StatusBlock[] getStatus() throws RemoteException
    {
        state.advanceClock();
        return statusBlocks;
    }

    /**
     * Get the current status pre-bundled as a String[]
     */
    public synchronized String[] getStatusToString() throws RemoteException 
    {
		state.advanceClock();

		if (statusBlocks == null)
			return null;

		ArrayList<String> status = new ArrayList<String>();

		int statusIndex = 0;
		for (int n = 0; n < statusBlocks.length; n++) {
			StatusBlock sb = statusBlocks[n];
			StatusItem[] si = sb.getStatusItems();

			status.add(statusIndex, sb.toString());
			statusIndex += 1;

			for (int k = 0; k < si.length; k++) {
				status.add(statusIndex, si[k].toString());
				statusIndex += 1;
			}
		}

		String returnStr[] = new String[status.size()];
		for (int i = 0; i < status.size(); i++) {
			returnStr[i] = status.get(i);
		}

		return (returnStr);
	}

    /**
     * This thread method gets data from the input socket and sends it down the
     * pipeline if it exists. It discards the data if the pipeline is null.
     */
    public abstract void run();
}
