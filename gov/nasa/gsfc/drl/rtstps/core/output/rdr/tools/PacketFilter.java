/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class PacketFilter {
	private static int index = 0;
	private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd kk:mm:ss.SSS zzz yyyy");
	
	
	private String mode = "UNKNOWN";
	private long scanNumber = -1L;  // only used for the mid/last sci packets
	public static int scanMode = -1;
	
	public PacketFilter() {
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
			
			
			
			while (pr.hasNext()) {

				Packet pp = (Packet) pr.next();
				
				int apid = pp.getApplicationId();
				
				if (apid != 11) {


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
