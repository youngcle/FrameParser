/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public abstract class CommonTableModel extends AbstractTableModel {
    protected Vector<String> columnNames = new Vector<String>();
    protected Vector<Vector<String>> rows = new Vector<Vector<String>>();

    protected boolean DEBUG = false;
  
 
    public int getColumnCount() {
        return columnNames.size();
    }

    public int getRowCount() {
        return rows.size();
    }

    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    public Object getValueAt(int row, int col) {
    	Vector<String> rowStr = rows.get(row);
    	String value = rowStr.get(col);
    	
    	return value;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
    	boolean state = false;
       if (col == 0) {
    	   state = true;;
       	}
    	return state;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        if (DEBUG) {
            System.out.println("Setting value at " + row + "," + col
                               + " to " + value
                               + " (an instance of "
                               + value.getClass() + ")");
        }
        if (col >= getColumnCount()) {
        	throw new IndexOutOfBoundsException("Col request[" + col + "] exceeds columns created [" + getColumnCount() + "]");
        }
        //FIXME this probably only works if data is put in sequentially without skipping any rows...
        
        while (rows.size() <= row) {
        	rows.add(new Vector<String>());
        }
        Vector<String> dataRow = rows.get(row);
        
        
        dataRow.add(col, (String)value);
       
        fireTableCellUpdated(row, col);

        if (DEBUG) {
            System.out.println("New value of data:");
            printDebugData();
        }
    }

    private void printDebugData() {
        int numRows = getRowCount();
        
        
       // System.out.println("Row size: " + numRows);
       // System.out.println("Col size: " + numCols);

        for (int i=0; i < numRows; i++) {
            System.out.println("    row " + i + "<");
            int numCols = rows.get(i).size(); // because this is being called while it grows, its not getColumnCount
            for (int j=0; j < numCols; j++) {
            	System.out.print(" col " + j + ":");
            	Vector<String> rowData = rows.get(i);
            	String value = rowData.get(j);
                System.out.print("  " +  value);
            }
            System.out.println(" >");
        }
        System.out.println("--------------------------");
    }
}

