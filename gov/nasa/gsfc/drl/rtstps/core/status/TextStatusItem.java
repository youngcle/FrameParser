/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.status;

/**
 * This is a text status item. It usually holds states.
 * 
 * 
 */
public final class TextStatusItem extends StatusItem
        implements java.io.Serializable
{
    /**
     * The string status value. It is public because the RT-STPS nodes
     * that set it will access it directly.
     */
    public String value = "";

    /**
     * Create a TextStatusItem with the given name.
     */
    public TextStatusItem(String label)
    {
        super(label);
    }

    /**
     * Create a TextStatusItem with the given name and initial value.
     */
    public TextStatusItem(String label, String initialValue)
    {
        super(label);
        value = initialValue;
    }

    /**
     * Get this item's value as a string.
     */
    public final String getValue()
    {
        return value;
    }

    /**
     * Clear this status item. Nothing happens if the item is not clearable.
     * By default, it is set to an empty string if clearable.
     */
    public void clear()
    {
        if (clearable) value = "";
    }
    
    private static final long serialVersionUID = 1L;			
}
