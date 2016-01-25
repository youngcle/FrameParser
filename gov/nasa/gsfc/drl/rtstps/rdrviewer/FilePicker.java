/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;


import java.io.File;

import javax.swing.JFileChooser;

public class FilePicker extends JFileChooser implements java.awt.event.ActionListener {
	
	private File dataFile = null;
	private RDRViewer viewer;
	
	private static final long serialVersionUID = 1L; 

	public FilePicker(RDRViewer viewer) {
		this.viewer = viewer;
		String baseDirectory = System.getProperty("directory", ".");
		setCurrentDirectory(new File(baseDirectory));
		setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
		setDialogTitle("Choose File");
		

	}

	public void actionPerformed(java.awt.event.ActionEvent event) {
		rescanCurrentDirectory();
		dataFile = null;
		int x = showOpenDialog(viewer.getFrame());
		if (x == JFileChooser.APPROVE_OPTION) {
			dataFile = getSelectedFile();
			//System.out.println("Working on file: " + dataFile.getAbsolutePath());
			Thread thread = new RDRReaderThread(viewer, dataFile);
			thread.start();
			
			
		}
	}
	public void clearFileToValidate() {
		dataFile = null;
	}
	public File getFileToValidate() {
		return dataFile;
	}

}