/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * The following table is used by this package to determine which packets it receives are going to processed.
 * Each packet is given its official name from the JPSS/NPOESS documentation and the application identifier which is 
 * of critical importance.  The description and a group designation are given but are largely unused in this package.
 * (VIIRS are group or segmented packets but none of the other this package currently processes, ATMS and CRIS, are like this)
 * This table is used to when defining the RDR packages this will process see {@link RDRName}.   In some cases slightly variations
 * in the official name has been chosen due to conflicts or other issues.  These special cases are fixed up in the toString method.
 * 
 *
 */
public enum PacketName {
	NoName("Used in place there must be a packetName, sort of like a null", -1, 1),
	
	// official name for many like ATMS_CAL is actually CAL and so on
	CRITICAL("Bus Critical Telemetry", 0, 1),
	ADCS_HKH("ADCS Houskeeping Telemetry Highrate",8 ,1),
	// ATMS science block
	ATMS_CAL("Calibration", 515, 1), 
	ATMS_SCI(" Science - Operational Mode as well as Diagnostic Mode only if sensor is commanded to Dwell or to output Diagnostic or Memory Dump packets",  528, 104),
	ENG_TEMP("Engineering - Hot Cal Temperatures",  530, 1),
	ENG_HS("Engineering - Health and Status - required for science processing", 531, 1),
	// 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 33, 17, 17, 17, 33, 33, 33, 33, 17, 0, 0, 0, 24
	M04("Moderate Resolution Band 4",  800, 17),  // 
	M05(" Moderate Resolution Band 5", 801, 17),
	M03("Moderate Resolution Band 3", 802, 17),
	M02("Moderate Resolution Band 2", 803, 17),
	M01("Moderate Resolution Band 1", 804, 17),
	M06("Moderate Resolution Band 6", 805, 17),
	M07("Moderate Resolution Band 7", 806, 17),
	M09("Moderate Resolution Band 9", 807, 17),
	M10("Moderate Resolution Band 10", 808, 17),
	M08("Moderate Resolution Band 8", 809, 17),
	M11("Moderate Resolution Band 11", 810, 17),
	M13("Moderate Resolution Band 13", 811, 17),
	M12("Moderate Resolution Band 12", 812, 17),
	I04("Imagery Resolution Band 4", 813, 33),
	M16("Moderate Resolution Band 16", 814, 17),
	M15("Moderate Resolution Band 15", 815, 17),
	M14("Moderate Resolution Band 14", 816, 17),
	I05("Imagery Resolution Band 5", 817, 33),
	I01("Imagery Resolution Band 1", 818, 33),
	I02("Imagery Resolution Band 2", 819, 33),
	I03("Imagery Resolution Band 3", 820, 33),
	DNB("Day Night Band", 821, 17),
	DNB_MGS("Day Night Band - Middle Gain Stage", 822, 17), //FIXSET1
	DNB_LGS("Day Night Band - Low Gain Stage", 823, 17), //FIXSET1
	CAL("Calibration", 825, 24), // FIXME, spec says 23
	ENG("Oper Engineering", 826, 1),	
	DIARY("Diary (Ephemeris and Attitude)", 11, 1),  // NPP
	
