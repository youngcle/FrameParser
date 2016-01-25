/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.ATMSScanState;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * Prints aspects of ATMS packet read from a file
 * 
 *
 */
public class ATMSPacketPrint {
	private static String newline = System.getProperty("line.separator"); 
	private static PrintStream out = System.out;
	private static int status = 1000;
	
	private List<Packet> scanFilling = new LinkedList<Packet>();
	private List<Packet> scanComplete;

	
	private final int maxScanGroup = 12;
	private ATMSScanState scanState = ATMSScanState.Begin;
	private int scanGroupCounter = 0;
	private int scanStarts = 0;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss.SSS zzz yyyy");

	public void put(Packet p) throws RtStpsException {
		
		
		setScanState(p);
		
		if (scanState == ATMSScanState.Complete) {
			// move the scanFilling pile to the scanComplete pile
			scanComplete = scanFilling;
			
			// make a new scanFilling and put the packet in it
			scanFilling = new LinkedList<Packet>();
			scanFilling.add(p);
		} else {
			// otherwise its always added
			scanFilling.add(p);
		}
	}
	
	public boolean hasNext() {
		if (scanState == ATMSScanState.Complete) {
			return true;
		}
		return false;
	}
	
	public  List<Packet> next() throws RtStpsException {
		scanState = ATMSScanState.Begin;
		
		scanGroupCounter = 0;
		
		// this should not be null since its a bug in logic of the code
		// 
		List<Packet> scansToReturn = scanComplete;
		
		if (scansToReturn == null) {
			throw new RtStpsException("Incorrect ATMS state: returning scan list is empty");
		}
		
		scanComplete = null;
		
		return scansToReturn;
	}
	
