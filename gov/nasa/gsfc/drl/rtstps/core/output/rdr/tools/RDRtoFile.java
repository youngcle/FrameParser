/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.AllDataReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.HDF5Util;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRAllReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRFileReader;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RDRName;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.RawApplicationPackets;
import gov.nasa.gsfc.drl.rtstps.core.output.rdr.StaticHeader;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;


public class RDRtoFile {
	private int hdfFile;
	//private RDRName rdrName;
	
	public RDRtoFile(String filename) throws RtStpsException {
		try {
			hdfFile = H5.H5Fopen(filename, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
			if (hdfFile < 0) {
				throw new RtStpsException("File not found: " + filename);
			}
			//this.rdrName = rdrName;
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
	}
	
	public AllDataReader createAllDataReader() throws RtStpsException {
		return new AllDataReader(hdfFile);
	}
	
	public void close() throws RtStpsException {
		try {
			H5.H5Fclose(hdfFile);
		} catch (HDF5LibraryException e) {
			throw new RtStpsException(e);
		}
		HDF5Util.cleanup();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("No file to process... or no output file");
			System.exit(-1);
		}
		try {
			
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(args[1]));

			RDRFileReader readRDR = new RDRFileReader(args[0]);
			
			AllDataReader allReader = readRDR.createAllDataReader();
			
			//RDRAllReader rar = allReader.getRDRAllReaderByRDRName(RDRName.NPP_Ephemeris_and_Attitude); SpacecraftAOS Diary
			
			RDRAllReader rar = allReader.getRDRAllReaderByRDRName(RDRName.VIIRS_Science); 
			
			while (rar.hasNext()) {

				RawApplicationPackets rap = rar.next();

				StaticHeader sh = rap.getStaticHeader();
				byte[] data = rap.getData();
				int offset = sh.getApStorageOffset();

				int len = data.length - offset;

				out.write(data, offset, len);

				rap.close();
			}
			
			rar.close();
			allReader.close();
			readRDR.close();
			
	
			out.flush();
			out.close();
			

		} catch (RtStpsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}

}