	// CRIS packets
	NLW1("LW 1 Earth Scene", 1315, 1),
	NLW2("LW 2 Earth Scene", 1316, 1), 
	NLW3("LW 3 Earth Scene", 1317, 1), 
	NLW4("LW 4 Earth Scene", 1318, 1), 
	NLW5("LW 5 Earth Scene", 1319, 1), 
	NLW6("LW 6 Earth Scene", 1320, 1), 
	NLW7("LW 7 Earth Scene", 1321, 1), 
	NLW8("LW 8 Earth Scene", 1322, 1), 
	NLW9("LW 9 Earth Scene", 1323, 1), 
	NMW1("MW 1 Earth Scene", 1324, 1), 
	NMW2("MW 2 Earth Scene", 1325, 1), 
	NMW3("MW 3 Earth Scene", 1326, 1), 
	NMW4("MW 4 Earth Scene", 1327, 1), 
	NMW5("MW 5 Earth Scene", 1328, 1), 
	NMW6("MW 6 Earth Scene", 1329, 1), 
	NMW7("MW 7 Earth Scene", 1330, 1), 
	NMW8("MW 8 Earth Scene", 1331, 1), 
	NMW9("MW 9 Earth Scene", 1332, 1), 
	NSW1("SW 1 Earth Scene", 1333, 1), 
	NSW2("SW 2 Earth Scene", 1334, 1), 
	NSW3("SW 3 Earth Scene", 1335, 1), 
	NSW4("SW 4 Earth Scene", 1336, 1), 
	NSW5("SW 5 Earth Scene", 1337, 1), 
	NSW6("SW 6 Earth Scene", 1338, 1), 
	NSW7("SW 7 Earth Scene", 1339, 1), 
	NSW8("SW 8 Earth Scene", 1340, 1), 
	NSW9("SW 9 Earth Scene", 1341, 1), 
	SLW1("LW 1 Deep Space",  1342, 1), 
	SLW2("LW 2 Deep Space",  1343, 1), 
	SLW3("LW 3 Deep Space",  1344, 1), 
	SLW4("LW 4 Deep Space",  1345, 1), 
	SLW5("LW 5 Deep Space",  1346, 1), 
	SLW6("LW 6 Deep Space",  1347, 1), 
	SLW7("LW 7 Deep Space",  1348, 1), 
	SLW8("LW 8 Deep Space",  1349, 1), 
	SLW9("LW 9 Deep Space",  1350, 1), 
	SMW1("MW 1 Deep Space",  1351, 1), 
	SMW2("MW 2 Deep Space",  1352, 1), 
	SMW3("MW 3 Deep Space",  1353, 1), 
	SMW4("MW 4 Deep Space",  1354, 1), 
	SMW5("MW 5 Deep Space",  1355, 1), 
	SMW6("MW 6 Deep Space",  1356, 1), 
	SMW7("MW 7 Deep Space",  1357, 1), 
	SMW8("MW 8 Deep Space",  1358, 1), 
	SMW9("MW 9 Deep Space",  1359, 1), 
	SSW1("SW 1 Deep Space",  1360, 1), 
	SSW2("SW 2 Deep Space",  1361, 1), 
	SSW3("SW 3 Deep Space",  1362, 1), 
	SSW4("SW 4 Deep Space",  1363, 1), 
	SSW5("SW 5 Deep Space",  1364, 1), 
	SSW6("SW 6 Deep Space",  1365, 1), 
	SSW7("SW 7 Deep Space",  1366, 1), 
	SSW8("SW 8 Deep Space",  1367, 1), 
	SSW9("SW 9 Deep Space",  1368, 1), 
	CLW1("LW 1 Internal Cal Target",  1369, 1), 
	CLW2("LW 2 Internal Cal Target",  1370, 1), 
	CLW3("LW 3 Internal Cal Target",  1371, 1), 
	CLW4("LW 4 Internal Cal Target",  1372, 1), 
	CLW5("LW 5 Internal Cal Target",  1373, 1), 
	CLW6("LW 6 Internal Cal Target",  1374, 1), 
	CLW7("LW 7 Internal Cal Target",  1375, 1), 
	CLW8("LW 8 Internal Cal Target",  1376, 1), 
	CLW9("LW 9 Internal Cal Target",  1377, 1), 
	CMW1("MW 1 Internal Cal Target",  1378, 1), 
	CMW2("MW 2 Internal Cal Target",  1379, 1), 
	CMW3("MW 3 Internal Cal Target",  1380, 1), 
	CMW4("MW 4 Internal Cal Target",  1381, 1), 
	CMW5("MW 5 Internal Cal Target",  1382, 1), 
	CMW6("MW 6 Internal Cal Target",  1383, 1), 
	CMW7("MW 7 Internal Cal Target",  1384, 1), 
	CMW8("MW 8 Internal Cal Target",  1385, 1), 
	CMW9("MW 9 Internal Cal Target",  1386, 1), 
	CSW1("SW 1 Internal Cal Target",  1387, 1), 
	CSW2("SW 2 Internal Cal Target",  1388, 1), 
	CSW3("SW 3 Internal Cal Target",  1389, 1), 
	CSW4("SW 4 Internal Cal Target",  1390, 1), 
	CSW5("SW 5 Internal Cal Target",  1391, 1), 
	CSW6("SW 6 Internal Cal Target",  1392, 1), 
	CSW7("SW 7 Internal Cal Target",  1393, 1), 
	CSW8("SW 8 Internal Cal Target",  1394, 1), 
	CSW9("SW 9 Internal Cal Target",  1395, 1), 
	EIGHT_S_SCI("Eight Second Science Cal", 1289, 1), 
	CRIS_ENG ("Four Minute Engineering", 1290, 1),
	NTC("OMPS Nadir Total Column Science", 560, 1),
	NP1("OMPS Nadir Profile Science RDR", 561, 1),
	LP1("OMPS Limb Profile Science RDR Long", 562, 1),
	LP2("OMPS Limb Profile Science RDR Short", 563, 1);
	
	
	private String description;
	private int appId;
	private int totalGroupCount;
	
	// description, appid and total packet count of group
	private PacketName(String description, int appId, int totalGroupCount) {
		this.description = description;
		this.appId = appId;
		this.totalGroupCount = totalGroupCount;
	}
	/**
	 * Return the enumeration from the application id
	 * @param appId
	 * @return one of Packets short names
	 */
	public static PacketName fromAppId(int appId) {
		PacketName[] values = PacketName.values();
		for (int i = 0; i < values.length; i++) {
			if (values[i].appId == appId) {
				return values[i];
			}
		}
		return null;
	}
	
	/**
	 * Return the name of the packet, certain special cases are fixed up here to match their official
	 * designation.
	 * @return the name of the packet in a String
	 */
	public String toString() {
		String result = this.name();
		
		
		// Official names do not uniquely identify by sensor
		if (result.equals("ATMS_CAL")) {
			result = "CAL";  // official name
		} else if (result.equals("ATMS_SCI")) {
			result = "SCI"; // official name
		} else if (result.equals("CRIS_ENG")) {
			result = "ENG";
		} 
		
		return result;
	}
	/**
	 * Return the description
	 * @return string description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Get the application id associate with the short name
	 * @return the application id
	 */
	public int getAppId() {
		return appId;
	}
	/**
	 * Return the number of total number of first, middle and last packets associated with the short name
	 * @return int count
	 */
	public int getTotalGroupCount() {
		return totalGroupCount;
	}
}
