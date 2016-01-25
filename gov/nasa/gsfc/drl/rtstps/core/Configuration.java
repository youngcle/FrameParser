/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Spacecraft;

/**
 * This class contains some information that is derived from the session
 * setup, which is an XML Document.<p>
 * If your node needs information from another node, you have three ways
 * to get it.<br>
 * 1. You can reference the field from the XML setup document. From your
 * load method, you get the Document from your Element, and from there
 * you can get any element in the document.<br>
 * 2. You can get the information from the node directly. You will need
 * to add a public method to the target node to return the field, and you
 * should defer getting the information until your node's "finishSetup"
 * method. "finishSetup" is an empty stub in RtStpsNode. Builder calls it for
 * every node after it has created all nodes and has completed all
 * standard links. You can implement finishSetup in your node. You should
 * not depend on the existence of other nodes when Builder calls your
 * load method.<br>
 * 3. You can add a field to this class, but you probably should not do
 * this unless other nodes need the same field. One node will be responsible
 * for initializing it, so you should not use it until Builder calls your
 * finishSetup method.
 * 
 * 
 */
public final class Configuration
{
    /**
     * Given a spacecraft name (the key), this map returns a SpacecraftAOS object
     * (the value).
     */
    private java.util.TreeMap<String,Spacecraft> spacecrafts = new java.util.TreeMap<String,Spacecraft>();	

    /**
     * This is a collection of all RtStpsNodes in this setup. The map returns a
     * RtStpsNode given a unique node name string.
     */
    private java.util.TreeMap<String,RtStpsNode> stpsNodes = new java.util.TreeMap<String,RtStpsNode>();	

    /** Use this factory to create RT-STPS nodes. */
    private RtStpsNodeFactory nodeFactory;

    /** A name for this configuration. */
    private String name;


    /**
     * Create a Configuration.
     * @param name A name for this configuration.
     * @param factory A populated RT-STPS node factory.
     */
    public Configuration(String name, RtStpsNodeFactory factory)
    {
        this.name = name;
        nodeFactory = factory;
    }

    /**
     * Get the RT-STPS Node factory.
     */
    public final RtStpsNodeFactory getNodeFactory()
    {
        return nodeFactory;
    }

    /**
     * Get the configuration name.
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Get a map of all spacecrafts.
     */
    public final java.util.TreeMap<String,Spacecraft> getSpacecrafts()	
    {
        return spacecrafts;
    }

    /**
     * Get a map of all RT-STPS nodes.
     */
    public final java.util.TreeMap<String,RtStpsNode> getStpsNodes()	
    {
        return stpsNodes;
    }
}
