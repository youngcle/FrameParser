/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.path;
import gov.nasa.gsfc.drl.rtstps.viewer.StatusListPanel;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

/**
 * PacketStatus shows status for one packet application id.
 * 
 * 
 */
class PacketStatus extends gov.nasa.gsfc.drl.rtstps.viewer.StatusWindow
{
    private static final int VALUE_DIGITS = 10;
    private StatusListPanel slp;

    public PacketStatus(javax.swing.JFrame frame, String blockName,
            Distributor distributor)
    {
        super(frame, blockName, blockName + " Status");
        slp = new StatusListPanel(distributor, VALUE_DIGITS);
        setContentPane(slp);

        String x = blockName + ".";
        String item2 = x + "Packets Output";
        slp.addTextGadget("Packets Output",item2);
        slp.addAlarmGadget("Discarded Packets",x+"Discarded Packets",
                item2,0.1f,10f);
        slp.addAlarmGadget("Bad Lengths",x+"Bad Lengths",item2,0f,10f);
        slp.addAlarmGadget("Packets With Fill", x+"Packets With Fill",
                item2,0f,10f);
        slp.addAlarmGadget("Sequence Errors", x+"Sequence Errors",
                item2,0.1f,10f);
        slp.addTextGadget("Missing Packets",x+"Missing Packets");
        slp.addTextGadget("Bad Length Sample",x+"Bad Length Sample");
        slp.setGadgetsToSameSize();

        try { distributor.doStatus(); }
        catch (java.rmi.RemoteException re) {}

        pack();
    }

    public final void dispose()
    {
        /** disconnect the distributor */
        slp.disconnect();
        super.dispose();
    }
    
    private static final long serialVersionUID = 1L;			
}
