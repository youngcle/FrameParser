/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import javax.swing.table.AbstractTableModel;

/**
 * This class is one VCDU row is a status table.
 * 
 */
final class VcduStatusRow extends StatusRow
{
    static final String[] ID = 
    {
        "CADUs",
        "CADU Seq Errors",
        "Missing CADUs",
        "VCDUs"
    };

    static final String[] HEADER = 
    {
        "CADUs",
        "Seq Errs",
        "Lost CADUs",
        "VCDUs Out"
    };

    VcduStatusRow(AbstractTableModel tm, String blockName,
            Distributor distributor, int row)
    {
        super(tm,"vcdu",blockName,ID,distributor,row);
    }
}
