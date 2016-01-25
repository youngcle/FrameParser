/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;

import java.io.File;

import javax.swing.SwingWorker;


public class RDRReaderSwingWorker extends SwingWorker<RDRFileReader, Object> {

	private File rdrFile;
	

	private RDRFileReader readRDR;
	
    public RDRReaderSwingWorker(File rdrFile) {
     	this.rdrFile = rdrFile;
    }






	@Override
	protected RDRFileReader doInBackground() throws Exception {
   	try {
    		
    		//System.out.println("HERE!");
			readRDR = new RDRFileReader(rdrFile.getAbsolutePath());
			
			//Thread.sleep(10 * 1000);  testing
			//System.out.println("THERE!");
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return readRDR;
	}
	

}
