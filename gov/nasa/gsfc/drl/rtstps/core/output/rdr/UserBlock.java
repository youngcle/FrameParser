/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Create the user block for the RDR file.
 * 
 *
 */
public class UserBlock {
	private MissionName missionName;
	private PlatformShortName platformShortName;
	private List<RDR> rdrs;
	private StringBuffer xmlBuffer = new StringBuffer();
	
	private RandomAccessFile out;
	
	/**
	 * Use the mission name, the platform short and the list of RDR sensors in the file to build the user block
	 * @param missionName the mission name
	 * @param platformShortName the platform short name
	 * @param rdrs the list of RDR objects
	 */
	public UserBlock(MissionName missionName, 
							PlatformShortName platformShortName,  
							List<RDR> rdrs) {
		
		this.missionName = missionName;
		this.platformShortName = platformShortName;
		this.rdrs = rdrs;
		
		toXML();
	}
	
	/**
	 * Return the mission name
	 * @return the MissionName
	 */
	public MissionName getMissionName() {
		return missionName;
	}
	
	/**
	 * Return the platform short name
	 * @return the PlatformShortName
	 */
	public PlatformShortName getPlatformShortName() {
		return platformShortName;
	}
	
	/**
	 * Return the number products (RDR objects) being used
	 * @return the number of products
	 */
	public int getNumberOfProducts() {
		return rdrs.size();
	}
	
	/**
	 * Return the list of RDR object used as inputs into the user block
	 * @return the list of RDRs
	 */
	public List<RDR> getRDRs() {
		return rdrs;
	}
	
	/**
	 * Return the XML created from the inputs
	 * @return a StringBuffer containing the XML
	 */
	public StringBuffer getXML() {
		return xmlBuffer;
	}
	
	/**
	 * Write the XML to the UserBlock of the RDR file given the HDF file handle.
	 * The handle is used to open another descriptor into the file and this is used
	 * to write the user block.
	 * @param hdfFile the handle to the RDR file
	 * @throws RtStpsException wraps any IOExceptions as an RtStpsException
	 */
	public void write(File hdfFile) throws RtStpsException {

		try {
			out = new RandomAccessFile(hdfFile, "rw");
			out.seek(0L);
			byte[] outBuf = xmlBuffer.toString().getBytes(Charset.forName("US-ASCII"));
			out.write(outBuf);
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * Close the internal descriptor used to write the UserBlock portion of the file
	 * @throws RtStpsException
	 */
	public void close() throws RtStpsException {
		try {
			out.close();
		} catch (IOException e) {
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * Build out the XML #1
	 */
	private void toXML() {
		xmlBuffer.append("<HDF_UserBlock>");
		xmlBuffer.append("<Mission_Name>");
		xmlBuffer.append(missionName.toString());
		xmlBuffer.append("</Mission_Name>");
		xmlBuffer.append("<Platform_Short_Name>");
		xmlBuffer.append(platformShortName.toString());
		xmlBuffer.append("</Platform_Short_Name>");
		xmlBuffer.append("<Number_of_Data_Products>");
		xmlBuffer.append(rdrs.size());
		xmlBuffer.append("</Number_of_Data_Products>");
		for (RDR rdr : rdrs) {
			
			Aggregate ag = rdr.getRDRProduct().getAggregate();
			GranuleId begg = rdr.getRDRProduct().getBeginningGranuleId();
			GranuleId endg = rdr.getRDRProduct().getEndingGranuleId();
			
			if ((ag == null) || (begg == null) || (endg == null)) continue;
			
			rdrProductToXML(rdr.getRDRProduct());
		}
		xmlBuffer.append("</HDF_UserBlock>");
	}

	/**
	 * Build out the XML related to the RDRProduct objects
	 * @param rdrProduct RDRProduct of interest
	 */
	private void rdrProductToXML(RDRProduct rdrProduct) {
		xmlBuffer.append("<Data_Product>");
		xmlBuffer.append("<Instrument_Short_Name>");
		xmlBuffer.append(rdrProduct.getSensor().toString());
		xmlBuffer.append("</Instrument_Short_Name>");
		xmlBuffer.append("<N_Collection_Short_Name>");
		xmlBuffer.append(rdrProduct.getRDRName().getRDRStringName());
		xmlBuffer.append("</N_Collection_Short_Name>");
		xmlBuffer.append("<N_Processing_Domain>");
		xmlBuffer.append(rdrProduct.getProcessingDomain().toString());
		xmlBuffer.append("</N_Processing_Domain>");
		xmlBuffer.append("<N_Dataset_Type_Tag>");
		xmlBuffer.append(rdrProduct.getDataSetType());
		xmlBuffer.append("</N_Dataset_Type_Tag>");
		xmlBuffer.append("<AggregateBeginningDate>");
		xmlBuffer.append(rdrProduct.getAggregate().getBeginningDateFormatted()); // 20090125
		xmlBuffer.append("</AggregateBeginningDate>");
		xmlBuffer.append("<AggregateBeginningOrbitNumber>");
		xmlBuffer.append(rdrProduct.getAggregate().getBeginningOrbit());
		xmlBuffer.append("</AggregateBeginningOrbitNumber>");
		xmlBuffer.append("<AggregateBeginningTime>");
		xmlBuffer.append(rdrProduct.getAggregate().getBeginningTimeFormatted()); // 054439.000000Z
		xmlBuffer.append("</AggregateBeginningTime>");
		xmlBuffer.append("<AggregateEndingDate>");
		xmlBuffer.append(rdrProduct.getAggregate().getEndingDateFormatted()); // 20090125
		xmlBuffer.append("</AggregateEndingDate>");
		xmlBuffer.append("<AggregateEndingOrbitNumber>");
		xmlBuffer.append(rdrProduct.getAggregate().getEndingOrbit());
		xmlBuffer.append("</AggregateEndingOrbitNumber>");
		xmlBuffer.append("<AggregateEndingTime>");
		xmlBuffer.append(rdrProduct.getAggregate().getEndingTimeFormatted()); // 054619.000000Z
		xmlBuffer.append("</AggregateEndingTime>");
		xmlBuffer.append("<AggregateBeginningGranuleID>");
		xmlBuffer.append(rdrProduct.getAggregate().getBeginningGranuleId());
		xmlBuffer.append("</AggregateBeginningGranuleID>");
		xmlBuffer.append("<AggregateEndingGranuleID>");
		xmlBuffer.append(rdrProduct.getAggregate().getEndingGranuleId());
		xmlBuffer.append("</AggregateEndingGranuleID>");
		xmlBuffer.append("</Data_Product>");
	}
}
