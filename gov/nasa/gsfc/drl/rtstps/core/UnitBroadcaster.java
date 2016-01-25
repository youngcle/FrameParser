/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;
import java.util.Iterator;

/**
 * RtStpsNodes that send units to UnitReceivers use this class to broadcast
 * units to more than one UnitReceiver.
 * 
 */
public final class UnitBroadcaster extends Broadcaster implements UnitReceiver
{
    /**
     * Create a UnitBroadcaster with an initial two target receivers.
     */
    public UnitBroadcaster(String name, UnitReceiver ur1, UnitReceiver ur2)
    {
        super(name,ur1,ur2);
    }

    /**
     * Give an array of units to this UnitReceiver.
     */
    public void putUnits(Unit[] units) throws RtStpsException
    {
        Iterator<Receiver> i = output.iterator();
        while (i.hasNext())
        {
            UnitReceiver ur = (UnitReceiver)i.next();
            ur.putUnits(units);
        }
    }

    /**
     * Give a unit to this UnitReceiver.
     */
    public void putUnit(Unit unit) throws RtStpsException
    {
        if (!unit.isDeleted())
        {
            Iterator<Receiver> i = output.iterator();
            while (i.hasNext())
            {
                UnitReceiver ur = (UnitReceiver)i.next();
                ur.putUnit(unit);
            }
        }
    }
}
