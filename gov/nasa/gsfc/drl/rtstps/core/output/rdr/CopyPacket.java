/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet.Annotation;


/**
 * Utility class to make a deep copy of a {@link Packet}.  This class is needed to specifically support this package
 * being included in the RT-STPS processing chain because RT-STPS caches its outgoing packets on a local list
 * and cannot guarantee they won't be used once the main processing loop has been iterated over.  Since several
 * part of this package hold onto packets in internal lists, a copy is needed in order for this to work within
 * RT-STPS.  If this module is stand-alone, then a copy of input packet is not necessary as long as any packet
 * reader does not assume it can reuse a packet object it creates.
 * 
 * In order to have some efficiency the Packet is taken from a Packet pool.  The pool creates new Packets if
 * there are not any.  When the packet is not longer being used, the deep copied packet this class returns
 * should be put back on the {@link PacketPool}.
 *  
 * 
 *
 */
public class CopyPacket {
	
	/**
	 * Deep copy an input Packet
	 * @param source an input Packet
	 * @return the deep copied packet
	 * @throws RtStpsException an exception from the PacketPool
	 */
	public static Packet deep(Packet source, PacketPool packetPool) throws RtStpsException {
		
		// byte array (payload), frame/packet annotation, and size of source packet
		byte[] fromData = source.getData();
		FrameAnnotation fromFrameAnnotation = source.getFrameAnnotation();
		Annotation fromPacketAnnotation = source.getPacketAnnotation();
		int fromSize = source.getSize();
		
		// byte array (payload), frame/packet annotation, and size of destination
		Packet destination = packetPool.get(fromSize);
		byte[] toData = destination.getData();
		Annotation toPacketAnnotation = destination.getPacketAnnotation();
		
		// copy items...
		for (int i = 0; i < fromSize; i++) {
			toData[i] = fromData[i];
		}
		// copy the frame annotation
		FrameAnnotation toFrameAnnotation = new FrameAnnotation();
		
		toFrameAnnotation.hasBadFirstHeaderPointer = fromFrameAnnotation.hasBadFirstHeaderPointer;
		toFrameAnnotation.hasCrcError = fromFrameAnnotation.hasCrcError;
		toFrameAnnotation.hasIdleVcdu = fromFrameAnnotation.hasIdleVcdu;
		toFrameAnnotation.hasPacketDecompositionError = fromFrameAnnotation.hasPacketDecompositionError;
		toFrameAnnotation.hasSequenceError = fromFrameAnnotation.hasSequenceError;
		toFrameAnnotation.isInverted = fromFrameAnnotation.isInverted;
		toFrameAnnotation.isLock = fromFrameAnnotation.isLock;
		toFrameAnnotation.isRsCorrected = fromFrameAnnotation.isRsCorrected;
		toFrameAnnotation.isRsUncorrectable = fromFrameAnnotation.isRsUncorrectable;
		toFrameAnnotation.isSlipped = fromFrameAnnotation.isSlipped;
		toFrameAnnotation.timestamp = fromFrameAnnotation.timestamp;
		
		destination.setFrameAnnotation(toFrameAnnotation);
		
		// copy the packet annotation.  slightly different here because
		// the under object is always created and then its returned in the get...
		// so it's just a matter of getting the destinations and copying sources fields values over...
		toPacketAnnotation.goodByteCount = fromPacketAnnotation.goodByteCount;
		toPacketAnnotation.hasSequenceError = fromPacketAnnotation.hasSequenceError;
		toPacketAnnotation.isInvalidLength = fromPacketAnnotation.isInvalidLength;
		toPacketAnnotation.isPacketWithFill = fromPacketAnnotation.isPacketWithFill;
		
		return destination;
	}
}
