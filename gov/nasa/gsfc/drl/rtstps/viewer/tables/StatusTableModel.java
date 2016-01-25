/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import java.util.Iterator;

/**
 * This class is an abstract table model for my various status tables.
 * 
 * 
 */
abstract class StatusTableModel extends javax.swing.table.AbstractTableModel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String[] header;
    protected java.util.List<StatusRow> data = new java.util.ArrayList<StatusRow>(64);
    protected Distributor distributor;

    /**
     * Create table model.
     */
    protected StatusTableModel(Distributor distributor)
    {
        this.distributor = distributor;
    }

    /**
     * Create a table row.
     */
    abstract void createRow(int row, String fullBlockName);

    /**
     * Use this method to finish configuration after all rows have been
     * created.
     */
    abstract void configure();

    public final boolean isCellEditable()
    {
        return false;
    }

    public final String getColumnName(int column)
    {
        return header[column];
    }

    public final int getRowCount()
    {
        return data.size();
    }

    public final int getColumnCount()
    {
        return header.length;
    }

    public final Class<String> getColumnClass(int column)		
    {
        return String.class;
    }

    public final Object getValueAt(int row, int column)
    {
        StatusRow sr = (StatusRow)data.get(row);
        return sr.getColumnValue(column);
    }

    /**
     * Disconnect the model from the distributor.
     */
    void dispose()
    {
        Iterator<StatusRow> i = data.iterator();
        while (i.hasNext())
        {
            StatusRow sr = (StatusRow)i.next();
            sr.dispose();
        }
    }
}
