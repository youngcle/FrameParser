/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Aggregate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.AllDataReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.CommonDataSetObject;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.DataProductsReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Granule;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.NPOESSFilename;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAllReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRProduct;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.UserBlockReader;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;




public class RDRNaviTree extends JTree implements TreeSelectionListener {
	//private Map<String, RawApplicationPackets> rapMap = new HashMap<String, RawApplicationPackets>();
	private Map<String, Object> nodeMap = new HashMap<String, Object>();
	private RDRViewer viewer;

	private boolean changed = false;

	
	private int maxRowWidthInChars = 7;  // RDR ... 
	
	public RDRNaviTree(RDRViewer xr) {

		super( new DefaultMutableTreeNode("RDR ..."));

		
		getSelectionModel().setSelectionMode (TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

		addTreeSelectionListener(this);
		
		this.viewer = xr;
		
		// the cell render is supposed to calculate the width the label in the tree a size it but
		// it doesn't do anything... or worse breaks the app...
		//this.setCellRenderer(new MyTreeCellRender());
	}

	// this seems to resize the navi panel if you tell the panel the location has changed
	// but only after an update which occurs when you touch that leaf... 
	// FIXME
	public Dimension getPreferredSize() {
		
		return new Dimension(this.maxRowWidthInChars * 10, nodeMap.size() * 21);
	}

	public void rdrToTree(RDRFileReader rdrReader) {
		
		//System.out.println("in rdrToTree");
		
		DefaultMutableTreeNode newTree = buildTree(rdrReader);
		((DefaultTreeModel)this.getModel()).setRoot(newTree);
		//addTreeSelectionListener(this);
		viewer.naviPaneResize();
		
		//viewer.frameResize();
		
	}
	private DefaultMutableTreeNode buildTree(RDRFileReader rdrReader) {
		DefaultMutableTreeNode rdrTree = null;
		try {

			NPOESSFilename nfn = rdrReader.getNPOESSFilename();

			viewer.createNPOESSFilenameTable(/*nfn*/);

			String name1 = rdrReader.getFile().getName();

			rdrTree = new DefaultMutableTreeNode(name1);

			nodeMap.put(name1, nfn);


			UserBlockReader userBlockReader = rdrReader.createUserBlockReader();
		
			DataProductsReader dataProductsReader = rdrReader.createDataProductsReader();
			
			
			
			if (dataProductsReader != null) {
				
				DefaultMutableTreeNode dataProductsNode = new DefaultMutableTreeNode("Data_Products");
				while (dataProductsReader.hasNext()) {
					
					
					RDRProduct prod = dataProductsReader.next();

					String pname = prod.getMetaRDRName();
					nodeMap.put(pname, prod);
					
					DefaultMutableTreeNode prodNode = new DefaultMutableTreeNode(pname);
					
					while (prod.hasNext()) {
						CommonDataSetObject ga = prod.next();  // either an attribute or granule
						
						ga.getName();
							
						String name = ga.getName();
						DefaultMutableTreeNode gaNode = new DefaultMutableTreeNode(name);

						if (name.length() > this.maxRowWidthInChars) {
							this.maxRowWidthInChars = name.length();
						}
						nodeMap.put(name, ga);

						prodNode.add(gaNode);
						
						
					}
					
					prod.close();
					
					dataProductsNode.add(prodNode);
				}
				
				rdrTree.add(dataProductsNode);
			}
			
			AllDataReader allDataReader = rdrReader.createAllDataReader();
			
			if (allDataReader != null) {
				
				DefaultMutableTreeNode allDataNode = new DefaultMutableTreeNode("All_Data");
				while (allDataReader.hasNext()) {
					
					
					RDRAllReader rar = allDataReader.next();
					
					
				
					DefaultMutableTreeNode rarNode = new DefaultMutableTreeNode(rar.getMetaRDRName());
					
					while (rar.hasNext()) {
						
						RawApplicationPackets rap = rar.nextRandomAccess();
						
						String name = rap.getName();
						DefaultMutableTreeNode rapNode = new DefaultMutableTreeNode(name);

						if (name.length() > this.maxRowWidthInChars) {
							this.maxRowWidthInChars = name.length();
						}
						nodeMap.put(rar.getMetaRDRName() + name, rap);

						rarNode.add(rapNode);
						
						//rap.close();
					}
					
					//rar.close();
					
					allDataNode.add(rarNode);
				}
				
				rdrTree.add(allDataNode);
			}
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rdrTree;

	}





	@Override
	public void valueChanged(TreeSelectionEvent e) {

		//System.out.println("In valueChange...");
		 
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.getLastSelectedPathComponent();
		
		if (node == null) return;  // it seems it can be null sometimes
		
		String parentName = (String) ((DefaultMutableTreeNode)node.getParent()).getUserObject();
		
		System.out.println("Node Parent == " + parentName);

		String name = (String)node.getUserObject();

		Object thing = nodeMap.get(parentName + name);
		
		if (thing == null) return;   // and this happens sometimes too because the item selected isn't stored in the map
		
		if (thing instanceof RawApplicationPackets) {
			
			//System.out.println("In valueChange...calling updateStaticHeaderModel");
			
			RawApplicationPackets rap = (RawApplicationPackets) thing;

			//viewer.updateStaticHeaderModel(rap);
			
			Thread thread = new UpdateStaticHeaderThread(viewer, rap);
			
			thread.start();

		} else if (thing instanceof NPOESSFilename) {
			//System.out.println("In valueChange...calling displayNPOESSfilename");
			viewer.displayNPOESSFilename(/**(NPOESSFilename) thing*/);
		} else if (thing instanceof RDRProduct) {
			
			RDRProduct product = (RDRProduct) thing;
			//System.out.println("In valueChange...have RDRProduct called -- " + product.getMetaRDRName());
			viewer.updateRDRProductModel(product);
			
		} else if (thing instanceof Granule) {
			Granule g = (Granule)thing;
			//System.out.println("In valueChange...have Granule called -- " + g.getName());
			viewer.updateGranuleModel(g);
			
		} else if (thing instanceof Aggregate) {
			Aggregate a = (Aggregate)thing;
			//System.out.println("In valueChange...have Aggregate called -- " + a.getName());
			
			viewer.updateAggregateModel(a);
		} else {
			System.out.println("In valueChange...you have selected an unknown thing -- " + thing);
		}



	}


}
