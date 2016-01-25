/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;
import gov.nasa.gsfc.drl.rtstps.library.layout.RowLayout;
import gov.nasa.gsfc.drl.rtstps.viewer.bulbs.DataLight;
import gov.nasa.gsfc.drl.rtstps.viewer.bulbs.ReadyLight;
import gov.nasa.gsfc.drl.rtstps.viewer.commands.CommandState;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import java.awt.Font;

import javax.swing.JLabel;

/**
 * This class manages the status lines at the bottom of the screen.
 * 
 * 
 */
final class StatusLine extends javax.swing.JPanel
{
    private static final Font FONT = new Font("Sans Serif",Font.BOLD,10);
    private static final java.awt.Color TEXTCOLOR = java.awt.Color.black;

    StatusLine(Distributor distributor, CommandState commandState)
    {
        /** Show the currently loaded configuration. */
        ConfigurationComponent config = new ConfigurationComponent();
        distributor.requestStatusItemDelivery(config,
                "Server.State.Configuration");

        /** Show the current time. */
        ClockComponent time = new ClockComponent();
        time.setFont(FONT);
        time.setForeground(TEXTCOLOR);
        distributor.requestStatusItemDelivery(time,"Server.State.Clock");

        JLabel goLabel = new JLabel("Go: ");
        goLabel.setFont(FONT);
        goLabel.setForeground(TEXTCOLOR);

        /** The "go clock" shows when the server was last enabled. */
        ClockComponent goClock = new ClockComponent();
        goClock.setFont(FONT);
        goClock.setForeground(TEXTCOLOR);
        distributor.requestStatusItemDelivery(goClock,
                "Server.State.Go Clock");

        JLabel stopLabel = new JLabel("Stop: ");
        stopLabel.setFont(FONT);
        stopLabel.setForeground(TEXTCOLOR);

        /** The "stop clock" shows when the server was last disabled. */
        ClockComponent stopClock = new ClockComponent();
        stopClock.setFont(FONT);
        stopClock.setForeground(TEXTCOLOR);
        distributor.requestStatusItemDelivery(stopClock,
                "Server.State.Stop Clock");

        ReadyLight readyLight = new ReadyLight(FONT);
        commandState.addCommandStateListener(readyLight);

        DataLight dataLight = new DataLight(FONT);
        distributor.requestStatusItemDelivery(dataLight,
                DataLight.STATUSITEM_1);
        distributor.requestStatusItemDelivery(dataLight,
                DataLight.STATUSITEM_2);

        RowLayout layout = new RowLayout(2);
        //layout.setInsets(new java.awt.Insets(0,4,0,4));
        setLayout(layout);
        RowLayout.Constraint lc = new RowLayout.Constraint();

        /** I want the configuration to grab all room on the first row. */
        lc.set(0,1f); add(config,lc);
        lc.set(0,0f); add(time,lc);
        lc.set(0,0f); add(javax.swing.Box.createHorizontalStrut(18),lc);

        /**
         * In row 2, the two glues will grab most of the space. I want the
         * two times to be near each other, so I will separate them with
         * a minimum pixel strut that can grow like glue but at a smaller
         * weight.
         */
        lc.set(1,0f); add(readyLight,lc);
        lc.set(1,1f); add(javax.swing.Box.createHorizontalGlue(),lc);
        lc.set(1,0f); add(goLabel,lc);
        lc.set(1,0f); add(goClock,lc);
        lc.set(1,0.2f); add(javax.swing.Box.createHorizontalStrut(8),lc);
        lc.set(1,0f); add(stopLabel,lc);
        lc.set(1,0f); add(stopClock,lc);
        lc.set(1,1f); add(javax.swing.Box.createHorizontalGlue(),lc);
        lc.set(1,0f); add(dataLight,lc);
    }

    /**
     * This class shows the currently loaded configuration.
     */
    class ConfigurationComponent extends JLabel implements
            gov.nasa.gsfc.drl.rtstps.viewer.status.StatusListener
    {
        ConfigurationComponent()
        {
            setForeground(TEXTCOLOR);
            setFont(FONT);
            setHorizontalAlignment(JLabel.CENTER);
        }

        public synchronized void processStatusItem(gov.nasa.gsfc.drl.rtstps.core.status.StatusItem item,
                String fullName)
        {
            final String value = item.getValue();
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    setText(value);
                }
            } );
        }
    
        private static final long serialVersionUID = 1L;		
    }
    
    private static final long serialVersionUID = 1L;			
}
