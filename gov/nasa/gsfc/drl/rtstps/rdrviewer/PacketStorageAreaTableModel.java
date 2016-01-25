/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;

public class PacketStorageAreaTableModel extends CommonTableModel {
	public PacketStorageAreaTableModel() {
		columnNames.add("SequenceFlags");
		columnNames.add("SequenceCounter");
		columnNames.add("ApplicationId");
		columnNames.add("PacketLength");
		columnNames.add("Timestamp");
		columnNames.add("Data");
	}
}
