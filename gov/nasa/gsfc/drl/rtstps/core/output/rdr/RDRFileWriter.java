/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;



import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/*
* This is the class that handles the proper creation/renaming of the actual
* HDF file generated from all of the CCSDS packets it receives.
*
* It uses an instance of the RDRCreateFile class to fill the contents of the
* HDF file according to standard JPSS RDR file format
*/
public class RDRFileWriter {
	
	private String tempFilename;
	private String destPath;
	
	// private List<RDRProduct> rdrProducts = new LinkedList<RDRProduct>();
	private LinkedList<RDRCreateFile> rdrFiles = new LinkedList<RDRCreateFile>();

	// Primary file handle for current granule
	private RDRCreateFile rdrFile;
	// Secondary file handle for previous granule
	private RDRCreateFile rdrNext = null;

	// Specified granule time span for an RDR file
	private long granuleTimeSpan = 0L;	

	private Stats stats = null;
	private int orbit;
	private int rdrCount;
	private Origin distributor;
	private MissionName missionName;
	private Origin datasetSource;
	private PlatformShortName platformShortname;
	private boolean multiMode = false;

	public RDRFileWriter(String destPath, 
			int orbit, 
			int rdrCount, 
			Origin distributor, 
			MissionName missionName, 
			Origin datasetSource, 
			PlatformShortName platformShortname) throws RtStpsException {

		this.distributor = distributor;
		this.missionName = missionName;
		this.datasetSource = datasetSource;
		this.platformShortname = platformShortname;
		this.orbit = orbit;
		this.rdrCount = rdrCount;
		this.destPath = destPath;

		// Generate a random temp. filename for this HDF file...
		tempFilename = UUID.randomUUID().toString() + ".tmp";
		
		VIIRSGranule.setBaseTimeSet(false);
		rdrFile = new RDRCreateFile(destPath, tempFilename, rdrCount, granuleTimeSpan);
	}
	
	public RDRFileWriter(Stats stats, 
			String destPath, 
			int orbit, 
			int rdrCount, 
			Origin distributor, 
			MissionName missionName, 
			Origin datasetSource, 
			PlatformShortName platformShortname,
			long gtspan ) throws RtStpsException {
		
		this.stats = stats;
		this.distributor = distributor;
		this.missionName = missionName;
		this.datasetSource = datasetSource;
		this.platformShortname = platformShortname;
		this.orbit = orbit;
		this.rdrCount = rdrCount;
		this.destPath = destPath;
		this.granuleTimeSpan = gtspan;

		// Generate a random temp. filename for this HDF file...
		tempFilename = UUID.randomUUID().toString() + ".tmp";

		VIIRSGranule.setBaseTimeSet(false);
		rdrFile = new RDRCreateFile(stats, destPath, tempFilename, rdrCount, granuleTimeSpan);
	}

	// Sets this class's multiMode boolean
	public void setMultiMode(boolean blv){
		this.multiMode = blv;
	}

