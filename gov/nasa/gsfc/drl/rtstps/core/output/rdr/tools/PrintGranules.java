/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.CommonDataSetObject;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.DataProductsReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Granule;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.LPEATEDate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRProduct;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Sensor;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.SequentialPacketReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

/**
 * Print the attributes of Products and Granules to the console
 * 
 *
 */
public class PrintGranules {

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
			DataProductsReader dataProductsReader = readRDR.createDataProductsReader();
			

			while (dataProductsReader.hasNext()) {
				
				RDRProduct rdrProduct = dataProductsReader.next();
				
				System.out.println("Instrument Short Name: " + rdrProduct.getInstrument_Short_Name());
				System.out.println("Collection Short Name: " + rdrProduct.getN_Collection_Short_Name());
				System.out.println("Dataset Type Tag: " + rdrProduct.getN_Dataset_Type_Tag());
				System.out.println("Processing Domain: " + rdrProduct.getN_Processing_Domain());
				//if (rdrProduct.getInstrument_Short_Name() != Sensor.VIIRS) {
				//	continue;
				//}
				boolean done = false;
				while (rdrProduct.hasNext()  && !done) {
					CommonDataSetObject ga = rdrProduct.next();
					if (ga instanceof Granule) {
						Granule g = (Granule) ga;
						System.out.println("==> Granule: " + g.getName());
						System.out.println("    Beginning_Date: " +  g.getBeginning_Date());
						System.out.println("    Beginning_Time: " + g.getBeginning_Time() );
						System.out.println("    Ending_Date: " + g.getEnding_Date());
						System.out.println("    Ending_Time: " +  g.getEnding_Time());
						System.out.println("    N_Beginning_Orbit_Number: " +  g.getN_Beginning_Orbit_Number());
						System.out.println("    N_Beginning_Time_IET: " + g.getN_Beginning_Time_IET() );
						System.out.println("    N_Creation_Date: " +  g.getN_Creation_Date());
						System.out.println("    N_Creation_Time: " +  g.getN_Creation_Time());
						System.out.println("    N_Ending_Time_IET: " +  g.getN_Ending_Time_IET());
						System.out.println("    N_Granule_ID: " +  g.getN_Granule_ID());
						System.out.println("    N_Granule_Status: " +  g.getN_Granule_Status());
						System.out.println("    N_Granule_Version: " +  g.getN_Granule_Version());
						System.out.println("    N_LEOA_Flag: " +  g.getN_LEOA_Flag());
						System.out.println("    N_NPOESS_Document_Ref: " +  g.getN_NPOESS_Document_Ref());

						System.out.println("    N_Packet_Type: " +  g.getN_Packet_Type());				
						System.out.println("    N_Packet_Type_Count: " +  g.getN_Packet_Type_Count());

						System.out.println("    N_Percent_Missing_Data: " +  g.getN_Percent_Missing_Data());

						System.out.println("    N_Reference_ID: " +  g.getN_Reference_ID());
						System.out.println("    N_Software_Version: " + g.getN_Software_Version());
						System.out.println();
						
						long begIET = g.getN_Beginning_Time_IET();
						long endIET = g.getN_Ending_Time_IET();
						
						double deltaT = (endIET - begIET) / 1000000.0;
						
						System.out.println("IET End-Beginning Delta = " + (endIET - begIET) + " or " + deltaT + " seconds");
						
						String timeStr = g.getBeginning_Time();
						int hour = Integer.parseInt(timeStr.substring(0, 2));  // hour						
						int minute = Integer.parseInt(timeStr.substring(2, 4));  // minute
						int seconds = Integer.parseInt(timeStr.substring(4, 6));  // seconds
						int millis = Integer.parseInt(timeStr.substring(7, 10));  // millis
						
						
						long btime = hour * 60L * 60L * 1000L + minute * 60L * 1000L + seconds * 1000L + millis;
						
						timeStr = g.getEnding_Time();
						hour = Integer.parseInt(timeStr.substring(0, 2));  // hour						
						minute = Integer.parseInt(timeStr.substring(2, 4));  // minute
						seconds = Integer.parseInt(timeStr.substring(4, 6));  // seconds
						millis = Integer.parseInt(timeStr.substring(7, 10));  // millis
						
						long etime = hour * 60L * 60L * 1000L + minute * 60L * 1000L + seconds * 1000L + millis;
						
						double deltaT2 = (etime - btime) / 1000.0;
						
						System.out.println("Etime-Btime Delta = " + (etime - btime) + " or " + deltaT2 + " seconds");

						
						RawApplicationPackets rap = g.getReferencedRawApplicationPackets();
						
						StaticHeader sh = rap.getStaticHeader();
						
						
						SequentialPacketReader pr = sh.createSequentialPacketReader();
						
						// assume there is at least one for now...
						Packet firstPacket = (Packet)pr.next();
						Packet lastPacket = firstPacket;
						while (pr.hasNext()) {
							lastPacket = (Packet) pr.next();
						}
						
						PDSDate fpts = new PDSDate(firstPacket.getTimeStamp(8));
						PDSDate lfpts = new LPEATEDate(firstPacket.getTimeStamp(8));

						long pbegIET = fpts.getMicrosSinceEpoch();
						//System.out.println("First packet IET time = " + fpts.getMicrosSinceEpoch());
						PDSDate lpts = new PDSDate(lastPacket.getTimeStamp(8));
						long pendIET = lpts.getMicrosSinceEpoch();
						//System.out.println("Last packet IET time = " + lpts.getMicrosSinceEpoch());
						
						double deltaT3 = (pendIET - pbegIET) / 1000000.0;
						
						System.out.println("Packet IET End-Beginning Delta = " + (pendIET - pbegIET) + " or " + deltaT3 + " seconds");

						long lpbegIET = lfpts.getMicrosSinceEpoch();
						System.out.println("First Packet IET  (      raw) = " + pbegIET);
						System.out.println("First Packet IET (leap/peate) = " + lpbegIET);
						System.out.println("Beginning    IET              = " + begIET);

	
						double deltaT4 = (lpbegIET - begIET) / 1000000.0;
						
						System.out.println("LPEATE/LEAP Beg IET End-Beginning Delta = " + (lpbegIET - begIET) + " or " + deltaT4 + " seconds");
						System.out.println();
						
						//foobar.get
						rap.close();
						
						//done = true; // one for now
					}
					ga.close();
				}
				rdrProduct.close();
			}
			
			dataProductsReader.close();
			readRDR.close();
			
		}  catch (RtStpsException e) {
			
			e.printStackTrace();
	
		} 
	}

}
