/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.server;

/**
 * This interface defines the methods a client like the viewer must use to
 * talk to the server.
 * 
 */
public interface RtStpsServices extends java.rmi.Remote
{
    /**
     * Set all clearable status counters to zero.
     */
    public void zeroStatus() throws java.rmi.RemoteException;

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
    public StatusBlock[] getStatus() throws java.rmi.RemoteException;

    /**
     * Precompute the status strings from the StatusBlocks
     */
    public String[] getStatusToString() throws java.rmi.RemoteException;
    
    /**
     * Load a configuration.
     */
    public void load(String configuration) throws java.rmi.RemoteException;

    /**
     * Load a configuration.
     */
    public void load(char[] configuration) throws java.rmi.RemoteException;

    /**
     * Load a configuration from a file that is local to the server.
     */
    public void loadFile(String file) throws java.rmi.RemoteException;

    /**
     * Enable or disable RLP data processing. This action does not reset the
     * board or change the configuration.
     */
    public void setEnabled(boolean enable) throws java.rmi.RemoteException;

    /**
     * Unload a configuration, which closes all open data files.
     */
    public void unload() throws java.rmi.RemoteException;

    /**
     * Get the name of the currently loaded configuration.
     * @return null if none is loaded.
     */
    public String getConfigurationName() throws java.rmi.RemoteException;

    /**
     * Is the server loaded and enabled?
     */
    public boolean isEnabled() throws java.rmi.RemoteException;

    /**
     * Stop the RT-STPS server.
     */
    public void stopServer() throws java.rmi.RemoteException;
}
