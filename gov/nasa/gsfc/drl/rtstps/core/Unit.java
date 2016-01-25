/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * This abstract class is the base class for all units, such as packets,
 * frames, etc.
 * 
 */
public abstract class Unit
{
    /**
     * All units that we use are derived from frames, so each unit has
     * associated frame annotation.
     */
    protected FrameAnnotation frameAnnotation;

    /**
     * The unit's data by default is stored in this array.
     */
    protected byte[] data = null;

    /**
     * Some units do not begin at byte offset zero of the data array.
     * This is the unit's starting byte offset.
     */
    protected int startOffset = 0;

    /**
     * Some units are shorter than the full data array length.
     * This is the unit's actual length.
     */
    protected int length;

    /**
     * When true, this unit is marked as deleted. In almost all cases, all
     * classes skip deleted frames. (One known exception is CCSDS v2 fill
     * frames, which are deleted. They may contains valid insert zone data.)
     */
    protected boolean deleted = false;

    /**
     * Get this unit's start offset within its byte array.
     */
    public final int getStartOffset()
    {
        return startOffset;
    }

    /**
     * Create a unit.
     * @param length The number of bytes to be allocated to the unit.
     */
    protected Unit(int length)
    {
        data = new byte[length];
        this.length = length;
    }

    /**
     * Create a unit. This constructor version does not allocate memory
     * for the data.
     */
    protected Unit()
    {
    }

    /**
     * Get this unit's frame annotation.
     */
    public final FrameAnnotation getFrameAnnotation()
    {
        return frameAnnotation;
    }

    /**
     * Set this unit's frame annotation.
     */
    public void setFrameAnnotation(FrameAnnotation a)
    {
        frameAnnotation = a;
    }

    /**
     * Get this unit's data.
     */
    public final byte[] getData()
    {
        return data;
    }

    /**
     * Get this unit's actual size in bytes. The actual size may be less
     * than the data array size.
     */
    public final int getSize()
    {
        return length;
    }

    /**
     * Mark this unit as deleted or not deleted.
     */
    public void setDeleted(boolean d)
    {
        deleted = d;
    }

    /**
     * Determine if this is a deleted frame.
     */
    public final boolean isDeleted()
    {
        return deleted;
    }
}
