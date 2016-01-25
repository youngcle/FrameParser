/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketI;

/**
 * PacketFactory Interface provides a generic model for making packets that can then tuned for 
 * specific needs inside this package.   This is really only used at the moment by the Sequential and RandomAccess
 * packet reader associated with reading info out of the StaticHeader in the RawApplicationPackets zone.
 * It also not tied into PacketPool in any manner either... 
 * 
 * 
 *
 */
public interface PacketFactoryI {
	public PacketI make(int index, byte[] dataBuf);
}
