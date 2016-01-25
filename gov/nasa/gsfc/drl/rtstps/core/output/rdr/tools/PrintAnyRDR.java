/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.AllDataReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PacketName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.ProductIdentifiers;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAllReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RandomAccessPacketReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * Print the application id and packet size for all RDRs found to the console.
 * This uses a RandomAccessReader so in theory seeking into the RDR can be done.
 * 
 *
 */
public class PrintAnyRDR {
	static int appidCount[] = new int[2048];
	/**
	 * @param args the RDR HDF File
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("No file to process...");
			System.exit(-1);
		}
		try {
			
			RDRFileReader readRDR = new RDRFileReader(args[0]);
			AllDataReader allReader = readRDR.createAllDataReader();
			
			List<ProductIdentifiers> pis = readRDR.getProductIndentifiers();
			for (ProductIdentifiers pi : pis) {
				if (pi.getShortName().contains("SCIENCE")) {
					
					RDRName rdrName = RDRName.fromProductIdentifier(pi);
					
					RDRAllReader rar = allReader.getRDRAllReaderByRDRName(rdrName);


					while (rar.hasNext()) {
						RawApplicationPackets rap = rar.next();

						StaticHeader sh = rap.getStaticHeader();

						RandomAccessPacketReader rapr = sh.createRandomAccessPacketReader();

						EnumSet<PacketName> packetNameSet = rdrName.getPacketsInRDR();
						
						Iterator<PacketName> packetNames = packetNameSet.iterator();
						
						while (packetNames.hasNext()) {
							
							PacketName packetName = packetNames.next();
							//System.out.println("Packetname -- " + packetName);
							if (rapr.findPacketTrackerByName(packetName)) {
								while (rapr.hasNext()) {
									Packet p = rapr.next();
									//System.out.println("Packet found: " + p.hdrToString());
									appidCount[p.getApplicationId()]++;
								}
							}

						}
						rap.close();
					}
					rar.close();
				}
			}
			allReader.close();
			readRDR.close();
			
			for (int i = 0; i < 2048; i++) {
				if (appidCount[i] != 0)
					System.out.println("AppIdCount[" + i + "] -- " + appidCount[i]);
			}

		} catch (RtStpsException e) {
			e.printStackTrace();
		}

	}

}
