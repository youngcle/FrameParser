/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.rdrviewer;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAppIdItem;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAppIdList;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class StaticHeaderViewer implements Runnable  {
	

	private JFrame frame;

	


	private JEditorPane htmlTextPane;
	private JScrollPane scrollPane;

	private String rdrFilename;
	private ReadStaticHeader readStaticHeader;
	

	public StaticHeaderViewer(String rdrFilename) throws RtStpsException {
		this.rdrFilename = rdrFilename;
		
		readStaticHeader = new ReadStaticHeader(rdrFilename);
	}



	public void createInitialDisplay() {
		
	      //Create and set up the window.
        frame = new JFrame("StaticHeaderViewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // create jeditorpane
        htmlTextPane = new JEditorPane();
        
        // make it read-only
        htmlTextPane.setEditable(false);
        
        //htmlTextPane = new JTextPane();
        htmlTextPane.setContentType("text/html"); 
        htmlTextPane.setEditorKit(new HTMLEditorKit());     
        htmlTextPane.setDocument(new HTMLDocument());     
		
      //  HTMLEditorKit kit = (HTMLEditorKit)htmlTextPane.getEditorKit();
       
      //  // add some styles to the html
      //  StyleSheet styleSheet = kit.getStyleSheet();
        
      //  styleSheet.addRule("table {color: black;}");
       

        
        scrollPane = new JScrollPane(htmlTextPane);
        scrollPane.setLayout(new ScrollPaneLayout());
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(300, 200));
		scrollPane.setMinimumSize(new Dimension(300, 200));

		
  
        
        frame.setContentPane(scrollPane);
        
        
        frame.setMinimumSize(new Dimension(800,600));
        positionFrame();
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
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
	

	private void drawHTML() throws RtStpsException {
		
		List<StaticHeader> hdrs = readStaticHeader.getStaticHeaders();
		
		String htmlStr = toHTML(hdrs.get(0));
		
		System.out.println("HTML string -- " + htmlStr);
		
		HTMLDocument doc = (HTMLDocument) htmlTextPane.getDocument();
        HTMLEditorKit kit = (HTMLEditorKit) htmlTextPane.getEditorKit();
		try {
			doc.remove(0, doc.getLength());
			
			kit.insertHTML(doc, doc.getLength(), htmlStr,  0, 0, HTML.Tag.TABLE);
		} catch (BadLocationException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}
	
	@Override
	public void run() {
		createInitialDisplay();
		
		try {
			drawHTML();
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// fails to show table border on Linux at least... (works properly in webbrowser if stored as file and displayed though)
	public String toHTML(StaticHeader sh) throws RtStpsException {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<html>");
		sb.append("<body>");
		sb.append("<h1>My First Heading</h1>");
		sb.append("<table border=2>");
		sb.append("<tr>");
			sb.append("<th>Field</th>");
			sb.append("<th>Value</th>");
		sb.append("</tr>");

		
		sb.append("<tr>");
			sb.append("<td>Satellite</td>");
			sb.append("<td>" + sh.readSatelliteString() + "</td>");
		sb.append("</tr>");
		
		String sensor = sh.readSensorString();
		String typeID = sh.readTypeIDString();
		RDRName rdrName = RDRName.fromSensorAndTypeID(sensor, typeID);
		
		sb.append("<tr>");
		sb.append("<td>Sensor</td>");
		sb.append("<td>" + sensor + "</td>");
		sb.append("</tr>");
		//System.out.println("             -- Sensor: " + sensor);
		
		sb.append("<tr>");
		sb.append("<td>TypeID</td>");
		sb.append("<td>" +typeID + "</td>");
		sb.append("</tr>");
		//System.out.println("             -- TypeID: " + typeID);
		
		
		sb.append("<tr>");
		sb.append("<td>NumAppIds</td>");
		sb.append("<td>" + sh.readNumAppIds() + "</td>");
		sb.append("</tr>");
		//System.out.println("             -- NumAppIds: " + sh.readNumAppIds() + " -- should be: " + rdrName.getNumberOfAppIdsInRDR());
		
		sb.append("<tr>");
		sb.append("<td>ApidListOffset</td>");
		sb.append("<td>" + sh.readApidListOffset() + "</td>");
		sb.append("</tr>");
		//System.out.println("             -- ApidListOffset  : " + sh.readApidListOffset());
		
		
		sb.append("<tr>");
		sb.append("<td>PktTrackerOffset</td>");
		sb.append("<td>" + sh.readPktTrackerOffset() + "</td>");
		sb.append("</tr>");
		//System.out.println("             -- PktTrackerOffset: " + sh.readPktTrackerOffset());
		
		sb.append("<tr>");
		sb.append("<td>ApStorageOffset</td>");
		sb.append("<td>" +  sh.readApStorageOffset() + "</td>");
		sb.append("</tr>");
		//System.out.println("             -- ApStorageOffset : " + sh.readApStorageOffset());
		
		sb.append("<tr>");
		sb.append("<td>NextPktPos</td>");
		sb.append("<td>" +  sh.readNextPktPos() + "</td>");
		sb.append("</tr>");
		//System.out.println("             -- NextPktPos      : " + sh.readNextPktPos());
		
		sb.append("<tr>");
		sb.append("<td>StartBoundary</td>");
		sb.append("<td>" + sh.readStartBoundary() + "</td>");
		sb.append("</tr>");
		//System.out.println("             -- StartBoundary: " + sh.readStartBoundary());
		
		sb.append("<tr>");
		sb.append("<td>EndBoundary</td>");
		sb.append("<td>" + sh.readEndBoundary() + "</td>");
		sb.append("</tr>");
		//System.out.println("             -- EndBoundary  : " + sh.readEndBoundary());
		
		RDRAppIdList appIdList = sh.readRDRAppIdList();
		
		List<RDRAppIdItem> items = appIdList.getAppIdItemList();
		
		/**
		for (int i = 0; i < items.size(); i++) {
			RDRAppIdItem item = items.get(i);
			System.out.println("             -- AppIdItem[" + i + "] " );
			System.out.println("                -- Name            : " + item.getName());
			System.out.println("                -- AppId           : " + item.getValue());
			System.out.println("                -- PktsReserved    : " + item.getPktsReserved());
			System.out.println("                -- PktsReceived    : " + item.getPktsReceived());
			System.out.println("                -- PktTrackerIndex : " + item.getPktTrackerIndex());
		}
		**/
		sb.append("</table>"); 
		sb.append("</body>");
		sb.append("</html>");
		
		return sb.toString();
		
	}
	
	

    public static void main(String[] args) {
    	
    	try {
			javax.swing.SwingUtilities.invokeLater(new StaticHeaderViewer(args[0]));
		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
    }






}
