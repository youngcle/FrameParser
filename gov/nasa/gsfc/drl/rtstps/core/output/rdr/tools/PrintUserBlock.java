/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;


import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.UserBlockReader;

import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Print the UserBlock in specified HDF file to the console.
 * 
 *
 */
public class PrintUserBlock {
	
	/**
	 * Pretty print the XML string
	 * @param doc the String containing the XML
	 * @param out the output stream (likely System.out)
	 * @throws RtStpsException any wrapped XML based exceptions
	 */
	static void xmlFormat(String doc, OutputStream out) throws RtStpsException {

		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer serializer;
		try {
			serializer = tfactory.newTransformer();
			//Setup indenting to "pretty print"
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			serializer.transform(new StreamSource(new StringReader(doc)), new StreamResult(out));

		} catch (TransformerException e) {
			throw new RtStpsException(e);
		}
	}
	
	/**
	 * @param args the RDR HDF file
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("No file to process... ");
			System.exit(-1);
		}
		try {
			
			RDRFileReader readRDR = new RDRFileReader(args[0]);
			UserBlockReader ubReader = readRDR.createUserBlockReader();
			
			PrintUserBlock.xmlFormat(ubReader.readString(), System.out);
			
			//byte[] block = ubReader.readBytes();
			//System.out.println(java.util.Arrays.toString(block));
	
			ubReader.close();
			readRDR.close();
			

		} catch (RtStpsException e) {
			
			e.printStackTrace();
	
		} 
	}

}
