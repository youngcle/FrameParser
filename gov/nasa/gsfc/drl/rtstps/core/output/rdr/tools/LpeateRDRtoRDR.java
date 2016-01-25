/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.CommonDataSetObject;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.DataProductsReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Granule;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.MissionName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Origin;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PlatformShortName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDR;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileWriter;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRProduct;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

/**
 * Read the static header data contents and send them to the DRL RT-STPS RDRBuilder processing chain as
 * a buffer.
 * 
 * This uses a tempfile, the in memory version did not work.
 * 
 * And print the attributes of Products and Granules to the console
 * 
 *
 */
public class LpeateRDRtoRDR {

	 File temp;

	private FileOutputStream dataFile;
	
	public LpeateRDRtoRDR(String inputFilename, String outputDir) throws RtStpsException { 
		
		
		
		processRDRfromFile(inputFilename);
		
		builderProcessing(outputDir);

	}

	
	
	public void builderProcessing(String destinationDir) throws RtStpsException {
		RDR.DocumentName = "D34862-02_NPOESS-CDFCB-X-Vol-II_D_20090603_I1.5.0.pdf";
		
		RDRFileWriter rdrBuilder = new RDRFileWriter(destinationDir, 1, 1, Origin.nfts, MissionName.NPP, Origin.nfts, PlatformShortName.NPP);
		
		FilePacketReader pr = new FilePacketReader(new NoCopyPacketFactory(), temp.getAbsolutePath());

		System.out.println("Beginning granule processing...");
		while (pr.hasNext()) {
			Packet p = (Packet) pr.next();
			int appid = p.getApplicationId();
			
			//LPEAT has this in the data...
			//if ((appid == 0) || (appid == 8)) {
			//	continue;
			//}
			rdrBuilder.put(p);
			
		}
	
		rdrBuilder.close(false); // in the case of being called by RDRDirConvert, this should false
		
		System.out.println("Granule processing complete.");
		

	}
	

	
	public void processRDRfromFile(String inputFilename) throws RtStpsException {

		 // Create temp file.
		   
		try {
			temp = File.createTempFile("data", ".tmp");
		

			// Delete temp file when program exits.
			temp.deleteOnExit();

			dataFile = new FileOutputStream(temp);
		
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
		

		RDRFileReader readRDR = new RDRFileReader(inputFilename);
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
					
					int size = end - offset;
					
					try {
						dataFile.write(data, offset, end);
					} catch (IOException e) {
						
						rap.close();
						ga.close();
						dataProductsReader.close();
						readRDR.close();
						throw new RtStpsException(e);
					}
					
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
		//readRDR.close();  // this closes out HDF for this process, so comment it out
		
		try {
			dataFile.close();
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
		
		
		
	}


	
	/**
	 * @param args the RDR HDF file
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("No input file to process... no output directory destination");
			System.exit(-1);
		}
		
		try {
			new LpeateRDRtoRDR(args[0], args[1]);
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}



}
