/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * Any class that implements UnitReceiver accepts units. This interface
 * is for receivers other than packet and frame receivers. It is used
 * primarily to handle CCSDS services besides the path packet service.
 * 
 */
public interface UnitReceiver extends Receiver
{
    /**
     * Give a unit to this UnitReceiver.
     */
    public void putUnit(Unit unit) throws RtStpsException;

    /**
     * Give an array of units to this UnitReceiver.
     */
    public void putUnits(Unit[] units) throws RtStpsException;

    /**
     * Flush the data pipeline.
     */
    public void flush() throws RtStpsException;

    /**
     * Get this receiver's name (for error messages).
     */
    public String getLinkName();
}
