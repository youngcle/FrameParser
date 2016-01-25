/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketI;



/**
 * A Packet is not created but simply re-used over and over again,
 * although the data is copied into it.  This is used specifically
 * for the RandomAccessPacketReader which reads packets out of the 
 * StaticHeader, it does not interact with the PacketPool in any 
 * manner.
 * 
 * 
 *
 */
class RandomAccessPacketFactory implements PacketFactoryI {

	private static Packet packet = new Packet(65542); // header + data
	private static FrameAnnotation fa = new FrameAnnotation();
	
	{
		packet.setFrameAnnotation(fa);
	}
	
	@Override
	public PacketI make(int index, byte[] data) {
		int totalSize = Packet.getPacketSize(index, data);  // assume on packet boundary, calculate total byte[] needed to hold it
		
		packet.setLength(totalSize);
		fa.reset();
		
		// now copy the input buffer to the packet's byte[] buffer
		System.arraycopy(data, index, packet.getData(), 0, totalSize);
		
		// set the frame annotation to default values
		//packet.setFrameAnnotation(new FrameAnnotation());
		
		return packet;
	}

}
