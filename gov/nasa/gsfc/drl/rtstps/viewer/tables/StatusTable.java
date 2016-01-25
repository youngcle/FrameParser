/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import javax.swing.JTable;

/**
 * StatusTable is a dialog window that shows a status table where each row
 * is one channel or stream.
 * 
 * 
 */
final class StatusTable extends gov.nasa.gsfc.drl.rtstps.viewer.StatusWindow
{
    private StatusTableModel tableModel;

    StatusTable(String title, javax.swing.JFrame frame,
            String windowIdentity, Distributor distributor,
            StatusTableModel tableModel)
    {
        super(frame,windowIdentity,title);
        this.tableModel = tableModel;
        JTable table = new JTable(tableModel);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        java.awt.Container cp = getContentPane();
        cp.add(table.getTableHeader(),"North");
        cp.add(table,"Center");
        try { distributor.doStatus(); }
        catch (java.rmi.RemoteException re) {}
        pack();
    }

    /**
     * Disconnect all listeners from the distributor.
     */
    public final void dispose()
    {
        tableModel.dispose();
        super.dispose();
    }
    
    private static final long serialVersionUID = 1L;			
}
