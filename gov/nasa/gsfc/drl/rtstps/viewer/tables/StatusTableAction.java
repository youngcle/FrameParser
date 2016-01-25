/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import javax.swing.JOptionPane;

/**
 * This action lets the user create a status table window.
 * 
 * 
 */
public abstract class StatusTableAction extends gov.nasa.gsfc.drl.rtstps.library.XAction
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private javax.swing.JFrame frame;
    private Distributor distributor;
    private java.util.TreeSet<String> activeWindows = new java.util.TreeSet<String>();	
    private char mnemonic;
    private int keystroke;
    private String tooltip = null;

    /**
     * Create the action.
     */
    protected StatusTableAction(String title, javax.swing.JFrame frame,
            Distributor distributor)
    {
        super(title);
        this.frame = frame;
        this.distributor = distributor;
    }

    public final void setMnemonic(char m)
    {
        mnemonic = m;
    }

    public final char getMnemonic()
    {
        return mnemonic;
    }

    public final void setKeyStroke(int key)
    {
        keystroke = key;
    }

    public final javax.swing.KeyStroke getKeyStroke()
    {
        return javax.swing.KeyStroke.getKeyStroke(keystroke,
                java.awt.event.InputEvent.CTRL_MASK);
    }

    public final void setToolTip(String tip)
    {
        tooltip = tip;
    }

    public final String getToolTip()
    {
        return tooltip;
    }

    /**
     * Get a list of block names from the Distributor.
     */
    protected java.util.List<String> getBlockList(String type)		
    {
        java.util.List<String> list = null;				
        try
        {
            list = distributor.getBlockNamesByType(type);
            if ((list == null) || (list.size() == 0))
            {
                JOptionPane.showMessageDialog(frame,
                        "No " + type + " status is available.",
                        "Alert!",JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (java.rmi.RemoteException ex)
        {
            JOptionPane.showMessageDialog(frame,ex.detail.getMessage(),
                    "Alert!",JOptionPane.ERROR_MESSAGE);
        }

        return list;
    }

    /**
     * Get a list of block names from the Distributor.
     */
    protected java.util.List<String> getBlockList(String[] typeNames)	
    {
        java.util.List<String> list = null;				
        try
        {
            list = distributor.getBlockNamesByTypes(typeNames);
            if ((list == null) || (list.size() == 0))
            {
                JOptionPane.showMessageDialog(frame,
                        "No " + getText() + " status is available.",
                        "Alert!",JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (java.rmi.RemoteException ex)
        {
            JOptionPane.showMessageDialog(frame,ex.detail.getMessage(),
                    "Alert!",JOptionPane.ERROR_MESSAGE);
        }

        return list;
    }

    abstract protected java.util.List<String> getBlockList();

    abstract protected StatusTableModel createStatusTableModel(Distributor d);

    /**
     * Ask the user to select blocks to display.
     */
    private Object[] pickBlocks(java.util.List<String> blockNames)	
    {
        Object[] array = null;
        PickerPanel pp = new PickerPanel(blockNames);

        int v = JOptionPane.showConfirmDialog(frame,pp,
                "Select " + getText() + " Streams",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (v == JOptionPane.OK_OPTION)
        {
            array = pp.getSelections();
        }

        return array;
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        java.util.List<String> blockNames = getBlockList();
        if ((blockNames == null) || (blockNames.size() == 0)) return;

        StatusTableModel tm = createStatusTableModel(distributor);

        String windowIdentifier = null;

        if (blockNames.size() > 1)
        {
            Object[] nameArray = pickBlocks(blockNames);
            if (nameArray == null) return;

            StringBuffer sb = new StringBuffer(500);
            for (int n = 0; n < nameArray.length; n++)
            {
                String bname = (String)nameArray[n];
                sb.append(bname);
                tm.createRow(n,bname);
            }
            windowIdentifier = sb.toString();
        }
        else
        {
            String bname = (String)blockNames.get(0);
            tm.createRow(0,bname);
            windowIdentifier = bname;
        }

        tm.configure();

        if (activeWindows.contains(windowIdentifier))
        {
            JOptionPane.showMessageDialog(frame,
                    "That " + getText() + " window is already visible.",
                    "Already Visible",JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            activeWindows.add(windowIdentifier);
            StatusTable table = new StatusTable(getText(),frame,
                    windowIdentifier,distributor,tm);
            table.setVisible(true);
            table.addWindowListener(new MyWindowAdapter());
        }
    }

    class MyWindowAdapter extends java.awt.event.WindowAdapter
    {
        public void windowClosed(java.awt.event.WindowEvent e)
        {
            StatusTable table = (StatusTable)e.getWindow();
            activeWindows.remove(table.getWindowIdentifier());
        }
    }
}