	// Put a CCSDS packet into this RDRFileWriter, which basically just calls the RDRCreateFile
	// object's put(packet p) function...
	public void put(Packet p) throws RtStpsException {
		if(this.multiMode){
			// Put the packet in the granule's RDR file; returns false and does NOT put the packet
			// if granule is full!
			int toCurrentRDR = 0;
			toCurrentRDR = rdrFile.checkThenPut(p);
			
			//********** Packet was not put in the RDR due to certain conditions. Two possible scenarios... **********
			if(toCurrentRDR > 0){
				// 1. Whole RDR is complete. Do all necessary steps for finalization/cleanup:
				if(rdrFile.isComplete()){

					// Get the co-temporal SpacecraftAOS Diary granule
					// FIXME What if the last spacecraft diary granule's end directly coincides with the
					//       science granule's end boundary? Then it's not exactly "co-temporal", is it? Should
					//       we copy it over to the next RDR in this case? Hmmm...
					// FIXME This is probably not a big issue with VIIRS, but what if other instruments are
					//       to be granulated as well? For example, ATMS and CrIS are about 30 seconds, whereas SD is
					//       20 seconds.
					RawApplicationPackets sd_overlap = rdrFile.getCurrentSpacecraftDiaryRAP();
					RDRCreateFile temprdr = rdrFile;

					// If there is a next RDR open, make it the current RDR. Else, create new RDR which will get any
					// co-temporal SpacecraftAOS Diary granules (rare scenario).
					if(this.rdrNext != null){
						rdrFile = rdrNext;
						rdrNext = null;
					}
					else{
						tempFilename = UUID.randomUUID().toString() + ".tmp";
						VIIRSGranule.setBaseTimeSet(false);
						if(this.stats != null)
							this.rdrFile = new RDRCreateFile(stats, destPath, tempFilename, rdrCount, granuleTimeSpan);
						else
							this.rdrFile = new RDRCreateFile(destPath, tempFilename, rdrCount, granuleTimeSpan);
					}
					
					// Update current destPath and tempFilename
					this.destPath = rdrFile.getDestPath();
					this.tempFilename = rdrFile.getFilename();

					// If co-temporal spacecraft diary granule is not null, transfer it to the new RDR file
					if(sd_overlap != null){
						this.rdrFile.createTransitioningSpacecraftDiaryRDR(sd_overlap);
					}
					else{
						System.out.println("Overlapping SpacecraftAOS Diary granule is null. Not writing.");
					}
				
					// Determine creation date for the full granule
					//Date creationDateAndTime = new Date();

					// Queue the old file, and put the new packet into the new file
					temprdr.flushToDisk();
					rdrFiles.push(temprdr);
					rdrFile.checkThenPut(p);
				}
				// 2. Current RDR is not yet complete; create a new RDR file if it doesn't already exist, and put packet there
				else {
					// Create next RDR file, if it doesn't exist yet
					if(this.rdrNext == null){
						String tfname = UUID.randomUUID().toString() + ".tmp";
						VIIRSGranule.setBaseTimeSet(false);
						if(this.stats != null){
							this.rdrNext = new RDRCreateFile(stats, destPath, tfname, rdrCount, granuleTimeSpan);
						}
						else{
							this.rdrNext = new RDRCreateFile(destPath, tempFilename, rdrCount, granuleTimeSpan);
						}
					}

					// If signal received is SPACECRAFT_DIARIES_COMPLETE, this packet is an SD packet that 
					// already exceeds current RDR's SD encapsulation. Hence, copy the co-temporal SD granule.
					if( toCurrentRDR == RDRCreateFile.SPACECRAFT_DIARIES_COMPLETE ){
						// Get the co-temporal SpacecraftAOS Diary granule
						// FIXME What if the last spacecraft diary granule's end directly coincides with the
						//       science granule's end boundary? Then it's not exactly "co-temporal", is it? Should
						//       we copy it over to the next RDR in this case? Hmmm...
						// FIXME This is probably not a big issue with VIIRS, but what if other instruments are
						//       to be granulated as well? For example, ATMS and CrIS are about 30 seconds, whereas SD is
						//       20 seconds.
						RawApplicationPackets sd_overlap = rdrFile.getCurrentSpacecraftDiaryRAP();
				
						// If co-temporal spacecraft diary granule is not null, transfer it to the new RDR file
						if(sd_overlap != null){
							this.rdrNext.createTransitioningSpacecraftDiaryRDR(sd_overlap);
						}
						else{
							System.out.println("Co-temporal SpacecraftAOS Diary granule is null. Not writing.");
						}
					}
					// Else, it's science data that exceeds the current RDR's science data span. No need to do anything;
					// just let it be put in rdrNext.					

					// Once all's said and done, put packet to the next RDR
					rdrNext.checkThenPut(p);
				}
				
			}
			//********** Else, packet is successfully written to current granule. No need to do anything. **********
		}
		else
			rdrFile.put(p);
	}

	public void close(boolean hdfCleanup) throws RtStpsException {
		
		if(this.multiMode){
			Date creationDateAndTime = new Date();
			
			// rdrNext may not have co-temporal SpacecraftDiary RDR upon close. 
			// Make sure all co-temporal SpacecraftAOS Diary granules are copied before it is closed!
			if( rdrNext != null ){
				if(!rdrNext.hasSpacecraftDiary()){
					System.out.println("Copying last co-temporal SpacecraftAOS Diary granule to final RDR file");
					RawApplicationPackets sd_overlap = rdrFile.getCurrentSpacecraftDiaryRAP();
					rdrNext.createTransitioningSpacecraftDiaryRDR(sd_overlap);
				}
			}

			// Close all RDR files, before entire HDF5 library is closed and flushed
			closeAll(creationDateAndTime);

			// No more need for HDF5 library, so flush it and free up all resources used
			if(hdfCleanup){
				HDF5Util.cleanup();
			}

			// Once HDF5 library is closed and freed up, rename RDR files to proper filenames
			finalizeAll(creationDateAndTime);
		}
		else {
			// Get the system's current date and time...
			Date creationDateAndTime = new Date();
			// the top level attributes
			writeAttributes(creationDateAndTime);
			// close the rdr file...
			rdrFile.close();
			// clean up any leftover housekeeping
			if (hdfCleanup) {
				HDF5Util.cleanup();
			}
			// rename to the final file name; using the most current date/time for
			// the creation timestamp.
			rename(creationDateAndTime);
		}
	}

