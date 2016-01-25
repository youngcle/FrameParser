/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

/**
 * This action lets the user create a virtual channel status table window.
 * 
 * 
 */
public final class VcStatusTableAction extends StatusTableAction
{
    private static final String[] typeNames = {"vcdu","bitstream","path"};

    public VcStatusTableAction(javax.swing.JFrame frame,
            gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor distributor)
    {
        super("Virtual Channel Status Table",frame,distributor);
        setMnemonic('V');
        setKeyStroke(java.awt.event.KeyEvent.VK_V);
        setToolTip("Show a status table for selected virtual channels.");
    }

    protected final java.util.List<String> getBlockList()		
    {
        return getBlockList(typeNames);
    }

    protected StatusTableModel createStatusTableModel(Distributor d)
    {
        return new VcTableModel(d);
    }
    
    private static final long serialVersionUID = 1L;			
}
