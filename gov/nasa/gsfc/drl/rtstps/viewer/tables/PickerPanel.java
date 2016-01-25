/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.library.AListModel;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JList;

/**
 * This class shows a panel that lets a user choose row items for a
 * status table.
 * 
 * 
 */
final class PickerPanel extends javax.swing.JPanel
{
    private javax.swing.JList list;

    /**
     * Create a PickerPanel.
     */
    PickerPanel(java.util.List<String> blockNames)			
    {
        setLayout(new BorderLayout());

        list = new JList(new AListModel(blockNames));
        PickerModel selectionModel = new PickerModel(blockNames.size());
        list.setSelectionModel(selectionModel);
        list.setVisibleRowCount(8);
        add(new javax.swing.JScrollPane(list),BorderLayout.CENTER);
        add(new Buttons(selectionModel),BorderLayout.NORTH);
    }

    /**
     * Get a list of selections.
     */
    final Object[] getSelections()
    {
        return list.getSelectedValues();
    }

    final class Buttons extends javax.swing.JPanel
            implements java.awt.event.ActionListener
    {
        private PickerModel selectionModel;

        Buttons(PickerModel selectionModel)
        {
            this.selectionModel = selectionModel;

            setLayout(new java.awt.GridLayout(1,2));

            JButton b = new JButton("Select All");
            b.setMnemonic('S');
            b.setDefaultCapable(true);
            b.setActionCommand("SELECT_ALL");
            b.addActionListener(this);
            add(b);

            b = new JButton("Clear All");
            b.setMnemonic('C');
            b.setActionCommand("CLEAR");
            b.addActionListener(this);
            add(b);
        }

        public void actionPerformed(java.awt.event.ActionEvent e)
        {
            String ac = e.getActionCommand();
            if (ac.equals("SELECT_ALL"))
            {
                selectionModel.selectAll();
            }
            else
            {
                selectionModel.clearSelection();
            }
        }
    
        private static final long serialVersionUID = 1L;			
    }
    
    private static final long serialVersionUID = 1L;			
}

