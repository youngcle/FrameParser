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
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.SequentialPacketReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

/**
 * Print the application id and packet size for all RDRs found in specified HDF file to the console,
 * using a sequential access approach.
 * 
 * 
 *
 */
public class PrintAllRDR {
	
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
			AllDataReader allReader = readRDR.createAllDataReader();
			

			while (allReader.hasNext()) {
				
				RDRAllReader rar = allReader.next();
				
				while (rar.hasNext()) {
					RawApplicationPackets rap = rar.next();
					
					StaticHeader sh = rap.getStaticHeader();
					//byte[] data = rap.getData();
					//int offset = sh.getApStorageOffset();
					
					SequentialPacketReader spr = sh.createSequentialPacketReader();
					
					while (spr.hasNext()) {
						PacketI p = spr.next();
						
						System.out.println("AppId=" + p.getApplicationId() + " Size: " + p.getPacketSize()  + " Sequence: " + p.getSequenceCounter());
					}
					
					//System.out.println("Offset into storage area: " + offset);
					
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