	private void setScanState(Packet p) throws RtStpsException {
		
		
		int apid = p.getApplicationId();
		
		if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
			if (apid == 528) {
				byte[] data = p.getData();

				int offset = 6 + 8 + 2;
				int scanStart = ((int)data[offset] >>> 7) & 0x00000001;
				if (scanStart == 1) { 
					if (scanState == ATMSScanState.Begin) {
						if (scanFilling.size() == 0) {
							scanState = ATMSScanState.Scanstart;
						} else if (scanFilling.size() > 0) {
							// this is the first set which is short...
							// we want to get it out of the way so
							// as to process whole groups
							scanState = ATMSScanState.Complete;
						}
						
						// track the number of scan starts
						++scanStarts;
						
					} else if (scanState == ATMSScanState.Scanstart) {
						//error state, this means two scan starts were found in row
						throw new RtStpsException(" Incorrect ATMS state: scan started but state is [" + scanState + "]");
						
					} else if (scanState == ATMSScanState.MidScan) {
						// We were collecting mid-scan and now a new scan has arrived
						
						// track the number of scan starts
						++scanStarts;
						if (scanGroupCounter > 0) {
							++scanGroupCounter;
						}
						// if the number of scans needed has been reached then the group is done
						if (scanGroupCounter >= maxScanGroup) {
							scanState = ATMSScanState.Complete;
						}
						
					} else {
						// error, the scan started but its already marked as complete
						throw new RtStpsException(" Incorrect ATMS state: scan started but state is [" + scanState + "]");
					}
				} else {
					if (scanState == ATMSScanState.Begin) {
						// no scan start has been seen yet and so we accumulate packets
						scanState = ATMSScanState.MidScan;
					} else if (scanState == ATMSScanState.Scanstart) {
						// the scan start was found previously, now we should be in that middle scan collection of packet
						scanState = ATMSScanState.MidScan;
					} else if (scanState == ATMSScanState.MidScan) {
						// once in mid-scan we stay in mid-scan until a new scan start
						// so nothing...
					} else {
						// everything else is a error
						// error, the scan started but its already marked as complete
						throw new RtStpsException(" Incorrect ATMS state: scan started but state is [" + scanState + "]");
					}
				}
			} else if (apid == 531) {
				// Health & Status packet arrives
				// seems to indicate the start of a three group scan
				// which is what needs to be collected
				
				// first off it things are normals:
				// the state should be Scanning and the scanCount should be 1
				if (scanState == ATMSScanState.MidScan) {
					//System.out.println("Setting scangroupctr from [" + scanGroupCounter + "] to [" + 1 + "]");
					if (scanGroupCounter == 0) {
						// kick off group counter...
						scanGroupCounter = 1;
					} // else we're already accumulating groups of scans...
				} else {
					// we are not expecting it except in the middle of a scan so anything else is an error
					throw new RtStpsException(" Incorrect ATMS state: H&S packet received at wrong time? [" + scanState + "]");
				}
				
			}
		}
		
	}
	
	private void printScanInfo() throws RtStpsException {
		List<Packet> packets = next();
		int[] appIds = new int[2048];
		
		for (Packet p : packets) {
			appIds[p.getApplicationId()]++;
		}
		
		System.out.println("Scan group contents: " + atmsSplice(appIds, ","));
		
	}

	public final String atmsSplice(int[] vs, String delimiter) {
	    if (vs.length == 0) return "";
	    StringBuffer buffer = new StringBuffer(vs[1]);
	    boolean firstTime = true;
	    for (int i = 0; i < vs.length; i++) {
	    	if (vs[i] != 0) {
	    		if (firstTime) {
	    			buffer.append("[appId=").append(i).append(",").append(vs[i]).append("]");
	    			firstTime = false;
	    		} else {
	    			buffer.append(delimiter).append(' ').append("[appId=").append(i).append(",").append(vs[i]).append("]");
	    		}
	    	}
	    }
	    
	    return buffer.toString();
	}
	
	private StringBuilder printAE(Packet p) {
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
	

	public static void main(String[] args) {

		if (args.length < 1) { 
			System.out.println("No packet input file");
			System.exit(-1);
		}

		//if (args.length < 2) { 
		//	System.out.println("No packet input file, and not optional output file");
		//	System.exit(-1);
		//}

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));



		try {

			//out = new PrintStream(args[1]);

			//TimeSortedPacketReader pr = new TimeSortedPacketReader(new FilePacketReader(new CopyPacketFactory(), args[0]));
			FilePacketReader pr = new FilePacketReader(new CopyPacketFactory(), args[0]);

			ATMSPacketPrint atms = new ATMSPacketPrint();


			while (pr.hasNext()) {

				Packet p = (Packet)pr.next();

				int apid = p.getApplicationId();

				if ((apid == 11)  || 
						(apid == 515) || 
						(apid == 528) || 
						(apid == 530) || 
						(apid == 531)) {

					atms.put(p);

					//if (atms.hasNext()) {  // means has an entire scan if true
						atms.printScanInfo();
					//}
				}

			}

		} catch (RtStpsException e) {
			
			e.printStackTrace();
		}
	}


	
	/****
					packetSizeSum += p.getPacketSize();
					
					StringBuilder str = new StringBuilder();
					
					
					//System.out.print("Packet APID[" + p.getApplicationId() + "] Size[" + p.getPacketSize() + "] SeqCnt [" + p.getSequenceCounter() + "]");
					if (p.isFirstPacketInSequence() || p.isStandalonePacket()) {
						int apid = p.getApplicationId();
						PDSDate pt = new PDSDate(p.getTimeStamp(8));
						str.append("Packet APID[" + apid + "] Size[" + p.getPacketSize() + "] SeqCnt [" + p.getSequenceCounter() + "]");

						str.append(" Timestamp[" + sdf.format(pt.getDate()) + "]");
						if ( ((apid >= 800) && (apid <= 821)) || (apid == 825)) {
							byte[] data = p.getData();
							int offset = 6 + 28;
							int scanNum = ( (int)data[offset] << 24 ) | 
							(((int)data[offset+1] << 16) & 0x00ff0000) | 
							(((int)data[offset+2] <<  8) & 0x0000ff00) | 
							((int)data[offset+3] & 0x000000ff) ;
							if (p.getApplicationId() != 11) {
								str.append(" ScanNumber[" + scanNum + "]");
							}
						} 
						if (apid == 528) {
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
						}
						
						if (apid == 515) {
							str.append(" ATMS Calibration Packet");
						}
						if (apid == 530) {
							str.append(" ATMS Hot/Cold Temperatures Packet");
						}
						if (apid == 531) {
							//str.append(" ATMS Health and Status Packet");
							
							if (state == 1) {
								++groupCount;
								state = 0;
							}
							
							str.append(" ATMS Health and Status Packet -- Group [" + groupCount + "]");
							
	
						}
						
						if (apid == 536) {
							str.append(" ATMS Point & Stare Continuous Sampling Packet");
						}
						
						if (apid == 11) {
							str.append(" <=== Attitude & Ephemeris");
							str.append(printAE(p));
							
							
						}
						
						if (apid == 1289) {
						
							str.append(" X=== Start of CRIS Scan"); 
							sampleCount = 1;
						}
						if ((sampleCount >= 1) && (apid >= 1315) && (apid <= 1341)) {
							str.append(" scanCount = " + sampleCount);
						}
						if ((sampleCount >= 1) && (apid == 1341)) {
							++sampleCount;
						}
						
						if ((sampleCount >= 1) && (apid >= 1342) && (apid <= 1350)) { 
							str.append(" LW 1 Deep Space");
						}
						if ((sampleCount >= 1) && (apid >= 1351) && (apid <= 1359)) { 
							str.append(" MW 1 Deep Space");
						}
						if ((sampleCount >= 1) && (apid >= 1360) && (apid <= 1368)) { 
							str.append(" SW 1 Deep Space");
						}
						if ((sampleCount >= 1) && (apid >= 1369) && (apid <= 1395)) { 
							str.append(" Internal Cal");
						}
						out.println(str.toString());
					}
					//System.out.println();
				//}
					if ((++count % status) == 0) {
						System.out.print(".");
						if ((++statusCount % 40) == 0) {
							System.out.println();
						}
					}
			}
			System.out.println("Amount of bytes processed into packets -- " + packetSizeSum);
			
			//System.out.println("Lefover that is not processable as it is short -- " + pr.getLeftOver() + " unprocessable bytes");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	**/

	
}
