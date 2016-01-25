/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

/**
 * This is the right-hand panel on the primary display.
 * 
 * 
 */
class FramePanelB extends StatusListPanel
{
    private static final String TOTAL_FRAMES = "frame_sync.Total Frames";

    FramePanelB(Distributor d, int digitsInValueField, String title)
    {
        super(title,digitsInValueField,d);

        addAlarmGadget("CRC Error Frames",
                "frame_status.CRC Error Frames",
                TOTAL_FRAMES,0.1f,10f);

        addTextGadget("RS Corrected Frames",
                "frame_status.RS-Corrected Frames");

        addAlarmGadget("RS Uncorrectables",
                "frame_status.RS-Uncorrectable Frames",
                TOTAL_FRAMES,0.1f,10f);

        addTextGadget("Deleted Frames","frame_status.Deleted Frames");
        addTextGadget("Passed Frames","frame_status.Passed Frames");
        addTextGadget("CADUs","cadu_service.Output CADUs");
        addTextGadget("Unrouteable CADUs","cadu_service.Unrouteables");
        addTextGadget("Fill CADUs","cadu_service.Fill CADUs");

        setGadgetsToSameSize();
    }
    
    private static final long serialVersionUID = 1L;			
}
