/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import javax.swing.table.AbstractTableModel;

/**
 * This is a path service row in the vc status table.
 * 
 */
final class PathStatusRow extends StatusRow
{
    static final String[] ID = 
    {
        "CADUs",
        "CADU Seq Errors",
        "Missing CADUs", 
        "Output Packets",
        "Discarded Fragments",
        "Unrouteable Packets"
    };

    static final String[] HEADER = 
    {
        "CADUs",
        "Seq Errs",
        "Lost CADUs", 
        "Packets Out",
        "Lost Fragments",
        "Unrouted Pkts"
    };

    PathStatusRow(AbstractTableModel tm, String blockName,
            Distributor distributor, int row)
    {
        super(tm,"path",blockName,ID,distributor,row);
    }
}
