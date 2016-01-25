/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * Creates legal NPOESS (JPSS) filenames.  The format is to mission specification. 
 * 
 *
 */
public class NPOESSFilename {
	private static FilenameTimefieldFormat eTime = new FilenameTimefieldFormat(StartStopFieldId.e);
	private static FilenameTimefieldFormat tTime = new FilenameTimefieldFormat(StartStopFieldId.t);
	private static FileCreationDateFormat dfCreation = new FileCreationDateFormat();
	
	private List<ProductIdentifiers> productIds = new LinkedList<ProductIdentifiers>();
	private PDSDate startDateTime;
	private PDSDate stopTime;
	private Date creationDateAndTime;
	private String startDateStr;
	private String startTimeStr;
	private String stopTimeStr;
	private String creationDateTimeStr;
	private SpacecraftId spacecraftId;
	private int orbit;
	private Origin origin;
	private String orbitStr;
	private String filename;
	private DomainDescription domain;
	
	private final static String delimiter = "_";
	private final static String productNameDelimiter = "-";
	private final static DomainDescription devDomain = new DomainDescription(FixedDomainDescription.dev);
	private final static String extension = ".h5";
	private final static char creationDateCode = 'c';
	private final static char orbitCode = 'b';
	private final static char startDateCode = 'd';
	private final static char tCode = 't';
	private final static char eCode = 'e';

	private final static String usageStr = " format error -- should be 9 fields seperated by underscore: " +
										"ProductsId(s), SpaceCraftId, StartDate, StartTime, StopTime, Orbit, CreationDate, Domain" +
										" -- then followed by an " +
										extension + 
										" extension, not -->> ";
	
	
	//private static SimpleDateFormat initializeCreationFormat() {
	//	SimpleDateFormat sdf =  new SimpleDateFormat("yyyyMMddHHmmss");
	//	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	//	return sdf;
	//}
	
	
	/**
	 * Create an filename from several RDRs (sensors).  Typical would be two:  a science and spacecraft diary.
	 * However there is not technical limit.
	 * @param rdrs one or more RDRs (sensors) are in the RDR file
	 * @param startDateTime the first time of the first packet
	 * @param stopTime the last time of the last packet
	 * @param creationDateAndTime the clock time
	 * @param spacecraftId the spacecraft identifier such as NPP, etc...
	 * @param orbit the orbit of the pass the packets are from
	 * @param origin the origin of the information (Development)
	 */
	public NPOESSFilename(List<RDR> rdrs, 
							PDSDate startDateTime,
							PDSDate stopTime, 
							Date creationDateAndTime, 
							SpacecraftId spacecraftId,
							int orbit, 
							Origin origin) {
		
		
		for (RDR rdr : rdrs) {
			this.productIds.add(rdr.getProductId());
		}
		commonInit(startDateTime, stopTime, creationDateAndTime, spacecraftId, orbit, origin);
	}
	
	/**
	 * Create an NPOESS filename from a product identifier (e.g. RATMS) and PDSDates which are from the packet time stamps
	 * @param productId a product identifier (e.g. RATMS)
	 * @param startDateTime the first packet's time in {@link PDSDate} format.
	 * @param stopTime the last packet's time
	 * @param creationDateAndTime the clock time
	 * @param spacecraftId the spacecraft identifier such as NPP, etc...
	 * @param orbit the orbit of the pass the packets are from
	 * @param origin  the origin of the information (Development)
	 */
	public NPOESSFilename(ProductIdentifiers productId, 
							PDSDate startDateTime,
							PDSDate stopTime, 
							Date creationDateAndTime,
							SpacecraftId spacecraftId,
							int orbit,  
							Origin origin) {

		this.productIds.add(productId);
		commonInit(startDateTime, stopTime, creationDateAndTime, spacecraftId, orbit, origin);
	}
	
