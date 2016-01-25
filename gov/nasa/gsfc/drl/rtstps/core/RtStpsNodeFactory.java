/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

/**
 * This class is an RT-STPS node factory. It maps unique node ids to blank
 * instances of nodes. The node id is the element tag in the xml
 * configuration file and is a class variable. (It is not the same as
 * a link name, which is the unique name given to one instance of a node.)
 * To create a node, pass an id, and the factory returns a clone of the
 * mapped node.
 * 
 * 
 */
public class RtStpsNodeFactory extends java.util.TreeMap<String,RtStpsNode>	
{
    /**
     * Add an RT-STPS node (RtStpsNode) object to the factory. This node is an
     * empty template and should have significant state because it will be
     * cloned to create useable instances.
     * @param id A node type name, which often matches a element tag name.
     *          It must be unique.
     * @param node The RT-STPS node
     */
    public void addNode(String id, RtStpsNode node) throws RtStpsException
    {
        Object duplicate = put(id,node);
        if (duplicate != null)
        {
            throw new RtStpsException(id + " is not a unique RT-STPS node name.");
        }
    }

    /**
     * Create an RtStpsNode.
     * @param id A node type name.
     * @return An RtStpsNode. It is not initialized, so you must load it.
     */
    public RtStpsNode create(String id) throws RtStpsException
    {
        RtStpsNode node = (RtStpsNode)get(id);
        if (node == null)
        {
            throw new RtStpsException(id + " is an unknown node type.");
        }

        try
        {
            node = (RtStpsNode)node.clone();
        }
        catch (CloneNotSupportedException cnse)
        {
            throw new RtStpsException(id + " class is not cloneable.");
        }

        return node;
    }

    /**
     * Create an RtStpsNode.
     * @param element An XML element from the RT-STPS configuration file.
     *          The element tag name is the RT-STPS Node id.
     * @param config A collection of configuration items.
     * @return An RtStpsNode. It is initialized.
     */
    public RtStpsNode create(org.w3c.dom.Element element, Configuration config)
            throws RtStpsException
    {
        String id = element.getTagName();
        RtStpsNode node = (RtStpsNode)get(id);
        if (node == null)
        {
            throw new RtStpsException(id + " is an unknown node type.");
        }

        try
        {
            node = (RtStpsNode)node.clone();
        }
        catch (CloneNotSupportedException cnse)
        {
            throw new RtStpsException(id + " class is not cloneable.");
        }

        node.load(element,config);

        return node;
    }
    
    private static final long serialVersionUID = 1L;			
}
