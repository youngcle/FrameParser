/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

/**
 * This action lets the user create a packet status table window.
 * 
 * 
 */
public class PacketStatusTableAction extends StatusTableAction
{
    public PacketStatusTableAction(javax.swing.JFrame frame,
            gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor distributor)
    {
        super("Packet Status Table",frame,distributor);
        setMnemonic('A');
        setKeyStroke(java.awt.event.KeyEvent.VK_A);
        setToolTip("Show a packet status table for selected application ID.");
    }

    protected final java.util.List<String> getBlockList()		
    {
        return getBlockList("packet");
    }

    protected StatusTableModel createStatusTableModel(Distributor d)
    {
        return new PacketTableModel(d);
    }
    
    private static final long serialVersionUID = 1L;			
}
