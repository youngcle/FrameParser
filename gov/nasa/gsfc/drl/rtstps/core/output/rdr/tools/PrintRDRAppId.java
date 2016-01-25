/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketI;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.AllDataReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAllReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.SequentialPacketReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;


/**
 * Print all the application ID and size of the packet for all all found packets in the corresponding RDR to the console.
 * The application ID is used to determine the RDR that should be in the given HDF file.  Any found, the 
 * items described will be printed to the console.
 * 
 * 
 *
 */
public class PrintRDRAppId {

	/**
	 * @param args the HDF RDR file
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("No file to process, AppID");
			System.exit(-1);
		}
		try {

			RDRFileReader readRDR = new RDRFileReader(args[0]);
			AllDataReader allReader = readRDR.createAllDataReader();
			
			int appId = Integer.parseInt(args[1]);
			
			RDRName rdrName = RDRName.fromAppId(appId);
			
			System.out.println("Looking for AppId[ " + appId + " ] in RDR[ " + rdrName.getRDRStringName() + " ]");
			
			RDRAllReader rar = allReader.getRDRAllReaderByRDRName(rdrName);
			
			if (rar != null) {
				
				while (rar.hasNext()) {
					RawApplicationPackets rap = rar.next();
					
					StaticHeader sh = rap.getStaticHeader();
					
					SequentialPacketReader spr = sh.createSequentialPacketReader();
					
					//byte[] data = rap.getData();
					//int offset = sh.getApStorageOffset();
					
					//SequentialPacketReader spr = new SequentialPacketReader(new SequentialPacketFactory(), data, offset);
					int pktCounter = 0;
					while (spr.hasNext()) {
						PacketI p = spr.next();
						
						if (p.getApplicationId() == appId) {
							System.out.print("AppId=" + p.getApplicationId() + " Size: " + p.getPacketSize());
							if (p.isFirstPacketInSequence()) {
								System.out.print(" <=== " + pktCounter++);
							}
							System.out.println();
						}
					}
					
					rap.close();
				}
				rar.close();
			}
			allReader.close();
			readRDR.close();

		} catch (RtStpsException e) {
			e.printStackTrace();
		} 

	}

}
