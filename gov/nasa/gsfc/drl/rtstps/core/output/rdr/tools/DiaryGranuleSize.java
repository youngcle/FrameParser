/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.DataProductsReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Granule;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRProduct;

/**
 * Open an RDR file and locate the SpacecraftAOS Diary granules.  Compare the times
 * stored in the granules with the times in its RawApplicationPackets through the reference
 * in the granule.  If the granule times do not encapsulate the RawApplicationPacket,
 * issue and error message.
 * 
 *
 */
public class DiaryGranuleSize {
	/**
	 * Check that the outside granule encapsulates the inside granule in time!
	 * @param g the granule of interest
	 * @throws RtStpsException
	 */
	private void granuleTime(Granule g) throws RtStpsException {
	
		long begt = g.getN_Beginning_Time_IET();
		long endt = g.getN_Ending_Time_IET();
		long duration = endt - begt;
		
		System.out.println("    Duration: " + (double)duration / 1000000.0 + " secs");
		
	
		
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
			DataProductsReader dataProductsReader = readRDR.createDataProductsReader();
			DiaryGranuleSize check = new DiaryGranuleSize();
			RDRProduct diaryProduct = dataProductsReader.getRDRDataProductByName("SPACECRAFT-DIARY-RDR");
			
			System.out.println("Instrument Short Name: " + diaryProduct.getInstrument_Short_Name());
			System.out.println("Collection Short Name: " + diaryProduct.getN_Collection_Short_Name());
			System.out.println("Dataset Type Tag: " + diaryProduct.getN_Dataset_Type_Tag());
			System.out.println("Processing Domain: " + diaryProduct.getN_Processing_Domain());
			System.out.println("...");

			int count = (int)diaryProduct.getGranuleCount();
			for (int n = 0; n < count; n++) {
				Granule gd = diaryProduct.getGranule(n);
		
				check.granuleTime(gd);
				
				
				gd.close();
				
			}
			/**
			//boolean done = false;
			while (diaryProduct.hasNext()) {
				CommonDataSetObject dga = diaryProduct.next();
				//CommonDataSetObject oga = diaryProduct.next();
				if (dga instanceof Granule) {
					Granule dg = (Granule) dga;
					check.granuleTime(dg);
					/**
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

							SequentialPacketReader foobar = sh.createSequentialPacketReader();

							System.out.println("Packet AppId = " + foobar.next().getApplicationId());


							rap.close();

							done = true; // one for now
					
				}
				dga.close();
			}
			**/
			diaryProduct.close();

			dataProductsReader.close();
			readRDR.close();

		}  catch (RtStpsException e) {
			
			e.printStackTrace();
	
		} 
	}

}
