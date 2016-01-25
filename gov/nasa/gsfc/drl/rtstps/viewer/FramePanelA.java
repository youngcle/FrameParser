/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

/**
 * This is the left-hand panel on the primary display.
 * 
 * 
 */
class FramePanelA extends StatusListPanel
{
    private static final String TOTAL_FRAMES = "frame_sync.Total Frames";

    FramePanelA(Distributor d, int digitsInValueField, String title)
    {
        super(title,digitsInValueField,d);

        addTextGadget("Mode","frame_sync.Mode");

        addAlarmGadget("Lost Sync Count","frame_sync.Lost Sync Count",
                TOTAL_FRAMES,1f,10f);

        addTextGadget("Flywheels","frame_sync.Flywheels");
        addTextGadget("Lock Frames","frame_status.Lock Frames");
        addTextGadget("Flywheels Output","frame_status.Flywheels");

        addAlarmGadget("Slipped Frames","frame_status.Slipped Frames",
                TOTAL_FRAMES,0f,10f);

        addTextGadget("True Frames","frame_status.True Frames");
        addTextGadget("Inverted Frames","frame_status.Inverted Frames");

        setGadgetsToSameSize();
    }
    
    private static final long serialVersionUID = 1L;			
}
