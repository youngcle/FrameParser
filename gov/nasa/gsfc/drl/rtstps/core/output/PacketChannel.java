/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketReceiver;

/**
 * NO ANNOTATION, PACKET OUTPUT CHANNEL.
 * This packet output channel writes packets to the output stream. It does
 * not attach any annotation.
 * 
 */
public final class PacketChannel extends AbstractPacketChannel
        implements PacketReceiver, Cloneable
{
    /**
     * This is a class name for this RT-STPS node type. It is not the link name,
     * which is the name of one particular object.
     */
    public static final String CLASSNAME = "PACKET_NONE";

    /**
     * The constructor.
     */
    public PacketChannel()
    {
        super(CLASSNAME);
    }

    /**
     * Give a packet to this PacketReceiver.
     */
    public void putPacket(Packet packet) throws gov.nasa.gsfc.drl.rtstps.core.RtStpsException
    {
        if (!packet.isDeleted())
        {
            if (consecutiveErrors == CONSECUTIVE_PERMITTED_ERRORS)
            {
                ++dicardedCount.value;
            }
            else
            {
              if (output != null) {	// in case a socket connection was refused or the file (directory) doesn't exist or couldn't be created
                try
                {
                    output.write(packet.getData(),0,packet.getSize());
                   // output.flush(); // some receivers get incomplete chunks and they break; this helps
                    ++count.value;
                    consecutiveErrors = 0;
                }
                catch (java.io.IOException e)
                {
                    ++outputErrorCount.value;
                    ++consecutiveErrors;
                }
              }
            }
        }
    }
}
