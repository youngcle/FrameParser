/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;



import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;


public class UpdatePacketStorageAreaSwingWorker extends SwingWorker<UpdatePacketStorageArea, Object> {
	private JFrame frame;
	private TableModel model;
	private StaticHeader sh;
	private UpdatePacketStorageArea upsa;
	
    public UpdatePacketStorageAreaSwingWorker(JFrame frame, StaticHeader sh, TableModel model) {
    	this.frame = frame;
     	this.sh = sh;
     	this.model = model;
    }


	@Override
	protected UpdatePacketStorageArea doInBackground() throws Exception {
   
    		
    		System.out.println("HERE!");
   		upsa = new UpdatePacketStorageArea(frame, sh, model);
			
	
		
		return upsa;
	}
	

}
