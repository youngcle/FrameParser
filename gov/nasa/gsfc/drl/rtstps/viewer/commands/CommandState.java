/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.commands;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;
import gov.nasa.gsfc.drl.rtstps.viewer.status.StatusListener;

import java.util.Iterator;

/**
 * This class represents the command state of the RT-STPS server: unloaded,
 * loaded and stopped, or loaded and go (running). Clients may register
 * as CommandStateListeners to receive the current command state. CommandState
 * will immediately send the current state to a registering listener.
 * <p>
 * This is the best place to get the current command state. You could also
 * get command state from the Distributor as a StatusItem or from
 * CommandButtons, but each has deficiencies. CommandButtons will only notify
 * you of button presses but won't tell you about external changes to the
 * command state. Distributor will tell you about external state changes but
 * only on a periodic interval, so there can be a noticeable delay until you
 * get a state change event. This class, CommandState, listens to both sources
 * and provides a single source for command state.
 *
 * 
 * 
 */
public class CommandState implements StatusListener, CommandStateListener
{
    private int currentState = CommandStateListener.UNLOADED;
    private boolean isLoaded = false;
    private java.util.LinkedList<CommandStateListener> listeners = new java.util.LinkedList<CommandStateListener>();

    /**
     * Create a CommandState object.
     */
    public CommandState(gov.nasa.gsfc.drl.rtstps.server.RtStpsServices server)
            throws java.rmi.RemoteException
    {
        if (server.isEnabled())
        {
            currentState = CommandStateListener.LOADED_GO;
        }
        else if (server.getConfigurationName() != null)
        {
            currentState = CommandStateListener.LOADED_STOPPED;
        }
        else
        {
            currentState = CommandStateListener.UNLOADED;
        }

        isLoaded = currentState != CommandStateListener.UNLOADED;
    }

    /**
     * Get the current state.
     */
    public final int getValue()
    {
        return currentState;
    }

    /**
     * As a CommandStateListener, this class processes command state changes
     * by sending state changes to its listeners.
     */
    public synchronized void commandStateChange(int state)
    {
        if (state != currentState)
        {
            currentState = state;
            Iterator<CommandStateListener> i = listeners.iterator();
            while (i.hasNext())
            {
                CommandStateListener csl = (CommandStateListener)i.next();
                csl.commandStateChange(currentState);
            }
        }
    }

    /**
     * Add a command state change listener.
     */
    public synchronized void addCommandStateListener(CommandStateListener csl)
    {
        csl.commandStateChange(currentState);
        listeners.add(csl);
    }

    /**
     * Remove a command state change listener.
     */
    public synchronized void removeCommandStateListener(CommandStateListener csl)
    {
        listeners.remove(csl);
    }

    /**
     * As a status listener, this class listens for externally-caused
     * state changes.
     */
    public synchronized void processStatusItem(StatusItem item, String fullName)
    {
        if (fullName.equals("Server.State.Configuration"))
        {
            isLoaded = item.getValue() != null;
        }
        else if (fullName.equals("Server.State.Running"))
        {
            int state;

            if (item.getValue().equals("yes"))
            {
                state = CommandStateListener.LOADED_GO;
            }
            else if (isLoaded)
            {
                state = CommandStateListener.LOADED_STOPPED;
            }
            else
            {
                state = CommandStateListener.UNLOADED;
            }

            commandStateChange(state);
        }
    }
}
