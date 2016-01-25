/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * This is a type of RtStpsNode that sends frames to a frame output receiver.
 * It makes no input demands. Nodes that send frames are not required to use
 * this class. I provide it as a convenience.
 * 
 */
public abstract class FrameSenderNode extends RtStpsNode implements Sender,
        Cloneable
{
    /** Where my frames go -- my output link. */
    protected FrameReceiver output = null;

    /**
     * Most FrameSenderNodes demand an output link, but setting this to false
     * makes that optional.
     */
    private boolean isOutputRequired = true;

    /**
     * I use this tool to create the output link. The tool hides the fact that
     * the output may be more than one receiver.
     */
    private FrameOutputTool fotool = null;



    /**
     * Create a FrameSenderNode.
     */
    public FrameSenderNode(String elementName)
    {
        super(elementName);
    }

    /**
     * Create a FrameSenderNode.
     */
    public FrameSenderNode(String elementName, String linkName)
    {
        super(elementName,linkName);
    }

    /**
     * By default, FrameSenderNode demands a non-null output link.
     * You may override this feature by setting this to false.
     */
    protected final void setOutputIsRequired(boolean ok)
    {
        isOutputRequired = ok;
    }

    /**
     * Set up this RT-STPS node with a configuration.
     */
    public abstract void load(org.w3c.dom.Element element,
            Configuration configuration) throws RtStpsException;

    /**
     * Add a Receiver to this sender's list of receivers.
     * @param receiver If the receiver is not of the expected type,
     *          then the method throws an RtStpsException.
     */
    public void addReceiver(Receiver receiver) throws RtStpsException
    {
        if (fotool == null) 
        {
            fotool = new FrameOutputTool(getLinkName());
        }
        
        fotool.addReceiver(receiver);
        output = fotool.getOutput();
    }

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     * If a derived class overrides this method, make sure it calls
     * super.finishSetup(configuration).
     */
    public void finishSetup(Configuration configuration) throws RtStpsException
    {
        if (fotool != null)
        {
            output = fotool.getOutput();
            fotool = null;   //no longer needed
        }

        if (isOutputRequired && (output == null))
        {
            throw new RtStpsException(toString() + " demands an output link.");
        }
    }

    /**
     * Flush the pipeline.
     */
    public void flush() throws RtStpsException
    {
        output.flush();
    }
}
