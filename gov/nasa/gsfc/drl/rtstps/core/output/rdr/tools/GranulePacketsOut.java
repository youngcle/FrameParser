/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.CommonDataSetObject;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.DataProductsReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Granule;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRProduct;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Print the attributes of Products and Granules to the console
 * 
 *
 */
public class GranulePacketsOut {

	/**
	 * @param args the RDR HDF file
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("No input file to process... no output file to write to");
			System.exit(-1);
		}
		try {
			
			FileOutputStream out = new FileOutputStream(args[1]);

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
						
						RawApplicationPackets rap = g.getReferencedRawApplicationPackets();
						
						StaticHeader sh = rap.getStaticHeader();
						
						byte[] data = sh.getData();
						int offset = sh.getApStorageOffset();
						int end = sh.getNextPktPos();
						
						int size = end - offset;  // FIXME to me it should be this but...
						
						out.write(data, offset, end);  //FIXME 'end' does not work on DRL RDRs but does on external RDRs
						//For old RT-STPS RDRs you can use the following
						//out.write(data, offset, end-offset);
						/**
						SequentialPacketReader foobar = sh.createSequentialPacketReader();
						int scan = 0;
						while (foobar.hasNext()) {
							Packet p = (Packet)	foobar.next();
							int apid = p.getApplicationId();
							System.out.println("Packet AppId = " + apid);
							if (apid == 826) {
								scan++;
							}
							
						}
						System.out.println("Scan count = " + scan);
						**/
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
	
		} catch (FileNotFoundException e) {

			e.printStackTrace();
			
		} catch (IOException e) {

			e.printStackTrace();
			
		} 
	}

}
