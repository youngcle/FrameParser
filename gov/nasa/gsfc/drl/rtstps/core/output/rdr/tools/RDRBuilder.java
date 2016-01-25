/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.MissionName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Origin;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PlatformShortName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDR;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileWriter;

public class RDRBuilder {
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length != 2) { 
			
			for (int i = 0; i < args.length; i++) {
				System.out.println("args[" + i + "] = " + args[i]);
			}
			System.out.println("Usage:  RDRBuilder [output directory path] [input packet input]");
			System.exit(-1);
		}

		try {
			RDR.DocumentName = "D34862-02_NPOESS-CDFCB-X-Vol-II_D_20090603_I1.5.0.pdf";
			
			RDRFileWriter rdrBuilder = new RDRFileWriter(args[0], 1, 1, Origin.nfts, MissionName.NPP, Origin.nfts, PlatformShortName.NPP);
			
			FilePacketReader pr = new FilePacketReader(new NoCopyPacketFactory(), args[1]);

			System.out.println("Beginning granule processing...");
			int ipacket=0;
			while (pr.hasNext()) {
				ipacket++;
				Packet p = (Packet) pr.next();
				int appid = p.getApplicationId();
				//System.out.println("Packet No:"+ipacket+" apid="+appid);
				
				//LPEAT has this in the data...
				//if ((appid == 0) || (appid == 8)) {
				//	continue;
				//}
				rdrBuilder.put(p);
				//if(ipacket==9361) break;
				//if(ipacket==56292) break;
				//if(ipacket==172812) break;
			}
		
			rdrBuilder.close(true);
			
			System.out.println("Granule processing complete.");
			
		} catch (RtStpsException e) {
			e.printStackTrace();
		} 
	}
}
