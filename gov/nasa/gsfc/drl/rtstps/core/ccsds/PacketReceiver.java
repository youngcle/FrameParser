/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds;

/**
 * Any class that implements PacketReceiver accepts packets from a PacketSender.
 * 
 */
public interface PacketReceiver extends gov.nasa.gsfc.drl.rtstps.core.Receiver
{
    /**
     * Give an array of packets to this PacketReceiver.
     */
    public void putPackets(Packet[] packets) throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

    /**
     * Give a packet to this PacketReceiver.
     */
    public void putPacket(Packet packet) throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

    /**
     * Flush the data pipeline.
     */
    public void flush() throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

    /**
     * Get this receiver's name (for error messages).
     */
    public String getLinkName();
}
