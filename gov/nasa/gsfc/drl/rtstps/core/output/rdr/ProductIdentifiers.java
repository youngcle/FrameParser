/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

/**
 * ProductionIdentfiers the "field terminal" supports only
 * 
 *
 */
public enum ProductIdentifiers {
	RATMS("RDRE-ATMS-C0030", "ATMS-SCIENCE-RDR", "ATMS Science RDR", "Centrals, ISF, SDS, LTA"),
	RCRIS("RDRE-CRIS-C0030", "CRIS-SCIENCE-RDR", "CRIS Science RDR", "Centrals, ISF, SDS, LTA"),
	RVIRS("RDRE-VIRS-C0030", "VIIRS-SCIENCE-RDR", "VIIRS Science RDR", "Centrals, ISF, SDS, LTA"),
	RONPS("RDRE-OMPS-C0030", "OMPS-NPSCIENCE-RDR", "OMPS Nadir Profile Science RDR", "Centrals, ISF, SDS, LTA"),
	ROLPS("RDRE-OMPS-C0032", "OMPS-LPSCIENCE-RDR", "OMPS Limb Profile Science RDR", "Centrals, ISF, SDS, LTA"),
	ROTCS("RDRE-OMPS-C0038", "OMPS-TCSCIENCE-RDR", "OMPS Nadir Total Column Science RDR", "Centrals, ISF, SDS, LTA"),
	//FIXME this is going to be a problem, the short name is the SAME for each... probably only one or the other should be valid
	RNSCA_NPP("RDRE-SCAE-C0030", "SPACECRAFT-DIARY-RDR", "NPP SpacecraftAOS Diary Attitude and Ephemeris RDR", "SDS (NPP ONLY), ISF, CLASS, C3S"),
	RNSCA_NPOESS("RDRE-SCAE-C0031", "SPACECRAFT-DIARY-RDR", "NPOESS SpacecraftAOS Diary Attitude and Ephemeris RDR", "ISF, CLASS, C3S");


	private String dataMnemonic;
	private String shortName;
	private String longName;
	private String receiver;

	private ProductIdentifiers(String dataMnemonic, String shortName, String longName, String receiver) {
		this.dataMnemonic = dataMnemonic;
		this.shortName = shortName;
		this.longName = longName;
		this.receiver = receiver;
	}
	public String getDataMnemonic() {
		return this.dataMnemonic;
	}
	public String getShortName() {
		return this.shortName;
	}
	public String getLongName() {
		return this.longName;
	}
	public String getReceiver() {
		return this.receiver;
	}
	public static ProductIdentifiers fromShortName(String shortName) {
		ProductIdentifiers[] ids = ProductIdentifiers.values();
		for (int i = 0; i < ids.length; i++) {
			if (ids[i].getShortName().equals(shortName)) {
				return ids[i];
			}
		}
		return null;
	}
	
	/**
	 * Return the real string name, unadulterated. This is mainly for the RNSCA NPP/NPOESS (attitude and ephemeris)
	 * which for most cases is exactly the same but whose description and other details differ slightly.
	
	 */
	public String unadulteratedToString() {
		return this.name();
	}
	
	/**
	 * Maps RNSCA_NPP and RNSCA_NPOESS to the same "RNSCA" string name
	 * @return name of the enum, except in the case of RNSCA_NPP and RNSCA_NPOESS
	 */
	@Override
	public String toString() {
		if (this.name().equals("RNSCA_NPP")) {
			return "RNSCA";
		} else if (this.name().equals("RNSCA_NPOESS")) {
			return "RNSCA";
		}
		
		return this.name();
	}
	
	/**
	 * The following are matched:
	 * RATMS --> RATMS
	 * RCRIS --> RCRIS
	 * RVIRS --> RVIRS
	 * RNSCA --> RNSCA_NPP
	 * @param name
	 * @return
	 */
	public static ProductIdentifiers fromNPPString(String name) {
		ProductIdentifiers[] values = ProductIdentifiers.values();
		for (int i = 0; i < values.length; i++) {

			if (values[i].toString().equals(name)) {
				// System.out.println("Found: " + items[0] + " and " +
				// items[1]);
				return values[i];
			}
		}
		
		return null;
	}
}
