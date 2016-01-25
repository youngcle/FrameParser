/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;

public class PacketTrackerTableModel extends CommonTableModel {
	public PacketTrackerTableModel() {
		columnNames.add("ObsTime");
		columnNames.add("SequenceNumber");
		columnNames.add("Size");
		columnNames.add("Offset");
		columnNames.add("FillPercent");
	}
}
