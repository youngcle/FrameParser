/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output;
import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.Frame;
import gov.nasa.gsfc.drl.rtstps.core.FrameReceiver;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsNode;
import gov.nasa.gsfc.drl.rtstps.core.Unit;
import gov.nasa.gsfc.drl.rtstps.core.UnitReceiver;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketReceiver;

/**
 * This is a null channel. It discards everything.
 * 
 */
public final class NullChannel extends RtStpsNode implements Cloneable,
        FrameReceiver, UnitReceiver, PacketReceiver
{
    public static final String CLASSNAME = "null";

    /**
     * Set up this stps node with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        super.setLinkName(element.getAttribute("label"));
    }

    public NullChannel()
    {
        super(CLASSNAME);
    }

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     */
    public void finishSetup(Configuration configuration)
            throws RtStpsException
    {
    }

    /**
     * Flush the output channel.
     */
    public void flush() throws RtStpsException
    {
    }

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
    }

    /**
     * Give a unit to this UnitReceiver.
     */
    public void putUnit(Unit unit) throws RtStpsException
    {
    }

    /**
     * Give an array of units to this UnitReceiver.
     */
    public void putUnits(Unit[] units) throws RtStpsException
    {
    }

    /**
     * Give an array of packets to this PacketReceiver.
     */
    public void putPackets(Packet[] packets) throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException
    {
    }

    /**
     * Give a packet to this PacketReceiver.
     */
    public void putPacket(Packet packet) throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException
    {
    }
}

