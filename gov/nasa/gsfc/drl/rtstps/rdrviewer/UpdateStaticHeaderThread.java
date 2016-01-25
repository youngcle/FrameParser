/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;



import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;

import java.awt.Dimension;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JProgressBar;



public class UpdateStaticHeaderThread extends Thread {
		private RDRViewer viewer;
		RawApplicationPackets rap;
		
		public UpdateStaticHeaderThread(RDRViewer viewer, RawApplicationPackets rap) {
			this.viewer = viewer;
			this.rap = rap;
			
		}
		public void run() {

			JDialog dialog = new JDialog(viewer.getFrame());
	
			JProgressBar bar = new JProgressBar();
			bar.setIndeterminate(true);
			dialog.setTitle("Reading ...");

			dialog.add(bar);
			
			dialog.setVisible(true);
			Dimension barPrefSize = bar.getPreferredSize();
			
			dialog.setLocation(viewer.getFrame().getLocation().x+viewer.getFrame().getWidth()/2- ((int)barPrefSize.getWidth()/2), viewer.getFrame().getLocation().y+viewer.getFrame().getHeight()/2-((int)barPrefSize.getHeight()/2));
			
			dialog.pack();

			UpdateStaticHeaderSwingWorker worker = new UpdateStaticHeaderSwingWorker(viewer, rap);

			worker.execute();
			
			
			try {
				UpdateStaticHeader result = worker.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//System.out.println("Updating naviTable table!");
			


			//connectionAnimation.stop();
			dialog.dispose();


		}
	
}
