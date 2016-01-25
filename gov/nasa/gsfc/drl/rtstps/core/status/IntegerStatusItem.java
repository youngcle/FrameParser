/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.status;

/**
 * This is an integer status item. It usually holds counters.
 * 
 * 
 */
public final class IntegerStatusItem extends StatusItem implements
        java.io.Serializable
{
    /**
     * The 32-bit status value. It is public because the RT-STPS nodes
     * that set it will access it directly.
     */
    public int value = 0;

    /**
     * Create an IntegerStatusItem with the given name.
     */
    public IntegerStatusItem(String label)
    {
        super(label);
    }

    /**
     * Get this item's value as a string.
     */
    public final String getValue()
    {
        return Integer.toString(value);
    }

    /**
     * Get this item's value as aN integer.
     */
    public final int getIntValue()
    {
        return value;
    }

    /**
     * Zero this status item. Nothing happens if the item is not clearable.
     */
    public void clear()
    {
        if (clearable) value = 0;
    }
    
    private static final long serialVersionUID = 1L;			
}
