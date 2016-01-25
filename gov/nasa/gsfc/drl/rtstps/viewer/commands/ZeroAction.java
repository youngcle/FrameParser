/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.commands;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Handle the "zero status" action.
 * 
 * 
 */
class ZeroAction extends gov.nasa.gsfc.drl.rtstps.library.XAction
{
    /** The parent JFrame. I use it for error dialogs. */
    private javax.swing.JFrame frame;
    private gov.nasa.gsfc.drl.rtstps.server.RtStpsServices server;

    /**
     * Create a ZeroAction object.
     */
    ZeroAction(javax.swing.JFrame frame,
            gov.nasa.gsfc.drl.rtstps.server.RtStpsServices server)
    {
        super("Zero");
        this.frame = frame;
        this.server = server;
    }

    public final char getMnemonic()
    {
        return 'Z';
    }

    public final KeyStroke getKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_MASK);
    }

    public final String getToolTip()
    {
        return "Zero all status counters";
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        try
        {
            server.zeroStatus();
        }
        catch (java.rmi.RemoteException ex)
        {
            JOptionPane.showMessageDialog(frame,ex.detail.getMessage(),
                    "Alert!",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
