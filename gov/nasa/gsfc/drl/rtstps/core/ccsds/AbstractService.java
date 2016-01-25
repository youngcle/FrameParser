/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds;
import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.Frame;
import gov.nasa.gsfc.drl.rtstps.core.FrameReceiver;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsNode;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * This is the base class for CCSDS service nodes. It receives CADUs (frames).
 * It provides a CADU and CADU-level sequence checking as well as establishing
 * a status item list. It does not assume any sort of output.
 * 
 * 
 */
public abstract class AbstractService extends RtStpsNode implements
        FrameReceiver, Cloneable
{
    protected Cadu cadu;
    protected CaduSequencer sequencer;
    protected org.w3c.dom.Element serviceElement;

    /**
     * Construct a service object.
     */
    protected AbstractService(String elementName)
    {
        super(elementName);
    }

    /**
     * Set up this stps node with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        serviceElement = element;
        String myname = element.getAttribute("label");
        super.setLinkName(myname);
        statusItemList = new java.util.ArrayList<StatusItem>(15);	
        sequencer = new CaduSequencer(statusItemList);
    }

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     */
    public void finishSetup(Configuration configuration) throws RtStpsException
    {
        /**
         * I must defer creating cadu until after Builder has created all
         * nodes because cadu needs parity lengths from CRC and RS nodes.
         */
        cadu = new Cadu(serviceElement,configuration);
        serviceElement = null;
    }

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
        for (int n = 0; n < frames.length; n++)
        {
            putFrame(frames[n]);
        }
    }
}
