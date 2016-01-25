/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.status;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;
import gov.nasa.gsfc.drl.rtstps.server.StatusBlock;

/**
 * This class contains a thread that periodically sends a status request to
 * the RT-STPS server. It then distributes the status items to registered item
 * listeners.
 * <p>
 * If you are interested in the server's command state (unloaded, loaded and
 * stopped, or loaded and go), you can register here to receive the two
 * StatusItems defining command state. However, StatusItems are only delivered
 * periodically, so you won't get instantaneous response to button presses.
 * It is better that you register with class CommandState, which will give you
 * more immediate command state updates.
 * <p>
 * Status items are identified by a three part naming convention. An example
 * is "path.vc42.Idle VCDUs", The first part is the type. It is the class name
 * for an RT-STPS node and is also the element name in the XML setup file. For
 * example, "path" identifies all CCSDS path service nodes. The second part is
 * called block name here. It identifies a specific RT-STPS node and is the
 * XML id field for an element. For example, "vc42" is just one CCSDS path
 * service node of possibly many. The third part is the status item label for
 * one specific status value. In the example, it is the number of idle VCDUs
 * that the "vc42" path service node detected.
 *
 * 
 * 
 */
public final class Distributor extends Thread
{
    private gov.nasa.gsfc.drl.rtstps.server.RtStpsServices server;
    private long msTimeDelay = 5000L;
    private boolean threadRunning = true;
    private java.util.TreeMap<String,StatusListener> statusListeners = new java.util.TreeMap<String,StatusListener>();

    /**
     * Create a Distributor object.
     */
    public Distributor(gov.nasa.gsfc.drl.rtstps.server.RtStpsServices server)
    {
        this.server = server;
    }

    /**
     * Create a Distributor object.
     */
    public Distributor(gov.nasa.gsfc.drl.rtstps.server.RtStpsServices server, int secondsDelay)
    {
        this.server = server;
        setDelay(secondsDelay);
    }

    /**
     * Set the period in seconds between status collections. It must be
     * greater than zero seconds.
     */
    public void setDelay(int seconds)
    {
        if (seconds >= 1)
        {
            msTimeDelay = (long)seconds * 1000L;
        }
    }

    /**
     * Request periodic delivery of a status item.
     * @param sl The StatusListener who will receive delivery.
     * @param itemLabel The full identification of the status item in the
     *          form "typeName.blockName.itemName"
     */
    public synchronized void requestStatusItemDelivery(StatusListener sl,
            String itemLabel)
    {
        Object o = statusListeners.get(itemLabel);
        if (o == null)
        {
            statusListeners.put(itemLabel,sl);
        }
        else if (o instanceof PointServer)
        {
            PointServer ps = (PointServer)o;
            ps.addListener(sl);
        }
        else
        {
            StatusListener slx = (StatusListener)o;
            if (sl != slx)
            {
                statusListeners.put(itemLabel,new PointServer(slx,sl));
            }
        }
    }

    /**
     * Cancel status item delivery.
     * @param sl The StatusListener who will receive delivery.
     * @param itemLabel The full identification of the status item in the
     *          form "typeName.blockName.itemName"
     */
    public synchronized void cancelStatusItemDelivery(StatusListener sl,
            String itemLabel)
    {
        Object o = statusListeners.get(itemLabel);
        if (o != null)
        {
            if (o instanceof PointServer)
            {
                PointServer ps = (PointServer)o;
                int listenerCount = ps.removeListener(sl);
                if (listenerCount == 0)
                {
                    statusListeners.remove(itemLabel);
                }
            }
            else
            {
                StatusListener slx = (StatusListener)o;
                if (sl == slx)
                {
                    statusListeners.remove(itemLabel);
                }
            }
        }
    }

    /**
     * Stop the distributor status collection thread.
     */
    public void stopDistributor()
    {
        threadRunning = false;
    }

