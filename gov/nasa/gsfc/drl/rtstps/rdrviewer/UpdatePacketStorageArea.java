/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.SequentialPacketReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import java.awt.Dimension;
import java.text.SimpleDateFormat;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.table.TableModel;

public class UpdatePacketStorageArea {

	private TableModel model;
	
	public UpdatePacketStorageArea(JFrame frame, StaticHeader sh, TableModel model) {
		
		this.model = model;
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd kk:mm:ss.SSS zzz yyyy");


		SequentialPacketReader spr = sh.createSequentialPacketReader();

		

		int i = 0; // rows
		int j = 0; // cols
		
		//int pc = 0;
		JDialog dialog = new JDialog(frame);

		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate(true);
		bar.setVisible(true);
		dialog.setTitle("Reading ...");
		dialog.add(bar);
		dialog.setVisible(true);
		Dimension barPrefSize = bar.getPreferredSize();
			
		dialog.setLocation(frame.getLocation().x+frame.getWidth()/2- ((int)barPrefSize.getWidth()/2), frame.getLocation().y+frame.getHeight()/2-((int)barPrefSize.getHeight()/2));
			
		dialog.pack();
			
		while (spr.hasNext()) {
			
			//System.out.println("Packet = "  + pc++);
			
			Packet p = (Packet) spr.next();

			
			model.setValueAt(Integer.toHexString(p.getSequenceFlags()), i, j++);

			model.setValueAt(Integer.toString(p.getSequenceCounter()), i, j++);

			model.setValueAt(Integer.toString(p.getApplicationId()), i, j++);

			int length = p.getPacketLength();
			model.setValueAt(Integer.toString(length), i, j++);


			if (p.hasSecondaryHeader()) { 
				PDSDate pt = new PDSDate(p.getTimeStamp(8));


				model.setValueAt(sdf.format(pt.getDate()), i, j++);


			} else {
				model.setValueAt("~", i, j++);
			}


			byte[] bytes = p.getData();
			int index = p.getStartOffset();
			model.setValueAt(dataToString(bytes, index, 10), i, j++);

			i++;
			j = 0;

		}
		dialog.dispose();

		

	}
	
	public TableModel getModel() {
		return model;
	}

	private String dataToString(byte[] bytes, int index, int max) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		int size = bytes.length;
		if (size > max) {
			size = max;
		}
		for (int i = index; i < bytes.length; i++) {
			sb.append(String.format("%02x", bytes[i]));
		}
		if (size == max) {
			sb.append("...");
		}
		sb.append("]");
		return sb.toString();
	}
}