	/**
	 * Create an NPOESS filename from a product identifier (e.g. RATMS) and Dates which are likely converted from packet time stamps
	 * @param productId a product identifier (e.g. RATMS)
	 * @param startDateTime the first packet's time in {@link java.util.Date} format.
	 * @param stopTime the last packet's time
	 * @param creationDateAndTime the clock time
	 * @param spacecraftId the spacecraft identifier such as NPP, etc...
	 * @param orbit the orbit of the pass the packets are from
	 * @param origin  the origin of the information (Development)
	 */
	/***
	public NPOESSFilename(ProductIdentifiers productId, 
            				Date startDateTime,
            				Date stopTime, 
            				Date creationDateAndTime,
            				SpacecraftId spacecraftId,
            				int orbit,  
            				Origin origin) {

		this.productIds.add(productId);
		commonInit(startDateTime, stopTime, creationDateAndTime, spacecraftId, orbit, origin);
	}
	**/
	/**
	 * Create an NPOESS filename from a list of product identifiers (e.g. RATMS, RNSCA_NPP) and Dates which are likely converted from packet time stamps
	 * @param productIds a product identifier (e.g. RATMS, RNSCA_NPP)
	 * @param startDateTime the first packet's time in {@link java.util.Date} format.
	 * @param stopTime the last packet's time
	 * @param creationDateAndTime the clock time
	 * @param spacecraftId the spacecraft identifier such as NPP, etc...
	 * @param orbit the orbit of the pass the packets are from
	 * @param origin  the origin of the information (Development)
	 */
	/***
	public NPOESSFilename(List<ProductIdentifiers> productIds, 
			Date startDateTime,
			Date stopTime, 
			Date creationDateAndTime,
			SpacecraftId spacecraftId,
			int orbit,  
			Origin origin) {
		
		this.productIds.addAll(productIds);
		commonInit(startDateTime, stopTime, creationDateAndTime, spacecraftId, orbit, origin);
	}
	***/
	


	/**
	 * De-construct a string into it constituent parts that is supposed to have a valid NPOESS filename.
	 * @param filename the string containing the NPOESS filename
	 * @throws ParseException throws except for certain failures in parsing the name
	 * @throws RtStpsException 
	 */
	public NPOESSFilename(String filename) throws RtStpsException {
		this.filename = filename;
		
		// first split across the "." to separate the extension from the rest of the filename
		String[] fields = filename.split("\\.");
		
		if (fields.length != 2) {
			throw new RtStpsException("Filename -- field length[" + fields.length + "] " + usageStr + filename);
		}
		
		// then split the file name portion into its pieces...
		String[] subfields = fields[0].split(delimiter);
		
		if (subfields.length != 9) {
			throw new RtStpsException("Filename" + usageStr + filename);
		}

		// and then parse each one, or at least try to do so.
		parseProductIds(subfields[0]);
		parseSpacecraftId(subfields[1]);
		parseStartDateTime(subfields[2], subfields[3]);
		parseStopTime(subfields[4]);
		parseOrbitNumber(subfields[5]);
		parseCreationDate(subfields[6]);
		parseOrigin(subfields[7]);
		parseDomainDescription(subfields[8]);
		parseExtension(fields[1]);
	}
	

	/**
	 * Once the filename is constructed it can be retrieved as a string. Or it will return the name
	 * passed into to the constructor which de-constructs filename strings.  Calling this method
	 * recomputes the name if that's appropriate.
	 * @return the string containing the filename.
	 */
	@Override
	public String toString() {
		buildFilename();
		return filename;
	}

	
	/**
	 * Get the product identifiers associated with this filename
	 * @return a List of {@link ProductIdentifiers}
	 */
	public List<ProductIdentifiers> getProductIdentifiers() {
		return productIds;
	}
	
	/**
	 * Return the start date and time
	 * @return the start date and time as a PDSDate
	 */
	public PDSDate getStartDateTime() {
		return startDateTime;
	}
	
	/**
	 * Return the stop date and time
	 * @return the stop date and time as a PDSDate
	 */
	public PDSDate getStopTime() {
		return stopTime;
	}
	
	/**
	 * Return the clock creation date and time
	 * @return the creation date and time as a Date
	 */
	public Date getCreationDateAndTime() {
		return creationDateAndTime;
	}
	
	/**
	 * Return the spacecraft identifier
	 * @return the {@link SpacecraftId}
	 */
	public SpacecraftId getSpacecraftId() {
		return spacecraftId;
	}
	
	/**
	 * Return the orbit of the pass this file is associated with
	 * @return the orbit as an <code>int</code>
	 */
	public int getOrbit() {
		return orbit;
	}
	
	/**
	 * Return the origin of the packets associated with this file
	 * @return the {@link Origin}
	 */
	public Origin getOrigin() {
		return origin;
	}
	
	/**
	 * Return the domain of the packets associated with this file
	 * @return the {@link DomainDescription}
	 */
	public DomainDescription getDomain() {
		return domain;
	}
	/**
	 * Add a product identifier to the name after the initial constructor has been issued
	 * @param productId the {@link ProductIdentifiers} of interest
	 */
	public void addProductIdentifier(ProductIdentifiers productId) {
		this.productIds.add(productId);
	}
	
