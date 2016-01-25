/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.AOS;

import gov.nasa.gsfc.drl.rtstps.core.*;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;

/**
 * This class extracts B_PDUs (Bitstream Protocol Data Units) from CADUs and
 * sends them to a UnitReceiver. It does not merge B_PDUs.
 * 
 */
public final class BPDUService extends AbstractService implements Sender,
        FrameReceiver, Cloneable
{
    /**
     * This is a class name for this STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "BPDU";

    private BPDU bpdu;
    private UnitReceiver output = null;
    private UnitOutputTool uotool = null;
    private LongStatusItem bpdus;


    /**
     * Create a BPDUService object.
     */
    public BPDUService()
    {
        super(CLASSNAME);
    }

    /**
     * Set up this stps node with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        super.load(element,configuration);
        bpdu = new BPDU();

        bpdus = new LongStatusItem("B_PDUs");
        statusItemList.add(bpdus);
    }

    /**
     * Add a Receiver to this sender's list of receivers.
     * @param receiver If the receiver is not of the expected type,
     *          then the method throws an StpsException.
     */
    public void addReceiver(Receiver receiver) throws RtStpsException
    {
        if (uotool == null)
        {
            uotool = new UnitOutputTool(getLinkName());
        }
        
        uotool.addReceiver(receiver);
        output = uotool.getOutput();
    }

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     */
    public void finishSetup(Configuration configuration) throws RtStpsException
    {
        super.finishSetup(configuration);
        output = uotool.getOutput();
        uotool = null;   //no longer needed
        if (output == null)
        {
            throw new RtStpsException(getLinkName() + " demands an output link.");
        }
    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        if (!frame.isDeleted() && !frame.isFillFrame())
        {
            aos.setFrame(frame);
            sequencer.check(aos);
            bpdu.setAOS(aos);
            output.putUnit(bpdu);
            ++bpdus.value;
        }
    }

    /**
     * Flush the pipeline.
     */
    public void flush() throws RtStpsException
    {
        output.flush();
    }

    /**
     * This class is the "Bitstream Protocol Data Unit."
     */
    class BPDU extends Unit
    {
        void setAOS(AOS aos)
        {
            data = aos.getData();
            startOffset = aos.getdataZoneStartOffset();
            length = aos.getdataZoneEndOffset() - startOffset + 1;
            frameAnnotation = aos.getFrameAnnotation();
            deleted = false;
        }
    }
}
