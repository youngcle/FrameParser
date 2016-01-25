/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.commands;

import gov.nasa.gsfc.drl.rtstps.library.AComboModel;
import gov.nasa.gsfc.drl.rtstps.library.AListModel;
import gov.nasa.gsfc.drl.rtstps.library.DialogShell;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Handle the "remote load" action. "Remote load" means the Viewer sends a
 * file name to the server, who then loads it from its setup directory.
 * 
 * 
 */
class RemoteLoadAction extends AbstractCommandAction implements
        CommandStateListener
{
    private DialogShell dialog = null;
    private MyPanel prompter = null;

    RemoteLoadAction(javax.swing.JFrame frame,
            gov.nasa.gsfc.drl.rtstps.server.RtStpsServices server)
    {
        super("RLoad",frame,server);
        setEnabled(true);
    }

    public final char getMnemonic()
    {
        return 'R';
    }

    public final KeyStroke getKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK);
    }

    public final String getToolTip()
    {
        return "Tell the RT-STPS server to load a configuration";
    }

    public void commandStateChange(int state)
    {
        setEnabled(state == CommandStateListener.UNLOADED);
    }

    public void actionPerformed(ActionEvent e)
    {
        setEnabled(false);
        if (dialog == null)
        {
            prompter = new MyPanel();
            dialog = new DialogShell(frame,prompter,"Remote Configuration");
            dialog.setVerifier(prompter);
        }

        dialog.setLocationRelativeTo(frame);
        /*dialog.show();*/ dialog.setVisible(true);			

        if (dialog.pressedOk())
        {
            try
            {
                server.loadFile(prompter.getSelection());
                notify(CommandStateListener.LOADED_STOPPED);
            }
            catch (java.rmi.RemoteException ex)
            {
                JOptionPane.showMessageDialog(frame,ex.detail.getMessage(),
                        "Alert!",JOptionPane.ERROR_MESSAGE);
                setEnabled(true);
            }
        }
    }

    class MyPanel extends javax.swing.JPanel implements DialogShell.Verifier
    {
        private AComboModel amodel;

        MyPanel()
        {
            setLayout(new java.awt.BorderLayout());
            JLabel label = new JLabel("Enter a configuration file that is on the server.");
            add(label,"North");
            amodel = new AComboModel(new AListModel());
            JComboBox box = new JComboBox(amodel);
            box.setEditable(true);
            add(box,"Center");
        }

        public void verify() throws Exception
        {
            String x = (String)amodel.getSelectedItem();
            if (x == null)
            {
                throw new Exception("No selected configuration file");
            }
            amodel.add(x);
        }

        String getSelection()
        {
            return (String)amodel.getSelectedItem();
        }
    
        private static final long serialVersionUID = 1L;			
    }
    
    private static final long serialVersionUID = 1L;			
}
