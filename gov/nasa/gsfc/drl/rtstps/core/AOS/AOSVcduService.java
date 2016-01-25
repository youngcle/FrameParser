/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.AOS;

import gov.nasa.gsfc.drl.rtstps.core.*;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * This class does the AOS VCDU service. It makes VCDUs from AOS
 * frames.
 * 
 * 
 */
public final class AOSVcduService extends AbstractService implements Sender,
        FrameReceiver, Cloneable
{
    /**
     * This is a class name for this STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "VCDUservice";

    private Vcdu vcdu;
    private UnitReceiver output = null;
    private UnitOutputTool uotool = null;
    private LongStatusItem vcdus;


    /**
     * Create a AOSVcduService object.
     */
    public AOSVcduService()
    {
        super(CLASSNAME);
    }


    public void vcduSetup() throws RtStpsException {
        vcdu = new Vcdu();
        vcdus = new LongStatusItem("VCDUs");
        statusItemList = new java.util.ArrayList<StatusItem>(15);
        sequencer = new AOSSequencer(statusItemList);
        statusItemList.add(vcdus);
    }
    /**
     * Set up this stps node with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        super.load(element,configuration);
        vcdu = new Vcdu(element);
        vcdus = new LongStatusItem("VCDUs");
        statusItemList.add(vcdus);
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
            vcdu.setAOS(aos);
            output.putUnit(vcdu);
            ++vcdus.value;
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
     * This class is a AOS VCDU.
     * I ignore (and eliminated) two fields in AbstractService because I don't
     * need them: OCFpresent and crcParityPresent.
     */
    class Vcdu extends Unit
    {
        private boolean discardRsParity = false;
        

        Vcdu(org.w3c.dom.Element element) throws RtStpsException
        {
            super();
            startOffset = 4;
            discardRsParity = Convert.toBoolean(element,"discardRsParity",false);
        }

        Vcdu() throws RtStpsException
        {
            super();
            startOffset = 4;
            discardRsParity = true;
        }

        void setAOS(AOS aos)
        {
            frameAnnotation = aos.getFrameAnnotation();
            data = aos.getData();
            length = aos.getSize() - startOffset;
            if (discardRsParity) length -= aos.getReedSolomonParityLength();
            deleted = false;
        }
    }
}
