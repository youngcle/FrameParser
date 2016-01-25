/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

import java.util.Iterator;

/**
 * This is the base class for broadcasters. RtStpsNodes that send units (frames,
 * packets, or units) to receivers use a broadcaster to send them to more than
 * one receiver.<p>
 * All broadcasters could be made into RtStpsNodes without difficulty. I did not
 * do this because I embed a broadcaster in every RT-STPS node, so there is no
 * need for a standalone broadcaster node.
 * 
 * 
 */
public abstract class Broadcaster implements Receiver
{
    protected String name;
    protected java.util.ArrayList<Receiver> output = new java.util.ArrayList<Receiver>();	

    /**
     * Create a Broadcaster with an initial two target receivers.
     * @param name A name used to tag error messages.
     * @param r1 The first receiver.
     * @param r2 The second receiver.
     */
    protected Broadcaster(String name, Receiver r1, Receiver r2)
    {
        this.name = name;
        output.add(r1);
        output.add(r2);
    }

    /**
     * Get this broacaster's name.
     */
    public String getLinkName()
    {
        return name + ".broadcaster";
    }

    /**
     * Add a receiver to the broadcast list of receivers. It is the user's
     * responsibility to ensure that the receiver is of the correct type.
     */
    public void addReceiver(Receiver r)
    {
        output.add(r);
    }

    /**
     * Flush the pipeline.
     */
    public void flush() throws RtStpsException
    {
        Iterator<Receiver> i = output.iterator();
        while (i.hasNext())
        {
            Receiver r = (Receiver)i.next();
            //System.out.println("Broadcaster flushing link " + r.getLinkName());
            
            // Don't let a failed broadcast output prevent the other outputs
            // from being flushed...
            try {
            	r.flush();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }

    public String toString()
    {
        return name;
    }
}
