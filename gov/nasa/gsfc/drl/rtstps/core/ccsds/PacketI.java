/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.ccsds;

public interface PacketI {
	public int getApplicationId();
	public int getSequenceCounter();
	public int getPacketLength();
	public int getPacketSize();
	public int getVersion();
	public int getType();
	public int getSequenceFlags();
	public boolean isFirstPacketInSequence();
	public boolean isMiddlePacketInSequence();
	public boolean isLastPacketInSequence();
	public boolean isStandalonePacket();
	public boolean hasSecondaryHeader();
}
