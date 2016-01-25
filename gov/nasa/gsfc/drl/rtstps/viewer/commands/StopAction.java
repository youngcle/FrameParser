/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.commands;
import gov.nasa.gsfc.drl.rtstps.server.RtStpsServices;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Handle the "stop" action.
 * 
 * 
 */
class StopAction extends AbstractCommandAction implements CommandStateListener
{
    StopAction(JFrame frame, RtStpsServices server)
    {
        super("Stop",frame,server);
        setEnabled(false);
    }

    public final char getMnemonic()
    {
        return 'S';
    }

    public final KeyStroke getKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK);
    }

    public final String getToolTip()
    {
        return "Stop the RT-STPS pipeline";
    }

    public void actionPerformed(ActionEvent e)
    {
        try
        {
            server.setEnabled(false);
            setEnabled(false);
            notify(CommandStateListener.LOADED_STOPPED);
        }
        catch (java.rmi.RemoteException ex)
        {
            JOptionPane.showMessageDialog(frame,ex.detail.getMessage(),
                    "Alert!",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void commandStateChange(int state)
    {
        setEnabled(state == CommandStateListener.LOADED_GO);
    }
    
    private static final long serialVersionUID = 1L;			
}
