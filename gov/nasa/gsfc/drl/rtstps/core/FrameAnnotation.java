/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;


/**
 * This class holds frame annotation information.
 * 
 * 
 */
public class FrameAnnotation implements Cloneable
{
    /**
     * A timestamp associated with this frame. It is milliseconds from some
     * epoch.
     */
    public long timestamp;

    /**
     * If true, the Frame Synchronizer was in lock mode when it received
     * this frame.
     */
    public boolean isLock = true;

    /**
     * If true, the frame was originally inverted. The polarity may or may
     * not have been corrected depending on a setup option.
     */
    public boolean isInverted = false;

    /**
     * If true, the Frame Synchronizer discovered that this frame was slipped
     * either 1 or 2 bits long or short.
     */
    public boolean isSlipped = false;

    /**
     * If true, this frame has a CRC error. It can only be true if CRC
     * detection is enabled.
     */
    public boolean hasCrcError = false;

    /**
     * If true, this frame had a Reed Solomon error, but the RS subsystem
     * corrected it. The flag can only be true if the frame is Reed Solomon
     * encoded and Reed Solomon block correction was enabled.
     */
    public boolean isRsCorrected = false;

    /**
     * If true, this frame had a Reed Solomon uncorrectable error that the RS
     * subsystem could or did not correct. This flag can only be true if the
     * frame is Reed Solomon encoded and Reed Solomon block detection was
     * enabled.
     */
    public boolean isRsUncorrectable = false;

    /**
     * If true, there is a sequence error between this frame and the previous
     * one. This flag is only meaningful for frames that have a sequence field,
     * such as CCSDS frames. It indicates a data dropout.
     */
    public boolean hasSequenceError = false;

    /**
     * If true, the CCSDS Path Service, which assembles packets, had some
     * unspecified problem extracting packets from this frame. It extracted
     * as much as possible before discarding some data. This flag is only
     * meaningful for CCSDS frames that have been routed through the CCSDS
     * Path Service.
     */
    public boolean hasPacketDecompositionError = false;

    /**
     * If true, the CCSDS Path Service determined that this frame has an
     * invalid first header pointer, so it was forced to ignore the entire
     * frame. This flag is only meaningful for CCSDS frames that have been
     * routed through the CCSDS Path Service.
     */
    public boolean hasBadFirstHeaderPointer = false;

    /**
     * If true, the VCDU (Virtual Channel Data Unit) inside this frame contains
     * all fill data. This flag is only meaningful if the frame is a CCSDS
     * version 2 frame.
     */
    public boolean hasIdleVcdu = false;



    /**
     * Resets the annotation fields to "at start" values so that they can be
     * used for a different frame.
     */
    public void reset()
    {
        timestamp = 0L;
        isLock = true;
        isInverted = false;
        isSlipped = false;
        hasCrcError = false;
        isRsCorrected = false;
        isRsUncorrectable = false;

        hasSequenceError = false;
        hasPacketDecompositionError = false;
        hasBadFirstHeaderPointer = false;
        hasIdleVcdu = false;
    }

    /**
     * Determine if this annotation has the same quality information as another
     * frame annotation object.
     */
    public boolean isEqualQuality(FrameAnnotation fa)
    {
        if (isLock != fa.isLock) return false;
        if (isInverted != fa.isInverted) return false;
        if (isSlipped != fa.isSlipped) return false;
        if (hasCrcError != fa.hasCrcError) return false;
        if (isRsCorrected != fa.isRsCorrected) return false;
        if (isRsUncorrectable != fa.isRsUncorrectable) return false;
        if (hasSequenceError != fa.hasSequenceError) return false;
        if (hasPacketDecompositionError != fa.hasPacketDecompositionError) return false;
        if (hasBadFirstHeaderPointer != fa.hasBadFirstHeaderPointer) return false;
        return (hasIdleVcdu != fa.hasIdleVcdu);
    }

    /**
     * Absorb the quality of the passed frame annotation into the quality of
     * this frame annotation so that it shows the worse of both.
     */
    public void addQuality(FrameAnnotation fa)
    {
        isLock = isLock && fa.isLock;
        if (fa.isInverted) isInverted = fa.isInverted;
        if (fa.isSlipped) isSlipped = fa.isSlipped;
        if (fa.hasCrcError) hasCrcError = fa.hasCrcError;
        if (fa.isRsCorrected) isRsCorrected = fa.isRsCorrected;
        if (fa.isRsUncorrectable) isRsUncorrectable = fa.isRsUncorrectable;
        if (fa.hasSequenceError) hasSequenceError = fa.hasSequenceError;
        if (fa.hasPacketDecompositionError) hasPacketDecompositionError = fa.hasPacketDecompositionError;
        if (fa.hasBadFirstHeaderPointer) hasBadFirstHeaderPointer = fa.hasBadFirstHeaderPointer;
        if (fa.hasIdleVcdu) hasIdleVcdu = fa.hasIdleVcdu;
    }

    /**
     * Create a copy of this object.
     */
    public Object clone()
    {
        Object fa = null;
        try
        {
            fa = super.clone();
        }
        catch (CloneNotSupportedException cnse) {}
        return fa;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer(512);

        sb.append("time=");
        sb.append(timestamp);
        sb.append(isLock? " lock" : " flywheel");
        sb.append(isInverted?  " inverted " : " true");
        if (isSlipped) sb.append(" slipped");
        if (hasCrcError) sb.append(" crc");
        if (isRsCorrected) sb.append(" corrected");
        if (isRsUncorrectable) sb.append(" uncorrectable");

        if (hasSequenceError) sb.append(" seqError");
        if (hasPacketDecompositionError) sb.append(" pktError");
        if (hasBadFirstHeaderPointer) sb.append(" badFhp");
        if (hasIdleVcdu) sb.append(" idleVcdu");

        return sb.toString();
    }
}
