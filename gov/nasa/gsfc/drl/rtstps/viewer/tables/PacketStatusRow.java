/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import javax.swing.table.AbstractTableModel;

/**
 * This is a packet row in the packet status table.
 * 
 */
final class PacketStatusRow extends StatusRow
{
    static final String[] ID =
    {
        "Packets Output",
        "Sequence Errors",
        "Missing Packets",
        "Discarded Packets",
        "Bad Lengths",
        "Packets With Fill"
    };

    static final String[] HEADER =
    {
        "Output Pkts",
        "Seq Errors",
        "Missing",
        "Discarded",
        "Bad Sizes",
        "Pkts w/ Fill"
    };

    PacketStatusRow(AbstractTableModel tm, String blockName,
            Distributor distributor, int row)
    {
        super(tm,"packet",blockName,ID,distributor,row);
    }
}
