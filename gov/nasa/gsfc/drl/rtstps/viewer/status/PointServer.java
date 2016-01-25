/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.status;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.util.Iterator;

/**
 * This is a service class for the Distributor. It handles the distribution
 * of one StatusItem to more than one registeed status listener.
 * 
 * 
 */
class PointServer implements StatusListener
{
    private java.util.LinkedList<StatusListener> listeners = new java.util.LinkedList<StatusListener>();	

    /**
     * Create a PointServer with two initial StatusListeners.
     */
    PointServer(StatusListener sl1, StatusListener sl2)
    {
        listeners.add(sl1);
        if (sl1 != sl2) listeners.add(sl2);
    }

    /**
     * Add a status listener.
     */
    synchronized void addListener(StatusListener sl)
    {
        if (!listeners.contains(sl))
        {
            listeners.add(sl);
        }
    }

    /**
     * Remove a status listener.
     */
    synchronized int removeListener(StatusListener sl)
    {
        listeners.remove(sl);
        return listeners.size();
    }

    /**
     * The listener gets a status item from the Distributor or its proxy.
     */
    public void processStatusItem(StatusItem item, String fullId)
    {
        synchronized (this)
        {
            Iterator<StatusListener> si = listeners.iterator();
            while (si.hasNext())
            {
                StatusListener sl = (StatusListener)si.next();
                sl.processStatusItem(item,fullId);
            }
        }
    }
}
