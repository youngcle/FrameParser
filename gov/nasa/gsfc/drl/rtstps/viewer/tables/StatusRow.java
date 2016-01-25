/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import javax.swing.table.AbstractTableModel;

/**
 * This class is one row in a status table.
 * 
 */
abstract class StatusRow
{
    private Distributor distributor;
    private Object[] data;

    /**
     * Create a status row.
     * @param tm the table model
     * @param typeName the type name for the status items in this row
     * @param blockName the block name for the items in this row
     * @param fieldList a list of short field names for the status items
     * @param distributor the status distributor
     * @param row the row number in the table for this status row
     */
    protected StatusRow(AbstractTableModel tm, String typeName,
            String blockName, String[] fieldList,
            Distributor distributor, int row)
    {
        this.distributor = distributor;

        data = new Object[fieldList.length + 2];
        data[0] = typeName;
        data[1] = blockName;

        String prefix = typeName + '.' + blockName + '.';
        int column = 2;
        for (int n = 0; n < fieldList.length; n++)
        {
            String fullId = prefix + fieldList[n];
            DataItem di = new DataItem(row,column,fullId,tm);
            distributor.requestStatusItemDelivery(di,fullId);
            data[column] = di;
            ++column;
        }
    }

    /**
     * Get the current value as a string for the item at the column.
     */
    final String getColumnValue(int column)
    {
        String v;
        if (column < data.length)
        {
            v = data[column].toString();
        }
        else
        {
            v = "";
        }
        return v;
    }

    /**
     * Disconnect all items in this row from the distributor.
     */
    void dispose()
    {
        for (int n = 2; n < data.length; n++)
        {
            DataItem di = (DataItem)data[n];
            distributor.cancelStatusItemDelivery(di,di.getFullId());
        }
    }
}

