/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.server;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.TextStatusItem;

/**
 * This class is the current server state: unloaded, loaded, stopped, or go.
 * Normally I would keep this information as a single state integer in the
 * server itself, but I also need to maintain the time of each event as well
 * as maintain a server status block.
 * 
 */
final class ServerState
{
    private StatusBlock statusBlock;
    private LongStatusItem clockItem;
    private LongStatusItem stopClockItem;
    private LongStatusItem goClockItem;
    private LongStatusItem loadClockItem;
    private LongStatusItem unloadClockItem;
    private TextStatusItem configurationItem;
    private TextStatusItem goItem;

    private static final int LOADED_GO = 2;
    private static final int LOADED_STOPPED = 1;
    private static final int UNLOADED = 0;
    private int state = UNLOADED;


    ServerState()
    {
        StatusItem[] blocks = new StatusItem[7];

        long now = System.currentTimeMillis();
        clockItem = new LongStatusItem("Clock",now);
        blocks[0] = clockItem;

        goClockItem = new LongStatusItem("Go Clock",now);
        blocks[1] = goClockItem;

        stopClockItem = new LongStatusItem("Stop Clock",now);
        blocks[2] = stopClockItem;

        loadClockItem = new LongStatusItem("Load Clock",now);
        blocks[3] = loadClockItem;

        unloadClockItem = new LongStatusItem("Unload Clock",now);
        blocks[4] = unloadClockItem;

        configurationItem = new TextStatusItem("Configuration");
        blocks[5] = configurationItem;

        goItem = new TextStatusItem("Running","no");
        blocks[6] = goItem;

        for (int n = 0; n < blocks.length; n++)
        {
            blocks[n].setClearable(false);
        }

        statusBlock = new StatusBlock("Server","State",blocks);
    }

    /**
     * Determine if the server is loaded with a configuration.
     */
    final boolean isLoaded()
    {
        return state != UNLOADED;
    }

    /**
     * Determine if the server is enabled to process data.
     */
    final boolean isEnabled()
    {
        return state == LOADED_GO;
    }

    /**
     * Get the status block that is associated with the server state.
     */
    final StatusBlock getStatusBlock()
    {
        return statusBlock;
    }

    /**
     * Set the current time status item to the current time.
     */
    final void advanceClock()
    {
        clockItem.value = System.currentTimeMillis();
    }

    /**
     * Set the server to the enabled state.
     */
    void go() throws java.rmi.RemoteException
    {
        if (state == LOADED_STOPPED)
        {
            state = LOADED_GO;
            goClockItem.value = System.currentTimeMillis();
            goItem.value = "yes";
        }
        else
        {
            throw new java.rmi.RemoteException("The RT-STPS server is not loaded.");
        }
    }

    /**
     * Set the server to the disabled state.
     */
    void stop() throws java.rmi.RemoteException
    {
        if (state == LOADED_GO)
        {
            state = LOADED_STOPPED;
            stopClockItem.value = System.currentTimeMillis();
            goItem.value = "no";
        }
        else
        {
            throw new java.rmi.RemoteException("The RT-STPS server processing pipeline is already shutdown.");
        }
    }

    /**
     * Change the server state so that it shows a loaded configuration.
     */
    void load(String configurationFileName) throws java.rmi.RemoteException
    {
        loadClockItem.value = System.currentTimeMillis();
        configurationItem.value = configurationFileName;
        state = LOADED_STOPPED;
    }

    /**
     * Set the server state to be unloaded.
     */
    void unload()
    {
        if (state == LOADED_GO)
        {
            stopClockItem.value = System.currentTimeMillis();
            goItem.value = "no";
        }
        state = UNLOADED;
        unloadClockItem.value = System.currentTimeMillis();
        configurationItem.value = null;
    }
}
