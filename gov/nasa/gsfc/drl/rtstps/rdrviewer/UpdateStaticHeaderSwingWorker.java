/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;



import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;


public class UpdateStaticHeaderSwingWorker extends SwingWorker<UpdateStaticHeader, Object> {

	private UpdateStaticHeader upsh;
	RDRViewer viewer; RawApplicationPackets rap;
	
    public UpdateStaticHeaderSwingWorker(RDRViewer viewer, RawApplicationPackets rap) {
    	
     	this.viewer = viewer;
     	this.rap = rap;
    }


	@Override
	protected UpdateStaticHeader doInBackground() throws Exception {
   
    
		viewer.updateStaticHeaderModel(rap);
			
	
		
		return upsh;
	}
	

}
