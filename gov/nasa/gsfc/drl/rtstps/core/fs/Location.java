/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;

/**
 * This simple class contains byte and bit location information within
 * a byte array.
 * 
 */
class Location implements Comparable<Location>
{
    /**
     * A byte offset into an array.
     */
    int offset;

    /**
     * A bit offset into a byte.
     */
    int bit;

    /**
     * Create a Location object.
     */
    Location(int offset, int bit)
    {
        this.offset = offset;
        this.bit = bit;
    }

    /**
     * Compare two locations.
     * @return A positive value if this location is after the passed location,
     *          A negative value if this location is before the passed location,
     *          and zero if they are equal.
     */
    public int compareTo(Location o)
    {
        //I won't bother checking for null o or wrong cast.
        Location loc = (Location)o;
        int oo = offset - loc.offset;
        if (oo == 0)
        {
            oo = bit - loc.bit;
        }
        return oo;
    }

    public String toString()
    {
        return "offset="+offset+" bit="+bit;
    }


}
