/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.path;
import gov.nasa.gsfc.drl.rtstps.viewer.StatusWindow;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This class is an action that causes a new status window to be opened for
 * one RT-STPS node block type.
 * <p>
 * I maintain a list of active windows by window identifier so as to avoid
 * duplicate windows. I become a window listener on each window so that I
 * can remove it from my list of open windows when it closes. It is the
 * window's responsibility to disconnect itself from the Distributor.
 * 
 * 
 */
public abstract class StatusWindowAction extends gov.nasa.gsfc.drl.rtstps.library.XAction
{
    private javax.swing.JFrame frame;
    private Distributor distributor;
    private String blockType;
    private java.util.TreeSet<String> activeWindows = new java.util.TreeSet<String>();

    /**
     * Create a status window action.
     * @param title The label associated with this action.
     * @param blockType A RT-STPS node block type such as "vcdu." The action
     *      will ask the user to select one status block to view from a
     *      list of blocks that match the block type.
     * @param frame The parent frame
     * @param distributor The status distributor
     */
    public StatusWindowAction(String title, String blockType, JFrame frame,
            Distributor distributor)
    {
        super(title);
        this.frame = frame;
        this.distributor = distributor;
        this.blockType = blockType;
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        /**
         * Get a list of packet blocks from the Distributor.
         */
        List<String> list = getBlockList(blockType);
        if ((list == null) || (list.size() == 0)) return;

        /**
         * If I got more than one block name, I ask the user to choose one.
         */
        String blockName = null;
        if (list.size() > 1)
        {
            blockName = pickBlock(blockType,list);
        }
        else
        {
            blockName = (String)list.get(0);
        }
        if (blockName == null) return;

        /**
         * If I am already showing the selected block, I do not open another
         * window.
         */
        if (activeWindows.contains(blockName))
        {
            JOptionPane.showMessageDialog(frame,
                    "That status window is already visible.",
                    "Already Visible",JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            /**
             * I save the block name. I create the window, show it, and
             * register myself as a window listener.
             */
            activeWindows.add(blockName);
            StatusWindow sw = createStatusWindow(frame,blockName,distributor);
            sw.setVisible(true);
            sw.addWindowListener(new MyWindowAdapter());
        }
    }

    /**
     * Create a custom status window (dialog or frame) to show what the user
     * chooses.
     */
    abstract protected StatusWindow createStatusWindow(JFrame frame,
            String blockName, Distributor distributor);

    /**
     * Get a list of packet blocks from the Distributor.
     */
    private List<String> getBlockList(String blockType)
    {
    	List<String> list = null;
		
        try
        {
            list = distributor.getBlockNamesByType(blockType);
            if (list == null)
            {
                JOptionPane.showMessageDialog(frame,
                        "No " + blockType + " status is available.",
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
     * Ask the user to choose a status block.
     * @param blockType the block type name
     * @param list a list of status block names
     */
    private String pickBlock(String blockType, List<String> list)
    {
        String pick = null;
        Iterator<String> i = list.iterator();
        String model = "";
        while (i.hasNext())
        {
            String x = i.next().toString();
            if (x.length() > model.length()) model = x;
        }

        javax.swing.JList xlist = new javax.swing.JList(list.toArray());
        xlist.setPrototypeCellValue(model);
        xlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        xlist.setVisibleRowCount(8);
        xlist.setSelectedIndex(1);
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(xlist);

        int v = JOptionPane.showConfirmDialog(frame,sp,
                "Select " + blockType,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (v == JOptionPane.OK_OPTION)
        {
            pick = (String)xlist.getSelectedValue();
        }
        return pick;
    }

    class MyWindowAdapter extends java.awt.event.WindowAdapter
    {
        public void windowClosed(java.awt.event.WindowEvent e)
        {
            StatusWindow sw = (StatusWindow)e.getWindow();
            activeWindows.remove(sw.getWindowIdentifier());
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
