/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;



import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;

import java.awt.Dimension;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JProgressBar;



public class RDRReaderThread extends Thread {
		private RDRViewer viewer;
		private File rdrFile;
		
		public RDRReaderThread(RDRViewer viewer, File rdrFile) {
			this.viewer = viewer;
			this.rdrFile = rdrFile;
		}
		public void run() {

			JDialog dialog = new JDialog(viewer.getFrame());
			
		  //    ImageIcon icon = new ImageIcon("/home/krice/Downloads/blue_earth.png");
			//	AnimatedPanel connectionAnimation = new AnimatedPanel(
			//	    "Reading the RDR...",
			//	    icon);
				
			//dialog.add(connectionAnimation); // new JLabel("Processing RDR file..."));
				JProgressBar bar = new JProgressBar();
			bar.setIndeterminate(true);
			dialog.setTitle("Reading ...");
			
			

			dialog.add(bar);
			
			
			//dialog.setUndecorated(true);
			//dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
			
			
			dialog.setVisible(true);
			//dialog.setPreferredSize(new Dimension(300, 300));
			//dialog.setLocation(viewer.getFrame().getLocation().x+viewer.getFrame().getWidth()/2-150, viewer.getFrame().getLocation().y+viewer.getFrame().getHeight()/2-150);
			Dimension barPrefSize = bar.getPreferredSize();
			
			dialog.setLocation(viewer.getFrame().getLocation().x+viewer.getFrame().getWidth()/2- ((int)barPrefSize.getWidth()/2), viewer.getFrame().getLocation().y+viewer.getFrame().getHeight()/2-((int)barPrefSize.getHeight()/2));
			
			dialog.pack();
			//connectionAnimation.start();

			//JOptionPane.showMessageDialog(xr.getFrame(), "Processing XTCE file...");

			RDRReaderSwingWorker readRDR = new RDRReaderSwingWorker(rdrFile);

			readRDR.execute();
			
			RDRFileReader rdrReader = null;
			try {
				rdrReader = readRDR.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//System.out.println("Updating naviTable table!");
			
			viewer.setRDRReader(rdrReader);
			viewer.getNaviTree().rdrToTree(rdrReader);

			//connectionAnimation.stop();
			dialog.dispose();


		}
	
}
