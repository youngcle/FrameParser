/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;
import gov.nasa.gsfc.drl.rtstps.server.RtStpsServices;
import gov.nasa.gsfc.drl.rtstps.viewer.commands.CommandButtons;
import gov.nasa.gsfc.drl.rtstps.viewer.commands.CommandState;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * This is the main class for the RT-STPS Viewer.
 * 
 * 
 */
public final class Viewer extends javax.swing.JFrame
{
    public static void main(String[] args)
    {
        String host = (args.length > 0)? args[0] : "localhost";
        String server = (args.length > 1)? args[1] : null;
        new Viewer(host,server);
    }

    public Viewer(String host, String serverName)
    {
        String title = System.getProperty("title","RT-STPS Viewer " + host);
        setTitle(title);

        /**
         * I hook up via RMI to the RLP server. Its full RMI URL will look
         * something like this: "rmi://host/RtStpsServices".
         */
        String target = "//" + host + "/RtStpsServices";
        if (serverName != null)
        {
            target += "." + serverName;
        }
        RtStpsServices server = null;
        try
        {
            server = (RtStpsServices)java.rmi.Naming.lookup(target);
        }
        catch (Exception ex)
        {
            System.err.println("Lookup error: " + target);
            System.exit(100);
        }

        /**
         * This is the icon used when this program is iconified.
         */
        ImageIcon icon;							
        try {
	  icon = new ImageIcon(getClass().getResource("/images/globe.gif"));
        } catch (Exception e) {
	  icon = new ImageIcon("images/globe.gif");
        }
        icon.setDescription("RT-STPS Viewer");
        setIconImage(icon.getImage());

        /**
         * This class monitors the current server command state: unloaded,
         * loaded and stopped, or loaded and go.
         */
        CommandState commandState = null;
        try
        {
            commandState = new CommandState(server);
        }
        catch (java.rmi.RemoteException rmie)
        {
            rmie.printStackTrace();
            System.exit(101);
        }

        /**
         * The distributor collects status from the server and distributes it
         * to window gadgets. It is a distinct thread.
         */
        Distributor distributor = new Distributor(server);

        /**
         * CommandState needs to know about external changes to the command
         * state, such as someone else issuing commands or the server itself
         * changing its state. That information is bundled in status.
         */
        distributor.requestStatusItemDelivery(commandState,
                "Server.State.Configuration");
        distributor.requestStatusItemDelivery(commandState,
                "Server.State.Running");

        /**
         * CommandButtons creates most of the actions, which translate to
         * buttons and menu items. It also ensures that they maintain a
         * consistent enabled/disabled state.
         */
        CommandButtons commandButtons = new CommandButtons(this,server,
            commandState.getValue());

        /**
         * CommandButtons must also know about external state changes so that
         * it can update the enable/disable state of its controls.
         */
        distributor.requestStatusItemDelivery(commandButtons,
                "Server.State.Configuration");
        distributor.requestStatusItemDelivery(commandButtons,
                "Server.State.Running");

        /**
         * CommandState attaches to CommandButtons so that it can get
         * instantaneous changes to the command state via button pushes.
         */
        commandButtons.addCommandStateListener(commandState);

        /**
         * My menubar.
         */
        setJMenuBar(new Menubar(this,distributor,commandButtons));

        java.awt.Container cp = getContentPane();

        /**
         * This is my toolbar, which has the primary commands.
         */
        cp.add(new Toolbar(commandButtons),BorderLayout.NORTH);

        JPanel area = new JPanel(new BorderLayout(6,6));
        area.setBorder(BorderFactory.createEmptyBorder(12,12,11,11));
        cp.add(area,BorderLayout.CENTER);

        area.add(new FramePanelA(distributor,14,"Frames"),BorderLayout.WEST);
        area.add(new FramePanelB(distributor,14,"Frames"),BorderLayout.EAST);
        area.add(new StatusLine(distributor,commandState),BorderLayout.SOUTH);

        pack();

        Dimension size = getSize();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (screen.width-size.width)/2;
        int h = (screen.height-size.height)/2;
        setLocation(w,h);

        final gov.nasa.gsfc.drl.rtstps.library.XAction exita = commandButtons.getExitAction();
        addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent ev) {
            exita.actionPerformed(null); }
          });

        distributor.start();

        setVisible(true);
    }
    
    private static final long serialVersionUID = 1L;			
}
