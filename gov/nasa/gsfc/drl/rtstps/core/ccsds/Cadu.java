/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds;
import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.Frame;
import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;
import gov.nasa.gsfc.drl.rtstps.core.ReedSolomonDecoder;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsNode;
import gov.nasa.gsfc.drl.rtstps.core.Unit;

import java.util.TreeMap;

/**
 * This class is a CADU, which is a CCSDS version 2 frame. It is a unit
 * itself, but it also encapsulates a frame, which it promotes to a CADU.
 * 
 */
public final class Cadu extends Unit
{
    private static final int FILL_MASK = 0x03f;
    private Frame frame = null;
    private int dataZoneStartOffset = 10;
    private int dataZoneEndOffset;
    private int trailerLength = 0;
    private int rsParityLength = 0;

    /**
     * Create a CADU with the characteristics defined by the setup. The
     * CADU does not contain a frame at this point. You must defer creating
     * a CADU with this constructor until after the Builder has created all
     * STPS nodes because CADU needs special information from them such as
     * parity lengths.
     */
    public Cadu(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        String spacecraftName = element.getAttribute("spacecraft");
        Spacecraft spacecraft =
                (Spacecraft)configuration.getSpacecrafts().get(spacecraftName);

        if (spacecraft == null)
        {
            throw new RtStpsException(element.getTagName() + " " +
                    element.getAttribute("label") +
                    " does not reference a defined spacecraft.");
        }

        TreeMap<String, RtStpsNode> nodes = configuration.getStpsNodes();

        /**
         * There is only one reed solomon node, so the node name is the same
         * as the class name.
         */
        String rsNodeName = ReedSolomonDecoder.CLASSNAME;
        ReedSolomonDecoder rs = (ReedSolomonDecoder)nodes.get(rsNodeName);
        if (rs != null)
        {
            rsParityLength = rs.getParityLength();
            trailerLength = rsParityLength;
        }

        boolean crcParityPresent = Convert.toBoolean(element,"crcParityPresent",false);
        if (crcParityPresent || (nodes.get("crc") != null))
        {
            trailerLength += 2;
        }

        boolean ocfPresent = Convert.toBoolean(element,"OCFpresent",false);
        if (ocfPresent) trailerLength += 4;

        dataZoneStartOffset = 10;  //sync pattern + VCDU header
        if (spacecraft.headerErrorControlPresent) dataZoneStartOffset += 2;
        dataZoneStartOffset += spacecraft.insertZoneLength;
    }

    /**
     * This CADU constructor is incomplete because it omits key elements,
     * such as parity length. Some services do not care about this because
     * they simply want the CADU to interpret the CADU header and nothing
     * more. This constructor is adequate for that purpose.
     */
    public Cadu()
    {
    }

    /**
     * Set this CADU's frame, which promotes the frame to a CADU and makes
     * this class fully functional. You may use the same Cadu object with
     * different frames.
     */
    public void setFrame(Frame frame)
    {
        this.frame = frame;
        data = frame.getData();
        frameAnnotation = frame.getFrameAnnotation();
        startOffset = 0;
        length = frame.getSize();
        deleted = frame.isDeleted();
        dataZoneEndOffset = frame.getSize() - trailerLength - 1;
    }

    /**
     * Get the CADU's Reed Solomon parity length.
     */
    public final int getReedSolomonParityLength()
    {
        return rsParityLength;
    }

    /**
     * Get the data zone start offset in bytes from the frame's start.
     */
    public final int getdataZoneStartOffset()
    {
        return dataZoneStartOffset;
    }

    /**
     * Get the data zone end offset in bytes from the frame's start.
     */
    public final int getdataZoneEndOffset()
    {
        return dataZoneEndOffset;
    }

    /**
     * Get the frame's CCSDS frame version number.
     */
    public int getVersion()
    {
        //must be 1 for CADUs.
        return ((int)data[4] >> 6) & 3;
    }

    /**
     * Get the frame's spacecraft ID.
     */
    public int getSpacecraft()
    {
        int p = ((int)data[4] & 0x03f) << 2;
        return p | (((int)data[5] >> 6) & 3);
    }

    /**
     * Get the frame's virtual channel number.
     */
    public int getVirtualChannel()
    {
        return (int)data[5] & 0x03f;
    }

    /**
     * Determine if this is a fill (idle) frame.
     */
    public boolean isFillFrame()
    {
        return ((int)data[5] & 0x03f) == FILL_MASK;
    }

    /**
     * Get this frame's sequence number.
     */
    public int getSequenceCount()
    {
        int s = ((int)data[6] & 0x0ff) << 16;
        s |= ((int)data[7] & 0x0ff) << 8;
        s |= ((int)data[8] & 0x0ff);
        return s;
    }

    /**
     * Get this frame's header error control word.
     */
    public int getHeaderErrorControlWord()
    {
        int h = ((int)data[10] << 8) & ((int)data[11] & 0x0ff);
        return h & 0x0ffff;
    }

    /**
     * Set this cadu's frame annotation.
     */
    public void setFrameAnnotation(FrameAnnotation a)
    {
        frameAnnotation = a;
        frame.setFrameAnnotation(a);
    }

    /**
     * Mark this cadu as deleted or not deleted.
     */
    public void setDeleted(boolean d)
    {
        deleted = d;
        frame.setDeleted(d);
    }
}
