/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.commands;
import gov.nasa.gsfc.drl.rtstps.server.RtStpsServices;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Handle the "unload" action.
 * 
 * 
 */
class UnloadAction extends AbstractCommandAction
        implements CommandStateListener
{
    UnloadAction(JFrame frame, RtStpsServices server)
    {
        super("Unload",frame,server);
        setEnabled(false);
    }

    public final char getMnemonic()
    {
        return 'U';
    }

    public final KeyStroke getKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_U,InputEvent.CTRL_MASK);
    }

    public final String getToolTip()
    {
        return "Close output files and sockets";
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        try
        {
            server.unload();
            setEnabled(false);
            notify(CommandStateListener.UNLOADED);
        }
        catch (java.rmi.RemoteException ex)
        {
            JOptionPane.showMessageDialog(frame,ex.detail.getMessage(),
                    "Alert!",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void commandStateChange(int state)
    {
        setEnabled(state == CommandStateListener.LOADED_STOPPED);
    }
    
    private static final long serialVersionUID = 1L;			
}
