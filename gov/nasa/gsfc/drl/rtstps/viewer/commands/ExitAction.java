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
 * Handle the "exit" action.
 * 
 * 
 */
class ExitAction extends gov.nasa.gsfc.drl.rtstps.library.XAction
{
    /** The parent JFrame. I use it for error dialogs. */
    private javax.swing.JFrame frame;

    ExitAction(javax.swing.JFrame frame)
    {
        super("Exit");
        this.frame = frame;
    }

    public final char getMnemonic()
    {
        return 'X';
    }

    public final KeyStroke getKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_X,InputEvent.CTRL_MASK);
    }

    public final String getToolTip()
    {
        return "Exit the viewer";					
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        setEnabled(false);
        int x = JOptionPane.showConfirmDialog(frame,
                "Do you REALLY want to EXIT?",
                 "Information",JOptionPane.YES_NO_OPTION);

        if (x == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
        else
        {
            setEnabled(true);
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
