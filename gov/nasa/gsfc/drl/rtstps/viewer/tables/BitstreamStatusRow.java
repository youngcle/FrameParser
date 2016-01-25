/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import javax.swing.table.AbstractTableModel;

final class BitstreamStatusRow extends StatusRow
{
    static final String[] ID =
    {
        "CADUs",
        "CADU Seq Errors",
        "Missing CADUs",
        "B_PDUs"
    };

    static final String[] HEADER =
    {
        "CADUs",
        "Seq Errs",
        "Lost CADUs",
        "B_PDUs Out"
    };

    BitstreamStatusRow(AbstractTableModel tm, String blockName,
            Distributor distributor, int row)
    {
        super(tm,"bitstream",blockName,ID,distributor,row);
    }
}
