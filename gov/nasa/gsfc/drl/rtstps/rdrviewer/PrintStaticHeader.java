/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.rdrviewer;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.AllDataReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAllReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAppIdItem;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAppIdList;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import java.util.List;

/**
 * Open an RDR file and locate the SpacecraftAOS Diary granules.  Compare the times
 * stored in the granules with the times in its RawApplicationPackets through the reference
 * in the granule.  If the granule times do not encapsulate the RawApplicationPacket,
 * issue and error message.
 * 
 *
 */
public class PrintStaticHeader {
	
	private static void print(StaticHeader sh) throws RtStpsException {
		System.out.println("StaticHeader -- Satellite: " + sh.readSatelliteString());
		String sensor = sh.readSensorString();
		String typeID = sh.readTypeIDString();
		RDRName rdrName = RDRName.fromSensorAndTypeID(sensor, typeID);
		
		System.out.println("             -- Sensor: " + sensor);
		System.out.println("             -- TypeID: " + typeID);
		System.out.println("             -- NumAppIds: " + sh.readNumAppIds() + " -- should be: " + rdrName.getNumberOfAppIdsInRDR());
		
		System.out.println("             -- ApidListOffset  : " + sh.readApidListOffset());
		
		
		
		System.out.println("             -- PktTrackerOffset: " + sh.readPktTrackerOffset());
		System.out.println("             -- ApStorageOffset : " + sh.readApStorageOffset());
		
		System.out.println("             -- NextPktPos      : " + sh.readNextPktPos());
		
		System.out.println("             -- StartBoundary: " + sh.readStartBoundary());
		System.out.println("             -- EndBoundary  : " + sh.readEndBoundary());
		
		RDRAppIdList appIdList = sh.readRDRAppIdList();
		
		List<RDRAppIdItem> items = appIdList.getAppIdItemList();
		for (int i = 0; i < items.size(); i++) {
			RDRAppIdItem item = items.get(i);
			System.out.println("             -- AppIdItem[" + i + "] " );
			System.out.println("                -- Name            : " + item.getName());
			System.out.println("                -- AppId           : " + item.getValue());
			System.out.println("                -- PktsReserved    : " + item.getPktsReserved());
			System.out.println("                -- PktsReceived    : " + item.getPktsReceived());
			System.out.println("                -- PktTrackerIndex : " + item.getPktTrackerIndex());
		}
		
	}
	
	/**
	 * @param args the RDR HDF file
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("No file to process... ");
			System.exit(-1);
		}
		try {

			RDRFileReader readRDR = new RDRFileReader(args[0]);
			
			AllDataReader allDataReader = readRDR.createAllDataReader();
			
			
			while (allDataReader.hasNext()) {
				
				RDRAllReader rar = allDataReader.next();
				
				while (rar.hasNext()) {
					RawApplicationPackets rap = rar.next();
					
					StaticHeader sh = rap.getStaticHeader();
					
					PrintStaticHeader.print(sh);
					/***
					byte[] data = rap.getData();
					int offset = sh.getApStorageOffset();
					
					SequentialPacketReader spr = sh.createSequentialPacketReader();
					
					while (spr.hasNext()) {
						PacketI p = spr.next();
						
						//System.out.println("AppId=" + p.getApplicationId() + " Size: " + p.getPacketSize());
					}
					
					//System.out.println("Offset into storage area: " + offset);
					**/
					rap.close();
				}
				rar.close();
			}
			

			allDataReader.close();
			readRDR.close();

		}  catch (RtStpsException e) {
			
			e.printStackTrace();
	
		} 
	}



}
