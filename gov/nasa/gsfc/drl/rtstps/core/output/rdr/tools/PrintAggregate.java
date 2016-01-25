/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.PacketI;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Aggregate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.DataProductsReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAllReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRProduct;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.SequentialPacketReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

/**
 * Print the attributes of Products and Granules to the console
 * 
 *
 */
public class PrintAggregate {

	static void print(RDRAllReader rar) throws RtStpsException {
	
		while (rar.hasNext()) {
			RawApplicationPackets rap = rar.next();
			
			StaticHeader sh = rap.getStaticHeader();
			byte[] data = rap.getData();
			int offset = sh.getApStorageOffset();
			
			SequentialPacketReader spr = sh.createSequentialPacketReader();
			
			while (spr.hasNext()) {
				PacketI p = spr.next();
				
				System.out.println("AppId=" + p.getApplicationId() + " Size: " + p.getPacketSize());
			}
			
			//System.out.println("Offset into storage area: " + offset);
			
			rap.close();
		}
		rar.close();
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
			

			while (dataProductsReader.hasNext()) {
				
				RDRProduct rdrProduct = dataProductsReader.next();
				
				System.out.println("Instrument Short Name: " + rdrProduct.getInstrument_Short_Name());
				System.out.println("Collection Short Name: " + rdrProduct.getN_Collection_Short_Name());
				System.out.println("Dataset Type Tag: " + rdrProduct.getN_Dataset_Type_Tag());
				System.out.println("Processing Domain: " + rdrProduct.getN_Processing_Domain());
				Aggregate a = rdrProduct.readAggregate();
				
				// FIXME something in here is amiss, it crashes the JVM on exit if H5close is called...
				// if this part is skipped it is fine...
			//	while (rdrProduct.hasNext()) {
				//	CommonDataSetObject ga = rdrProduct.next();
					/**
					if (ga instanceof Aggregate) {
						
						Aggregate a = (Aggregate) ga;
						**/
						System.out.println("==> Aggregate");
						System.out.println("    AggregateBeginningDate: " + a.getBeginningDateFormatted());
						System.out.println("    AgggregateBeginningGranuleID: " + a.getBeginningGranuleId());
						System.out.println("    AggregateBeginninOrbitNumber: " + a.getBeginningOrbit());
						System.out.println("    AggregateBeginningTime: " + a.getBeginningTimeFormatted());
						System.out.println("    AggregateEndingDate: " + a.getEndingDateFormatted());
						System.out.println("    AggregateEndingGranuleID: " + a.getEndingGranuleId());
						System.out.println("    AggregateEndingOrbitNumber: " + a.getEndingOrbit());
						System.out.println("    AggregateEndingTime: " + a.getEndingTimeFormatted());
						System.out.println("    AggregateNumberGranules: " + a.getGranuleCount());
						System.out.println();
						/**&
						//RDRAllReader allReader = a.getReferencedRDRAll();
						
						//allReader.close();
						
					} 
					**/
					//ga.close();
					
				//}
						//RDRAllReader allReader = a.getReferencedRDRAll();
						PrintAggregate.print(a.getReferencedRDRAll());
						//allReader.close();
				a.close();
				rdrProduct.close();
			}
			dataProductsReader.close();
			readRDR.close();
		}  catch (RtStpsException e) {
			
			e.printStackTrace();
	
		} 
	}

}
