/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.commands;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Handle the "local load" action. "Local load" means the viewer loads a
 * configuration file that is local to the viewer. A "remote load" means
 * the viewer tells the server to load a file from its computer.
 * 
 * 
 */
class LocalLoadAction extends AbstractCommandAction implements
        CommandStateListener
{
    private LoadFileChooser loadFileChooser = null;

    LocalLoadAction(javax.swing.JFrame frame,
            gov.nasa.gsfc.drl.rtstps.server.RtStpsServices server)
    {
        super("Load",frame,server);
        setEnabled(true);
    }

    public final char getMnemonic()
    {
        return 'L';
    }

    public final KeyStroke getKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_MASK);
    }

    public final String getToolTip()
    {
        return "Load a local configuration into the RT-STPS server";
    }

    public void commandStateChange(int state)
    {
        setEnabled(state == CommandStateListener.UNLOADED);
    }

    public void actionPerformed(ActionEvent e)
    {
        /** disable so I don't get duplicates */
        setEnabled(false);

        if (loadFileChooser == null)
        {
            loadFileChooser = new LoadFileChooser();
        }
        else
        {
            loadFileChooser.rescanCurrentDirectory();
        }

        int x = loadFileChooser.showOpenDialog(frame);
        if (x == javax.swing.JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File file = loadFileChooser.getSelectedFile();
                int blen = (int)file.length();
                char[] cbuffer = new char[blen];
                BufferedReader r = new BufferedReader(new FileReader(file));
                int rlen = r.read(cbuffer,0,blen);
                if (blen != rlen) throw new IOException("Truncated file.");
                server.load(cbuffer);
                notify(CommandStateListener.LOADED_STOPPED);
            }
            catch (java.rmi.RemoteException ex)
            {
                String m = ex.detail.getMessage();
                if (m == null) m = ex.getMessage();
                JOptionPane.showMessageDialog(frame,m,
                        "Alert!",JOptionPane.ERROR_MESSAGE);
                setEnabled(true);
            }
            catch (IOException jioe)
            {
                JOptionPane.showMessageDialog(frame,jioe.getMessage(),
                        "Alert!",JOptionPane.ERROR_MESSAGE);
                setEnabled(true);
            }
        }
        else
        {
            setEnabled(true);
        }
    }

    class LoadFileChooser extends javax.swing.JFileChooser
    {
        LoadFileChooser()
        {
            String baseDirectory = System.getProperty("configDir",".");
            setCurrentDirectory(new File(baseDirectory));
            setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
            setDialogTitle("Load RT-STPS Configuration");
            setApproveButtonText("Load");
            setApproveButtonMnemonic('L');
            setApproveButtonToolTipText("Load a configuration");
            SimpleFilter f = new SimpleFilter();
            setFileFilter(f);
            //addChoosableFileFilter(f);
        }
    
        private static final long serialVersionUID = 1L;			
    }

    public static class SimpleFilter extends javax.swing.filechooser.FileFilter
    {
        /**
         * Determine if the filter accepts the prospective file.
         */
        public boolean accept(File f)
        {
            return (f != null) &&
                (f.isDirectory() || f.getName().endsWith(".xml"));
        }

        /**
         * Get the filter description.
         */
        public final String getDescription()
        {
            return "RT-STPS xml configuration files";
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
