/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class PacketPrint {
	private static int index = 0;
	private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd kk:mm:ss.SSS zzz yyyy");
	
	
	private String mode = "UNKNOWN";
	private long scanNumber = -1L;  // only used for the mid/last sci packets
	public static int scanMode = -1;
	
	public PacketPrint() {
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public StringBuffer ae(Packet p) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(p.hdrToString());
		PDSDate pt = new PDSDate(p.getTimeStamp(8));

		sb.append(" -- Attitude and Ephemeris");
		if (mode.equals("UNKNOWN")) {
			sb.append("      ");
		} else if (mode.equals("NIGHT")) {
			sb.append("    ");
		} else if (mode.equals("DAY")) {
			sb.append("  ");
		}
		sb.append(" -- S/C TimeStamp[" + sdf.format(pt.getDate()) + "]");
		
		return sb;
	}
	
	public StringBuffer sci(Packet p) {
		StringBuffer sb = new StringBuffer();
	
		sb.append(p.hdrToString());
		if (p.isFirstPacketInSequence()) {
			sb.append(" --  First Packet[" + String.format("%2d", index++) + "]"); 
			
			scanNumber  = getScanNumber(p, 34);

			scanMode  = getScanMode(p);
			if (scanMode == 4) {
				mode = "DAY";
			} else if (scanMode == 5) {
				mode = "NIGHT";
			}
			
			sb.append(" -- " + mode);
			
		} else if (p.isMiddlePacketInSequence()) {
			PDSDate pt = new PDSDate(p.getTimeStamp(8,8));
			sb.append(" -- Middle Packet[" + String.format("%2d", index++) + "]");
			sb.append(" -- " + mode);
			sb.append(" -- Pkt TimeStamp[" + sdf.format(pt.getDate()) + "]");
		} else if (p.isLastPacketInSequence()) {
			PDSDate pt = new PDSDate(p.getTimeStamp(8,8));
			sb.append(" --   Last Packet[" + String.format("%2d", index) + "]");
			sb.append(" -- " + mode);
			sb.append(" -- Pkt TimeStamp[" + sdf.format(pt.getDate()) + "]");
			index = 0;
		} 
	
		if (p.hasSecondaryHeader()) { 
			PDSDate pt = new PDSDate(p.getTimeStamp(8));
			sb.append(" -- S/C TimeStamp[" + sdf.format(pt.getDate()) + "]");
		}
		sb.append(" -- Scanline[" + String.format("%4d", scanNumber) + "]");
		return sb;
	}
	
	// 825
	public StringBuffer calibration(Packet p) {
		StringBuffer sb = new StringBuffer();
		
		
		sb.append(p.hdrToString());
		sb.append(" -- Calibration");
		
		if (p.isFirstPacketInSequence()) {
			sb.append(" --  First Packet[" + String.format("%2d", index++) + "]"); 
			
			scanNumber  = getScanNumber(p, 34);

	
		} else if (p.isMiddlePacketInSequence()) {
			PDSDate pt = new PDSDate(p.getTimeStamp(8,8));
			sb.append(" -- Middle Packet[" + String.format("%2d", index++) + "]");
			
			//sb.append(" -- Pkt TimeStamp[" + sdf.format(pt.getDate()) + "]");
		} else if (p.isLastPacketInSequence()) {
			PDSDate pt = new PDSDate(p.getTimeStamp(8,8));
			sb.append(" --   Last Packet[" + String.format("%2d", index) + "]");
			
			//sb.append(" -- Pkt TimeStamp[" + sdf.format(pt.getDate()) + "]");
			index = 0;
		} 
		
		if (p.hasSecondaryHeader()) { 
			PDSDate pt = new PDSDate(p.getTimeStamp(8));
			sb.append(" -- S/C TimeStamp[" + sdf.format(pt.getDate()) + "]");
		}
		
		sb.append(" -- Scanline[" + String.format("%4d", scanNumber) + "]");
		return sb;
	}
	
	// 826
	public StringBuffer engineering(Packet p) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(p.hdrToString());
		sb.append(" -- Engineering");
		

		if (p.hasSecondaryHeader()) { 
			PDSDate pt = new PDSDate(p.getTimeStamp(8));
			sb.append(" -- S/C TimeStamp[" + sdf.format(pt.getDate()) + "]");
		}
		scanNumber = getScanNumber(p, 48);

		sb.append(" -- Scanline[" + String.format("%4d", scanNumber) + "]");
		
		return sb;
	}
	
	private long getScanNumber(Packet p, int offset) {
		byte[] data = p.getData();
		int index = p.getStartOffset() + offset;
		long scanNum = ( (long)data[index] << 24 ) | 
						(((long)data[index+1] << 16) & 0x0000000000ff0000l) | 
						(((long)data[index+2] <<  8) & 0x0000000000000ff00l) | 
						((long)data[index+3] & 0x00000000000000ffl) ;
		scanNum &= 0x00000000ffffffffl;
		return scanNum;
	}

	private int getScanMode(Packet p) {
		byte[] data = p.getData();
		int offset = p.getStartOffset() + 6 + 28 + 4 + 8;
		int scanMode = (int)data[offset] & 0x000000ff ;
		
		return scanMode;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 2) {
			System.out.println("No files to process: [packet input file] <output binary file>");
			System.exit(-1);
		}
		try {
		
			FileOutputStream fileOut = null;
			
			if (args.length == 2) {
				fileOut = new FileOutputStream(args[1]);
			}
			
			FilePacketReader pr = new FilePacketReader(new CopyPacketFactory(), args[0]);
			
			//TimeSortedPacketReader pr = new TimeSortedPacketReader(new FilePacketReader(new CopyPacketFactory(), args[0]));
			
			PacketPrint pktPrint = new PacketPrint();
			
			int aeCount = 0;
			
			
			while (pr.hasNext()) {

				Packet pp = (Packet) pr.next();
				
				int apid = pp.getApplicationId();
				
				if (apid == 11) {

					StringBuffer sb = pktPrint.ae(pp);
					System.out.println(sb);
					
					aeCount++;
								
				} else if ((apid >= 800) && (apid <= 823)) {
					
					StringBuffer sb = pktPrint.sci(pp);
					if (pktPrint.scanNumber > 815) {
						System.out.println(sb);
					} else {
						continue;
					}
					
				} else if (apid == 825) {

					StringBuffer sb = pktPrint.calibration(pp);
					if (pktPrint.scanNumber > 815) {
						System.out.println(sb);
					} else {
						continue;
					}
					
				} else if (apid == 826) {
					
					StringBuffer sb = pktPrint.engineering(pp);
					if (pktPrint.scanNumber > 815) {
						System.out.println(sb);
					} else {
						continue;
					}
				}
				
				if (fileOut != null) {
					if (aeCount > 0) {
						fileOut.write(pp.getData(), pp.getStartOffset(), pp.getPacketSize());
					}
					if (aeCount > 1000) break;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RtStpsException e) {
			e.printStackTrace();
		}
	}

}
