/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.LinkedList;
import java.util.List;





/************
 * DEPRECATED
 ************/







/**
 * Build the RawApplicationPackets (or read a pre-existing one) for the HDF for ATMS.
 * The ATMS outputs scans of 104 packets.  Groups of 3 of these are associated together.
 * When the appid 528 packet arrives, the first one sets the scan start flag set, 103 ATMS
 * packets should arrive before the next appid 528 packets should arrive.   
 * Somewhere near the end of this first scan group the 531 packet arrives.
 * When it does, this signals the start of a group of 3 scans.   
 * This class "locks" on the first 3 group scans and counts out groups of them before
 * writing them to the RawApplicationPackets area.
 * The "scansPerGranule" should be in groups of 3, although this is not enforced as these
 * form a single unit from processing standpoint.
 * The initial set of scans received may be partial since we do not know when signal lock occurs,
 * this is taken into account here so the first Granule/RawApp area is likely to be "short".
 * 
 * 
 *
 */
public class ATMSRawApplicationPackets extends RawApplicationPackets {
	private List<Packet> scanFilling = new LinkedList<Packet>();

	private int maxScanGroup = 12;
	private ATMSScanState scanState = ATMSScanState.Begin;
	private int scanGroupCounter = 0;
	private int scanStarts = 0;
	
	/**
	 * Constructor for creating an nth instance of a ATMS raw application data packet area builder.
	 * @param satellite  the name of the spacecraft as a SpacecraftId
	 * @param setNum  the set number
	 * @param scansPerGranule the number of sensor scans per granule
	 */
	public ATMSRawApplicationPackets(SpacecraftId satellite, int setNum, int scansPerGranule, PacketPool packetPool) {
		super(satellite, RDRName.ATMS_Science, setNum, packetPool);
		
		if (setNum < 0) {
			throw new IllegalArgumentException("Illegal Index [" + setNum + "]");
		}
		
		this.maxScanGroup = scansPerGranule;
	}
	
	/**
	 * Constructor which attempts to read the RawApplicationPacket entry that pre-exists.
	 * The contents of the dataspace are read into a memory buffer... assuming it will fit.
	 * @param allRDRId  the rdrAll Groups id
	 * @param setNum the set number of raw entry
	 * @throws RtStpsException Wraps any HDF exception
	 */
	public ATMSRawApplicationPackets(int allRDRId, int setNum) throws RtStpsException {
		super(allRDRId, setNum);
	}
	
	public ATMSRawApplicationPackets(int readId, int setNum, boolean usedByGranuleOnly) throws RtStpsException {
		super(readId, setNum, true);
	}
	
	/**
	 * Determine if the object will take more packets.  The ATMS is considered
	 * full when a certain number of scan groups have arrived.  The scan groups
	 * are formed around 3s, from the arrival of packet 531.  The 528 packets
	 * forms a single scan of 104 packets, and packet 531 arrives near end of
	 * single scan.  The next two scans plus that one are one group.  Although
	 * this algorithm doesn't specifically count groups, it accumulates all the packets
	 * in one list, in essence the 531s arrival initiates the "group counting" portion
	 * of the state machine.
	 * The state machine says when its full or not then...
	 * 
	 * @param p packet to be written
	 * @return true if not full, false if it is full
	 * @throws RtStpsException Wraps any HDF exception
	 */
	public boolean notFull(Packet p) throws RtStpsException {

		if (scanState == ATMSScanState.Complete) {
			return false;  // FULL
		}
		return true;  // NOT FULL
	}
	
	
	/**
	 * The packet is stored on a local list until the state machine says it is full or complete.
	 * Note that the "notFull()" method must be called first and if it returns false, the write()
	 * method must be called next.  This will write all the packets int the list to the HDF file
	 * and then this method can be called.
	 * If notFull() is true, then this routine should always be called next.
	 * 
	 * @param p ATMS science packet to be written (appid 514, 528, 530, 531)
	 * @throws RtStpsException an exception related to the state machine or wraps any HDF exception
	 */
	@Override
	public void put(Packet p) throws RtStpsException {
		
		this.updateAppIdCounters(p.getApplicationId());
		
		RDRName rdrName = RDRName.fromAppId(p.getApplicationId());
		
		if (rdrName != RDRName.ATMS_Science) {
			throw new RtStpsException("Not a ATMS science packet [" + p.getApplicationId() + "]");
		}
	
		setScanState(p);
		// the packet is deep copied, it is allocated off a pool
		// which may hold previously copied packets...  they must be put
		// back on the pool when done...
		
		Packet pcopy = CopyPacket.deep(p, packetPool);
		
		if (scanState == ATMSScanState.Complete) {
			// move the scanFilling ile to the scanComplete pile
			getPacketList().addAll(scanFilling);
			
			// make a new scanFilling and put the packet in it
			scanFilling = new LinkedList<Packet>();
			scanFilling.add(pcopy);
		} else {
			// otherwise its always added
			scanFilling.add(pcopy);
			
		}

		// collect time info
		if (p.hasSecondaryHeader()) {

			// secondary time stamp only valid in in first or standalone packets...
			if (getFirstTime() == 0L) {
				//System.out.println("Here...");
				setFirstTime(p.getTimeStamp(8));
			}
			
			setLastTime(p.getTimeStamp(8));
		}
	
	}
	
	
	/**
	 * Write the ATMSRawApplication structure to the given HDF file specified by the HDF input handle
	 * @param hdfFile input handle to a previous opened HDF file
	 * @return true if the write succeeds
	 * @throws RtStpsException Wraps HDF exceptions
	 */
	@Override
	public boolean write(int hdfFile) throws RtStpsException {
		
		scanState = ATMSScanState.Begin;
		
		scanGroupCounter = 0;
	
		super.write(hdfFile);
		
		return true;
		
	}
	
	/**
	 * The ATMS state machine algorithm is built around the scan of packets appid 528 which are in
	 * groups of 3 as dictated by the appid 531 packet.  When a scan has the appid 531 packet, the next two
	 * are associate with that scan.  This algorithm tracks scan, but takes into account a "short" 
	 * or partial scan in the beginning...
	 * 
	 * @param p an ATMS input packet to be processed through the scan state machine
	 * @throws RtStpsException Wraps HDF exceptions
	 */
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
				
				// first off if things are normal:
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
	
	/**
	 * Close out the RawApplicationPacket which writes the results to the HDF file and cleans up.
	 */
	public void close() throws RtStpsException {
		super.close();
		scanGroupCounter = 0;
		scanStarts = 0;
		scanState = ATMSScanState.Begin;
	}

}
