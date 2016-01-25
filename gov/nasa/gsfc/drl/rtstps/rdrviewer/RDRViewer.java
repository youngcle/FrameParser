/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/


package gov.nasa.gsfc.drl.rtstps.rdrviewer;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Aggregate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.Granule;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.NPOESSFilename;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PDSDate;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PacketTrackerItem;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.PacketTrackerList;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.ProductIdentifiers;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAppIdItem;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAppIdList;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRProduct;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RandomAccessPacketReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPacketsRandomAccess;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.SequentialPacketReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class RDRViewer implements Runnable  {
	

	// certain components have assigned names so they can turned on or off at various times
	private static final String RawApplicationDisplay = "RawApplicationDisplay";
	private static final String FilenameDisplay = "FilenameDisplay";
	

	
	private JSplitPane staticHeaderSplitPane;  // main display item for static header info
	private JTable filenameTable; // main display item for file name break out 
	
	
	// supporting cast...
	private JFrame frame;
	private JEditorPane htmlTextPane;
	private JScrollPane mainScrollPane;

	
	private JSplitPane splitPane2;
	private JSplitPane splitPane3;
	private JScrollPane headerScroll;
	private JScrollPane appIdListScroll;
	private JScrollPane packetTrackerScroll;
	private JScrollPane packetStorageAreaScroll;
	private RDRNaviTree naviTree;
	private JSplitPane naviPane; // main display item for navigation tree
	private JMenuBar menubar;
	private JMenu    fileMenu;


	private JMenuItem openItem;
	private FilePicker filePicker;
	private RDRFileReader rdrReader;
	private JTable headerTable;
	private JTable appIdListTable;
	private JTable packetTrackerTable;
	private JTable packetStorageArea;
	private JScrollPane naviTreeScroll;
	
	private JTable rdrProductTable;
	private JTable granuleTable;
	private JTable aggregateTable;
	
	private JSplitPane fileXMLSplit;
	private JSplitPane twoViewPane;
	private JScrollPane rawPacketPanelScroll;
	
	private JTabbedPane packetDisplayTabs;
	
	private JTable rawPacketDataTable;
	private List<Packet> currentPacketList;
	private JTable cookedPacketDataTable;
	private JScrollPane cookedPacketPanelScroll;

	

	
	
	public RDRViewer() throws RtStpsException {
		//this.rdrFilename = rdrFilename;
		
		//readStaticHeader = new ReadStaticHeader(rdrFilename);
	}



	public void createInitialDisplay() {
		
	      //Create and set up the window.
        frame = new JFrame("RDRViewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        menubar = new JMenuBar();

        fileMenu = new JMenu("File");
        openItem = new JMenuItem("Open...");
        
        filePicker = new FilePicker(this);
        openItem.addActionListener(filePicker);
        
        //... Assemble the menu
        menubar.add(fileMenu);
        fileMenu.add(openItem);
        
        frame.setJMenuBar(menubar);
        
        // creates the view (panes, scrollers, model) for the static header
        createStaticHeaderView();
           
        createRDRProductTable();
        
        createAggregateTable();
        
        createGranuleTable();
        
        // when an RDR file is read the file picker calls a series of routines that eventually creates
        // the filename break out display table... that's not done here since we don't have the info.  
        // (and we don't use the custom model for it)
 
        // create the empty navigation tree, navi pane, navi tree scroller, and the main scroll pane all the other
        // views go into when selected...
        
        naviTree = new RDRNaviTree(this);
		
        naviPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        
        naviTreeScroll = new JScrollPane(naviTree);
        naviTreeScroll.setLayout(new ScrollPaneLayout());
        naviTreeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        naviTreeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        naviPane.add(naviTreeScroll);
        
        twoViewPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        //mainScrollPane = new JScrollPane(splitPane1);
        mainScrollPane = new JScrollPane();
        mainScrollPane.setLayout(new ScrollPaneLayout());
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mainScrollPane.setPreferredSize(new Dimension(300, 200));
        mainScrollPane.setMinimumSize(new Dimension(300, 200));

        twoViewPane.add(mainScrollPane);
        
        packetDisplayTabs = new JTabbedPane();
   
        rawPacketDataTable = new JTable(new RawPacketDataTableModel());
		
        rawPacketPanelScroll = new JScrollPane(rawPacketDataTable);
        rawPacketPanelScroll.setLayout(new ScrollPaneLayout());
        rawPacketPanelScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        rawPacketPanelScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        rawPacketPanelScroll.setMinimumSize(this.rawPacketDataTable.getSize());
        rawPacketPanelScroll.setMaximumSize(this.rawPacketDataTable.getSize());
        rawPacketPanelScroll.setPreferredSize(this.rawPacketDataTable.getSize());
        
      
        packetDisplayTabs.addTab("Raw Packet Data", rawPacketPanelScroll);
        
        cookedPacketDataTable = new JTable(new CookedPacketDataTableModel());
		
        cookedPacketPanelScroll = new JScrollPane(cookedPacketDataTable);
        cookedPacketPanelScroll.setLayout(new ScrollPaneLayout());
        cookedPacketPanelScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        cookedPacketPanelScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        cookedPacketPanelScroll.setMinimumSize(this.cookedPacketDataTable.getSize());
        cookedPacketPanelScroll.setMaximumSize(this.cookedPacketDataTable.getSize());
        cookedPacketPanelScroll.setPreferredSize(this.cookedPacketDataTable.getSize());
        
        packetDisplayTabs.addTab("Cooked Packet Data", cookedPacketPanelScroll);
        
        twoViewPane.add(packetDisplayTabs);
        
		naviPane.add(twoViewPane);
		

        
        frame.setContentPane(naviPane);
        
       

        
        frame.setMinimumSize(new Dimension(800,600));
        positionFrame();
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	// the timer calls this and all work items are processed one at a time
	// ... 
	private void processingWorker() {
		
		
	}
	
	// creates all the models, scrollers, panes etc.. for display the staticHeader
	private void createStaticHeaderView() {
	       createHeaderTable(); 
	        createHeaderTableScrollPane();
	 
	        
	        createAppIdListTable();
	        createAppIdListTableScrollPane();

	        
	        createPacketTrackerTable();
	        createPacketTrackerTableScrollPane();

	        
	        createPacketStorageArea();
	        createPacketStorageAreaScrollPane();

	        
	        createStaticHeaderSplitPanes();
		
	}



	private void createStaticHeaderSplitPanes() {
	       splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	        
	        splitPane3.add(packetTrackerScroll);

	        splitPane3.add(packetStorageAreaScroll);
	        
	       

	        
	        splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT); 
	        splitPane2.add(appIdListScroll);
	        splitPane2.add(splitPane3);
	        
	        staticHeaderSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT); 
	        staticHeaderSplitPane.add(headerScroll);
	        staticHeaderSplitPane.add(splitPane2);
	        staticHeaderSplitPane.setName(RawApplicationDisplay);
		
	}



	private void createPacketTrackerTableScrollPane() {
        packetTrackerScroll = new JScrollPane(packetTrackerTable);
        packetTrackerScroll.setLayout(new ScrollPaneLayout());
        packetTrackerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        packetTrackerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        packetTrackerScroll.setMinimumSize(this.headerTable.getSize());
        packetTrackerScroll.setMaximumSize(this.headerTable.getSize());
        packetTrackerScroll.setPreferredSize(this.headerTable.getSize());
		
	}



	private void createPacketStorageAreaScrollPane() {
        packetStorageAreaScroll = new JScrollPane(this.packetStorageArea);
        packetStorageAreaScroll.setLayout(new ScrollPaneLayout());
        packetStorageAreaScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        packetStorageAreaScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        packetStorageAreaScroll.setMinimumSize(this.headerTable.getSize());
        packetStorageAreaScroll.setMaximumSize(this.headerTable.getSize());
        packetStorageAreaScroll.setPreferredSize(this.headerTable.getSize());
		
	}



	private void createAppIdListTableScrollPane() {
        appIdListScroll = new JScrollPane(appIdListTable);
        appIdListScroll.setLayout(new ScrollPaneLayout());
        appIdListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        appIdListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        appIdListScroll.setMinimumSize(this.headerTable.getSize());
        appIdListScroll.setMaximumSize(this.headerTable.getSize());
        appIdListScroll.setPreferredSize(this.headerTable.getSize());
		
	}



	private void createHeaderTableScrollPane() {
	       
        headerScroll = new JScrollPane(this.headerTable);
        headerScroll.setLayout(new ScrollPaneLayout());
        headerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        headerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        headerScroll.setMinimumSize(this.headerTable.getSize());
        headerScroll.setMaximumSize(this.headerTable.getSize());
        headerScroll.setPreferredSize(this.headerTable.getSize());

	}



	public JEditorPane getHtmlTextPane() {
		return htmlTextPane;
	}

	private void positionFrame() {
		Dimension size = frame.getSize();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (screen.width - size.width) / 2;
		int h = (screen.height - size.height) / 2;
		frame.setLocation(w, h);
	}
	
	// need something proportional, not fixed...
	public void frameResize() {
		Dimension d1 = this.naviPane.getLeftComponent().getSize();
		Dimension d2 = this.headerTable.getSize();
		//System.out.println("D1 width = " + d1.getWidth());
		//System.out.println("D2 width = " + d2.getWidth());
		
		Dimension d3 = this.getFrame().getSize();
		
		//this.getFrame().setSize(new Dimension((int)(d1.getWidth() + d2.getWidth()), (int)d3.getHeight()));
		
		
	}
	public void naviPaneResize() {
		//printMainScrollPaneComponents("after navi read file but before resize");
		// WORKD until I split again
		
		this.twoViewPane.setDividerLocation(-1); 
		this.naviPane.setDividerLocation(-1);  // causes the preferred size of the component for the left/top pane of the split pane to be resized (i think)
		//printMainScrollPaneComponents("after navi read file and after resize");
	}

    // this is called by the navitree ... when leaf is selected, the raw app leaf..
	public synchronized void updateStaticHeaderModel(RawApplicationPackets rapTmp) {
		
		//System.out.println("update static header model start");
		RawApplicationPacketsRandomAccess rap = (RawApplicationPacketsRandomAccess) rapTmp;
		try {
			rap.open();
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("A...");
		StaticHeader sh = rap.getStaticHeader();
		System.out.println("B...");
		updateHeaderTable(sh);
		System.out.println("C...");
		updateAppIdListTable(sh);
		System.out.println("D...");
		updatePacketTrackerTable(sh);
		System.out.println("E...");
		updatePacketStorageArea(sh);
		System.out.println("F...");
		
		// these make the UI resize things... 
		// they are not perfect in their setting at this time
		this.naviPane.setDividerLocation(-1);  // causes the preferred size of the component for the left/top pane of the split pane to be resized (i think)
		//this.splitPane3.setDividerLocation(-1);
		//
		this.staticHeaderSplitPane.setDividerLocation(-1);
		this.splitPane2.setDividerLocation(0.25);
		
		
	//	printMainScrollPaneComponents("update static header model setting to invisble/visible");
		
	//	Component[] cs = ((ScrollPaneLayout)mainScrollPane.getLayout()).getViewport().getComponents();
	//	for (int i = 0; i < cs.length; i++) {
			
			
	//		if (cs[i].getName().equals(RDRViewer.RawApplicationDisplay)) {
	//			System.out.println("Setting raw display to true...");
	//			cs[i].setVisible(true);
	//		} else if (cs[i].getName().equals(RDRViewer.FilenameDisplay)) {
	//			System.out.println("Setting filename display to false...");
	//			cs[i].setVisible(false);
	//		}
	//	}
		
		
		displayOnMainScrollView(staticHeaderSplitPane);
		
		this.twoViewPane.setDividerLocation(-1); 
		
	//	printMainScrollPaneComponents("update static header model end");
	}

	private void displayOnMainScrollView(Component component) {
		((ScrollPaneLayout)mainScrollPane.getLayout()).getViewport().add(component);
		component.setVisible(true);
		
		component.repaint();
		
	}

	public void createRDRProductTable() {
		
		rdrProductTable = new JTable(new RDRProductTableModel());
		
        int height = rdrProductTable.getRowCount() * rdrProductTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        rdrProductTable.setMaximumSize(d);
        rdrProductTable.setMinimumSize(d);
        rdrProductTable.setPreferredSize(d);
		
	}

	public void updateRDRProductModel(RDRProduct product) {
		TableModel model = rdrProductTable.getModel();
		
		model.setValueAt("Instrument_Short_Name", 0, 0);
		model.setValueAt(product.getInstrument_Short_Name().toString(), 0, 1);

		model.setValueAt("N_Collection_Short_Name", 1, 0);
		model.setValueAt(product.getN_Collection_Short_Name().toString(), 1, 1);

		model.setValueAt("N_Dataset_Type_Tag", 2, 0);
		model.setValueAt(product.getN_Dataset_Type_Tag().toString(), 2, 1);

		model.setValueAt("N_Processing_Domain", 3, 0);
		model.setValueAt(product.getN_Processing_Domain().toString(), 3, 1);
		((AbstractTableModel)model).fireTableStructureChanged();
		
		int rowCount = rdrProductTable.getRowCount();
		
        int height = rowCount * rdrProductTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        rdrProductTable.setMaximumSize(d);
        rdrProductTable.setMinimumSize(d);
        rdrProductTable.setPreferredSize(d);
        
		displayOnMainScrollView(rdrProductTable);

	}


	public void createGranuleTable() {
		granuleTable = new JTable(new GranuleTableModel());
		
        int height = granuleTable.getRowCount() * granuleTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        granuleTable.setMaximumSize(d);
        granuleTable.setMinimumSize(d);
        granuleTable.setPreferredSize(d);
		
		
		
	}
	public void updateGranuleModel(Granule g) {
		TableModel model = granuleTable.getModel();
		
		model.setValueAt("Beginning_Date", 0, 0);
		model.setValueAt(g.getBeginning_Date(), 0, 1);

		model.setValueAt("Beginning_Time", 1, 0);
		model.setValueAt(g.getBeginning_Time(), 1, 1);

		model.setValueAt("Ending_Date", 2, 0);
		model.setValueAt(g.getEnding_Date(), 2, 1);

		model.setValueAt("Ending_Time", 3, 0);
		model.setValueAt(g.getEnding_Time(), 3, 1);

		model.setValueAt("N_Beginning_Orbit_Number", 4, 0);
		model.setValueAt(Long.toString(g.getN_Beginning_Orbit_Number()), 4, 1);

		model.setValueAt("N_Beginning_Time_IET", 5, 0);
		model.setValueAt(Long.toString(g.getN_Beginning_Time_IET()), 5, 1);

		model.setValueAt("N_Creation_Date", 6, 0);
		model.setValueAt(g.getN_Creation_Date(), 6, 1);

		model.setValueAt("N_Creation_Time", 7, 0);
		model.setValueAt(g.getN_Creation_Time(), 7, 1);

		model.setValueAt("N_Ending_Time_IET", 8, 0);
		model.setValueAt(Long.toString(g.getN_Ending_Time_IET()), 8, 1);

		model.setValueAt("N_Granule_ID", 9, 0);
		model.setValueAt(g.getN_Granule_ID().toString(), 9, 1);

		model.setValueAt("N_Granule_Status", 10, 0);
		model.setValueAt(g.getN_Granule_Status(), 10, 1);

		model.setValueAt("N_Granule_Version", 11, 0);
		model.setValueAt(g.getN_Granule_Version(), 11, 1);

		model.setValueAt("N_LEOA_Flag", 12, 0);
		model.setValueAt(g.getN_LEOA_Flag().toString(), 12, 1);

		model.setValueAt("N_NPOESS_Document_Ref", 13, 0);
		model.setValueAt(g.getN_NPOESS_Document_Ref(), 13, 1);

		model.setValueAt("N_Packet_Type", 14, 0);
		model.setValueAt(g.getN_Packet_Type(), 14, 1);

		model.setValueAt("N_Packet_Type_Count", 15, 0);
		model.setValueAt(g.getN_Packet_Type_Count(), 15, 1);

		model.setValueAt("N_Percent_Missin_Data", 16, 0);
		model.setValueAt(Float.toString(g.getN_Percent_Missing_Data()), 16, 1);

		model.setValueAt("N_Reference_ID", 17, 0);
		model.setValueAt(g.getN_Reference_ID().toString(), 17, 1);

		model.setValueAt("N_Software_Version", 18, 0);
		model.setValueAt(g.getN_Software_Version(), 18, 1);

		((AbstractTableModel)model).fireTableStructureChanged();
		
		int rowCount = granuleTable.getRowCount();
		
        int height = rowCount * granuleTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        granuleTable.setMaximumSize(d);
        granuleTable.setMinimumSize(d);
        granuleTable.setPreferredSize(d);
        
		displayOnMainScrollView(granuleTable);
	}


	public void createAggregateTable() {
		aggregateTable = new JTable(new AggregateTableModel());
		
        int height = aggregateTable.getRowCount() * aggregateTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        aggregateTable.setMaximumSize(d);
        aggregateTable.setMinimumSize(d);
        aggregateTable.setPreferredSize(d);
		
		
		
	}
	public void updateAggregateModel(Aggregate a) {
		TableModel model = aggregateTable.getModel();
		
		model.setValueAt("AggregateBeginningDate", 0, 0);
		model.setValueAt(a.getBeginningDateFormatted(), 0, 1);

		model.setValueAt("AggregateBeginningGranuleID", 1, 0);
		model.setValueAt(a.getBeginningGranuleId().toString(), 1, 1);

		model.setValueAt("AggregateBeginningOrbitNumber", 2, 0);
		model.setValueAt(Long.toString(a.getBeginningOrbit()), 2, 1);

		model.setValueAt("AggregateBeginningTime", 3, 0);
		model.setValueAt(a.getBeginningTimeFormatted(), 3, 1);

		model.setValueAt("AggregateEndingDate", 4, 0);
		model.setValueAt(a.getEndingDateFormatted(), 4, 1);

		model.setValueAt("AggregateEndingGranuleID", 5, 0);
		model.setValueAt(a.getEndingGranuleId().toString(), 5, 1);

		model.setValueAt("AggregateEndingOrbitNumber", 6, 0);
		model.setValueAt(Long.toString(a.getEndingOrbit()), 6, 1);

		model.setValueAt("AggregateNumberGranules", 7, 0);
		model.setValueAt(Long.toString(a.getGranuleCount()), 7, 1);
		
		((AbstractTableModel)model).fireTableStructureChanged();
		
		int rowCount = aggregateTable.getRowCount();
		
        int height = rowCount * aggregateTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        aggregateTable.setMaximumSize(d);
        aggregateTable.setMinimumSize(d);
        aggregateTable.setPreferredSize(d);
        
		displayOnMainScrollView(aggregateTable);
		
	}
	

	
	
	public void createHeaderTable()  {
		
		headerTable = new JTable(new HeaderTableModel());
		
        int height = headerTable.getRowCount() * headerTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        headerTable.setMaximumSize(d);
        headerTable.setMinimumSize(d);
        headerTable.setPreferredSize(d);

        
	}
	
	public void updateHeaderTable(StaticHeader sh) {


		TableModel model = headerTable.getModel();
		
		model.setValueAt("Satellite", 0, 0);
		model.setValueAt(sh.readSatelliteString(), 0, 1);
		


		String sensor = sh.readSensorString();
		String typeID = sh.readTypeIDString();
		//try {
		//	RDRName rdrName = RDRName.fromSensorAndTypeID(sensor, typeID);
		//} catch (RtStpsException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}


		model.setValueAt("Sensor", 1, 0);
		model.setValueAt(sensor, 1, 1);

		model.setValueAt("TypeID", 2, 0);
		model.setValueAt(typeID, 2, 1);

		model.setValueAt("NumAppIds", 3, 0);
		model.setValueAt(Integer.toString(sh.readNumAppIds()), 3, 1);

		model.setValueAt("ApidListOffset", 4, 0);
		model.setValueAt(Long.toString(sh.readApidListOffset()), 4, 1);

		model.setValueAt("PktTrackerOffset", 5, 0);
		model.setValueAt(Long.toString(sh.readPktTrackerOffset()), 5, 1);

		model.setValueAt("ApStorageOffset", 6, 0);
		model.setValueAt(Long.toString(sh.readApStorageOffset()), 6, 1);

		model.setValueAt("NextPktPos", 7, 0);
		model.setValueAt(Long.toString(sh.readNextPktPos()), 7, 1);

		model.setValueAt("StartBoundary", 8, 0);
		model.setValueAt(Long.toString(sh.readStartBoundary()), 8, 1);


		model.setValueAt("EndBoundary", 9, 0);
		model.setValueAt(Long.toString(sh.readEndBoundary()), 9, 1);

		((AbstractTableModel)model).fireTableStructureChanged();
		
		
		int rowCount = headerTable.getRowCount();
		
        int height = rowCount * headerTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        headerTable.setMaximumSize(d);
        headerTable.setMinimumSize(d);
        headerTable.setPreferredSize(d);
       // System.out.println("header table Row width = " + width + " height = " + height);
       // headerScroll.setMinimumSize(d);
       // headerScroll.setMaximumSize(d);
        headerScroll.setPreferredSize(d);

	}
	
	private void createAppIdListTable() {
		
	//	Vector<Vector<String>> data = new Vector<Vector<String>>();
		Vector<String> names = new Vector<String>();
	//	
		//names.add("Name");
	//	names.add("AppId");
		//names.add("PktsReserved");
		//names.add("PktsReceived");
		//names.add("PktTrackerIndex");
		
		appIdListTable = new JTable(new AppIdListTableModel());
	

		int height = appIdListTable.getRowCount() * appIdListTable.getRowHeight();
		int width = 500;
		Dimension d = new Dimension(width, height);
		//appIdListTable.setMaximumSize(d);
		appIdListTable.setMinimumSize(d);
		appIdListTable.setPreferredSize(d);
	//	System.out.println("width = " + width + " height = " + height);
	}
	
	private void updateAppIdListTable(StaticHeader sh) {
	
		RDRAppIdList appIdList = sh.readRDRAppIdList();

		List<RDRAppIdItem> items = appIdList.getAppIdItemList();
		//System.out.println("ITEMS -- " + items.size());

		TableModel model = appIdListTable.getModel();
		
		for (int i = 0; i < items.size(); i++) {
			RDRAppIdItem item = items.get(i);
	
			model.setValueAt(item.getName(), i, 0);

			model.setValueAt(Integer.toString(item.getValue()), i, 1);

			model.setValueAt(Integer.toString(item.getPktsReserved()), i, 2);

			model.setValueAt(Integer.toString(item.getPktsReceived()), i, 3);

			model.setValueAt(Integer.toString(item.getPktTrackerIndex()), i, 4);
			
		}


		((AbstractTableModel)model).fireTableStructureChanged();
		
		
		int rowCount = appIdListTable.getRowCount();
		System.out.println("app id list Row count = " + rowCount);
        int height = rowCount * appIdListTable.getRowHeight();
        if (height < 80) { // if two small it never opens the splitpanel
        	height = 80;
        }
        int width = 500;
        Dimension d = new Dimension(width, height);
        //appIdListTable.setMaximumSize(d);
        appIdListTable.setMinimumSize(d);
        appIdListTable.setPreferredSize(d);
       // System.out.println("app id list  width = " + width + " height = " + height);
        appIdListScroll.setMinimumSize(d);
      //  appIdListScroll.setMaximumSize(d);
        appIdListScroll.setPreferredSize(d);

	}
	
	
	private void createPacketTrackerTable() {
		
		//Vector<Vector<String>> data = new Vector<Vector<String>>();
		//Vector<String> names = new Vector<String>();
		
		//names.add("ObsTime");
		//names.add("SequenceNumber");
		//names.add("Size");
		//names.add("Offset");
		//names.add("FillPercent");
		
		packetTrackerTable =  new JTable(new PacketTrackerTableModel());

		int height = packetTrackerTable.getRowCount() * packetTrackerTable.getRowHeight();
		int width = 500;
		Dimension d = new Dimension(width, height);
		//packetTrackerTable.setMaximumSize(d);
		packetTrackerTable.setMinimumSize(d);
		packetTrackerTable.setPreferredSize(d);
		//System.out.println("width = " + width + " height = " + height);
	        
	}
	private void updatePacketTrackerTable(StaticHeader sh) {

		PacketTrackerList packetTrackerList = new PacketTrackerList((int)sh.readPktTrackerOffset(), sh.getData(), (int)sh.readApStorageOffset());

		List<PacketTrackerItem> packetTrackerItemList = packetTrackerList.getPacketTrackerItemList();
		
		TableModel model = packetTrackerTable.getModel();
		
		for (int i = 0; i < packetTrackerItemList.size(); i++) {
			PacketTrackerItem pti = packetTrackerItemList.get(i);

			model.setValueAt(Long.toString(pti.getObsTime()), i, 0);
			model.setValueAt(Integer.toString(pti.getSequenceNumber()), i, 1);
			model.setValueAt(Integer.toString(pti.getSize()), i, 2);
			model.setValueAt(Integer.toString(pti.getOffset()), i, 3);
			model.setValueAt(Integer.toString(pti.getFillPercent()), i, 4);

		}

		((AbstractTableModel)model).fireTableStructureChanged();
		int rowCount = packetTrackerTable.getRowCount();
		//System.out.println("packet tracker Row count = " + rowCount);
        int height = rowCount * packetTrackerTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        //packetTrackerTable.setMaximumSize(d);
        packetTrackerTable.setMinimumSize(d);
        packetTrackerTable.setPreferredSize(d);
       // System.out.println("packet tracker width = " + width + " height = " + height);
        packetTrackerScroll.setMinimumSize(d);
      //  packetTrackerScroll.setMaximumSize(d);
        packetTrackerScroll.setPreferredSize(d);
	}
	
	private void createPacketStorageArea() {
		
		
		//Vector<Vector<String>> data = new Vector<Vector<String>>();
		//Vector<String> names = new Vector<String>();
		
		//Vector<String> row =  new Vector<String>();
		
		//names.add("SequenceFlags");
		//names.add("SequenceCounter");
		//names.add("ApplicationId");
		//names.add("PacketLength");
		//names.add("Timestamp");
		//names.add("Data");
		
		packetStorageArea = new JTable(new PacketStorageAreaTableModel());
		
		packetStorageArea.getSelectionModel().addListSelectionListener(new RowListener());
		//packetStorageArea.getColumnModel().getSelectionModel().addListSelectionListener(new ColumnListener());
		packetStorageArea.setCellSelectionEnabled(true);


		int height = packetStorageArea.getRowCount() * packetStorageArea.getRowHeight();
		int width = 500;
		Dimension d = new Dimension(width, height);
		packetStorageArea.setMaximumSize(d);
		packetStorageArea.setMinimumSize(d);
		packetStorageArea.setPreferredSize(d);
	//	System.out.println("width = " + width + " height = " + height);
		
		
		
	}
	
	private void updatePacketStorageArea(StaticHeader sh) {

		
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd kk:mm:ss.SSS zzz yyyy");


		SequentialPacketReader spr = sh.createSequentialPacketReader();
		
		currentPacketList = null;
		currentPacketList = new LinkedList<Packet>();
		
		
		TableModel model = this.packetStorageArea.getModel();

		int i = 0; // rows
		int j = 0; // cols
		
		//int pc = 0;
		JDialog dialog = new JDialog(this.getFrame());

		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate(true);
		bar.setVisible(true);
		dialog.setTitle("Reading ...");
		dialog.add(bar);
		dialog.setVisible(true);
		Dimension barPrefSize = bar.getPreferredSize();
			
		dialog.setLocation(this.getFrame().getLocation().x+this.getFrame().getWidth()/2- ((int)barPrefSize.getWidth()/2), this.getFrame().getLocation().y+this.getFrame().getHeight()/2-((int)barPrefSize.getHeight()/2));
			
		dialog.pack();
			
		int pc = 0;
		/***SPEED
		while (spr.hasNext()) {
			
			//System.out.println("Packet = "  + pc++);
			
			Packet p = (Packet) spr.next();

			currentPacketList.add(p);
			
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
		****/
		dialog.dispose();

		
		//Set up tool tips for the sport cells.
        //DefaultTableCellRenderer renderer =
        //        new DefaultTableCellRenderer();
       // renderer.setToolTipText("Show the Data");
       // packetStrorageArea.getColumnModel().getColumn(5).setCellRenderer(renderer);
		packetStorageArea.setToolTipText("Show the Data");

		((AbstractTableModel)model).fireTableStructureChanged();
		int rowCount = packetStorageArea.getRowCount();
		System.out.println("packet storage area  Row count = " + rowCount);
        int height = rowCount * packetStorageArea.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        packetStorageArea.setMaximumSize(d);
        packetStorageArea.setMinimumSize(d);
        packetStorageArea.setPreferredSize(d);
       // System.out.println("packet storage area width = " + width + " height = " + height);
       // packetStorageAreaScroll.setMinimumSize(d);
      //  packetStorageAreaScroll.setMaximumSize(d);
        packetStorageAreaScroll.setPreferredSize(d);
	}
	/*****
	private void updatePacketStorageArea(StaticHeader sh) {


		
		
		
		
		UpdatePacketStorageAreaThread upsat = new UpdatePacketStorageAreaThread(this.getFrame(), sh, this.packetStrorageArea.getModel());
		
		upsat.execute();

		UpdatePacketStorageArea foobar=null;
		TableModel model=null;
		try {
			foobar = upsat.get();
			model = foobar.getModel();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        packetStrorageArea.setToolTipText("Show the Data");

		((AbstractTableModel)model).fireTableStructureChanged();
		int rowCount = packetStrorageArea.getRowCount();
	//	System.out.println("packet storage area  Row count = " + rowCount);
        int height = rowCount * packetStrorageArea.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        packetStrorageArea.setMaximumSize(d);
        packetStrorageArea.setMinimumSize(d);
        packetStrorageArea.setPreferredSize(d);
       // System.out.println("packet storage area width = " + width + " height = " + height);
       // packetStorageAreaScroll.setMinimumSize(d);
      //  packetStorageAreaScroll.setMaximumSize(d);
        packetStorageAreaScroll.setPreferredSize(d);
	}
***/
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


	public JFrame getFrame() {
		return frame;
	}

	public RDRNaviTree getNaviTree() {
		
		return naviTree;
	}


	// this is called when the user opens a new RDR file through the Filepicker sequence
	// it is used only (at this time) by the method used in building the filename tables
	// and associated XML user block display
	public void setRDRReader(RDRFileReader rdrReader) {
		this.rdrReader = rdrReader;
		
	}
	




	// this creates the NPOES filename table and the userblock xml into the filename splitpane
	// is uses the rdrReader which was set essentially by the filepicker dialog sequence...
	//
	public void createNPOESSFilenameTable(/* NPOESSFilename nfilename*/) {
		
		NPOESSFilename nfilename = rdrReader.getNPOESSFilename();
		
		Vector<Vector<String>> data = new Vector<Vector<String>>();
		Vector<String> names = new Vector<String>();
		
		names.add("Field");
		names.add("Value");
		
		Vector<String> row = new Vector<String>();
		
		row.add("Creation date and time");
		row.add(nfilename.getCreationDateAndTime().toString());
		data.add(row);
		
		row = new Vector<String>();
		row.add("Orbit");
		row.add(Integer.toString(nfilename.getOrbit()));
		data.add(row);
		row = new Vector<String>();
		row.add("Origin");
		row.add(nfilename.getOrigin().toString());
		data.add(row);
		for (ProductIdentifiers pi : nfilename.getProductIdentifiers()) {
			row = new Vector<String>();
			row.add("Product Id");
			row.add(pi.toString());
			data.add(row);
		}
		
		row = new Vector<String>();
		row.add("SpacecraftAOS");
		row.add(nfilename.getSpacecraftId().toString());
		data.add(row);
		
		row = new Vector<String>();
		row.add("Start date and time");
		row.add(nfilename.getStartDateTime().toString());
		data.add(row);
		
		row = new Vector<String>();
		row.add("Stop time");
		row.add(nfilename.getStopTime().toString());
		data.add(row);
		
	

		filenameTable = new JTable(data, names);
		filenameTable.setName(RDRViewer.FilenameDisplay);
	//	filenameTable.setVisible(false);
		

		JTextPane xtp = new JTextPane();
		
		// basic XML formatting
		try {
		
			 Source xmlInput = new StreamSource(new StringReader(rdrReader.createUserBlockReader().readString()));
		        StringWriter stringWriter = new StringWriter();
		        StreamResult xmlOutput = new StreamResult(stringWriter);
		        TransformerFactory transformerFactory = TransformerFactory.newInstance();
		        transformerFactory.setAttribute("indent-number", 5);
		        Transformer transformer = transformerFactory.newTransformer(); 
		        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		        transformer.transform(xmlInput, xmlOutput);
		       

			xtp.getDocument().insertString(0, xmlOutput.getWriter().toString(), null);
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 fileXMLSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		 
		 fileXMLSplit.add(filenameTable);
		fileXMLSplit.add(xtp);
	}

	public void displayNPOESSFilename() {
		


		// so just setting to the desired item does it...
	//	((ScrollPaneLayout)mainScrollPane.getLayout()).getViewport().add(this.filenameTable);
	//	this.filenameTable.setVisible(true);

//		mainScrollPane.repaint();
		//frame.doLayout();
		//frame.validate();
		//frame.repaint();
		
		//printMainScrollPaneComponents("displayNPOESFilename end");
		
		displayOnMainScrollView(/*filenameTable*/ fileXMLSplit);
	
	}

	//private void printMainScrollPaneComponents(String prefix) {
	//	Component[] cs = ((ScrollPaneLayout)mainScrollPane.getLayout()).getViewport().getComponents();
	//	System.out.println(prefix + ": I found this many components: " + cs.length);
	//	for (int i = 0; i < cs.length; i++) {
	//		System.out.println(prefix + ": " + cs[i]);
	//		
	//	}
	//}

	
	private class RowListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			
			outputSelection("Row ");
		}


	}

	private class ColumnListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			
			outputSelection("Col ");
		}
	}

	// SINGLE selection must be set for this work properly
	private void outputSelection(String msg) {
		
        int[] r = packetStorageArea.getSelectedRows();
        int[] c = packetStorageArea.getSelectedColumns();
        
        // col 5 is the data column
        if (c[0] != 5) return;
            
       displayPacket(r[0]);
        
        
    }

	
	private void displayPacket(int packetIndex) {
		
		
		
		
		TableModel model = this.rawPacketDataTable.getModel();
		
		Packet p = currentPacketList.get(packetIndex);
		
		byte[] data = p.getData();
		
		
		int col = 0; int row = 0;
		int len = p.getPacketSize();
		
		int index = 0;
	
		while (index < len) {
			
			if (col > 7) { 
				col = 0;
				++row;
			}
			
			//System.out.println("Setting value [" + row + "][" + col + "]");
			model.setValueAt(String.format("%02x", data[index]), row, col);
			
			++index; ++col;
		}


		while (col < 8) {
			//System.out.println("Setting blank [" + row + "][" + col + "]");
			model.setValueAt(" ", row, col);
			++col;
		}



		((AbstractTableModel)model).fireTableStructureChanged();
		int rowCount = rawPacketDataTable.getRowCount();
		//System.out.println("Raw Packet  Row count = " + rowCount);
        int height = rowCount * rawPacketDataTable.getRowHeight();
        int width = 100;
        Dimension d = new Dimension(width, height);
        rawPacketDataTable.setMaximumSize(d);
        rawPacketDataTable.setMinimumSize(d);
        rawPacketDataTable.setPreferredSize(d);
      
       // rawPacketDataTable.setVisible(true);
        
       
        //workPanelScroll.setVisible(true);
        
        
        rawPacketPanelScroll.setPreferredSize(d);
        //workPanelScroll.repaint();
        
        
        displayCookedPacket(p);
        
        
	}


	private void displayCookedPacket(Packet p) {
		displayAE(p);
	}
	
	private void displayAE(Packet p) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss.SSS zzz yyyy");
		
		TableModel model = cookedPacketDataTable.getModel();
		
		
		int row = 0;
		
		model.setValueAt("SequenceFlags", row, 0);
		model.setValueAt(Integer.toHexString(p.getSequenceFlags()), row++, 1);
		
		model.setValueAt("SequenceCounter", row, 0);
		model.setValueAt(Integer.toString(p.getSequenceCounter()), row++, 1);
		
		model.setValueAt("ApplicationId", row, 0);
		model.setValueAt(Integer.toString(p.getApplicationId()), row++, 1);
		
		model.setValueAt("PacketLength", row, 0);
		int length = p.getPacketLength();
		model.setValueAt(Integer.toString(length), row++, 1);
		
		
		if (p.hasSecondaryHeader()) { 
			PDSDate pt = new PDSDate(p.getTimeStamp(8));

			model.setValueAt("Timestamp", row, 0);
			model.setValueAt(sdf.format(pt.getDate()), row++, 1);


		} 
		

		
		byte[] data = p.getData();
		
		
		int offset = 14;
		int id = ((int)data[offset] & 0x0ff);
		model.setValueAt("SpacecraftAOS ID", row, 0);
		model.setValueAt(Integer.toString(id), row++, 1);
		offset += 1;
		

		
		PDSDate eTime = new PDSDate(p.getTimeStamp(offset, 8));
		

		String eTimeStr = sdf.format(eTime.getDate());
		model.setValueAt("Ephemeris Timestamp", row, 0); 
		model.setValueAt(eTimeStr, row++, 1);
		
		offset += 8;
		
		int e1 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int e2 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int e3 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;

		float ef1 = Float.intBitsToFloat(e1);
		float ef2 = Float.intBitsToFloat(e2);
		float ef3 =  Float.intBitsToFloat(e3);
		
		model.setValueAt("E-pos1", row, 0);
		model.setValueAt(Float.toString(ef1), row++, 1);
		
		model.setValueAt("E-pos2", row, 0);
		model.setValueAt(Float.toString(ef2), row++, 1);
		
		model.setValueAt("E-pos3", row, 0);
		model.setValueAt(Float.toString(ef3), row++, 1);
		
		
		
		int ev1 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int ev2 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int ev3 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		

		
		float evf1 = Float.intBitsToFloat(ev1);
		float evf2 = Float.intBitsToFloat(ev2);
		float evf3 = Float.intBitsToFloat(ev3);
		model.setValueAt("E-vol1", row, 0);
		model.setValueAt(Float.toString(evf1), row++, 1);
		
		model.setValueAt("E-vol2", row, 0); 
		model.setValueAt(Float.toString(evf2), row++, 1);
		
		model.setValueAt("E-vol3", row, 0); 
		model.setValueAt(Float.toString(evf3), row++, 1);
		 
		

		offset += 4;
		
		PDSDate aTime = new PDSDate(p.getTimeStamp(offset, 8));
		String aTimeStr =  sdf.format(aTime.getDate());
		model.setValueAt("Attitude Timestamp", row, 0);
		model.setValueAt(aTimeStr, row++, 1);
		
		offset += 8;
		int q1 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int q2 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int q3 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;
		
		offset += 4;
		int q4 = ( (int)data[offset] << 24 ) | 
		(((int)data[offset+1] << 16) & 0x00ff0000) | 
		(((int)data[offset+2] <<  8) & 0x0000ff00) | 
		((int)data[offset+3] & 0x000000ff) ;

		float qf1 = Float.intBitsToFloat(q1);
		float qf2 = Float.intBitsToFloat(q2);
		float qf3 = Float.intBitsToFloat(q3);
		float qf4 = Float.intBitsToFloat(q4); 
		model.setValueAt("Quat1", row, 0); 
		model.setValueAt(Float.toString(qf1), row++, 1);
		
		model.setValueAt("Quat2", row, 0); 
		model.setValueAt(Float.toString(qf2), row++, 1);
		
		model.setValueAt("Quat3", row, 0);
		model.setValueAt(Float.toString(qf3), row++, 1);
		
		model.setValueAt("Quat4", row, 0); 
		model.setValueAt(Float.toString(qf4), row++, 1);
		
		((AbstractTableModel)model).fireTableStructureChanged();
		
		
		int rowCount = cookedPacketDataTable.getRowCount();
		
        int height = rowCount * cookedPacketDataTable.getRowHeight();
        int width = 500;
        Dimension d = new Dimension(width, height);
        cookedPacketDataTable.setMaximumSize(d);
        cookedPacketDataTable.setMinimumSize(d);
        cookedPacketDataTable.setPreferredSize(d);

        this.cookedPacketPanelScroll.setPreferredSize(d);
		
	}
	
	@Override
	public void run() {
		createInitialDisplay();
		
		
	}
	
	public static void main(String[] args) {
    	
    	try {
			javax.swing.SwingUtilities.invokeLater(new RDRViewer());
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
    }





}
