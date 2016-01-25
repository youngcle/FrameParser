/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketI;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PacketFactoryI;

/**
 * A Packet is not created but simply used over and over again,
 * although the data is copied into it.  This more closely simulates
 * how RT-STPS actually works (which keeps a packet pool and resuses packets)
 * This is appropriate if the receiver of the packet actually copies it as well,
 * which is true at the moment of the RDR builder code...
 * 
 *
 */
public class NoCopyPacketFactory implements PacketFactoryI {

	private static Packet packet = new Packet(65535);
	
	{
		packet.setFrameAnnotation(new FrameAnnotation());
	}
	
	@Override
	public PacketI make(int index, byte[] data) {
		int totalSize = Packet.getPacketSize(index, data);  // assume on packet boundary, calculate total byte[] needed to hold it
		
		packet.setLength(totalSize);
		
		// now copy the input buffer to the packet's byte[] buffer
		System.arraycopy(data, index, packet.getData(), 0, totalSize);
		
		// set the frame annotation to default values
		//packet.setFrameAnnotation(new FrameAnnotation());
		
		return packet;
	}

}
