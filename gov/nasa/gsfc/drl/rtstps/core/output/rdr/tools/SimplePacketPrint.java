/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class SimplePacketPrint {
	private static PrintStream out = System.out;
	private static int status = 1000;
	
	public static void main(String[] args) {
		
		if (args.length < 1) { 
			System.out.println("No packet input file");
			System.exit(-1);
		}
		String inputFilename = args[0];
		
		if (args.length > 2) { 
			System.out.println("No packet input file, and not optional output file");
			System.exit(-1);
		} else if (args.length == 2) {
			try {
				out = new PrintStream(args[1]);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd kk:mm:ss.SSS zzz yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		int packetSizeSum = 0;
		try {
			//out = new PrintStream(args[1]);
			
			FilePacketReader pr = new FilePacketReader(new CopyPacketFactory(), inputFilename);

			int count = 0; int statusCount = 0;
			while (pr.hasNext()) {

				Packet p = (Packet)pr.next();

				//if (p.getApplicationId() == 820) {
			
					packetSizeSum += p.getPacketSize();
				
					//System.out.print("Packet APID[" + p.getApplicationId() + "] Size[" + p.getPacketSize() + "] SeqCnt [" + p.getSequenceCounter() + "]");
					if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
						int apid = p.getApplicationId();
						out.print("Packet APID[" + apid + "] Size[" + p.getPacketSize() + "] SeqCnt [" + p.getSequenceCounter() + "]");
						PDSDate pt = new PDSDate(p.getTimeStamp(8));
						
						out.print(" TimeStampRaw Day[" + pt.getDaySegment() + "], Millis[" + pt.getMillisSegment() + "], Micros[" + pt.getMicrosSegment() + "]  -- Timestamp UTC[" + sdf.format(pt.getDate()) + "]");
						out.print(" -- Timestamp Internal: " + pt.toString());
						if ( ((apid >= 800) && (apid <= 821)) || (apid == 825)) {
							byte[] data = p.getData();
							int offset = 6 + 28;
							int scanNum = ( (int)data[offset] << 24 ) | 
							(((int)data[offset+1] << 16) & 0x00ff0000) | 
							(((int)data[offset+2] <<  8) & 0x0000ff00) | 
							((int)data[offset+3] & 0x000000ff) ;
							if (p.getApplicationId() != 11) {
								out.print("ScanNumber[" + scanNum + "]");
							}
						}
						out.println();
					} else {
						out.println("blah ==>" + p.toString());
					}
				//}
					
				if ((++count % status) == 0) {
					System.out.print(".");
					if ((++statusCount % 40) == 0) {
						System.out.println();
					}
				}
			}
			out.println("Amount of bytes processed into packets -- " + packetSizeSum);
			
			out.println("Lefover that is not processable as it is short -- " + pr.getLeftOver() + " unprocessable bytes");
			
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
