/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.rdrviewer;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.AllDataReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAllReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import java.util.LinkedList;
import java.util.List;

/**
 * Open an RDR file and locate the SpacecraftAOS Diary granules.  Compare the times
 * stored in the granules with the times in its RawApplicationPackets through the reference
 * in the granule.  If the granule times do not encapsulate the RawApplicationPacket,
 * issue and error message.
 * 
 *
 */
public class ReadStaticHeader {
	private RDRFileReader readRDR;
	private AllDataReader allDataReader;
	private RDRAllReader rar = null;
	
	private List<StaticHeader> staticHeaders = new LinkedList<StaticHeader>();

	public ReadStaticHeader(String filename) throws RtStpsException {

		RDRFileReader readRDR = new RDRFileReader(filename);

		AllDataReader allDataReader = readRDR.createAllDataReader();
		while (allDataReader.hasNext()) {
			
			RDRAllReader rar = allDataReader.next();
			
			while (rar.hasNext()) {
				RawApplicationPackets rap = rar.next();
				
				StaticHeader sh = rap.getStaticHeader();
				
				staticHeaders.add(sh);

				rap.close();
			}
			rar.close();
		}
	}
	
	public List<StaticHeader> getStaticHeaders() {
		return staticHeaders;
	}
	






}
