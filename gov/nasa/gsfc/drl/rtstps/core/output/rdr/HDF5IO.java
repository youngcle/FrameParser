/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;


import java.text.SimpleDateFormat;
import java.util.Date;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

@Deprecated
public class HDF5IO {
	private int        file_id, dataset_id, dataset_id2 , dataset_id3, dataspace_id, dataspace_id2,  cparms;   
	private long     offset[] = new long[1];
	private long  more[] = new long[1];

	private long dims[] = new long[1];
	
	private long maxdims[] = { HDF5Constants.H5S_UNLIMITED };
	private long diary_maxdims[] = { HDF5Constants.H5S_UNLIMITED };
	private int g1;
	private int g2;
	private int g3;
	private int diary_dataset_id;
	private int diary_dataspace_id;
	private int diary_cparms;
	private long[] diary_more = new long[1];
	private long[] diary_offset = new long[1];
	private long diary_dims[] = new long[1];

	private static int opened=0;
	
	
	


	public void openHDF5(String filename, int size) throws NullPointerException, HDF5Exception {
		long chunkDims[] = { size };
	
		more[0] = size;
		offset[0] = 0;

		//System.out.println("openHDF5 -- ids? " + H5.getOpenIDCount());
		
	    /* Create the data space with unlimited dimensions. */
	    dataspace_id = H5.H5Screate_simple (1, chunkDims, maxdims); 

	    /* Create a new file. If file exists its contents will be overwritten. */
	    file_id = H5.H5Fcreate (filename, HDF5Constants.H5F_ACC_TRUNC, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

	    /* Modify dataset creation properties, i.e. enable chunking  */
	    cparms = H5.H5Pcreate (HDF5Constants.H5P_DATASET_CREATE);
	    H5.H5Pset_chunk (cparms, 1, chunkDims);
	    
	    /* Create a new dataset within the file using cparms creation properties.  */

		g1 = H5.H5Gcreate(file_id, "/All_Data", 0);
		g2 = H5.H5Gcreate(file_id, "/All_Data/VIIRS-SCIENCE-RDR_All", 0);

	    dataset_id = H5.H5Dcreate(file_id, 
	    						"/All_Data/VIIRS-SCIENCE-RDR_All/RawApplicationPackets_0", 
	    						HDF5Constants.H5T_STD_U8BE, 
	    						dataspace_id, 
	    						cparms);

	    // spacecraft diary
	    /* Create the data space with unlimited dimensions. */
	    g3 = H5.H5Gcreate(file_id, "/All_Data/SPACECRAFT-DIARY-RDR_All", 0);
	    diary_dataspace_id = H5.H5Screate_simple (1, chunkDims, diary_maxdims); 
	    diary_cparms = H5.H5Pcreate (HDF5Constants.H5P_DATASET_CREATE);
	    H5.H5Pset_chunk (diary_cparms, 1, chunkDims);
	    diary_dataset_id = H5.H5Dcreate(file_id, "/All_Data/SPACECRAFT-DIARY-RDR_All/RawApplicationPackets_0", HDF5Constants.H5T_STD_U8BE, diary_dataspace_id, diary_cparms);
	
		//System.out.println("openHDF5 -- created ids? " + H5.getOpenIDCount());

	   ++opened;
	}
	

	public int writeSomeHDF5(byte[] buf, int bufSize) throws NullPointerException, HDF5Exception {

		//System.out.println("writeSomeHDF5 -- ids? " + H5.getOpenIDCount());

		if ((bufSize + offset[0]) > more[0]) {
			
			//more[0] = bufSize + more[0];
			more[0] = bufSize + offset[0];

			//cout << "Asking for total: " << more[0] << endl;

			H5.H5Dextend (dataset_id, more);
			
		}

		int space_id = H5.H5Dget_space (dataset_id);

		dims[0] = bufSize;
	   	
		H5.H5Sselect_hyperslab (space_id, HDF5Constants.H5S_SELECT_SET, offset, null, dims, null);  

	    int dataspace = H5.H5Screate_simple (1, dims, null); 

		H5.H5Dwrite (dataset_id, HDF5Constants.H5T_STD_U8BE, dataspace, space_id, HDF5Constants.H5P_DEFAULT, buf, true);
		
		H5.H5Sclose(space_id);
		H5.H5Sclose(dataspace);

		offset[0] += bufSize;
		
		//System.out.println("writeSomeHDF5 -- created ids? " + H5.getOpenIDCount());


		return bufSize;
	}
	

	
	public int writeSomeSpaceCraftDiaryHDF5(byte[] buf, int bufSize) throws NullPointerException, HDF5Exception {

		System.out.println("writeSomeHDF5 -- ids? " + H5.getOpenIDCount());

		if ((bufSize + diary_offset [0]) > diary_more [0]) {
			
			//more[0] = bufSize + more[0];
			diary_more[0] = bufSize + diary_offset[0];

			//cout << "Asking for total: " << more[0] << endl;

			H5.H5Dextend (diary_dataset_id, diary_more);
			
		}

		int space_id = H5.H5Dget_space (diary_dataset_id);

		diary_dims[0] = bufSize;
	   	
		H5.H5Sselect_hyperslab (space_id, HDF5Constants.H5S_SELECT_SET, diary_offset, null, diary_dims, null);  

	    int dataspace = H5.H5Screate_simple (1, diary_dims, null); 

		H5.H5Dwrite (diary_dataset_id, HDF5Constants.H5T_STD_U8BE, dataspace, space_id, HDF5Constants.H5P_DEFAULT, buf, true);
		
		H5.H5Sclose(space_id);
		H5.H5Sclose(dataspace);

		diary_offset[0] += bufSize;
		
		//System.out.println("writeSomeHDF5 -- created ids? " + H5.getOpenIDCount());


		return bufSize;
	}
	
	
	public void closeHDF5() throws NullPointerException, HDF5Exception {
		
		//System.out.println("closeHDF5 -- ids? " + H5.getOpenIDCount());
		
	    H5.H5Dclose(diary_dataset_id);
	    H5.H5Sclose(diary_dataspace_id);
	    H5.H5Pclose(diary_cparms);
	    
		H5.H5Dclose(dataset_id);
		
		//System.out.println("          -- done step a");

		H5.H5Dclose(dataset_id2);
		
		//System.out.println("          -- done step b");
		
		H5.H5Dclose(dataset_id3);

		//System.out.println("          -- done step c");

		H5.H5Sclose(dataspace_id);
		
		//System.out.println("          -- done step d");

		H5.H5Sclose(dataspace_id2);
		
		//System.out.println("          -- done step e");

		H5.H5Fclose(file_id);
		
		//System.out.println("          -- done step f");

		H5.H5Gclose(g1);
		
		//System.out.println("          -- done step g");

		H5.H5Gclose(g2);
		
		H5.H5Gclose(g3);
		
		//System.out.println("          -- done step h");
		
	    H5.H5Pclose(cparms);

		
		if (--opened==0) {
			//System.out.println("          -- done step i");

			hdfCleanup();
		}
		
		//System.out.println("CloseHDF5 -- end -- open ids? "  + H5.getOpenIDCount());
		
		

	}
	
	// FIXME -- this works but is a heavy... for some reason not all descriptors are closed in the code, this does it with sledgehammer
	private void hdfCleanup() {
		int descriptors = H5.getOpenIDCount();
		int status;
		
		for (int i = 0; i < descriptors; i++) {
			int descriptor = H5.getOpenID(i);
			try {
				status = H5.H5Aclose(descriptor);
			} catch (HDF5LibraryException e) {
				
			}
			try {
				status = H5.H5Dclose(descriptor);
			} catch (HDF5LibraryException e) {
				
			}
			try {
				status = H5.H5Fclose(descriptor);
			} catch (HDF5LibraryException e) {
				
			}
			try {
				status = H5.H5Gclose(descriptor);
			} catch (HDF5LibraryException e) {
				
			}
			try {
				status = H5.H5Pclose(descriptor);
			} catch (HDF5LibraryException e) {
				
			}
			try {
				status = H5.H5Sclose(descriptor);
			} catch (HDF5LibraryException e) {
				
			}
			try {
				status = H5.H5Tclose(descriptor);
			} catch (HDF5LibraryException e) {
				
			}
			
		}
		
		try {
			status = H5.H5close();
		} catch (HDF5LibraryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void writeMetaDataHDF5(HDF5Stats hdfStats) throws NullPointerException, HDF5Exception {
		
		SimpleDateFormat dfDate = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat dfTime = new SimpleDateFormat("HHmmss.ssssssZ");
		SimpleDateFormat dfCreation = new SimpleDateFormat("yyyyMMddHHmmss");

		//System.out.println("writeMetaDataHDF5 -- ids? " + H5.getOpenIDCount());

		
		// creation date times...
		Date creationDateTime = new Date();
		
		String creationDateStr = dfDate.format(creationDateTime);
		String creationTimeStr = dfTime.format(creationDateTime);
	
		
		long     dims[] = new long[2];
		long adim[] = {1, 1};
		long a2dim[] = {1, 1};

		//The Data_Products
		int g3 = H5.H5Gcreate(file_id, "/Data_Products", 0);
		
		//System.out.println("writeMetaDataHDF5 -- a -- ids? " + H5.getOpenIDCount());

		
		int g4 = H5.H5Gcreate(file_id, "/Data_Products/VIIRS-SCIENCE-RDR", 0);
		
		//System.out.println("writeMetaDataHDF5 -- b -- ids? " + H5.getOpenIDCount());

		//group_id = H5Gcreate(file_id, "/Data_Products/VIIRS-SCIENCE-RDR/VIIRS-SCIENCE-RDR_Aggr", 0);
		//group_id = H5Gcreate(file_id, "/Data_Products/VIIRS-SCIENCE-RDR/VIIRS-SCIENCE-RDR_Gran_0", 0);
		dims[0]=1;
		dataspace_id2 = H5.H5Screate_simple(1, dims, null);
		
		//System.out.println("writeMetaDataHDF5 -- c -- ids? " + H5.getOpenIDCount());

		dataset_id2 = H5.H5Dcreate( file_id,
				"/Data_Products/VIIRS-SCIENCE-RDR/VIIRS-SCIENCE-RDR_Gran_0",
				HDF5Constants.H5T_STD_REF_OBJ,
				dataspace_id2,
				HDF5Constants.H5P_DEFAULT );
		
		//System.out.println("writeMetaDataHDF5 -- d -- ids? " + H5.getOpenIDCount());


		byte[] wbuf  = H5.H5Rcreate(file_id,"/All_Data/VIIRS-SCIENCE-RDR_All/RawApplicationPackets_0",HDF5Constants.H5R_OBJECT,-1);
		
		//System.out.println("writeMetaDataHDF5 -- e -- ids? " + H5.getOpenIDCount());

		
		//System.out.println("wbuf length: " + wbuf.length);
		H5.H5Dwrite(dataset_id2,
				HDF5Constants.H5T_STD_REF_OBJ,
				HDF5Constants.H5S_ALL,
				HDF5Constants.H5S_ALL,
				HDF5Constants.H5P_DEFAULT,
				wbuf,
				false);

		//System.out.println("writeMetaDataHDF5 -- f -- ids? " + H5.getOpenIDCount());

		
		CDSPacketTime firstPktTime = new CDSPacketTime(hdfStats.firstPacketTime);
		CDSPacketTime lastPktTime = new CDSPacketTime(hdfStats.lastPacketTime);
		Date fptDateTime = firstPktTime.getDate();
		Date lptDateTime = lastPktTime.getDate();
		IETTime fptIETTime = new IETTime(firstPktTime);
		IETTime lptIETTime = new IETTime(lastPktTime); 
		
		//System.out.println("Date first: " + fptDateTime.toString() + " Date last: " + lptDateTime.toString());
		
		String begDateStr = dfDate.format(fptDateTime);
		String begTimeStr = dfTime.format(fptDateTime);

		String endDateStr = dfDate.format(lptDateTime);
		String endTimeStr = dfTime.format(lptDateTime);

		
		writeStringAttribute("Beginning_Date",begDateStr,2, adim,dataset_id2);
		writeStringAttribute("Beginning_Time",begTimeStr,2, adim,dataset_id2);
		writeStringAttribute("Ending_Date",endDateStr,2, adim,dataset_id2);
		writeStringAttribute("Ending_Time",endTimeStr,2, adim,dataset_id2);
		long temp=9999;
		writeLongAttribute("N_Beginning_Orbit_Number",temp,2, adim,dataset_id2);
		writeLongLongAttribute("N_Beginning_Time_IET",fptIETTime.getTime(),2, adim,dataset_id2);
		writeStringAttribute("Creation_Date",creationDateStr,2,adim,dataset_id2);
		writeStringAttribute("Creation_Time",creationTimeStr,2,adim,dataset_id2);
		writeLongLongAttribute("N_Ending_Time_IET",lptIETTime.getTime(),2,adim,dataset_id2);
		writeStringAttribute("N_Granule_ID","NoValue",2,adim,dataset_id2);
		writeStringAttribute("N_Granule_Version","A1",2,adim,dataset_id2);
		writeStringAttribute("N_LEOA_Flag","Off",2,adim,dataset_id2);
		writeStringAttribute("N_NPOESS_Document_Ref","DCO_B1_D34862-05%20CDFCB-X_Vol_5_ECR_549C.pdf",2,adim,dataset_id2);
		writeStringArrAttribute("N_Packet_Type","XXX",a2dim,3, dataset_id2);
		writeLongArrAttribute("N_Packet_Type_Count",hdfStats.packetCount,a2dim,dataset_id2);
		writeFloatAttribute("N_Percent_Missing_Data", 9999.0f, 2, adim, dataset_id2);
		writeStringAttribute("N_Reference_ID","NoValue",2,adim,dataset_id2);

		//System.out.println("writeMetaDataHDF5 -- g -- ids? " + H5.getOpenIDCount());

		dataset_id3 = H5.H5Dcreate( file_id,"/Data_Products/VIIRS-SCIENCE-RDR/VIIRS-SCIENCE-RDR_Aggr",HDF5Constants.H5T_STD_REF_OBJ,dataspace_id2,HDF5Constants.H5P_DEFAULT );
		
		//System.out.println("writeMetaDataHDF5 -- h -- ids? " + H5.getOpenIDCount());
		
		wbuf = H5.H5Rcreate(file_id,"/All_Data/VIIRS-SCIENCE-RDR_All",HDF5Constants.H5R_OBJECT,-1);
		
		//System.out.println("writeMetaDataHDF5 -- i -- ids? " + H5.getOpenIDCount());
		
		H5.H5Dwrite(dataset_id3,HDF5Constants.H5T_STD_REF_OBJ,HDF5Constants.H5S_ALL,HDF5Constants.H5S_ALL,HDF5Constants.H5P_DEFAULT,wbuf);
		writeStringAttribute("AggregateBeginningDate",begDateStr,2,adim,dataset_id3);
		writeStringAttribute("AggregateBeginningGranuleID","NoValue",2,adim,dataset_id3);
		writeLongAttribute("AggregateBeginningOrbitNumber",temp,2,adim,dataset_id3);
		writeStringAttribute("AggregateBeginningTime",begTimeStr,2,adim,dataset_id3);
		writeStringAttribute("AggregateEndingDate",endDateStr,2,adim,dataset_id3);
		writeStringAttribute("AggregateEndingGranuleID","NoValue",2,adim,dataset_id3);
		writeLongAttribute("AggregateEndingOrbitNumber",temp,2,adim,dataset_id3);
		writeStringAttribute("AggregateEndingTime",endTimeStr,2,adim,dataset_id3);
		int tempint=1;
		writeIntAttribute("AggregateNumberGranules",tempint,2,adim,dataset_id3);
		
		//System.out.println("writeMetaDataHDF5 -- j -- ids? " + H5.getOpenIDCount());

		H5.H5Gclose(g3);
		H5.H5Gclose(g4);
		
		//System.out.println("writeMetaDataHDF5 -- end -- open ids? "  + H5.getOpenIDCount());

	}
	private int writeStringAttribute(String attrName, String attrValue,int rank, long adim[],int dataset_id) throws NullPointerException, HDF5Exception
	{
	   int aid, atype,attr, dsi;
	   int status;
		//System.out.println("--> writeStringAttribute -- ids? " + H5.getOpenIDCount());

	   aid  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
	   dsi=H5.H5Sset_extent_simple(aid,2, adim, null);
	   atype = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
	   H5.H5Tset_size(atype, attrValue.length());
	   attr = H5.H5Acreate(dataset_id, attrName, atype, aid,HDF5Constants.H5P_DEFAULT);
	   status = H5.H5Awrite(attr, atype, attrValue.getBytes()); 
	   
	   H5.H5Tclose(atype);
	   H5.H5Sclose(dsi);
	   status = H5.H5Sclose(aid); 
	   status = H5.H5Aclose(attr); 
	   
		//System.out.println("--> writeStringAttribute -- close ids? " + H5.getOpenIDCount());

	   return(status);
	}

	private int  writeStringArrAttribute(String attrName,String attrValue, long adim[],int size, int dataset_id) throws NullPointerException, HDF5Exception
	{
	   int aid, atype,attr, dsi;
	   int status;

		//System.out.println("--> writeStringArrAttribute -- ids? " + H5.getOpenIDCount());

	   aid  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
	   dsi=H5.H5Sset_extent_simple(aid,2, adim, null);
	   atype = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
	   H5.H5Tset_size(atype, size); // length of packet type character code... 3 chars
	   attr = H5.H5Acreate(dataset_id, attrName, atype, aid,HDF5Constants.H5P_DEFAULT);
	   status = H5.H5Awrite(attr, atype, attrValue.getBytes()); 
	   
	   H5.H5Tclose(atype);
	   H5.H5Sclose(dsi);
	   status = H5.H5Sclose(aid); 
	   status = H5.H5Aclose(attr); 
	   
		//System.out.println("--> writeStringArrAttribute -- close ids? " + H5.getOpenIDCount());
	   
	   
	   return(status);
	}

	private int  writeLongAttribute(String attrName,long attrValue,int rank, long adim[],int dataset_id) throws NullPointerException, HDF5Exception
	{
	   int aid,attr,dsi;
	   int status;

		//System.out.println("--> writeLongAttribute -- ids? " + H5.getOpenIDCount());

		
	   long[] wrappedValue = new long[1];
	   wrappedValue[0] = attrValue;
	   
	   aid  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
	   dsi=H5.H5Sset_extent_simple(aid,2, adim, null);
	   attr = H5.H5Acreate(dataset_id, attrName, HDF5Constants.H5T_NATIVE_ULONG, aid,HDF5Constants.H5P_DEFAULT);
	   status = H5.H5Awrite(attr, HDF5Constants.H5T_NATIVE_ULONG, wrappedValue); 
	   
	   H5.H5Sclose(dsi);
	   status = H5.H5Sclose(aid); 
	   status = H5.H5Aclose(attr); 
	   
		//System.out.println("--> writeLongAttribute -- close ids? " + H5.getOpenIDCount());

	   return(status);
	}
	
	private int  writeLongLongAttribute(String attrName,long attrValue,int rank, long adim[],int dataset_id) throws NullPointerException, HDF5Exception
	{
	   int aid,attr,dsi;
	   int status;

		//System.out.println("--> writeLongAttribute -- ids? " + H5.getOpenIDCount());

		
	   long[] wrappedValue = new long[1];
	   wrappedValue[0] = attrValue;
	   
	   aid  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
	   dsi=H5.H5Sset_extent_simple(aid,2, adim, null);
	   attr = H5.H5Acreate(dataset_id, attrName, HDF5Constants.H5T_NATIVE_ULLONG, aid,HDF5Constants.H5P_DEFAULT);
	   status = H5.H5Awrite(attr, HDF5Constants.H5T_NATIVE_ULLONG, wrappedValue); 
	   
	   H5.H5Sclose(dsi);
	   status = H5.H5Sclose(aid); 
	   status = H5.H5Aclose(attr); 
	   
		//System.out.println("--> writeLongAttribute -- close ids? " + H5.getOpenIDCount());

	   return(status);
	}
	
	private int  writeLongArrAttribute(String attrName,int attrValue, long adim[],int dataset_id) throws NullPointerException, HDF5Exception
	{
	   int aid,attr, dsi;
	   int status;
		//System.out.println("--> writeLongArrAttribute -- start -- ids? " + H5.getOpenIDCount());

	   aid  = H5.H5Screate(HDF5Constants.H5S_SIMPLE);
	   
		//System.out.println("----> writeLongArrAttribute -- a -- ids? " + H5.getOpenIDCount());

	   dsi=H5.H5Sset_extent_simple(aid,2, adim, null);
		//System.out.println("----> writeLongArrAttribute -- b -- ids? " + H5.getOpenIDCount());

	   attr = H5.H5Acreate(dataset_id, attrName, HDF5Constants.H5T_NATIVE_ULONG, aid,HDF5Constants.H5P_DEFAULT);
	   
	   long[] foo = new long[1];
	   foo[0] = attrValue;
	   status = H5.H5Awrite(attr, HDF5Constants.H5T_NATIVE_ULONG, foo); 
	   
		//System.out.println("----> writeLongArrAttribute -- c -- ids? " + H5.getOpenIDCount());

	   status = H5.H5Sclose(dsi);
	   
		//System.out.println("----> writeLongArrAttribute -- e -- ids? " + H5.getOpenIDCount() + " status=" + status);

	   status = H5.H5Aclose(attr); 
	   
		//System.out.println("----> writeLongArrAttribute -- f -- ids? " + H5.getOpenIDCount());

		
	   status = H5.H5Sclose(aid); 
	   //status = H5.H5Aclose(attr); 
	   
		//System.out.println("--> writeLongArrAttribute -- close ids? " + H5.getOpenIDCount());

	   return(status);
	}
	
	private int  writeIntAttribute(String attrName,int attrValue,int rank, long adim[],int dataset_id) throws NullPointerException, HDF5Exception
	{
	   int aid,attr, dsi;
	   int status;

		//System.out.println("--> writeIntAttribute -- ids? " + H5.getOpenIDCount());

	   long[] wrappedValue = new long[1];
	   wrappedValue[0] = attrValue;
	   
	   aid  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
	   dsi=H5.H5Sset_extent_simple(aid,2, adim, null);
	   attr = H5.H5Acreate(dataset_id, attrName, HDF5Constants.H5T_NATIVE_INT, aid,HDF5Constants.H5P_DEFAULT);
	   status = H5.H5Awrite(attr, HDF5Constants.H5T_NATIVE_INT, wrappedValue); 
	   
	   H5.H5Sclose(dsi);
	   status = H5.H5Sclose(aid); 
	   status = H5.H5Aclose(attr); 
	   
		//System.out.println("--> writeIntAttribute -- close ids? " + H5.getOpenIDCount());

	   return(status);
	}
	
	private int writeFloatAttribute(String attrName, float attrValue, int rank, long adim[],int dataset_id) throws NullPointerException, HDF5Exception
	{
	   int aid,attr, dsi;
	   int status;
	   
		//System.out.println("--> writeFloatAttribute -- ids? " + H5.getOpenIDCount());

	   float[] wrappedValue = new float[1];
	   wrappedValue[0] = attrValue;

	   aid  = H5.H5Screate(HDF5Constants.H5S_SCALAR);
	   dsi=H5.H5Sset_extent_simple(aid,2, adim, null);
	   attr = H5.H5Acreate(dataset_id, attrName, HDF5Constants.H5T_NATIVE_FLOAT, aid,HDF5Constants.H5P_DEFAULT);
	   status = H5.H5Awrite(attr, HDF5Constants.H5T_NATIVE_FLOAT, wrappedValue); 
	   
	   H5.H5Sclose(dsi);
	   status = H5.H5Sclose(aid); 
	   status = H5.H5Aclose(attr); 
	   
		//System.out.println("--> writeFloatAttribute -- close ids? " + H5.getOpenIDCount());

	   return(status);
	}
	
}
