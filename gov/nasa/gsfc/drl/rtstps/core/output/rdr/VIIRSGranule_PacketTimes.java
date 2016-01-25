/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.text.ParseException;

/**
 * This uses the packet times to calculate the beg/end IET times
 * @author krice
 *
 */
public class VIIRSGranule_PacketTimes extends Granule {

	
	private LPEATEDate firstDateTime;
	private LPEATEDate lastDateTime;
	private PDSDate predictedEndDateTime;
	private PDSDateTrunc beginningTimeDateTime;
	private PDSDateTrunc predictedEndingTimeDateTime;
	
	/**
	 * Constructor for a Granule instance, the arguments are associated with the granule's attributes.
	 * An instance of this class in this packet is created by some other factory method.
	 * 
	 * @param rap the corresponding RawApplicationPackets area
	 * @param orbit the orbit number of the pass
	 * @param granuleId the granuleId {@link GranuleId}
	 * @param leoaState the LEO state flag
	 * @param docName the document name of the specification controlling this granules construction
	 * @param packetTypes an array of packet types received in this granule
	 * @param packetTypeCounts the counts per type of the packets received
	 * @param referenceId the reference identifier which is a UUID {@link java.util.UUID}
	 * @param granuleNumber the granule number which corresponds to the RawApplicationPackets number in the RDR/HDF file
	 * @param dataSpaceOfRaw the HDF DataSpace handle of the RawApplicationPackets area associated with this granule
	 */
	public VIIRSGranule_PacketTimes(RawApplicationPackets rap,
					long orbit,
					GranuleId granuleId,
					LEOAFlag leoaState,
					String docName,
					String[] packetTypes,
					long[] packetTypeCounts,
					ReferenceId referenceId,
					int granuleNumber, 
					int dataSpaceOfRaw) {
		super(rap, 
				orbit, 
				granuleId, 
				leoaState, 
				docName, 
				packetTypes, 
				packetTypeCounts, 
				referenceId, 
				granuleNumber,  
				dataSpaceOfRaw,
				RDRName.VIIRS_Science);
		
		
	
		
	}
	
	long getScansPerGranule() {
		
		return 48;  // should be 48 but its the scan count for VIIRSRawAppPacket3 right now
	}

	/**
	 * Read the Granule out of the HDF file and fill in the various attributes and items in it for
	 * these access methods
	 * @param groupId the HDF group handle
	 * @param granuleName  the name of the granule
	 * @throws ParseException 
	 */
	public VIIRSGranule_PacketTimes(int groupId, String granuleName) throws RtStpsException {
		super(groupId, granuleName);
	}
	@Override
	public PDSDate getBeginningObservationDateTime(RawApplicationPackets rap) {
		this.firstDateTime = new LPEATEDate(rap.getFirstTime());  // packet time
		

		return firstDateTime;
	}

	
	@Override
	public PDSDate getEndingObservationDateTime(RawApplicationPackets rap) {
		this.lastDateTime = new LPEATEDate(rap.getLastTime());  // this is saved for no particular reason at this time

		
		this.predictedEndDateTime = lastDateTime;
		

		return this.predictedEndDateTime;
	}
	@Override
	public PDSDate getBeginningTimeDateTime() {
		beginningTimeDateTime = new PDSDateTrunc(firstDateTime.getOriginalPacketTime());
		
		
		return beginningTimeDateTime;
	}
	@Override
	public PDSDate getEndingTimeDateTime() {

		predictedEndingTimeDateTime = new PDSDateTrunc(lastDateTime.getOriginalPacketTime());
		
		
		return predictedEndingTimeDateTime;

	}


}
