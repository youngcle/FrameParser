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


public class PacketSort {
	
	public static void main(String[] args) {
		
		if (args.length != 2) { 
			System.out.println("No packet input file and output file");
			System.exit(-1);
		}

		try {
			
			TimeSortedPacketReader pr = new TimeSortedPacketReader(new FilePacketReader(new CopyPacketFactory(), args[0]));
			FileOutputStream fileOut = new FileOutputStream(args[1]);
			
			while (pr.hasNext()) {

				Packet p = (Packet)pr.next();
				fileOut.write(p.getData(), p.getStartOffset(), p.getPacketSize());
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RtStpsException e) {
			e.printStackTrace();
		}
	}
}
