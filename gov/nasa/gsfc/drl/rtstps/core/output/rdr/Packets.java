/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.LinkedList;

/**
 * Use by PacketPool
 * 
 *
 */
public class Packets extends LinkedList<Packet> {

	/**
	 * make warning go away
	 */
	private static final long serialVersionUID = 1L;
	
	int touched = 0; // count of packets used on this list

}
