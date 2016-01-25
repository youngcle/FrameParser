/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.path;
import gov.nasa.gsfc.drl.rtstps.viewer.StatusListPanel;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

/**
 * PathServiceStatus shows a status winow for one path virtual channel.
 * 
 * 
 */
class PathServiceStatus extends gov.nasa.gsfc.drl.rtstps.viewer.StatusWindow
{
    private static final int VALUE_DIGITS = 10;
    private StatusListPanel slp;

    public PathServiceStatus(javax.swing.JFrame frame, String blockName,
            Distributor distributor)
    {
        super(frame, blockName, blockName + " Status");

        slp = new StatusListPanel(distributor, VALUE_DIGITS);
        setContentPane(slp);

        String x = blockName + ".";
        String item2 = x + "CADUs";

        slp.addTextGadget("CADUs",item2);
        slp.addAlarmGadget("CADU Seq Errors",x+"CADU Seq Errors",
                item2,0.1f,10f);
        slp.addTextGadget("Missing CADUs",x+"Missing CADUs");
        slp.addTextGadget("Idle VCDUs",x+"Idle VCDUs");

        slp.addAlarmGadget("Bad FHPs",x+"Bad FHPs",item2,0f,10f);
        slp.addAlarmGadget("Troublesome Frames",x+"Troublesome Frames",
                item2,0f,10f);
        slp.addAlarmGadget("Irrational Packet Lengths",
                x+"Irrational Packet Lengths",item2,0f,10f);
        slp.addTextGadget("Discarded Fragments",x+"Discarded Fragments");
        slp.addTextGadget("Discarded Bytes",x+"Discarded Bytes");

        slp.addTextGadget("Created Packets",x+"Created Packets");
        slp.addTextGadget("Unrouteable Packets",x+"Unrouteable Packets");
        slp.addTextGadget("Idle Packets",x+"Idle Packets");
        slp.addTextGadget("Deleted Packets",x+"Deleted Packets");
        slp.addTextGadget("Output Packets",x+"Output Packets");

        slp.setGadgetsToSameSize();

        try { distributor.doStatus(); }
        catch (java.rmi.RemoteException re) {}

        pack();
    }

    public final void dispose()
    {
        slp.disconnect();
        super.dispose();
    }
    
    private static final long serialVersionUID = 1L;			
}
