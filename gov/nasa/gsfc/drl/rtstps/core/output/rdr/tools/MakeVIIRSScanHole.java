/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.VIIRSSciencePacket;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class MakeVIIRSScanHole {
	private static int index = 0;
	private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd kk:mm:ss.SSS zzz yyyy");
	
	
	private String mode = "UNKNOWN";
	private long scanNumber = -1L;  // only used for the mid/last sci packets
	public static int scanMode = -1;
	
	public MakeVIIRSScanHole() {
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}



	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("No files to process: [packet input file] [packet output file]");
			System.exit(-1);
		}
		try {
		
	
			
			FilePacketReader pr = new FilePacketReader(new CopyPacketFactory(), args[0]);
			
			FileOutputStream fileOut = new FileOutputStream(args[1]);
			
			long currentScanCount = 0L;
			int minorScanCount = 0;
			int majorScanCount = 0;
			int state = 1;
			
			while (pr.hasNext()) {

				Packet pp = (Packet) pr.next();
				
				int apid = pp.getApplicationId();
			
				
				//   get scan count
				// if scan count changes increment
				// if 48 have come through, increment major count
				// if major count is > X then...
				//   skip the next N...
				//   hopefuly this creates a hole
				// then just copy everything else to output
				
				if (state == 1) {
					// write packet to file
					fileOut.write(pp.getData(), pp.getStartOffset(), pp.getPacketSize());
					
					// if viirs standalone
					if ( (( (apid >= 800) && (apid <= 821)) || 
							(apid == 825) || 
							(apid == 826)) && 
							(pp.isFirstPacketInSequence() || pp.isStandalonePacket())) {

						long scanCount = VIIRSSciencePacket.getScanNumber(pp);
						if (scanCount != currentScanCount) {
							++minorScanCount;
							currentScanCount = scanCount;
						}

						if (minorScanCount >= 48) {
							++majorScanCount;
							minorScanCount = 0;
						}
						
						if (majorScanCount >= 6) {
							System.out.println("Major Scan Phase 1 Reached");
							state = 2;
						}
					}
				} else if (state == 2) {
					// state == 2
					// if viirs standalone
				
					if ( (( (apid >= 800) && (apid <= 821)) || 
							(apid == 825) || 
							(apid == 826)) && 
							(pp.isFirstPacketInSequence() || pp.isStandalonePacket())) {

						long scanCount = VIIRSSciencePacket.getScanNumber(pp);
						if (scanCount != currentScanCount) {
							++minorScanCount;
							currentScanCount = scanCount;
						}

						// skip 20 more scans
						if (minorScanCount >= 20) {
							
							System.out.println("Major Scan Phase 2 -- skipping over");
							state = 3;
						}
					}
				} else {
					// state == 3
					// write packet to file
					fileOut.write(pp.getData(), pp.getStartOffset(), pp.getPacketSize());
				}
	
			}
			
			fileOut.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RtStpsException e) {
			e.printStackTrace();
		}
	}

}
