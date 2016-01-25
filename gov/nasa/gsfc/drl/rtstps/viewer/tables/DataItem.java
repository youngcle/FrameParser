/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import javax.swing.table.AbstractTableModel;

/**
 * This class represents one cell in a status table model. It listens
 * for status updates from the distributor.
 * 
 */
final class DataItem implements gov.nasa.gsfc.drl.rtstps.viewer.status.StatusListener
{
    private final String fullId;
    private final int column;
    private final int row;
    private String value = "0";
    private AbstractTableModel tableModel;

    /**
     * Create a data item at a particular row and column cell.
     * @param row row number
     * @param column column number
     * @param fullId the full name of the status item
     * @param tm the table model
     */
    DataItem(int row, int column, String fullId, AbstractTableModel tm)
    {
        this.fullId = fullId;
        this.row = row;
        this.column = column;
        tableModel = tm;
    }

    /**
     * Get the full name for the status item.
     */
    final String getFullId()
    {
        return fullId;
    }

    public final String toString()
    {
        return value;
    }

    /**
     * Receive a status update.
     */
    public synchronized void processStatusItem(StatusItem item, String fullName)
    {
        value = item.getValue();
        tableModel.fireTableCellUpdated(row,column);
    }
}
