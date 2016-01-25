/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;


public class AppIdListTableModel extends CommonTableModel {
	
	public AppIdListTableModel() {
		columnNames.add("Name");
		columnNames.add("AppId");
		columnNames.add("PktsReserved");
		columnNames.add("PktsReceived");
		columnNames.add("PktTrackerIndex");
	}
}
