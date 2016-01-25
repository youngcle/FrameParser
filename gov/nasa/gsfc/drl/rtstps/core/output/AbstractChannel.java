/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output;

import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsNode;
import gov.nasa.gsfc.drl.rtstps.core.Unit;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * This abstract class is the base class for some output channels. An output
 * channel is concerned with the output format and has little interest in the
 * actual output device. This class can construct the proper device class as
 * part of the load method.
 * 
 */
public abstract class AbstractChannel extends RtStpsNode implements Cloneable
{
    /**
     * If a channel encounters this number of consecutive write errors, it
     * stops writing to the output device and begins descarding data.
     */
    public static int CONSECUTIVE_PERMITTED_ERRORS = 5;

    /**
     * Unit type. The output unit is a frame, which includes the sync pattern
     * and all parity. You may optionally include frame annotation with each
     * written frame.
     */
    public static final String FRAME = "FRAME";

    /**
     * Unit type. The output unit is a CCSDS packet. You may optionally
     * include frame and packet annotation with each written packet.
     */
    public static final String PACKET = "PACKET";

    /**
     * Unit type. The output unit type may be almost anything -- frame,
     * packet, VCDU, OCF, bitstream data, etc. You may optionally include
     * frame annotation with each written unit.
     */
    public static final String GENERIC = "UNIT";

    /**
     * This field defines the type of unit (packet, frame, unit, etc) that
     * will be sent to this channel.
     */
    protected String unitType = GENERIC;

    /**
     * Annotation option. All unit annotation is written immediately before
     * the unit is written.
     */
    public static final String BEFORE = "BEFORE";

    /**
     * Annotation option. All unit annotation is written immediately after
     * the unit is written.
     */
    public static final String AFTER = "AFTER";

    /**
     * Annotation option. Only the unit is written to the device. No
     * annotation is written.
     */
    public static final String NO_ANNOTATION = "NONE";

    /**
     * This field determines if annotation is written with each unit and
     * where it is written -- before or after the unit data. Annotation
     * contains quality information and for frame annotation, the time
     * at which the frame was received. If a unit was constructed from
     * more than one frame, the frame quality is ANDed from the
     * contributing frames, but the time is the time of the first frame.
     * Each unit may include frame annotation, but only packets may
     * include packet annotation. Packet annotation always precedes
     * frame annotation.
     */
    protected String annotationOption = BEFORE;

    /**
     * A count of the number of units that have passed through this
     * channel.
     */
    protected LongStatusItem count;

    /**
     * A count of units that could not be written for any reason.
     */
    protected LongStatusItem outputErrorCount;

    /**
     * A count of units that the channel discarded because the consecutive
     * error threshold was triggered.
     */
    protected LongStatusItem dicardedCount;

    /**
     * The current number of consecutive write errors.
     */
    protected int consecutiveErrors = 0;

    /**
     * This is a device to which the channel writes data. A device is a special
     * class that envelopes a DataOutputStream.
     * I probably need a more robust class to handle the output streams than
     * even this.
     * (1) file: multiple files based upon a fixed file size
     * (2) network: handling disconnections and overruns
     * (3) quicklook
     */
    protected AbstractOutputDevice device = null;
    protected java.io.DataOutputStream output = null;



    /**
     * A constructor.
     */
    protected AbstractChannel(String elementName)
    {
        super(elementName);
    }

    /**
     * Set up this stps node with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        super.setLinkName(element.getAttribute("label"));

        unitType = element.getAttribute("unitType");
        annotationOption = element.getAttribute("annotation");

        if (element.getTagName().equals("file"))
        {
            device = new FileDevice(element,unitType);
        }
        else if (element.getTagName().equals("socket"))
        {
          
            try {
              device = new SocketDevice(element);
            } catch (Exception e) {
            	//System.out.println("No Simulcast connection available. Please ensure Simulcast server is running.");
              //System.out.println("failed to create socket device: " + e.toString());
            }
        }
        
        if (device != null)
        {
            output = device.getOutputStream();
        }

        count = new LongStatusItem("Output");
        outputErrorCount = new LongStatusItem("Errors");
        dicardedCount = new LongStatusItem("Discarded");

        statusItemList = new java.util.ArrayList<StatusItem>(3);	
        statusItemList.add(count);
        statusItemList.add(outputErrorCount);
        statusItemList.add(dicardedCount);
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
        if (device != null)
        {
            device.shutdown();
        }
    }

    /**
     * Format this unit's frame annotation into two 32-integers, and write
     * them to the output stream.
     * The high 16 bits of the first integer contains a quality mask.
     * The remaining bits contain the frame time.
     * <pre>
     *  [0]  bit 25     1=Frame contains an idle/fill VCDU (CCSDS state)
     *  [0]  bit 24     1=Frame has bad first header pointer (CCSDS error)
     *  [0]  bit 23     1=Path Service had problem composing a packet from
     *                      this frame
     *  [0]  bit 22     1=sequence error between this and preceding frame
     *  [0]  bit 21     1=frame is Reed Solomon uncorrectable
     *  [0]  bit 20     1=frame is Reed Solomon corrected
     *  [0]  bit 19     1=frame has CRC error
     *  [0]  bit 18     1=slipped frame
     *  [0]  bit 17     1=inverted frame (polarity was corrected)
     *  [0]  bit 16     1=lock frame
     *  [0]  bits 0-15  day of year (1-366)
     *  [1]  bits 0-31  milliseconds of day
     * </pre>
     */
    protected void writeFrameAnnotation(Unit unit)
            throws java.io.IOException
    {
        FrameAnnotation fa = unit.getFrameAnnotation();
        int quality = 0;
        if (fa.isLock) quality |= 1;
        if (fa.isInverted) quality |= 2;
        if (fa.isSlipped) quality |= 4;
        if (fa.hasCrcError) quality |= 8;
        if (fa.isRsCorrected) quality |= 0x10;
        if (fa.isRsUncorrectable) quality |= 0x20;
        if (fa.hasSequenceError) quality |= 0x40;
        if (fa.hasPacketDecompositionError) quality |= 0x80;
        if (fa.hasBadFirstHeaderPointer) quality |= 0x0100;
        if (fa.hasIdleVcdu) quality |= 0x200;

        final long MS_PER_DAY = 86400000L;
        int days = (int)(fa.timestamp / MS_PER_DAY);
        int msOfDay = (int)(fa.timestamp % MS_PER_DAY);

        output.writeInt((quality << 16) | days);
        output.writeInt(msOfDay);
    }
}
