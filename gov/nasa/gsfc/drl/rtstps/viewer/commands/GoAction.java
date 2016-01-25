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
 * Handle the "go" action.
 * 
 * 
 */
class GoAction extends AbstractCommandAction implements CommandStateListener
{
    GoAction(JFrame frame, RtStpsServices server)
    {
        super("Go",frame,server);
        setEnabled(false);
    }

    public final char getMnemonic()
    {
        return 'G';
    }

    public final KeyStroke getKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_G,InputEvent.CTRL_MASK);
    }

    public final String getToolTip()
    {
        return "Enable the RT-STPS pipeline";
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        try
        {
            server.setEnabled(true);
            setEnabled(false);
            notify(CommandStateListener.LOADED_GO);
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
