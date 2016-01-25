/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.DataProductsReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Granule;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRProduct;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.SequentialPacketReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.TimeFormat;

/**
 * Open an RDR file and locate the SpacecraftAOS Diary granules.  Compare the times
 * stored in the granules with the times in its RawApplicationPackets through the reference
 * in the granule.  If the granule times do not encapsulate the RawApplicationPacket,
 * issue and error message.
 * 
 *
 */
public class CheckDiary {
	/**
	 * Check that the outside granule encapsulates the inside granule in time!
	 * @param outside probably the spacecraft diary
	 * @param inside probably one of the other sensors like VIIRS, CRIS or ATMS
	 * @throws RtStpsException
	 */
	private void granuleTime(Granule outside, Granule inside) throws RtStpsException {
		PDSDate begObs = outside.getBeginningObservationDateTime();
		PDSDate endObs = outside.getEndingObservationDateTime();
		
		RawApplicationPackets rap = inside.getReferencedRawApplicationPackets();
		
		PDSDate firstPktTime = getFirstPacketTime(rap);
		PDSDate lastPktTime = getLastPacketTime(rap);
		
		boolean noGranTimeErr = true;
		
		if (begObs.compareTo(firstPktTime) > 0) {
			granuleError("Beg Time ", begObs, firstPktTime, endObs, lastPktTime, outside);
			noGranTimeErr = false;
		}
		if (endObs.compareTo(lastPktTime) < 0) {
			granuleError("End Time ", begObs, firstPktTime, endObs, lastPktTime, outside);
			noGranTimeErr = false;
		}
		
		//if (noGranTimeErr) {
			granulePrint(outside, firstPktTime, inside, lastPktTime);
		//}
		
		rap.close();
		
	}
	private void granuleError(String which, PDSDate begObs, PDSDate firstPktTime, PDSDate endObs, PDSDate lastPktTime, Granule g) {
		System.out.println(which + " Diary Time Encaptulation Error ---");
		System.out.println("==> Start Granule: " + g.getName());
		System.out.println(" --- Beg Obs OrigStrAttr: " + g.getBeginning_Date() + "::" + g.getBeginning_Time());
		System.out.println(" --- Beg Obs Time (conv): " + begObs.toString()); 
		System.out.println(" --- First Packet Sensor Time: " + firstPktTime.toString());
		
		System.out.println(" --- Last  Packet Sensor Time: " + lastPktTime.toString());
		System.out.println(" --- End Obs Time (Conv): " + endObs.toString());
		System.out.println(" --- End Obs OrigStrAttr: " + g.getEnding_Date() + "::" + g.getEnding_Time());
		
		System.out.println("==> End Granule: " + g.getName());
		//System.out.println("    Beginning_Date: " +  g.getBeginning_Date() );
		//System.out.println("    Beginning_Time: " + g.getBeginning_Time() );
		//System.out.println("    Ending_Date: " + g.getEnding_Date());
		//System.out.println("    Ending_Time: " +  g.getEnding_Time());
		//granulePrintRemaining(g);
	}
	

	private void granulePrint(Granule outside, PDSDate firstPktTime, Granule inside, PDSDate lastPktTime) throws RtStpsException {
		RawApplicationPackets pout = outside.getReferencedRawApplicationPackets();
		RawApplicationPackets pin = inside.getReferencedRawApplicationPackets();
		
		System.out.println("==> Granule: " + outside.getName() + " Encapsulutes Packets");
		System.out.println("    Beginning AE Date/Time: " +  outside.getBeginning_Date() + ":" + outside.getBeginning_Time() + " -- " + hdrToString(getFirstPacket(pout)));
		System.out.println("    ==> First Packet Sensor Date/Time: " +  TimeFormat.formatPDSDateTime(firstPktTime).insert(8, ':').toString() + " -- " + hdrToString(getFirstPacket(pin)));
		System.out.println("    ==> Last  Packet Sensor Date/Time: " +  TimeFormat.formatPDSDateTime(lastPktTime).insert(8, ':').toString() + " -- " + hdrToString(getLastPacket(pin)));
		System.out.println("    Ending    AE Date/Time: " + outside.getEnding_Date() + ":" +  outside.getEnding_Time() + " -- " + hdrToString(getLastPacket(pout)));
		
		/**System.out.println("    N_Beginning_Orbit_Number: " +  g.getN_Beginning_Orbit_Number());
		System.out.println("    N_Beginning_Time_IET: " + g.getN_Beginning_Time_IET() );
		System.out.println("    N_Creation_Date: " +  g.getN_Creation_Date());
		System.out.println("    N_Creation_Time: " +  g.getN_Creation_Time());
		System.out.println("    N_Ending_Time_IET: " +  g.getN_Ending_Time_IET());
		System.out.println("    N_Granule_ID: " +  g.getN_Granule_ID());
		System.out.println("    N_Granule_Status: " +  g.getN_Granule_Status());
		System.out.println("    N_Granule_Version: " +  g.getN_Granule_Version());
		System.out.println("    N_LEOA_Flag: " +  g.getN_LEOA_Flag());
		System.out.println("    N_NPOESS_Document_Ref: " +  g.getN_NPOESS_Document_Ref());
		**/
		System.out.println("    ===> Sensor N_Packet_Type: " +  inside.getN_Packet_Type());				
		System.out.println("    ===> Sensor N_Packet_Type_Count: " +  inside.getN_Packet_Type_Count());

		/**
		System.out.println("    N_Percent_Missing_Data: " +  g.getN_Percent_Missing_Data());
		
		System.out.println("    N_Reference_ID: " +  g.getN_Reference_ID());
		System.out.println("    N_Software_Version: " + g.getN_Software_Version());
		**/
		System.out.println();
		
	}
	