	/**
	 * Set the start date and time
	 * @param startDateTime a PDSDate containing the new date/time
	 */
	public void setStartDateTime(PDSDate startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	/**
	 * Set the stop time
	 * @param stopTime a PDSDate containing the new time (any date information is ignored)
	 */
	public void setStopTime(PDSDate stopTime) {
		this.stopTime = stopTime;
	}
	
	/**
	 * Set the creation date and time for the file
	 * @param creationDateAndTime a Date containing the new date/time
	 */
	public void setCreationDateAndTime(Date creationDateAndTime) {
		this.creationDateAndTime = creationDateAndTime;
	}
	
	/**
	 * Set the spacecraft identifier
	 * @param spacecraftId the new {@link SpacecraftId}
	 */
	public void setSpacecraftId(SpacecraftId spacecraftId) {
		this.spacecraftId = spacecraftId;
	}
	
	/**
	 * Set the orbit number
	 * @param orbit the orbit in an <code>int</code>
	 */
	public void setOrbit(int orbit) {
		this.orbit = orbit;
	}
	
	/**
	 * Set the origen
	 * @param origen the new {@link Origin}
	 */
	public void setOrigin(Origin origen) {
		this.origin = origen;
	}
	
	/**
	 * A common method for initializing the key fields in the class
	 * @param startDateTime
	 * @param stopTime
	 * @param creationDateAndTime
	 * @param spacecraftId
	 * @param orbit
	 * @param origin
	 */
	private void commonInit(PDSDate startDateTime,
							PDSDate stopTime, 
							Date creationDateAndTime,
							SpacecraftId spacecraftId,
							int orbit,  
							Origin origin) {
		this.startDateTime = startDateTime;
		this.stopTime = stopTime;
		this.creationDateAndTime = creationDateAndTime;
		startDateStr = startDateCode + TimeFormat.formatPDSDate(startDateTime).toString();
		startTimeStr = tCode + TimeFormat.formatPDSFilenameTime(startDateTime).toString();
		stopTimeStr = eCode + TimeFormat.formatPDSFilenameTime(stopTime).toString();
		creationDateTimeStr = dfCreation.format(creationDateAndTime);
		this.spacecraftId = spacecraftId;
		this.orbit = (orbit % 10000);
		orbitStr = orbitCode + String.format("%05d", orbit);
		this.origin = origin;
	}
	
	/**
	 * Parse the extension (e.g. ".h5")
	 * @param extensionStr
	 * @throws ParseException
	 */
	private void parseExtension(String extensionStr) throws RtStpsException {
		if (!extensionStr.equals(extension.substring(1))) { // skip the '.'
			throw new RtStpsException("Extension" + usageStr + filename);
		}
	}

	/**
	 * Parse the domain (e.g. "Development")
	 * @param domainStr
	 * @throws ParseException
	 */
	private void parseDomainDescription(String domainStr) throws RtStpsException {
		FixedDomainDescription fixedDomainDescription = FixedDomainDescription.valueOf(domainStr);
		
		if (fixedDomainDescription == null) {
			throw new RtStpsException("Unknown fixed domain  [" + domainStr + "]  -- " + usageStr + filename);
		}
		
		this.domain = new DomainDescription(fixedDomainDescription);
		
	}

	/**
	 * Parse the origin (e.g. "all")
	 * @param originStr
	 * @throws ParseException
	 */
	private void parseOrigin(String originStr) throws RtStpsException {
		originStr = originStr.replaceAll("all-","all");
		Origin origin = Origin.valueOf(originStr);
		if (origin == null) {
			throw new RtStpsException("Unknown origin [" + originStr + "] -- " + usageStr + filename);
		}
		this.origin = origin;
	}

	/**
	 * Parse the creation date 
	 * @param creationDateStr
	 * @throws ParseException
	 * @throws RtStpsException 
	 */
	private void parseCreationDate(String creationDateStr) throws RtStpsException {
		if (creationDateStr.charAt(0) != creationDateCode) {
			throw new RtStpsException("Creation date [" + creationDateStr + "] -- must start with a \'" + creationDateCode + "\' -- " + usageStr + filename);			
		}
		this.creationDateAndTime = dfCreation.parse(creationDateStr);
	}

	/**
	 * Parse the orbit sub-field
	 * @param orbitStr
	 * @throws ParseException
	 */
	private void parseOrbitNumber(String orbitStr) throws RtStpsException {
		if (orbitStr.charAt(0) != orbitCode) {
			throw new RtStpsException("Orbit [" + orbitStr + "] -- must start with a \'" + orbitCode + "\' -- " + usageStr + filename);			
		}
		this.orbit = Integer.valueOf(orbitStr.substring(1));
	}

	/**
	 * Parse the stop time
	 * @param stopTimeStr
	 * @throws ParseException
	 */
	private void parseStopTime(String stopTimeStr) throws RtStpsException {
		if (stopTimeStr.charAt(0) != StartStopFieldId.e.name().charAt(0)) {
			throw new RtStpsException("StopTime [" + stopTimeStr + "] -- must start with a \'" + StartStopFieldId.e + " \' -- " + usageStr + filename);			
		}
		try {
			Date stopTime = eTime.parse(stopTimeStr);
			this.stopTime = new PDSDate(stopTime);
		} catch (ParseException e) {
			throw new RtStpsException(e);
		}
	}


	/**
	 * Parse the date and time
	 * @param startDateStr
	 * @param startTimeStr
	 * @throws ParseException
	 */
	private void parseStartDateTime(String startDateStr, String startTimeStr) throws RtStpsException {
		if (startDateStr.charAt(0) != startDateCode) {
			throw new RtStpsException("StartDate [" + startDateStr + "] -- must start with a \'" + startDateCode + "\' -- " + usageStr + filename);			
		}
		
		SimpleDateFormat createDateFormat = new SimpleDateFormat("yyyyMMdd");
		createDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			Date startDateTime = createDateFormat.parse(startDateStr.substring(1));
			this.startDateTime = new PDSDate(startDateTime);
		} catch (ParseException e) {
			throw new RtStpsException(e);
		}

		if (startTimeStr.charAt(0) != StartStopFieldId.t.name().charAt(0)) {
			throw new RtStpsException("StartTime [" + startTimeStr + "] -- must start with a \'" + StartStopFieldId.t + "\' -- " + usageStr + filename);			
		}
		//this.startTime = tTime.parse(startTimeStr);
		// FIXME !!!
	}

