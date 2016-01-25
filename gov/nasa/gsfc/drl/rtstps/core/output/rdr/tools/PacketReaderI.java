/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketI;

/**
 * PacketReader interface
 * 
 *
 */
public interface PacketReaderI {

	/**
	 * Return a Packet constructed from an index into the buffer
	 * @return the next packet
	 */
	public PacketI next();

	/**
	 * If there's space for a packet there's another...
	 * @return true if there is another packet, false if not 
	 */
	public boolean hasNext();

	/**
	 * Remove current packet. Generally not supported and
	 * throws a runtime exception
	 */
	public void remove();

}