    /**
     * Get a list of block names for a type. For example, get all path
     * service block names for the "path" service type.
     * @param typeName the type name
     * @return the list of block names, which have the form "type.blockname"
     */
    public synchronized java.util.List<String> getBlockNamesByType(String typeName)	
            throws java.rmi.RemoteException
    {
        java.util.List<String> list = null;				
        StatusBlock[] statusBlocks = doStatus();
        if (statusBlocks != null)
        {
            list = getBlockNamesByType(typeName,statusBlocks,list);
        }
        return list;
    }

    /**
     * Get a list of full block names for a list of types.
     * @param typeNames Get all blocks that match the type name
     * @return the list of block names, which have the form "type.blockname"
     */
    public synchronized java.util.List<String> getBlockNamesByTypes(String[] typeNames)	
            throws java.rmi.RemoteException
    {
        java.util.List<String> list = null;				
        StatusBlock[] statusBlocks = doStatus();
        if (statusBlocks != null)
        {
            for (int t = 0; t < typeNames.length; t++)
            {
                list = getBlockNamesByType(typeNames[t],statusBlocks,list);
            }
        }
        return list;
    }

    /**
     * Get a list of block names for a type.
     * @param typeName Get all blocks that match the type name
     * @param statusBlocks a non-null list of status blocks
     * @param list It appends block names to this list. If null, it creates
     *          a new list.
     * @return the list of block names, which have the form "type.blockname"
     */
    private java.util.List<String> getBlockNamesByType(String typeName,	
            StatusBlock[] statusBlocks, java.util.List<String> list)	
            throws java.rmi.RemoteException
    {
        if (list == null)
        {
            list = new java.util.ArrayList<String>(statusBlocks.length);	
        }

        for (int s = 0; s < statusBlocks.length; s++)
        {
            StatusBlock sb = statusBlocks[s];
            String type = sb.getType();
            if (typeName.equals(type))
            {
                list.add(sb.toString());
            }
        }

        return list;
    }

    /**
     * Get a list of status item names for a block.
     */
    public synchronized String[] getItemNamesByBlock(String blockName)
            throws java.rmi.RemoteException
    {
        String[] names = null;
        StatusBlock[] statusBlocks = doStatus();
        if (statusBlocks != null)
        {
            for (int s = 0; s < statusBlocks.length; s++)
            {
                StatusBlock sb = statusBlocks[s];
                if (blockName.equals(sb.getName()))
                {
                    StatusItem[] items = sb.getStatusItems();
                    names = new String[items.length];
                    for (int n = 0; n < items.length; n++)
                    {
                        names[n] = items[n].getName();
                    }
                    break;
                }
            }
        }
        return names;
    }

    /**
     * Deliver status items to all listeners.
     * @return the StatusBlock list.
     */
    public synchronized StatusBlock[] doStatus()
            throws java.rmi.RemoteException
    {
        StatusBlock[] statusBlocks = server.getStatus();
        if (statusBlocks != null)
        {
            for (int s = 0; s < statusBlocks.length; s++)
            {
                StatusBlock sb = statusBlocks[s];
                String identity = sb.toString();
                StatusItem[] statusItems = sb.getStatusItems();
                for (int i = 0; i < statusItems.length; i++)
                {
                    StatusItem si = statusItems[i];
                    String fullId = identity + '.' + si.getName();
                    StatusListener sl = (StatusListener)statusListeners.get(fullId);
                    if (sl != null)
                    {
                        sl.processStatusItem(si,fullId);
                    }
                }
            }
        }
        return statusBlocks;
    }

    public void run()
    {
        threadRunning = true;
        while (threadRunning)
        {
            try
            {
                doStatus();
            }
            catch (java.rmi.RemoteException rme)
            {
                //rme.printStackTrace();
		System.out.println("RT-STPS Viewer: Server status request failed. The RT-STPS Server may be busy or closed. Retrying...");
            }

            try
            {
                sleep(msTimeDelay);
            }
            catch(InterruptedException e)
            {
                threadRunning = false;
            }
        }
    }
}