	/**
	 * Parse the spacecraft identifier (e.g. "NPP")
	 * @param spacecraftIdStr
	 * @throws ParseException
	 */
	private void parseSpacecraftId(String spacecraftIdStr) throws RtStpsException {
		SpacecraftId spacecraftId = SpacecraftId.valueOf(spacecraftIdStr);
		if (spacecraftId == null) {
			throw new RtStpsException("Unknown SpaceCraftId [" + spacecraftIdStr + "]" + usageStr + filename);
		}
		this.spacecraftId = spacecraftId;
	}

	/**
	 * Parse the product identifier(s) (e.g. "RATMS")
	 * @param productIdsStr
	 * @throws ParseException
	 */
	private void parseProductIds(String productIdsStr) throws RtStpsException {
		String[] products = productIdsStr.split(productNameDelimiter);
		
		for (int i = 0; i < products.length; i++) {
			ProductIdentifiers productId = ProductIdentifiers.fromNPPString(products[i]);
			if (productId == null) {
				throw new RtStpsException("Unknown ProductId [" + productIdsStr + "]" + usageStr + filename);
			}
			this.productIds.add(productId);
		}
		
		// doesn't seem likely this will trip...
		if (this.productIds.size() <= 0) {
			throw new RtStpsException("No ProductIds found [" + productIdsStr + "]" + usageStr + filename);
		}
	}

	/**
	 * build the filename -- this must be called once the item is updated and the string must be produced
	 * as it stands now it is lazily called in toString...
	 */
	private void buildFilename() {
		
		//System.out.println( productIdsToString());
		//System.out.println( spacecraftId.toStringLower());
		//		System.out.println( startDateStr);
		//				System.out.println( startTimeStr );
		//						System.out.println( stopTimeStr );
		//								System.out.println( orbitStr );
		//										System.out.println( creationDateTimeStr );
		//												System.out.println( origin.toString() );
		//														System.out.println( devDomain.toString() );
		
		filename = productIdsToString() + delimiter +
		  		spacecraftId.toStringLower() + delimiter +
		  		startDateStr + delimiter +
		  		startTimeStr + delimiter +
		  		stopTimeStr + delimiter + 
		  		orbitStr + delimiter +
		  		creationDateTimeStr + delimiter +
		  		origin.toString() + delimiter +
		  		devDomain.toString() + extension;
	}
	
	/**
	 * make the list of product IDs into a ID0-ID1 style string
	 * maybe there's a better way to do this...?
	 */
	private String productIdsToString() {
		int count = 0;  String productStr = "";
		
		Collections.sort(productIds, new ProductIdentifiersComparator()); // FIXME nice if this had no side effects but I guess it does not matter.
		
		for (ProductIdentifiers productID : productIds) {
			if (count > 0) {
				productStr += "-";
			}
			productStr += productID.toString();
			++count;
		}
		return productStr;
	}
	
	
	/**
	 * make the list of product IDs into a ID0-ID1 style string
	 * maybe there's a better way to do this...?
	 */
	public static String productIdsToString(List<RDR> rdrs) {
		int count = 0;  String productStr = "";
		List<ProductIdentifiers> productIds = new LinkedList<ProductIdentifiers>();
		
		for (RDR rdr : rdrs) {
			productIds.add(rdr.getProductId());
		}
		
		
		Collections.sort(productIds,  new ProductIdentifiersComparator()); 
		for (ProductIdentifiers productID : productIds) {
			if (count > 0) {
				productStr += "-";
			}
			productStr += productID.toString();
			++count;
		}
		return productStr;
	}

}
