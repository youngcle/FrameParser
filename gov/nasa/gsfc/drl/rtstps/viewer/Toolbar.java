/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;
import gov.nasa.gsfc.drl.rtstps.library.XAction;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * The RT-STPS Viewer's toolbar.
 * 
 * 
 */
class Toolbar extends javax.swing.JToolBar
{
    private static final Color BUTTONBG = new Color(225,225,225);
    private static final Color BUTTONFG = Color.black;

    /**
     * Create the toolbar.
     */
    Toolbar(gov.nasa.gsfc.drl.rtstps.viewer.commands.CommandButtons commandButtons)
    {
        super("RT-STPS Viewer",HORIZONTAL);

        /** I use the earth image as a toolbar separator. */
        ImageIcon earthIcon;						
        try {
          earthIcon = new ImageIcon(getClass().getResource("/images/earth.gif"));
        } catch (Exception e) {
          earthIcon = new ImageIcon("images/earth.gif");
        }
        JLabel earth = new JLabel(earthIcon);
        earth.setOpaque(true);
        earth.setBackground(Color.black);

        /**
         * For appearance reasons, I want all buttons to be the same size.
         * To ensure this, I create a dummy button of the largest size that
         * I expect, and I give it the same attributes as real buttons.
         * I then save its preferred size, which I will later force onto
         * the real buttons. I also set the preferred height to be the same
         * as the earth image icon.
         */
        JButton temp = new JButton("UNLOAD");
        temp.setBorder(BorderFactory.createRaisedBevelBorder());
        Dimension ps = new Dimension(temp.getPreferredSize());
        ps.height = earth.getPreferredSize().height;
        temp = null;  //emphasizing that I don't need the temp button.

        /**
         * I want the earth icon to be the same preferred size as the
         * buttons.
         */
        earth.setPreferredSize(ps);
        earth.setMaximumSize(ps);

        add(new ToolbarButton(commandButtons.getLocalLoadAction(),ps));
        add(new ToolbarButton(commandButtons.getGoAction(),ps));
        add(new ToolbarButton(commandButtons.getStopAction(),ps));
        add(new ToolbarButton(commandButtons.getUnloadAction(),ps));
        add(earth);
        add(new ToolbarButton(commandButtons.getZeroAction(),ps));
        add(new ToolbarButton(commandButtons.getExitAction(),ps));
        add(Box.createHorizontalGlue());
    }

    /**
     * This is a toolbar button.
     */
    class ToolbarButton extends javax.swing.JButton
    {
        ToolbarButton(XAction action, Dimension size)
        {
            super(action);
            setToolTipText(action.getToolTip());
            setMnemonic(action.getMnemonic());
            setBackground(BUTTONBG);
            setForeground(BUTTONFG);
            setPreferredSize(size);
            setMaximumSize(size);
            setBorder(BorderFactory.createRaisedBevelBorder());
        }
    
        private static final long serialVersionUID = 1L;	
    }
    
    private static final long serialVersionUID = 1L;			
}
