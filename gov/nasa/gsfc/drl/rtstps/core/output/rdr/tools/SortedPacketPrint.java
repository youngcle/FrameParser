/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.LPEATEDate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class SortedPacketPrint {
	private static String newline = System.getProperty("line.separator"); 
	private static PrintStream out = System.out;
	private static int status = 1000;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss.SSS zzz yyyy");
	private static long firstTime = 0L;
	private static int aeCounter=1;
	
	public static void main(String[] args) {
		
		if (args.length < 1) { 
			System.out.println("No packet input file");
			System.exit(-1);
		}
		String inputFilename = args[0];
		
		//if (args.length < 2) { 
		//	System.out.println("No packet input file, and not optional output file");
		//	System.exit(-1);
		//}
		
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		

		int sampleCount = 0;
		
		int packetSizeSum = 0;
		try {
			
			//out = new PrintStream(args[1]);
			
			// timesorted has bugs in it, does not take into segmented packets
			///TimeSortedPacketReader pr = new TimeSortedPacketReader(new FilePacketReader(new CopyPacketFactory(), args[0]));
			
			FilePacketReader pr = new FilePacketReader(new CopyPacketFactory(), args[0]);
			int scanCount = 0;
			int skipCount = 0;
			int count = 0; int statusCount = -1;
			int state = 0;
			int groupCount = 0;
			
			while (pr.hasNext()) {

				Packet p = (Packet)pr.next();

				//if (skipCount++ < 650000) continue;  big cris file
				
				//if (skipCount++ < 230000) continue; // big atms file
				
				//if (skipCount++ < 50000) continue; // big atms file
				
				//if (p.getApplicationId() == 820) {
			
					packetSizeSum += p.getPacketSize();
					
					
					
					StringBuilder str = new StringBuilder();
					
					//System.out.println(">> Packet APID[" + p.getApplicationId() + "] Size[" + p.getPacketSize() + "] SeqCnt [" + p.getSequenceCounter() + "] << ");
					
					//System.out.print("Packet APID[" + p.getApplicationId() + "] Size[" + p.getPacketSize() + "] SeqCnt [" + p.getSequenceCounter() + "]");
					//if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
					str.append("Packet APID[" + p.getApplicationId() + "] Size[" + p.getPacketSize() + "] SeqCnt [" + p.getSequenceCounter() + "]");
					
					if (p.hasSecondaryHeader()) {
						int apid = p.getApplicationId();
						
						long t_micros = 1422169897950000L - (32L * 1000000L);
						long c_Day = t_micros / PDSDate.MicrosPerDay;
						
						t_micros = t_micros - (c_Day * PDSDate.MicrosPerDay);
						
						long c_Millis = (t_micros / 1000L);
						
						t_micros = t_micros - (c_Millis * 1000L);

						
						long rawDay = c_Day;
						long rawMillis = c_Millis;
						long rawMicros =  t_micros;
						

						long rawPacketTime =   ((rawDay << 48)    & 0xffff000000000000L);
						rawPacketTime |= ((rawMillis << 16)  & 0x0000ffffffff0000L);
						rawPacketTime |=  (rawMicros         & 0x000000000000ffffL);

						
						PDSDate test = new PDSDate(rawPacketTime);
						
						//System.out.println("Test -- " + test.toString());
						
						PDSDate pt = new PDSDate(p.getTimeStamp(8)); // packet time
						PDSDate vt = new PDSDate(p.getTimeStamp(20, 8)); // viirs hdr time
						PDSDate tt = new PDSDate(p.getTimeStamp(38, 8)); // viirs terminus time  - these last two only work if it is viirs
						
						// leap variations
						LPEATEDate lpt = new LPEATEDate(p.getTimeStamp(8));
						LPEATEDate lvt = new LPEATEDate(p.getTimeStamp(20, 8));
						LPEATEDate ltt = new LPEATEDate(p.getTimeStamp(38, 8));
						
						int size = p.getPacketSize();
						
					
					str.append(" NoLeapDays[" + pt.getDaySegment() + "] NoLeapMillis[" + pt.getMillisSegment() + "] NoLeapMicros[" + pt.getMicrosSegment() + "]");
					str.append(" NoLeapIET[" + pt.getMicrosSinceEpoch() + "] LeapIET[" + lpt.getMicrosSinceEpoch() + "] ");

					str.append(" Cal -- PDSDate: " + pt.toString());
						if (firstTime == 0L) {
							firstTime = pt.getMicrosSinceEpoch();
						}
						
						long time = pt.getMicrosSinceEpoch() - firstTime;
						
						//str.append(" NoLeapTimeStamp[" + pt.getMicrosSinceEpoch() + "]"); 
						
						//str.append(" LeapTimestamp[" + lpt.getMicrosSinceEpoch() + "]"); 

						if ( ((apid >= 800) && (apid <= 821)) || (apid == 825)) {
							
							str.append(" VIIRS ID Time[" + lvt.getMicrosSinceEpoch() + "]  Scan Terminus Time[" + ltt.getMicrosSinceEpoch() + "]");
							byte[] data = p.getData();
							int offset = 6 + 28;
							int scanNum = ( (int)data[offset] << 24 ) | 
							(((int)data[offset+1] << 16) & 0x00ff0000) | 
							(((int)data[offset+2] <<  8) & 0x0000ff00) | 
							((int)data[offset+3] & 0x000000ff) ;
							if (p.getApplicationId() != 11) {
								str.append(" ScanNumber[" + scanNum + "]");
							}
						} else if (apid == 528) {
							byte[] data = p.getData();
							int offset = 6 + 8;
							int scanAngleCount = (((int)data[offset] << 8) & 0x0000ff00) | ((int)data[offset+1] & 0x000000ff) ;
							double scanAngleCountF = scanAngleCount;
							double scanAngleDegrees = scanAngleCountF * 0.00543d + -0.4998812d;
							str.append(" ScanAngle[" + String.format("%04.4f", scanAngleDegrees) + "]");
							
							offset = 6 + 8 + 2;
							int scanStart = ((int)data[offset] >>> 7) & 0x00000001;
							if (scanStart == 1) { 
								scanCount = 0;
							}
							
							boolean scanState = (scanStart == 1);
							
							if (scanState) {
								
								str.insert(0, newline + ">>>>>>>>>>>>>>>>>" + newline);
								 								
								state = 1;
							}
							str.append(" ScanStart[" + scanState + "]");
							
							int timeError = ((int)data[offset] >>> 4) & 0x0000000f;
							
							//out.print(" TimeError Pulse?[" + (timeError == 3) + "]");
							
							str.append(" Uppernibble [" + Integer.toString(timeError, 2) + "]");
							
							str.append(" Scan Angle Count [" + (++scanCount) + "]");
						} else if (apid == 515) {
							str.append(" ATMS Calibration Packet");
						} else if (apid == 530) {
							str.append(" ATMS Hot/Cold Temperatures Packet");
						} else if (apid == 531) {
							//str.append(" ATMS Health and Status Packet");
							
							if (state == 1) {
								++groupCount;
								state = 0;
							}
							
							str.append(" ATMS Health and Status Packet -- Group [" + groupCount + "]");
							
	
						} else if (apid == 536) {
							str.append(" ATMS Point & Stare Continuous Sampling Packet");
						} else if (apid == 11) {
							
							long micros = lpt.getMicrosSinceEpoch();
							
							long endIET = micros + (1L * 1000000L);
							str.append(" endIET(+1s) " + endIET);
							str.append(" <=== Attitude & Ephemeris -- "  + aeCounter++);
							//str.append(printAE(p));
							
							
						} else if (apid == 1289) {
						
							str.append(" X=== Start of CRIS Scan"); 
							++sampleCount;
						} else if ((sampleCount >= 1) && (apid >= 1315) && (apid <= 1341)) {
							str.append(" scanCount = " + sampleCount);
						} else if ((sampleCount >= 1) && (apid == 1341)) {
							++sampleCount;
						} else if ((sampleCount >= 1) && (apid >= 1342) && (apid <= 1350)) { 
							str.append(" LW 1 Deep Space");
						} else if ((sampleCount >= 1) && (apid >= 1351) && (apid <= 1359)) { 
							str.append(" MW 1 Deep Space");
						} else if ((sampleCount >= 1) && (apid >= 1360) && (apid <= 1368)) { 
							str.append(" SW 1 Deep Space");
						} else if ((sampleCount >= 1) && (apid >= 1369) && (apid <= 1395)) { 
							str.append(" Internal Cal");
						} else {
							//str.append(" An unspecified packet -- AppId [" + apid + "]");
						}
						
					}
					//System.out.println();
				//}
					
					String output = str.toString();
					if (output.equals("") == false) {
						out.println(output);
					}
					//if ((++count % status) == 0) {
					//	System.out.print(".");
					//	if ((++statusCount % 40) == 0) {
					//		System.out.println();
					//	}
					//}
			}
			System.out.println("Amount of bytes processed into packets -- " + packetSizeSum);
			
			//System.out.println("Leftover that is not processable as it is short -- " + pr.getLeftOver() + " unprocessable bytes");
			
		
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static StringBuilder printAE(Packet p) {
		StringBuilder str = new StringBuilder();
		
		byte[] data = p.getData();
		
		str.append(newline);

		int offset = 14;
		str.append(" -- S/C Id [" + ((int)data[offset] & 0x0ff) + "]").append(newline);
		
		offset += 1;
		
		//PDSDate test = new PDSDate(0L);
		//out.println("Test -- " + sdf.format(test.getDate()));
		
		PDSDate eTime = new PDSDate(p.getTimeStamp(offset, 8));
		
		//out.println(" -- time & raw time >> " + eTime.toString() + " <<");
		//out.println(" -- plain >> " + eTime.getDate() + " <<");
		str.append(" -- Ephemeris Timestamp[" + sdf.format(eTime.getDate()) + "]").append(newline);
		
		offset += 8;
		
		int e1 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int e2 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int e3 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;

		str.append(" -- E-pos1[" + Float.intBitsToFloat(e1) + "] E-pos2[" + Float.intBitsToFloat(e2) + "] E-pos3[" + Float.intBitsToFloat(e3) + "]").append(newline);
		  
		
		int ev1 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int ev2 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int ev3 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		

		offset += 4;
		
		str.append(" -- E-vol1[" + Float.intBitsToFloat(ev1) + "] E-vol2[" + Float.intBitsToFloat(ev2) + "] E-vol3[" + Float.intBitsToFloat(ev3) + "]").append(newline);
		
		PDSDate aTime = new PDSDate(p.getTimeStamp(offset, 8));
		str.append(" -- Attitude Timestamp[" + sdf.format(aTime.getDate()) + "]").append(newline);
		
		
		offset += 8;
		int q1 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int q2 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int q3 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int q4 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;

		str.append(" -- Quat1[" + Float.intBitsToFloat(q1) + "] Quat2[" + Float.intBitsToFloat(q2) + "] Quat3[" + Float.intBitsToFloat(q3) + "] Quat4[" + Float.intBitsToFloat(q4) + "]").append(newline);
		
		return str;	
	}
	
	
}
