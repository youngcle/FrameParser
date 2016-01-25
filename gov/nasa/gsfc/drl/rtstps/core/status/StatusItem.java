/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.status;

/**
 * This class contains a single status item. It is most often a long
 * integer counter. The derivatives define the type of value.
 * 
 */
public abstract class StatusItem implements java.io.Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * A name for this item. It does not need to be unique. A client
     * will often display the name adjacent to the value.
     */
    private final String name;

    /**
     * If true, this item can be cleared. For integer type, it means
     * it can be set to zero.
     */
    protected boolean clearable = true;

    /**
     * Create a StatusItem with the given name.
     */
    protected StatusItem(String label)
    {
        name = label;
    }

    /**
     * Get the name.
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Set to clearable or not. A clearable status item may be set
     * to zero if an integer. An item is clearable by default.
     */
    public final void setClearable(boolean x)
    {
        clearable = x;
    }

    /**
     * Determine if this status item is clearable.
     */
    public final boolean isClearable()
    {
        return clearable;
    }

    /**
     * Get this item in the form "name=value".
     */
    public String toString()
    {
        return name + "=" + getValue();
    }

    /**
     * Get this item's value as a string.
     */
    public abstract String getValue();

    /**
     * Clear this status item. Nothing happens if the item is not clearable.
     */
    public abstract void clear();
}
