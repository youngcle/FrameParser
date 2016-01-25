/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.commands;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;
import gov.nasa.gsfc.drl.rtstps.library.XAction;
import gov.nasa.gsfc.drl.rtstps.server.RtStpsServices;

/**
 * This class controls the primary RT-STPS button commands. It creates the
 * appropriate Action classes and links them so they enable or disable
 * themselves depending on the current command state. Not all XActions
 * may be defined here.
 * <p>
 * ButtonCommands is a StatusListener so that it can see external changes to
 * the command state and thereby change the enable/disable status of the
 * buttons. It should be attached to the Distributor to listen for state-
 * related StatusItems.
 * <p>
 * A client may register with ButtonCommands as a CommandStateListener. The
 * client will be notified of command state changes due to button presses.
 * It will NOT be notified of command state changes due to external events,
 * which is available from the Distributor as periodic StatusItems.
 * <p>
 * If a client needs command state change notification, it should register
 * instead as a CommandStateListener with CommandState, which will notify it
 * of both internal and external command state changes.
 *
 * 
 */
public class CommandButtons implements gov.nasa.gsfc.drl.rtstps.viewer.status.StatusListener
{
    private LocalLoadAction lload;
    private RemoteLoadAction rload;
    private GoAction go;
    private StopAction stop;
    private UnloadAction unload;
    private ExitAction exit;
    private ZeroAction zero;

    /** This is the last state I saw from the Distributor. It is not
     * necessarily the current button state. */
    private int currentState = CommandStateListener.UNLOADED;
    private boolean isLoaded = false;

    /**
     * Create the command buttons.
     */
    public CommandButtons(javax.swing.JFrame frame, RtStpsServices server,
            int initialCommandState)
    {
        lload = new LocalLoadAction(frame,server);
        rload = new RemoteLoadAction(frame,server);
        zero = new ZeroAction(frame,server);
        stop = new StopAction(frame,server);
        exit = new ExitAction(frame);
        go = new GoAction(frame,server);
        unload = new UnloadAction(frame,server);

        addCommandStateListener(lload);
        addCommandStateListener(rload);
        addCommandStateListener(unload);
        addCommandStateListener(go);
        addCommandStateListener(stop);

        setButtonState(initialCommandState);
    }

    public final XAction getLocalLoadAction()
    {
        return lload;
    }

    public final XAction getRemoteLoadAction()
    {
        return rload;
    }

    public final XAction getGoAction()
    {
        return go;
    }

    public final XAction getStopAction()
    {
        return stop;
    }

    public final XAction getUnloadAction()
    {
        return unload;
    }

    public final XAction getExitAction()
    {
        return exit;
    }

    public final XAction getZeroAction()
    {
        return zero;
    }

    public synchronized void addCommandStateListener(CommandStateListener csl)
    {
        /**
         * I don't track the command state myself because I don't need it.
         * Therefore, I rewire all listeners to the buttons that change
         * the command state directly.
         */
        lload.addCommandStateListener(csl);
        rload.addCommandStateListener(csl);
        unload.addCommandStateListener(csl);
        go.addCommandStateListener(csl);
        stop.addCommandStateListener(csl);
    }

    public synchronized void removeCommandStateListener(CommandStateListener csl)
    {
        lload.removeCommandStateListener(csl);
        rload.removeCommandStateListener(csl);
        unload.removeCommandStateListener(csl);
        go.removeCommandStateListener(csl);
        stop.removeCommandStateListener(csl);
    }

    /**
     * Process command state status items so that the button's enable/disable
     * state reflect the current command state.
     */
    public synchronized void processStatusItem(StatusItem item, String fullName)
    {
        /**
         * Command state status is contained in two separate status items.
         * I capture the load state, and I don't update the button state
         * until I get the second StatusItem, which is go/stop.
         */
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
            setButtonState(state);
        }
    }

    /**
     * Change the enable/disable status of the buttons depending on the
     * command state.
     */
    private void setButtonState(int state)
    {
        if (state != currentState)
        {
            currentState = state;
            lload.commandStateChange(state);
            rload.commandStateChange(state);
            stop.commandStateChange(state);
            go.commandStateChange(state);
            unload.commandStateChange(state);
        }
    }
}
