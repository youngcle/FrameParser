/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.server;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class contains the status information for one RtStpsNode object.
 * It is not created for those nodes that do not produce status. The core
 * nodes do not use it directly. It is merely a container for transport to
 * a RT-STPS status listener.
 * <p>
 * The status block has two labels: a type and a name. The type string
 * is the node class name and corresponds to the XML element name. The name
 * string is the name for a particular node instance and corresponds to the
 * link name. The link name is always unique even across different types.
 * <p>
 * Types are predefined. "frame_sync", "frame_status", and "packet" are
 * type examples. Names are user defined and correspond to the label attribute
 * in most XML elements. They can be anything.
 * <p>
 * Some nodes have a single type and name. Those nodes are singletons. For
 * example, "frame_status" is the type and name of the FrameStatus node.
 * <p>
 * Note that it is possible to create s status block that is not node based.
 * The server itself creates a status block that contains state information.
 *
 * 
 * 
 */
public class StatusBlock implements java.io.Serializable
{
    /**
     * The RtStpsNode type or class name. It is also the element tag name from
     * the XML setup file.
     */
    private String type;

    /**
     * The RtStpsNode link name, which is also the XML label attribute.
     * For singleton nodes such as "frame_sync", the type and name are the
     * same.
     */
    private String name;

    /**
     * A list of status items for this node. It should never be null or empty.
     */
    private StatusItem[] statusItems;


    /**
     * Create a StatusBlock from an RtStpsNode.
     */
    StatusBlock(gov.nasa.gsfc.drl.rtstps.core.RtStpsNode node)
    {
        type = node.getElementName();
        name = node.getLinkName();
        Collection<StatusItem> status = node.getStatusItems();
        int count = status.size();
        statusItems = new StatusItem[count];

        Iterator<StatusItem> i = status.iterator();
        int n = 0;
        while (i.hasNext())
        {
            statusItems[n++] = (StatusItem)i.next();
        }
    }

    /**
     * A constructor that is not node-based.
     * @param btype A type name that corresponds to an element name for
     *          RT-STPS node status blocks.
     * @param bname A link name that corresponds to a link name for
     *          RT-STPS node status blocks.
     * @param itemList A list of status items for this block.
     */
    StatusBlock(String btype, String bname, StatusItem[] itemList)
    {
        type = btype;
        name = bname;
        statusItems = itemList;
    }

    /**
     * Get the type string. (the element tag)
     */
    public final String getType()
    {
        return type;
    }

    /**
     * Get the name string. (the link name)
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Get the array of StatusItems. It is never null or empty.
     */
    public final StatusItem[] getStatusItems()
    {
        return statusItems;
    }

    /**
     * Gets a combined string consisting of the type and name separated by
     * a dot. However, it returns just the type (or name, which is the same)
     * if the node is a singleton.
     */
    public String toString()
    {
        String x;
        if (type.equals(name))
        {
            x = type;
        }
        else
        {
            x = type + '.' + name;
        }
        return x;
    }

    /**
     * Zero/clear the status items in this block. This is meaningful to the
     * server only.
     */
    void clear()
    {
        for (int n = 0; n < statusItems.length; n++)
        {
            statusItems[n].clear();
        }
    }
    
    private static final long serialVersionUID = 1L;			
}

