/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

/**
 * This class is the table model for the virtual channel status table.
 * 
 * 
 */
final class VcTableModel extends StatusTableModel
{
    private boolean vcduPresent = false;
    private boolean bitstreamPresent = false;
    private boolean pathPresent = false;

    /**
     * Create table model.
     */
    VcTableModel(Distributor distributor)
    {
        super(distributor);
    }

    /**
     * Create a table row. The full block name must have a block type of
     * either "vcdu", "bitstream", or "path". Only use this method to
     * prepare the initial model. Do not use it after you have configured.
     */
    void createRow(int row, String fullBlockName)
    {
        if (fullBlockName.startsWith("vcdu."))
        {
            vcduPresent = true;
            String blockName = fullBlockName.substring(5);
            data.add(new VcduStatusRow(this,blockName,distributor,row));
        }
        else if (fullBlockName.startsWith("bitstream."))
        {
            bitstreamPresent = true;
            String blockName = fullBlockName.substring(10);
            data.add(new BitstreamStatusRow(this,blockName,distributor,row));
        }
        else if (fullBlockName.startsWith("path."))
        {
            pathPresent = true;
            String blockName = fullBlockName.substring(5);
            data.add(new PathStatusRow(this,blockName,distributor,row));
        }
    }

    /**
     * Use this method to finish configuration after all rows have been
     * created.
     */
    void configure()
    {
        if (pathPresent)
        {
            createHeader(PathStatusRow.HEADER);
            if (vcduPresent || bitstreamPresent)
            {
                header[5] = "Output";
            }
        }
        else if (vcduPresent)
        {
            createHeader(VcduStatusRow.HEADER);
            if (bitstreamPresent)
            {
                header[5] = "Output";
            }
        }
        else if (bitstreamPresent)
        {
            createHeader(BitstreamStatusRow.HEADER);
        }
    }

    /**
     * Create the table header.
     */
    private void createHeader(String[] id)
    {
        header = new String[id.length + 2];
        header[0] = "Type";
        header[1] = "Channels";
        for (int n = 0; n < id.length; n++)
        {
            header[n+2] = id[n];
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
