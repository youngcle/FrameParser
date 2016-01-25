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
 * Create a new Packet and copy the data into it
 * 
 *
 */
public class CopyPacketFactory implements PacketFactoryI {

	@Override
	public PacketI make(int index, byte[] data) {
		int totalSize = Packet.getPacketSize(index, data);  // assume on packet boundary, calculate total byte[] needed to hold it
		Packet packet = new Packet(totalSize);
		
		//System.out.println("TotalSize: " + totalSize);
		//System.out.println("Data.length: " + data.length);
		//System.out.println("Index: " + index);
		
		// now copy the input buffer to the packet's byte[] buffer
		System.arraycopy(data, index, packet.getData(), packet.getStartOffset(), totalSize);
		
		// set the frame annotation to default values
		packet.setFrameAnnotation(new FrameAnnotation());
		
		return packet;
	}

}
