/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.status;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * Viewer objects use this interface to get status items from the viewer's
 * status distributor or a proxy.
 * 
 */
public interface StatusListener
{
    /**
     * The listener gets a status item from the Distributor or its proxy.
     */
    public void processStatusItem(StatusItem item, String fullName);
}