	private void closeAll(Date creationDateAndTime) throws RtStpsException{
		// Close backlog of RDR files
		for( RDRCreateFile rdrf : rdrFiles ){
			if( rdrf != null )
				finishFile(rdrf, creationDateAndTime);
		}

		// Close current RDR file, if possible
		if( rdrFile != null )
			finishFile(rdrFile, creationDateAndTime);

		// Close next RDR file, if possible
		if( rdrNext != null )
			finishFile(rdrNext, creationDateAndTime);
	}
	
	private void finalizeAll(Date creationDateAndTime) throws RtStpsException{
		// Finalize backlog of RDR files
		for( RDRCreateFile rdrf : rdrFiles ){
			if( rdrf != null )
				rename(creationDateAndTime, rdrf);
		}

		// Finalize current RDR file, if possible
		if( rdrFile != null )
			rename(creationDateAndTime, rdrFile);

		// Finalize next RDR file, if possible
		if( rdrNext != null )
			rename(creationDateAndTime, rdrNext);
	}

	// Performs the required cleanup on an RDR file
	private void finishFile(RDRCreateFile target, Date creationDateAndTime) throws RtStpsException{
		if(target != null){
			// Write attributes to the RDR file
			try{
				writeAttributes(creationDateAndTime, target);
			}
			catch (RtStpsException e){
				System.out.println("finishFile(): Could not write attributes; skipping file...");				
			}
			// Now close the RDRCreateFile. This closes all "RDR" objects, which then
			// closes all RAPs, HDF5 groups/datasets, etc, and frees up resources
			try{
				target.close();
			}
			catch (RtStpsException e){
				// Regular close failed for some reason. Force close and free up all resources!
				target.closeAllRaps();
				System.out.println("finishFile(): RDR file force closed!");
			}
		}
	}

	private void writeAttributes(Date creationDateAndTime) throws RtStpsException {
		rdrFile.writeAttributes(creationDateAndTime, distributor, this.missionName, this.datasetSource, platformShortname);
		System.out.println("writeAttributes completed");
	}

	private void writeAttributes(Date creationDateAndTime, RDRCreateFile target) throws RtStpsException {
		target.writeAttributes(creationDateAndTime, distributor, this.missionName, this.datasetSource, platformShortname);
		System.out.println("writeAttributes completed");
	}

