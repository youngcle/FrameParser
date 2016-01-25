/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.path;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

/**
 * This class is an action that causes a new status window to be opened.
 * The window shows all packet status for one application id.
 * <p>
 * I maintain a list of active windows by block name so as to avoid duplicate
 * windows. I become a window listener on each window so that I can remove
 * it from my list of open windows when it closes. It is the window's
 * responsibility to disconnect itself from the Distributor.
 * 
 * 
 */
public class PacketAction extends StatusWindowAction
{
    public PacketAction(javax.swing.JFrame frame, Distributor distributor)
    {
        super("Packet Status","packet",frame,distributor);
    }

    public final char getMnemonic()
    {
        return 'K';
    }

    public final javax.swing.KeyStroke getKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_K,InputEvent.CTRL_MASK);
    }

    public final String getToolTip()
    {
        return "Show all packet status for one application id.";
    }

    protected gov.nasa.gsfc.drl.rtstps.viewer.StatusWindow createStatusWindow(
            javax.swing.JFrame frame, String blockName, 
            Distributor distributor)
    {
        return new PacketStatus(frame,blockName,distributor);
    }
    
    private static final long serialVersionUID = 1L;			
}
