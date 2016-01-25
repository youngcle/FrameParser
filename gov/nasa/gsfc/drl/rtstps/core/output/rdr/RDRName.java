/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.util.EnumSet;
import java.util.Iterator;

/**
 * Currently defined RDRs, note that only some of these are actually active by having corresponding concrete {@link RDR}.
 * This can be determined as well by looking at the EnumSet of each -- if its just 'NoName' then the set is not really defined.
 * If the entry has an EnumSet.range then its active and the range are the individual names packets (corresponding to app ids)
 * for that RDR.
 * 
 * And if any set has overlaps with another, then that will be a problem...
 */
public enum RDRName {
	//NoNameRDR(Sensor.NoNameSensor, TypeID.NoNameTypeId, 0, EnumSet.of(PacketName.NoName)),
	A_DCS_Science_Mission( Sensor.A_DCS, TypeID.SCIENCE, 1, EnumSet.of(PacketName.NoName), 0 ),
	A_DCS_Telemetry_Housekeeping( Sensor.A_DCS, TypeID.TELEMETRY, 1, EnumSet.of(PacketName.NoName), 0 ),
	ATMS_Science( Sensor.ATMS, TypeID.SCIENCE, 4, EnumSet.range(PacketName.ATMS_CAL, PacketName.ENG_HS), 12 ),
	//ATMS_Science( Sensor.ATMS, TypeID.SCIENCE, 4, EnumSet.range(PacketName.ATMS_SCI, PacketName.ATMS_SCI), 12 ),
	ATMS_Diagnostic( Sensor.ATMS, TypeID.DIAGNOSTIC, 2, EnumSet.of(PacketName.NoName), 0 ),
	ATMS_Dwell( Sensor.ATMS, TypeID.DWELL, 1, EnumSet.of(PacketName.NoName), 0 ),
	ATMS_Telemetry( Sensor.ATMS, TypeID.TELEMETRY, 1, EnumSet.of(PacketName.NoName), 0 ),
	ATMS_Memory_Dump( Sensor.ATMS, TypeID.DUMP, 1, EnumSet.of(PacketName.NoName), 0 ),
	CRIS_Science( Sensor.CRIS, TypeID.SCIENCE, 83, EnumSet.range(PacketName.NLW1, PacketName.CRIS_ENG), 120 ),
	//CRIS_Diagnostic( Sensor.CRIS, TypeID.DIAGNOSTIC, 3, EnumSet.range(PacketName.CLW1, PacketName.CSW1), 8 ),
	CRIS_HSK_Dwell( Sensor.CRIS, TypeID.HSKDWELL, 1, EnumSet.of(PacketName.NoName), 0 ),
	CRIS_SSM_Dwell( Sensor.CRIS, TypeID.SSMDWELL, 1, EnumSet.of(PacketName.NoName), 0 ),
	CRIS_IM_Dwell( Sensor.CRIS, TypeID.IMDWELL, 1, EnumSet.of(PacketName.NoName), 0 ),
	CRIS_Telemetry( Sensor.CRIS, TypeID.TELEMETRY, 8, EnumSet.of(PacketName.NoName), 0 ),
	CRIS_Memory_Dump( Sensor.CRIS, TypeID.DUMP, 1, EnumSet.of(PacketName.NoName), 0 ),
	CERES_Science( Sensor.CERES, TypeID.SCIENCE, 2, EnumSet.of(PacketName.NoName), 0),
	CERES_Diagnostic( Sensor.CERES, TypeID.DIAGNOSTIC, 1, EnumSet.of(PacketName.NoName), 0 ),
	CERES_Telemetry( Sensor.CERES, TypeID.TELEMETRY, 1, EnumSet.of(PacketName.NoName), 0 ),
	SARR_Telemetry( Sensor.SARR, TypeID.TELEMETRY, 1, EnumSet.of(PacketName.NoName), 0 ),
	SARP_Telemetry( Sensor.SARP, TypeID.TELEMETRY, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_NPScience( Sensor.OMPS_NP, TypeID.SCIENCE, 1, EnumSet.of(PacketName.NP1), 256 ),
	OMPS_NP_Calibration( Sensor.OMPS_NP, TypeID.CALIBRATION, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_NP_Diagnostic_EarthView( Sensor.OMPS_NP, TypeID.DIAG_SCI, 1, EnumSet.of(PacketName.NoName), 0),
	OMPS_NP_Diagnostic_Calibration( Sensor.OMPS_NP, TypeID.DIA_CAL, 1, EnumSet.of(PacketName.NoName), 4 ),
	OMPS_TCScience( Sensor.OMPS_TC, TypeID.SCIENCE, 1, EnumSet.of(PacketName.NTC), 1280 ),
	OMPS_TC_Calibration( Sensor.OMPS_TC, TypeID.CALIBRATION, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_TC_Diagnostic_EarthView( Sensor.OMPS_TC, TypeID.DIAG_SCI, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_TC_Diagnostic_Calibration( Sensor.OMPS_TC, TypeID.DIA_CAL, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_LPScience( Sensor.OMPS_LP, TypeID.SCIENCE, 2, EnumSet.range(PacketName.LP1,PacketName.LP2), 512 ),
	OMPS_LP_Calibration( Sensor.OMPS_LP, TypeID.CALIBRATION, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_LP_Diagnostic_Exposure1_EarthView(  Sensor.OMPS_LP, TypeID.DIAGEXPONE, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_LP_Diagnostic_Exposure2_EarthView(  Sensor.OMPS_LP, TypeID.DIAGEXPTWO, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_LP_Diagnostic_Calibration(  Sensor.OMPS_LP, TypeID.DIA_CAL, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_Dwell(  Sensor.OMPS, TypeID.DWELL, 1, EnumSet.of(PacketName.NoName), 0 ),
	//OMPS_Telemetry(  Sensor.OMPS, TypeID.TELEMETRY, 1, EnumSet.of(PacketName.TLM_OMPS), 8 ),
	OMPS_Memory_Dump(  Sensor.OMPS, TypeID.DUMP, 1, EnumSet.of(PacketName.NoName), 0 ),
	OMPS_Flight_Software_Bootup(  Sensor.OMPS, TypeID.FSW_BOOTUP, 1, EnumSet.of(PacketName.NoName), 0 ),
	VIIRS_Science( Sensor.VIIRS, TypeID.SCIENCE, 26, EnumSet.range(PacketName.M04, PacketName.ENG), 48), // granule is 48 scans
	VIIRS_Diagnostic( Sensor.VIIRS, TypeID.DIAGNOSTIC, 26, EnumSet.of(PacketName.NoName), 0 ),
	VIIRS_Telemetry( Sensor.VIIRS, TypeID.TELEMETRY, 1, EnumSet.of(PacketName.NoName), 0 ),
	VIIRS_Diagnostic_Telemetry(  Sensor.VIIRS, TypeID.DIAGTELEMETRY, 1, EnumSet.of(PacketName.NoName), 0 ),
	VIIRS_Memory_Dump( Sensor.VIIRS, TypeID.DUMP, 1, EnumSet.of(PacketName.NoName), 0 ),
	NPP_Spacecraft_Telemetry( Sensor.SPACECRAFT, TypeID.TELEMETRY, 30, EnumSet.of(PacketName.NoName), 0),
	NPP_Ephemeris_and_Attitude( Sensor.SPACECRAFT, TypeID.DIARY, 3, EnumSet.of(PacketName.DIARY, PacketName.ADCS_HKH, PacketName.CRITICAL), 21), // granule is 20 seconds worth
	NPOESS_Spacecraft_Telemetry( Sensor.SPACECRAFT, TypeID.TELEMETRY, 100000, EnumSet.of(PacketName.NoName), 0), // EDFCB2-TBD-10140, not a real number in other words
	NPOESS_Ephemeris_and_Attitude( Sensor.SPACECRAFT, TypeID.DIARY, 2, EnumSet.of(PacketName.NoName), 0);
	
	private Sensor sensor;
	private TypeID typeID;
	private int defaultNumPackets;
	private EnumSet<PacketName> packetsInRDR;
	private int granuleSize;
	
	public static int ApplicationPacketCount = 100;

	/**
	 * Constructor
	 * @param sensor the Sensor of interest
	 * @param typeID the TypeID
	 * @param defaultNumPackets the number of packets associated with the RDR
	 * @param packetsInRDR and Set of packets (names) in the RDR
	 * @param granuleSize the granule size is intepreted by each sensor implementation as is appropriate
	 */
	private RDRName(Sensor sensor, TypeID typeID, int defaultNumPackets, EnumSet<PacketName> packetsInRDR, int granuleSize) {
		this.sensor = sensor;
		this.typeID = typeID;
		this.defaultNumPackets = defaultNumPackets;
		this.packetsInRDR = packetsInRDR;
		this.granuleSize = granuleSize;
	}
	
	/**
	 * Return the granule size
	 * @return an integer value interpreted by the caller
	 */
	public int getGranuleSize() {
		return granuleSize;
	}
	/**
	 * Return the Sensor
	 * @return the {@link Sensor}
	 */
	public Sensor getSensor() {
		return sensor;
	}
	
	/**
	 * Return the TypeID
	 * @return the {@link TypeID}
	 */
	public TypeID getTypeID() {
		return typeID;
	}
	
	/**
	 * Return the default number of packets
	 * @return the number of packets as an <code>int</code>
	 */
	public int getDefaultNumPackets() {
		return defaultNumPackets;
	}
	
	/**
	 * Count the number of distinct packet (app ids) in the RDR and return that count.
	 * @return int the count of packets (app ids) -- 0 means the RDR is not currently supported.
	 */
	public int getNumberOfAppIdsInRDR() {
		Iterator<PacketName> iterator = packetsInRDR.iterator();
		
		int count = 0;
		while (iterator.hasNext()) {
			PacketName pn = iterator.next();
			if (pn.equals(PacketName.NoName)) {
				// skip the 'NoName' as this is basically the 'null' packet
				// normally one would expect either a set of packetNames, or the NonName...
				// so if we are in this section, it should be likely be the last and only one in the set
				continue;
			}
			++count;
		}
		return count;
	}
	public EnumSet<PacketName> getPacketsInRDR() {
		return packetsInRDR;
	}

	/**
	 * Return the enumeration from the application id
	 * @param appId the application identifier of a packet in the RDR
	 * @return one of RDR short names or <code>null</code>
	 */
	public static RDRName fromAppId(int appId) {
		RDRName[] values = RDRName.values();
		for (int i = 0; i < values.length; i++) {
			//System.out.println("APID: " + appId + " PacketName: " + PacketName.fromAppId(appId) + " setSize: " + values[i].packetsInRDR.size());
			if (values[i].packetsInRDR.contains(PacketName.fromAppId(appId))) {
				return values[i];
			}
		}
		return null; // Or would null be better?
	}
	
	/**
	 * Returns the sensor.toString() +  "-" + typeID.toString() + "-RDR" appended to it.
	 * Example: 
	 * if NPP_Ephemeris_and_Attitude, then 
	 *  Sensor.SPACECRAFT, TypeID.DIARY
	 *  return SPACECRAFT-DIARY-RDR
	 * @return the sensor + typeID + 'RDR' separated by '-'
	 */
	public String getRDRStringName() {
	    //Special case for OMPS Science sensors, as their string name contains a dash in it, which classes with the RDR name itself used internally
	    //Yet we don't want to affect OMPS Telemetry RDRs, as they are OK and do not have such a dash.
	    if(sensor.toString().contains("OMPS-"))
	    {
		return sensor.toString() + "" + typeID.toString() + "-" + "RDR";
	    }
	    else
	    {
		return sensor.toString() + "-" + typeID.toString() + "-" + "RDR";
	    }
	}

	public int getDepth() {
		int depth = 0;
		for (PacketName packetName : this.packetsInRDR) {
			depth += packetName.getTotalGroupCount();
		}
		if (depth < ApplicationPacketCount) {
			depth = ApplicationPacketCount;
		}
		return depth;
	}

	/**
	 * Given an already existing product name such as SPACECRAFT-DIARY-RDR or SPACECRAFT-DIARY-RDR_All
	 * convert that to the appropriate RDRName enums.
	 * @param rdrNameStr the name of pre-existing RDR item
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	public static RDRName fromRDRNameStr(String rdrNameStr) throws RtStpsException {
	    	System.out.println("rdrNameStr is: " + rdrNameStr);
		String[] items = rdrNameStr.split("-");
		
		if (items.length != 3) {
			throw new RtStpsException("RDR name string is not the correct format -- got [" + rdrNameStr + "], expecting something like SPACECRAFT-DIARY-RDR, etc...");
		}
		//System.out.println("Items length: " + items.length);
		
		//System.out.println("Items[0] = " + items[0]);
		//System.out.println("Items[1] = " + items[1]);
		//System.out.println("Items[2] = " + items[2]);
		
		// match the Sensor and TypeID, and return the result
		if(items[1].equals("LPSCIENCE"))
		{
		    items[0] = "OMPS-LP";
		    items[1] = "SCIENCE";
		    
		}
		if(items[1].equals("NPSCIENCE"))
		{
		    items[0] = "OMPS-NP";
		    items[1] = "SCIENCE";
		    
		}
		if(items[1].equals("TCSCIENCE"))
		{
		    items[0] = "OMPS-TC";
		    items[1] = "SCIENCE";
		    
		}
		RDRName[] values = RDRName.values();
		for (int i = 0; i < values.length; i++) {
			System.out.println(values[i].sensor.toString());
			System.out.println(values[i].typeID.toString());
			System.out.println(" ");
			System.out.println("item zer is " + items[0]);
			System.out.println("item one is " + items[1]);
			System.out.println(" ");
			if (values[i].sensor.toString().equals(items[0]) && values[i].typeID.toString().equals(items[1])) {
				//System.out.println("Found: " + items[0] + " and " + items[1]);
				return values[i];
			
			}
		}
		//return NoNameRDR; // Or would null be better?
		return null;
	}
	
	/**
	 * Given the sensor and typeID of the RDR, return the RDRName.
	 * This pretty much replicates the private constructor.
	 * @param sensor the Sensor
	 * @param typeID the TypeID
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	public static RDRName fromSensorAndTypeID(Sensor sensor, TypeID typeID) throws RtStpsException {
		
		// match the Sensor and TypeID, and return the result
		RDRName[] values = RDRName.values();
		for (int i = 0; i < values.length; i++) {
			
			if ((values[i].sensor == sensor) && (values[i].typeID == typeID)) {
				
				return values[i];
			}
		}
		//return NoNameRDR; // Or would null be better?
		return null;
	}
	
	/**
	 * Given the sensor and typeID string values, return the RDRName.
	 * This pretty much replicates the private constructor as well
	 * @param sensor the sensor name as a String
	 * @param typeID the TypeID as a String
	 * @throws RtStpsException wraps any HDF exceptions
	 */
	public static RDRName fromSensorAndTypeID(String sensor, String typeID) throws RtStpsException {
		
		// match the Sensor and TypeID, and return the result
		RDRName[] values = RDRName.values();
		for (int i = 0; i < values.length; i++) {
			
			if (values[i].sensor.toString().equals(sensor) && values[i].typeID.toString().equals(typeID)) {
				
				return values[i];
			}
		}
		//return NoNameRDR; // Or would null be better?
		return null;
	}
	
	/**
	 * Given a product identifier, match it against the RDR name and return that RDR if found.
	 * @param productIdentifier a ProductIdentifiers
	 * @return an RDRName if found, null otherwise
	 * @throws RtStpsException not sure this comes into play, perhaps if its not found it should throw the exception
	 */
	public static RDRName fromProductIdentifier(ProductIdentifiers productIdentifier) throws RtStpsException {
		
		// basically the product short name look like:  VIIRS-SCIENCE-RDR
		// And what we want is the VIIR-SCIENCE part to look like VIIRS_SCIENCE
		// Then we can compare them to the RDRName strings which must also be upper cased
		
		String productName = productIdentifier.getShortName().toUpperCase().replace('-', '_');
		int startOfRDR = productName.indexOf("_RDR");


		String rdrNameStr = productName.substring(0, startOfRDR);

		RDRName[] values = RDRName.values();
		for (int i = 0; i < values.length; i++) {

			if (values[i].toString().toUpperCase().equals(rdrNameStr)) {
				// System.out.println("Found: " + items[0] + " and " +
				// items[1]);
				return values[i];
			}
		}
		
		return null;
	}
}
