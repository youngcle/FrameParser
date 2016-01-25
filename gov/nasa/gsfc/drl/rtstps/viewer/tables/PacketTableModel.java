/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

/**
 * This class is the table model for the packet status table.
 * 
 * 
 */
final class PacketTableModel extends StatusTableModel
{
    /**
     * Create table model.
     */
    PacketTableModel(Distributor distributor)
    {
        super(distributor);
    }

    /**
     * Create a table row.
     */
    void createRow(int row, String fullBlockName)
    {
        String blockName = fullBlockName.substring(7);
        data.add(new PacketStatusRow(this,blockName,distributor,row));
    }

    /**
     * Use this method to finish configuration after all rows have been
     * created.
     */
    void configure()
    {
        String[] id = PacketStatusRow.HEADER;
        header = new String[id.length + 2];
        header[0] = "Type";
        header[1] = "Stream";
        for (int n = 0; n < id.length; n++)
        {
            header[n+2] = id[n];
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