	// Renames the HDF file to its final filename, with the creation timestamp 
	// provided as an argument (the "_c...." field):
	private void rename(Date creationDateAndTime) throws RtStpsException {
		if( rdrFile == null )
			return;		

		// Grab the current HDF file, which should still have its temp filename
		File old = new File(destPath, tempFilename);
		// We get a list of all the XXX-RDR groups; RDR groups should have all info we need to
		// identify the instrument (for the filename)
		List<RDR> rdrs = rdrFile.getRDRs();

		// PDSDate d1 = rdrFile.getFirstDateTime();
		// PDSDate d2 = rdrFile.getLastDataTime();
		
		// Check for null aggregates.
		// If any are null, we won't be able to write the RDR
		// as it checks aggregates for timestamps
		for(int i = 0; i < rdrs.size(); i++) {
			// Check the Aggregates of each RDR handled by this RDRFileWriter
			// E.g. if it only handles VIIRS-SCIENCE-RDR, then only VIIRS-SCIENCE-RDR_Aggr is checked
			if(rdrs.get(i).getAggregate() == null)
				rdrs.remove(i);
		}
		
		// look for RDRs that only contain NPP ephemeris data
		// if one is found, do not rename file, and delete old temp file handle
		if ((rdrs.size() == 1) && (rdrs.get(0).getRDRName() == RDRName.NPP_Ephemeris_and_Attitude)){
			System.out.println("No science data in RDR, cannot write file " + tempFilename);
			old.delete();
			return;
		}
		
		Aggregate firstAgg = findFirstScienceWeightedAggregate(rdrs);
		PDSDate temp1 = null; 
		PDSDate d1 = null;
		PDSDate d2 = null;

		// we can get this far and still have issues with no science RDRs
		// so we check for RDRs with only a diary aggregate
		
		/*
		if (firstAgg.getName().equals("SPACECRAFT-DIARY-RDR_Aggr"))
		{
			System.out.println("No science data in RDR, cannot write file.");
			old.delete();
			return;
		}
		*/
		
		// LPEATE seems to want the original data based times not the rounded/truncated times in the file name
		try { 
			temp1 = firstAgg.getBeginningDateTime();
			d1 = new PDSDate(temp1.getOriginalPacketTime());
			d2 = firstAgg.getEndingDateTime();	
		} 
		// FILE can't be written due to no/null aggregate; delete it.
		catch (NullPointerException e){	
			System.out.println("Not enough data, cannot write file " + tempFilename);
			// Take out file handle
			old.delete();
			return;
		}
		
		//PDSDate temp2 = firstAgg.getEndingDateTime();
		//PDSDate d2 = new PDSDate(temp2.getOriginalPacketTime());
		
		//PDSDate d1 = firstAgg.getBeginningDateTime();
		
		// But this one is offset from it and so is not really a LPEATE or LeapDate
		// it's the same if you get the original time or not in other words.
		// That means we have to substract the leap time from it.
		//PDSDate d2 = firstAgg.getEndingDateTime();
		
		if (rdrs == null) return;
		if (rdrs.size() <= 0) return;
		if ((d1 == null) || (d2 == null)) return;
		
		// Create an NPOESSFilename object with the obtained information so far; this
		// will assemble the proper RDR filename for us (with correct instrument/timestamps)
		NPOESSFilename nfn = new NPOESSFilename(rdrs, d1, d2, creationDateAndTime,
							SpacecraftId.npp, orbit, Origin.all);
		
		// Finally, rename the temp file to its proper filename and create a new user block
		createUserBlock(old);
		String realFilename = nfn.toString();
		File nnew = new File(destPath, realFilename);
		old.renameTo(nnew);
		//createUserBlock(nnew);
	}

	private void rename(Date creationDateAndTime, RDRCreateFile target) throws RtStpsException{
		if( target == null )
			return;		

		// Grab the current HDF file, which should still have its temp filename
		File old = new File(target.getDestPath(), target.getFilename());
		// We get a list of all the XXX-RDR groups; RDR groups should have all info we need to
		// identify the instrument (for the filename)
		List<RDR> rdrs = target.getRDRs();

		// Check for null aggregates.
		// If any are null, we won't be able to write the RDR
		// as it checks aggregates for timestamps
		for(int i = 0; i < rdrs.size(); i++) {
			// Check the Aggregates of each RDR handled by this RDRFileWriter
			// E.g. if it only handles VIIRS-SCIENCE-RDR, then only VIIRS-SCIENCE-RDR_Aggr is checked
			if(rdrs.get(i).getAggregate() == null)
				rdrs.remove(i);
		}
		
		// look for RDRs that only contain NPP ephemeris data
		// if one is found, do not rename file, and delete old temp file handle
		if ((rdrs.size() == 1) && (rdrs.get(0).getRDRName() == RDRName.NPP_Ephemeris_and_Attitude)){
			System.out.println("No science data in RDR, cannot write file " + tempFilename);
			old.delete();
			return;
		}
		
		Aggregate firstAgg = findFirstScienceWeightedAggregate(rdrs);
		PDSDate temp1 = null; 
		PDSDate d1 = null;
		PDSDate d2 = null;
		
		// LPEATE seems to want the original data based times not the rounded/truncated times in the file name
		try { 
			temp1 = firstAgg.getBeginningDateTime();
			d1 = new PDSDate(temp1.getOriginalPacketTime());
			d2 = firstAgg.getEndingDateTime();	
		} 
		// FILE can't be written due to no/null aggregate; delete it.
		catch (NullPointerException e){	
			System.out.println("Not enough data, cannot write file " + tempFilename);
			old.delete();
			return;
		}
		
		if (rdrs == null) return;
		if (rdrs.size() <= 0) return;
		if ((d1 == null) || (d2 == null)) return;
		
		// Create an NPOESSFilename object with the obtained information so far; this
		// will assemble the proper RDR filename for us (with correct instrument/timestamps)
		NPOESSFilename nfn = new NPOESSFilename(rdrs, d1, d2, creationDateAndTime,
							SpacecraftId.npp, orbit, Origin.all);
		
		// Finally, rename the temp file to its proper filename and create a new user block
		createUserBlock(old, target);
		String realFilename = nfn.toString();
		File nnew = new File(target.getDestPath(), realFilename);
		old.renameTo(nnew);
		//createUserBlock(nnew, target);
	}	


