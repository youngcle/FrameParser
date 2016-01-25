/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.commands;
import gov.nasa.gsfc.drl.rtstps.server.RtStpsServices;

import java.util.Iterator;

import javax.swing.JFrame;

/**
 * A special abstract action for go, stop, load, and unload. It sends messages
 * to registered CommandStateListeners when its action is triggered.
 * 
 * 
 */
abstract class AbstractCommandAction extends gov.nasa.gsfc.drl.rtstps.library.XAction
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The parent JFrame. I use it for error dialogs. */
    protected JFrame frame;
    protected RtStpsServices server;
    private java.util.LinkedList<CommandStateListener> listeners = new java.util.LinkedList<CommandStateListener>();	

    /**
     * Create an AbstractCommandAction.
     */
    protected AbstractCommandAction(String text, JFrame frame,
            RtStpsServices server)
    {
        super(text);
        this.frame = frame;
        this.server = server;
    }

    /**
     * Create an AbstractCommandAction with an icon.
     */
    protected AbstractCommandAction(String text, JFrame frame,
            RtStpsServices server, javax.swing.Icon icon)
    {
        super(text,icon);
        this.frame = frame;
        this.server = server;
    }

    /**
     * Notify all listeners of a command state change.
     */
    protected final void notify(int state)
    {
        Iterator<CommandStateListener> i = listeners.iterator();
        while (i.hasNext())
        {
            CommandStateListener csl = (CommandStateListener)i.next();
            csl.commandStateChange(state);
        }
    }

    /**
     * Add a command state listener.
     */
    void addCommandStateListener(CommandStateListener csl)
    {
        listeners.add(csl);
    }

    /**
     * Remove a command state listener.
     */
    void removeCommandStateListener(CommandStateListener csl)
    {
        listeners.remove(csl);
    }
}
