/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;

public class Stats {
	public LongStatusItem sci_createdGranules = new LongStatusItem("Sci Created Granules");
	public LongStatusItem sci_discardedGranules = new LongStatusItem("Sci Discarded Granules");
	public LongStatusItem sci_freePoolPackets = new LongStatusItem("Sci Free Pool Packets");
	public LongStatusItem sci_createdPackets = new LongStatusItem("Sci Created Created");
	public LongStatusItem sci_packetsMemory = new LongStatusItem("Sci Packets Memory");
	public LongStatusItem ae_createdGranules = new LongStatusItem("ae Created Granules");
	public LongStatusItem ae_discardedGranules = new LongStatusItem("ae Discarded Granules");
	public LongStatusItem ae_freePoolPackets = new LongStatusItem("ae Free Pool Packets");
	public LongStatusItem ae_createdPackets = new LongStatusItem("ae Created Created");
	public LongStatusItem ae_packetsMemory = new LongStatusItem("ae Packets Memory");
	public LongStatusItem drop = new LongStatusItem("Unsupported RDR packets");;
}
