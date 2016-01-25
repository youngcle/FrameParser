/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

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
 * Print all the Granules begin IET and end IET times sorted together
 * 
 *
 */
public class PrintSortedGranuleTimes {
	
	private class Things {
		Long time;
		String name;
		String label = "";
		Things(Long time, String name) {
			this.time = time;
			this.name = name;
		}
		Things(Long time, String name, String label) {
			this.time = time;
			this.name = name;
			this.label = label;
		}
	}
	private class ThingsComparator implements Comparator<Things> {

		@Override
		public int compare(Things arg0, Things arg1) {
			if (arg0.time < arg1.time) return -1;
			if (arg0.time > arg1.time) return 1;
			
			return 0;
		}
		
	}
	private List<Things> gtimes = new LinkedList<Things>();
	
	public PrintSortedGranuleTimes(String filename) throws RtStpsException {
		RDRFileReader readRDR = new RDRFileReader(filename);
		DataProductsReader dataProductsReader = readRDR.createDataProductsReader();
		

		while (dataProductsReader.hasNext()) {
			
			RDRProduct rdrProduct = dataProductsReader.next();
			
			while (rdrProduct.hasNext()) {
				CommonDataSetObject ga = rdrProduct.next();
				if (ga instanceof Granule) {
					Granule g = (Granule) ga;
					/***
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
					***/
					
					RawApplicationPackets rap = g.getReferencedRawApplicationPackets();
					
					StaticHeader sh = rap.getStaticHeader();

					SequentialPacketReader pr = sh.createSequentialPacketReader();
					
					// We want the first and last packet with a time header
					Packet firstPacket = null;
					Packet lastPacket = null;
					while (pr.hasNext()) {
						Packet p = (Packet) pr.next();
						if (p.hasSecondaryHeader()) {
							if (firstPacket == null) {
								firstPacket = p;  // first packet with a time
							}
							lastPacket = p;   // last packet with a time 
						}
					}
					
					// if either are still null, it will crash the program...
					
					PDSDate fpts = new PDSDate(firstPacket.getTimeStamp(8));
					PDSDate lpts = new PDSDate(lastPacket.getTimeStamp(8));
					PDSDate lfpts = new LPEATEDate(firstPacket.getTimeStamp(8));
					PDSDate llpts = new LPEATEDate(lastPacket.getTimeStamp(8));

					
					long pbegIET = fpts.getMicrosSinceEpoch();
					long pendIET = lpts.getMicrosSinceEpoch();

					long lpbegIET = lfpts.getMicrosSinceEpoch();
					long lpendIET = llpts.getMicrosSinceEpoch();
				
					
					String name = g.getName();
					long begIET = g.getN_Beginning_Time_IET();
					long endIET = g.getN_Ending_Time_IET();
					
					long startb = sh.readStartBoundary();
					long endb = sh.readEndBoundary();
					
					gtimes.add(new Things(begIET, name + " begIET"));
					gtimes.add(new Things(endIET, name + " endIET"));

					if (name.startsWith("VIIRS-SCIENCE")) {
						
						gtimes.add(new Things(pbegIET, name + " 1stPkt", "(raw)"));
						gtimes.add(new Things(pendIET, name + " LstPkt", "(raw)"));
						
						gtimes.add(new Things(lpbegIET, name + " 1stPkt", "(leap/trunc -- lpeate style)"));
						gtimes.add(new Things(lpendIET, name + " LstPkt", "(leap/trunc -- lpeate style)"));

						
						gtimes.add(new Things(startb, name + " Sbound"));
						gtimes.add(new Things(endb,   name + " Ebound"));

					}

				}
				ga.close();
			}
			rdrProduct.close();
		}
		
		dataProductsReader.close();
		readRDR.close();
		
	}
	
	public void printSortedTimes() {
		
		Collections.sort(gtimes, new ThingsComparator());
		Iterator<Things> entries = gtimes.iterator();
		
		while (entries.hasNext()) {
			Things entry = entries.next();
			String out = String.format("%34s --- %d %s", entry.name, entry.time, entry.label);
			System.out.println(out);
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

			PrintSortedGranuleTimes psgt = new PrintSortedGranuleTimes(args[0]);
			
			psgt.printSortedTimes();
			
		}  catch (RtStpsException e) {
			
			e.printStackTrace();
	
		} 
	}

}