	private String hdrToString(Packet packet) {
		StringBuffer sb = new StringBuffer();
		sb.append("APID: ");
		sb.append(Integer.toString(packet.getApplicationId()));
		sb.append(" SeqCtr: ");
		sb.append(Integer.toString(packet.getSequenceCounter()));
		return sb.toString();
	}
	// return last packet with a S/C timestamp (probably)
	private Packet getLastPacket(RawApplicationPackets rap) throws RtStpsException {
		StaticHeader sh = rap.getStaticHeader();
		
		SequentialPacketReader reader = sh.createSequentialPacketReader();
		
		if (reader.hasNext() == false) {
			throw new RtStpsException("Last packet not found");
		}
		Packet lastPacket = (Packet)reader.next();  // first packet should probably have the S/C, but we don't check it now...
		
		while (reader.hasNext()) {
			Packet tmp = (Packet)reader.next();
			if (tmp.isFirstPacketInSequence() || tmp.isStandalonePacket()) {
				lastPacket = (Packet)tmp;
			}
		}		
		
		return lastPacket;
	}
	
	// first packet should probably have the S/C, but we don't check it now...
	private Packet getFirstPacket(RawApplicationPackets rap) throws RtStpsException {
		StaticHeader sh = rap.getStaticHeader();
		
		SequentialPacketReader reader = sh.createSequentialPacketReader();
	
		if (reader.hasNext() == false) {
			throw new RtStpsException("First packet not found: raw app data -- index[" + reader.getIndex() + "], dataLength [" + reader.getDataLength() + "]");
		}
		Packet firstPacket = (Packet)reader.next();
		
		return firstPacket;
	}
	
	
	private PDSDate getLastPacketTime(RawApplicationPackets rap) throws RtStpsException {
		StaticHeader sh = rap.getStaticHeader();
		
		SequentialPacketReader reader = sh.createSequentialPacketReader();
		
		if (reader.hasNext() == false) {
			throw new RtStpsException("Last packet not found");
		}
		Packet lastPacket = (Packet)reader.next();
		
		PDSDate pt = new PDSDate(lastPacket.getTimeStamp(8));
		
		//System.out.println("Lst Pkt time (strt) -- " + pt.toRawFields());
		while (reader.hasNext()) {
			lastPacket = (Packet)reader.next();
			if (lastPacket.isFirstPacketInSequence() || lastPacket.isStandalonePacket()) {
				pt = new PDSDate(lastPacket.getTimeStamp(8));
			}
			//System.out.println("Lst Pkt time (interim) -- " + pt.toRawFields());
		}
		//System.out.println("Lst Lst Pkt time (final)-- " + pt.toRawFields());
		
		
		return pt;
	}
	
	// This just takes the first packet in the Granule which SHOULD be either a Standalone packet
	// or the first packet in the sequence.  And it should have a S/C timestamp... 
	// But none of this is checked at the moment.
	//
	private PDSDate getFirstPacketTime(RawApplicationPackets rap) throws RtStpsException {
		StaticHeader sh = rap.getStaticHeader();
		
		SequentialPacketReader reader = sh.createSequentialPacketReader();
	
		if (reader.hasNext() == false) {
			throw new RtStpsException("First packet not found: raw app data -- index[" + reader.getIndex() + "], dataLength [" + reader.getDataLength() + "]");
		}
		Packet firstPacket = (Packet)reader.next();
		
		PDSDate pt = new PDSDate(firstPacket.getTimeStamp(8));
		
		//System.out.println("Fst Pkt time -- " + pt.toRawFields());
		
		return pt;
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
			CheckDiary check = new CheckDiary();
			RDRProduct diaryProduct = dataProductsReader.getRDRDataProductByName("SPACECRAFT-DIARY-RDR");
			RDRProduct otherProduct = dataProductsReader.getRDRDataProductByName("ATMS-SCIENCE-RDR");
			
			System.out.println("Instrument Short Name: " + diaryProduct.getInstrument_Short_Name());
			System.out.println("Collection Short Name: " + diaryProduct.getN_Collection_Short_Name());
			System.out.println("Dataset Type Tag: " + diaryProduct.getN_Dataset_Type_Tag());
			System.out.println("Processing Domain: " + diaryProduct.getN_Processing_Domain());
			System.out.println("...");
			System.out.println("Instrument Short Name: " + otherProduct.getInstrument_Short_Name());
			System.out.println("Collection Short Name: " + otherProduct.getN_Collection_Short_Name());
			System.out.println("Dataset Type Tag: " + otherProduct.getN_Dataset_Type_Tag());
			System.out.println("Processing Domain: " + otherProduct.getN_Processing_Domain());
			System.out.println("...");

			int count = (int)diaryProduct.getGranuleCount();
			for (int n = 0; n < count; n++) {
				Granule gd = diaryProduct.getGranule(n);
				Granule go = otherProduct.getGranule(n);
				//System.out.println("GDiary ==> " + gd.toString());
				//System.out.println("GSensor ==> " + go.toString());
				check.granuleTime(gd, go);
				
				
				gd.close();
				go.close();
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
