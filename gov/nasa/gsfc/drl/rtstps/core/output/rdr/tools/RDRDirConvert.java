package gov.nasa.gsfc.drl.rtstps.core.output.rdr.tools;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Convert all the RDRs found to DRL RDRs in the given directory.
 * The results are placed in the same location...
 * 
 * @author krice
 *
 */
public class RDRDirConvert {

	
	public RDRDirConvert(String directoryName) throws RtStpsException {
		File directory = new File(directoryName);
		
		FilenameFilter filter =new FilenameFilter() {
	        public boolean accept(File dir, String name)
	        {
	            return name.endsWith(".h5"); // RDR products end in h5 (hdf5)
	        }
	    };
	    
		File[] files = directory.listFiles(filter);
		
		for (int i = 0; i < files.length; i++) {
			new LpeateRDRtoRDR(files[i].getAbsolutePath(), directoryName);
		}
	}

	/**
	 * @param args the RDR HDF file
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("No input directory to process...");
			System.exit(-1);
		}
		
		try {
			new RDRDirConvert(args[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
