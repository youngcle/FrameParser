/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;
import gov.nasa.gsfc.drl.rtstps.library.XAction;
import gov.nasa.gsfc.drl.rtstps.viewer.commands.CommandButtons;
import gov.nasa.gsfc.drl.rtstps.viewer.path.PacketAction;
import gov.nasa.gsfc.drl.rtstps.viewer.path.PathServiceAction;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;
import gov.nasa.gsfc.drl.rtstps.viewer.tables.PacketStatusTableAction;
import gov.nasa.gsfc.drl.rtstps.viewer.tables.VcStatusTableAction;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * The RT-STPS Viewer's menubar.
 * 
 * 
 */
class Menubar extends javax.swing.JMenuBar
{
    Menubar(JFrame frame, Distributor distributor,
            CommandButtons commandButtons)
    {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        fileMenu.add(new MenuItem(commandButtons.getExitAction()));

        JMenu commandMenu = new JMenu("Commands");
        commandMenu.setMnemonic('C');
        commandMenu.add(new MenuItem(commandButtons.getLocalLoadAction(),
                "Local Load"));
        commandMenu.add(new MenuItem(commandButtons.getRemoteLoadAction(),
                "Remote Load"));
        commandMenu.add(new MenuItem(commandButtons.getGoAction()));
        commandMenu.add(new MenuItem(commandButtons.getStopAction()));
        commandMenu.add(new MenuItem(commandButtons.getUnloadAction()));
        commandMenu.add(new MenuItem(commandButtons.getZeroAction(),
                "Zero status"));

        JMenu statusMenu = new JMenu("Status");
        statusMenu.setMnemonic('S');

        XAction a = new PathServiceAction(frame,distributor);
        statusMenu.add(new MenuItem(a));

        a = new PacketAction(frame,distributor);
        statusMenu.add(new MenuItem(a));

        a = new VcStatusTableAction(frame,distributor);
        statusMenu.add(new MenuItem(a));

        a = new PacketStatusTableAction(frame,distributor);
        statusMenu.add(new MenuItem(a));

        add(fileMenu);
        add(commandMenu);
        add(statusMenu);
    }

    class MenuItem extends JMenuItem
    {
        MenuItem(XAction action)
        {
            super(action);
            setToolTipText(action.getToolTip());
            setMnemonic(action.getMnemonic());
            KeyStroke keystroke = action.getKeyStroke();
            if (keystroke != null) setAccelerator(action.getKeyStroke());
        }

        MenuItem(XAction action, String substituteText)
        {
            this(action);
            setText(substituteText);
        }
    
        private static final long serialVersionUID = 1L;		
    }
    
    private static final long serialVersionUID = 1L;			
}
