/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.util.Collection;
import java.util.Iterator;

/**
 * This is the base class for all nodes in the RT-STPS system.
 * 
 * 
 */
public abstract class RtStpsNode implements Cloneable
{
    /**
     * Every public node should have a unique name so that it can be linked
     * to other nodes.
     */
    protected String linkName = null;

    /**
     * This is a class name for the derived node type. It is also the element
     * name in the XML setup file. It must be unique for each node type.
     */
    protected String typeName = null;

    /**
     * A list of StatusItem objects. If this derived node collects status,
     * then create a statusItemList and add StatusItem objects to it. By
     * default, it does not create a statusItemList.
     */
    protected java.util.Collection<StatusItem> statusItemList = null;	


    /**
     * A constructor.
     * @param elementName The XML element name. Also the class name.
     */
    protected RtStpsNode(String elementName)
    {
        typeName = elementName;
    }

    /**
     * A constructor.
     * @param elementName The XML element name. Also the class name.
     * @param linkName The link name. Also the object name.
     */
    protected RtStpsNode(String elementName, String linkName)
    {
        typeName = elementName;
        this.linkName = linkName;
    }

    /**
     * Get the link name. It is used to link this node to other nodes.
     */
    public final String getLinkName()
    {
        return linkName;
    }

    /**
     * Get the element name. It is also the class type name.
     */
    public final String getElementName()
    {
        return typeName;
    }

    /**
     * Set the link name. Every node must have a unique name.
     */
    public final void setLinkName(String name)
    {
        linkName = name;
    }

    /**
     * Configure from an XML document. You cannot assume that any other stps
     * nodes have been created.
     */
    public abstract void load(org.w3c.dom.Element element,
            Configuration configuration) throws RtStpsException;

    /**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     */
    public abstract void finishSetup(Configuration configuration)
            throws RtStpsException;

    /**
     * Get the status item list, which is a collection of StatusItem objects.
     * An RtStpsNode is not required to collect status.
     */
    public final Collection<StatusItem> getStatusItems()
    {
        return statusItemList;
    }

    /**
     * Clear items in the StatusItemList.
     */
    public final void clear()
    {
        if (statusItemList != null)
        {
            Iterator<StatusItem> i = statusItemList.iterator();
            while (i.hasNext())
            {
                StatusItem si = (StatusItem)i.next();
                si.clear();
            }
        }
    }

    public String toString()
    {
        String x;
        if (typeName.equals(linkName)) x = typeName;
        else x = typeName + '.' + linkName;
        return x;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
