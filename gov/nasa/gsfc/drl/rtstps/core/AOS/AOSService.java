/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.AOS;

import gov.nasa.gsfc.drl.rtstps.core.*;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * This node receives all frames that require AOS processing. It sorts
 * frames and redirects them to service processors based on the spacecraft
 * and virtual channel numbers.
 * 
 * 
 */
public final class AOSService extends FrameSenderNode implements FrameReceiver
{
    /**
     * This is a class name for this STPS node type, which is also the
     * element name.
     */
    public static final String CLASSNAME = "aos_service";

    private java.util.HashMap<VcKey,FrameReceiver> outputMap;		
    private FrameReceiver deadletters = null;
    
    private VcKey lookup;
    private AOS aos;
    private org.w3c.dom.NodeList svlinks;
    private LongStatusItem unrouteableCadus;
    private LongStatusItem fillFrames;
    private LongStatusItem outputFrames;

    /**
     * Create a AOSService object.
     */
    public AOSService()
    {
        /**
         * There is only one AOSService object, so the class name is the
         * same as the link/object name.
         */
        super(CLASSNAME,CLASSNAME);

        /**
         * Normally "output" is null because I use my own special vc sorting
         * to send CADUs to targets. If "output" is not null, then I have
         * been configured via a link to also send CADUs to one or more
         * additional FrameReceivers. They receive unsorted CADUs.
         */
        setOutputIsRequired(false);
    }


    public void AOSServiceSetup(){
        lookup = new VcKey(0,0);
        aos = new AOS();
        int spacecrafts = 1;
        int capacity = spacecrafts * 64;
        outputMap = new java.util.HashMap<VcKey,FrameReceiver>(capacity);
        statusItemList = new java.util.ArrayList<StatusItem>(4);
        unrouteableCadus = new LongStatusItem("Unrouteables");
        statusItemList.add(unrouteableCadus);
        fillFrames = new LongStatusItem("Fill CADUs");
        statusItemList.add(fillFrames);
        outputFrames = new LongStatusItem("Output CADUs");
        statusItemList.add(outputFrames);
    }
    /**
     * Set up this stps node with a configuration.
     */
    public void load(Element element, Configuration configuration)
            throws RtStpsException
    {
        lookup = new VcKey(0,0);
        aos = new AOS();
        org.w3c.dom.Document doc = element.getOwnerDocument();
        org.w3c.dom.NodeList nodelist = doc.getElementsByTagName("spacecraft");
        int spacecrafts = nodelist.getLength();
        int capacity = spacecrafts * 64;
        outputMap = new java.util.HashMap<VcKey,FrameReceiver>(capacity); 
        svlinks = element.getElementsByTagName("svlink");

        statusItemList = new java.util.ArrayList<StatusItem>(4);	
        unrouteableCadus = new LongStatusItem("Unrouteables");
        statusItemList.add(unrouteableCadus);
        fillFrames = new LongStatusItem("Fill CADUs");
        statusItemList.add(fillFrames);
        outputFrames = new LongStatusItem("Output CADUs");
        statusItemList.add(outputFrames);
    }

    /**
     * Finish the configuration. The AOSService class at this time links to
     * the specific targets.
     */
    public void finishSetup(Configuration configuration) throws RtStpsException
    {
        super.finishSetup(configuration);
        TreeMap<String, RtStpsNode> stpsNodes = configuration.getStpsNodes();
        int length = svlinks.getLength();
        for (int n = 0; n < length; n++)
        {
            Element svlink = (Element)svlinks.item(n);
            int spid = Convert.toInteger(svlink,"spid",0);
            int vcid = Convert.toInteger(svlink,"vcid",0);
            String target = svlink.getAttribute("label");
            RtStpsNode stpsNode = (RtStpsNode)stpsNodes.get(target);

            if (stpsNode instanceof FrameReceiver)
            {
                addFrameReceiver(spid,vcid,(FrameReceiver)stpsNode);
            }
            else
            {
                svlinks = null;
                throw new RtStpsException("aosService: " + target +
                        " is not a frame receiver");
            }
        }
        svlinks = null;
    }

    /**
     * Add a FrameReceiver to the outputMap list. AOSService sends a frame
     * to this receiver if the frame has a matching spacecraft and virtual
     * channel id.
     * @param spid A spacecraft id.
     * @param vcid A virtual channel id.
     * @param fr The frame receiver.
     */
    public void addFrameReceiver(int spid, int vcid, FrameReceiver fr)
    {
        VcKey key = new VcKey(spid,vcid);
        FrameReceiver fr0 = outputMap.get(key);

        if (fr0 == null)
        {
            outputMap.put(key,fr);
        }
        else if (fr0 instanceof FrameBroadcaster)
        {
            FrameBroadcaster fb = (FrameBroadcaster)fr0;
            fb.addReceiver(fr);
        }
        else
        {
            FrameBroadcaster fb = new FrameBroadcaster(CLASSNAME,fr0,fr);
            outputMap.put(key,fb);
        }
    }

    /**
     * AOSService sends frames with no frame receiver with a matching
     * spacecraft and virtual channel id to this frame receiver.
     * Otherwise, it discards unrouteable frames.
     */
    public void setDeadletterPath(FrameReceiver fr)
    {
        deadletters = fr;
    }

    /**
     * Flush the pipeline.
     */
    public void flush() throws RtStpsException
    {
        if (output != null) output.flush();
        Iterator<FrameReceiver> i = outputMap.values().iterator();
        while (i.hasNext())
        {
            FrameReceiver fr = (FrameReceiver)i.next();
            fr.flush();
        }
    }

    /**
     * Give an array of frames to AOSService.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
        for (int n = 0; n < frames.length; n++)
        {
            putFrame(frames[n]);
        }
    }

    /**
     * Give a frame to AOSService.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        if (frame.isDeleted()) return;

        /**
         * I created my template aos using the blank constructor because
         * I am only interested in the aos header.
         */
        aos.setFrame(frame);

        //correct the vcdu header. tbd.

        if (aos.isFillFrame())
        {
            frame.setFillFrame(true);
            aos.setDeleted(true);
            ++fillFrames.value;
        }
        else
        {
            ++outputFrames.value;
            int spid = aos.getSpacecraft();
            int vcid = aos.getVirtualChannel();
            lookup.set(spid,vcid);
            FrameReceiver target = outputMap.get(lookup);
            Frame bpduframe = new Frame(aos.getBPDUData());




            if (target != null)
            {
                target.putFrame(bpduframe);
                if (output != null) output.putFrame(bpduframe);
            }
            else
            {
                ++unrouteableCadus.value;
                if (deadletters != null) if (deadletters != null)
                {
                    deadletters.putFrame(frame);
                }
            }
        }
    }

    /**
     * This class holds spacecraft and virtual channel ids so they can be
     * stored as a key in a map.
     */
    class VcKey
    {
        int spid;
        int vcid;

        VcKey(int spid, int vcid)
        {
            this.spid = spid + 1;
            this.vcid = vcid + 1;
        }

        void set(int spid, int vcid)
        {
            this.spid = spid + 1;
            this.vcid = vcid + 1;
        }

        public int hashCode()
        {
            return (spid << 8) | vcid;
        }

        public boolean equals(Object obj)
        {
            boolean match = false;
            if ((obj != null) && (obj instanceof VcKey))
            {
                VcKey key = (VcKey)obj;
                match = spid == key.spid && vcid == key.vcid;
            }
            return match;
        }
    }
}
