/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsNode;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketReceiver;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.util.ArrayList;
import java.lang.NumberFormatException;

/*
*	An output channel which receives Packets and creates the final RDR product.
	Provides a simple "packet in, RDR out" black-box interface for RT-STPS. Very
	analogous to how SPA wrappers hide the intricacies of the core algorithm...
*/
public class RDROutput extends RtStpsNode implements PacketReceiver, Cloneable
{
	
    /**
     * This is a class name for this RT-STPS node type. It is not the link name,
     * which is the name of one particular object.  This is the name of the element
     * in the config file.
     */
    public static final String CLASSNAME = "RDR";

	// Self-explanatory: The class used to write to & build the final RDR product
	private RDRBuilderNPP rdrBuilder;
	
	private static int openCount = 0;
	
    /**
     * Constructor.
     */
    public RDROutput()
    {
        super(CLASSNAME);
        //System.out.println("a RDROutput constructor -- " + this.getLinkName());
    }

    /**
     * Set up this stps node with a configuration.
     */
    @Override
    public void load(org.w3c.dom.Element element, Configuration configuration) throws RtStpsException
    {
    	++openCount;

	// If this is the first RDR node to be loaded, have it open the HDF5 Library    	
	if(openCount == 1){
		HDF5Util.openLibrary();
	}

        super.setLinkName(element.getAttribute("label"));
        String mission = element.getAttribute("mission"); 
        String directory = element.getAttribute("directory");
	String oMode = element.getAttribute("mode");

	long granuleTimeSpan = 0L;
	try{
		granuleTimeSpan = Long.parseLong(element.getAttribute("timespan"));
	}
	catch(NumberFormatException nfe){
		System.out.println("Invalid granule time span. Using default.");
	}
	
        if (mission.equals("NPP") == false) {
        	throw new RtStpsException("Only NPP supported in RDR creation at this time");
        }
        
        // Class that contains various statuses; mostly Long-type statuses used as counters 
        Stats stats = new Stats();
       
        // have to pre-allocate status counters because server reads them once as a fixed block
        // even if they are not all used here.  AE are only going to be set if the attitude and ephemeris
        // packets are present in the stream...
        //
        statusItemList = new ArrayList<StatusItem>(10);

        statusItemList.add(stats.sci_createdGranules);
        statusItemList.add(stats.sci_discardedGranules);
        statusItemList.add(stats.sci_createdPackets);
        statusItemList.add(stats.sci_freePoolPackets);
        statusItemList.add(stats.sci_packetsMemory);
        statusItemList.add(stats.ae_createdGranules);
        statusItemList.add(stats.ae_discardedGranules);
        statusItemList.add(stats.ae_createdPackets);
        statusItemList.add(stats.ae_freePoolPackets);
        statusItemList.add(stats.ae_packetsMemory);

	// Initialize the RDRBuilderNPP object with the appropriate status and output directory...
        rdrBuilder = new RDRBuilderNPP(directory, stats, oMode, granuleTimeSpan);
        
    	//System.out.print("a Loaded link: " + this.getLinkName());
    }

	/**
	* Finish the setup. When this method is called, you may assume all nodes
	* have been created and exist by name in the map, and all standard links
	* have been resolved. This is a last chance to prepare for data flow.
	* In this case it does nothing, all set up is done in the load.
	*/
    	@Override
	public void finishSetup(Configuration configuration) throws RtStpsException {
		//System.out.println("In finishSetup -- " + this.getLinkName());
	}

    
	/**
	 * Write an array of packet to the output. This just calls putPacket
	 * for each Packet in the array.
	 */
	@Override
	public void putPackets(Packet[] packets) throws RtStpsException {
		for (Packet p : packets) {
			putPacket(p);
		}
	}

	/**
	 * Write a packet to the output RDR file.
	 */
	@Override
	public void putPacket(Packet packet) throws RtStpsException {
		// In the off chance the processing is shutdown but packets are still being processed
		if (rdrBuilder == null) return;

		// Otherwise, just feed the packet directly into the RDRBuilderNPP "black box"...
		rdrBuilder.put(packet);
	}
	
    /**
     * Closes all the resources with the RDR files
     */
    @Override
    public void flush() throws RtStpsException {
    	// this seems to be called more than once during the shutdown sequence resulting in an error
    	// so only close everything up one time.  Load resets it.
    	
    	//System.out.print("Trying to flush link: " + this.getLinkName());
    	if (rdrBuilder != null) {
    		if (openCount <= 1) {
    			
    			rdrBuilder.close(true); // clean up all the HDF
    			//System.out.println("a Flushed " + this.getLinkName() + " -- closed out HDF");
    		} else {
    			
    			rdrBuilder.close(false);
    			//System.out.println("a Flushed " + this.getLinkName() + " -- HDF untouched");
    		}
    		openCount--;
    		rdrBuilder = null;  // let the heap reclaim its resources
    	}// else {
    		
    	//	System.out.println("a Already Flushed " + this.getLinkName());
    		
    		//System.out.println(">>> stack trace <<<");
    		//new Exception().printStackTrace();
    		//System.out.println(">>> end stack trace <<<");
    	//}
    	
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
    	//System.out.println("a RDR Output being cloned.");
        return super.clone();
    }

}
