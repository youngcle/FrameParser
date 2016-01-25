/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds;
import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.Frame;
import gov.nasa.gsfc.drl.rtstps.core.FrameReceiver;
import gov.nasa.gsfc.drl.rtstps.core.Receiver;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.Sender;
import gov.nasa.gsfc.drl.rtstps.core.Unit;
import gov.nasa.gsfc.drl.rtstps.core.UnitOutputTool;
import gov.nasa.gsfc.drl.rtstps.core.UnitReceiver;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;

/**
 * This class does the CCSDS VCDU service. It makes VCDUs from CCSDS
 * version 2 frames.
 * 
 * 
 */
public final class VcduService extends AbstractService implements Sender,
        FrameReceiver, Cloneable
{
    /**
     * This is a class name for this STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "vcdu";

    private Vcdu vcdu;
    private UnitReceiver output = null;
    private UnitOutputTool uotool = null;
    private LongStatusItem vcdus;


    /**
     * Create a AOSVcduService object.
     */
    public VcduService()
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
            cadu.setFrame(frame);
            sequencer.check(cadu);
            vcdu.setCadu(cadu);
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
     * This class is a CCSDS VCDU.
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

        void setCadu(Cadu cadu)
        {
            frameAnnotation = cadu.getFrameAnnotation();
            data = cadu.getData();
            length = cadu.getSize() - startOffset;
            if (discardRsParity) length -= cadu.getReedSolomonParityLength();
            deleted = false;
        }
    }
}