	// Given a List of XXX-RDR groups, it searches SCIENCE RDR groups for their XXX-RDR_Aggr (Aggregates)
	// FIXME only supports either RDRs with "SCIENCE" in the name or the DIARY
	private Aggregate findFirstScienceWeightedAggregate(List<RDR> rdrs) throws RtStpsException {
		// Initialize a List of RDRs which will only contain Science RDRs
		List<RDR> sciRDRs = new LinkedList<RDR>();
		RDR diaryRDR = null;
		// Loop through all RDR groups in the given list and only consider SCIENCE or DIARY RDRs
		for (RDR rdr : rdrs) {
			// double-check for null aggregates
			if(rdr.getAggregate() == null)
				continue;
			
			// Avoid using toString() with RDRName; instead use the specialized getRDRStringName().
			String rdrNameStr = rdr.getRDRName().getRDRStringName();

			// If SCIENCE RDR, add it to the sciRDR List
			if (rdrNameStr.contains("SCIENCE")) {
				sciRDRs.add(rdr);
			}
			// If DIARY RDR, assign it to the diaryRDR variable (in the end, it will hold the latest) 
			else if (rdrNameStr.contains("DIARY")) {
				diaryRDR = rdr;
			}
			// Else, throw an exception because this RDR is not supported. 
			else {
				throw new RtStpsException("No support for non-science/non-diary RDRs [" + rdrNameStr + "]");
			}
		}
		
		// If no Science or SpacecraftAOS Diary RDRs were found, then we cannot get the desired Aggregates...
		if ((sciRDRs.size() <= 0) && (diaryRDR == null)) {
			return null;
			//throw new RtStpsException("No science/diary RDRs found -- unable to get first/last time for filename");			
		}
	
		// If there are any XXX-SCIENCE-RDR groups, use their aggregates. Else, use Diary's.
		Aggregate firstAggregate = null;
		if (sciRDRs.size() > 0) {
			// If multiple XXX-SCIENCE-RDR groups exist, sort each group's Aggregate by 
			// beginningDateTime and return the earliest.
			firstAggregate = findFirstAggregate(sciRDRs);
		} else {
			// Otherwise, there MUST be a non-null diary right?
			firstAggregate = diaryRDR.getAggregate();
		}
		
		return firstAggregate;
	}

	// loop through RDRs and find the earliest RDR aggregate by beginningDateTime
	private Aggregate findFirstAggregate(List<RDR> rdrs) {
		Aggregate firstAgg = null;
		long firstMicros = 0L;
		
		for (RDR rdr : rdrs) {
			Aggregate agg = rdr.getAggregate();
			if(agg == null)
				continue;
			long micros = agg.getBeginningDateTime().getMicrosSinceEpoch();
			
			if (firstMicros == 0L) {
				firstMicros = micros;
				firstAgg = agg;
			} else if (micros < firstMicros) {
				firstMicros = micros;
				firstAgg = agg;
			}
		}
		return firstAgg;
	}

	// Takes a finished HDF file and writes the appropriate user block to it.
	private void createUserBlock(File file) throws RtStpsException {
		
		List<RDR> rdrs = rdrFile.getRDRs();
		
		if (rdrs == null) return;
		if (rdrs.size() <= 0) return;
		
		UserBlock userBlock = new UserBlock(MissionName.NPP, PlatformShortName.NPP, rdrs);
		userBlock.write(file);
		userBlock.close();
	}

	private void createUserBlock(File file, RDRCreateFile target) throws RtStpsException {
		
		List<RDR> rdrs = target.getRDRs();
		
		if (rdrs == null) return;
		if (rdrs.size() <= 0) return;
		
		UserBlock userBlock = new UserBlock(MissionName.NPP, PlatformShortName.NPP, rdrs);
		userBlock.write(file);
		userBlock.close();
	}

	public String toString() {
		return NPOESSFilename.productIdsToString(rdrFile.getRDRs());
	}

	/**
	* Sets the desired granule time span for RDR files
	* @param timespan Desired granule time span (in microseconds) for RDR files
	*/
	public void setTimeSpan(long timespan){
		this.granuleTimeSpan = timespan;
	}
}
