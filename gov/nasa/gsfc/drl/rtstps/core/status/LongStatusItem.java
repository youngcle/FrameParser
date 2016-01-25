/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.status;

/**
 * This is a long integer status item. It usually holds counters.
 * 
 * 
 */
public final class LongStatusItem extends StatusItem
        implements java.io.Serializable
{
    /**
     * The 64-bit status value. It is public because the RT-STPS nodes
     * that set it will access it directly. Other users, such as gadgets
     * within viewers, should please use the <code>getLongValue()</code>
     * method instead. There is no assurance that this value will remain
     * public.
     */
    public long value = 0L;

    /**
     * Create a LongStatusItem with the given name.
     */
    public LongStatusItem(String label)
    {
        super(label);
    }

    /**
     * Create a LongStatusItem with the given name and an initial value.
     */
    public LongStatusItem(String label, long initialValue)
    {
        super(label);
        value = initialValue;
    }

    /**
     * Get this item's value as a string.
     */
    public final String getValue()
    {
        return Long.toString(value);
    }

    /**
     * Get this item's value as a long integer.
     */
    public final long getLongValue()
    {
        return value;
    }

    /**
     * Zero this status item. Nothing happens if the item is not clearable.
     */
    public void clear()
    {
        if (clearable) value = 0L;
    }
    
    private static final long serialVersionUID = 1L;			
}
