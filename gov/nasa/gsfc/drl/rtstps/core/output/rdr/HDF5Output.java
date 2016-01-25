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
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.util.ArrayList;


// old stuff partially changed over and now called RDROutput

@Deprecated
public class HDF5Output extends RtStpsNode implements PacketReceiver, Cloneable
{
	
    /**
     * This is a class name for this RT-STPS node type. It is not the link name,
     * which is the name of one particular object.  This is the name of the element
     * in the config file.
     */
    public static final String CLASSNAME = "RDR";

    /**
     * If a channel encounters this number of consecutive write errors, it
     * stops writing to the output device and begins discarding data.
     */
    public static int CONSECUTIVE_PERMITTED_ERRORS = 5;

  
    /**
     * A count of the number of units that have passed through this
     * channel.
     */
    protected LongStatusItem count;

    /**
     * A count of units that could not be written for any reason.
     */
    protected LongStatusItem outputErrorCount;

    /**
     * A count of units that the channel discarded because the consecutive
     * error threshold was triggered.
     */
    protected LongStatusItem discardedCount;

    /**
     * The current number of consecutive write errors.
     */
    protected int consecutiveErrors = 0;

	private boolean flushed = false; // flush once...


	private RDRBuilderNPP rdrBuilder;
	
    /**
     * Constructor.
     */
    public HDF5Output()
    {
        super(CLASSNAME);
        //System.out.println("HDF5Output");
    }

    /**
     * Set up this stps node with a configuration.
     */
    @Override
    public void load(org.w3c.dom.Element element, Configuration configuration) throws RtStpsException
    {
    	//System.out.println("LOAD OF HDF5");
        super.setLinkName(element.getAttribute("label"));
        String mission = element.getAttribute("mission"); 
        String directory = element.getAttribute("directory");

        if (mission.equals("NPP") == false) {
        	throw new RtStpsException("Only NPP supported in RDR creation at this time");
        }
        
        
        flushed = false;
        
        rdrBuilder = new RDRBuilderNPP(directory, null);

     

        count = new LongStatusItem("Output");
        outputErrorCount = new LongStatusItem("Errors");
        discardedCount = new LongStatusItem("Discarded");

        statusItemList = new ArrayList<StatusItem>(3);
        statusItemList.add(count);
        statusItemList.add(outputErrorCount);
        statusItemList.add(discardedCount);
    }


        

	/**
     * Finish the setup. When this method is called, you may assume all nodes
     * have been created and exist by name in the map, and all standard links
     * have been resolved. This is a last chance to prepare for data flow.
     * In this case it does nothing, all set up is done in the constructor.
     */
    @Override
    public void finishSetup(Configuration configuration) throws RtStpsException {
    	// does nothing
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
		rdrBuilder.put(packet);
	}
	
	/**
     * Closes all the resources and finishes the HDF metadata
     */
    @Override
    public void flush() throws RtStpsException {
    	// this seems to be called more than once during the shutdown sequence resulting in a error
    	// so only close everything up one time.  Load resets it.
    	if (flushed == false) {
    		flushed = true;
    		rdrBuilder.close(true);
    	}
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
    	System.out.println("HDF5Output being cloned.");
        return super.clone();
    }

}
