/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import java.util.Random;


/**
 * A Reference Identifier is defined in the JPSS/NPOESS documentation but is not precise.  At the moment
 * this class implements the reference by generating several random number for each part.  The format is
 * a String composed as follows: <code>xxxxxxxx-xxxxx-xxxxxxxx-xxxxxxxx</code>
 * 
 *
 */
public class ReferenceId {
	//private UUID refID;
	private long part1;
	private long part2;
	private long part3;
	private long part4;
	private String refIdStr;
	
	/**
	 * Create a ReferenceId
	 */
	//public ReferenceId() {
	//	refID = UUID.randomUUID();
	//}
	
	/**
	 * Create a ReferenceId
	 */
	public ReferenceId() {
		Random ran = new Random();
		part1 = ran.nextInt() & 0x00000000ffffffffL;
		
		part2 = ran.nextInt(0xfffff) & 0x00000000000fffffL;
		
		part3 = ran.nextInt() & 0x00000000ffffffffL;
		part4 = ran.nextInt() & 0x00000000ffffffffL;
		
	
		this.refIdStr = String.format("%08x", (part1)) + "-" + String.format("%05x", (part2)) + "-" + String.format("%08x", (part3)) + "-" + String.format("%08x", (part4));	
	}
	
	/**
	 * Given a pre-existing ReferenceId in a String, try to parse it and store 
	 * the result internally
	 * @param referenceIdStr a Reference Identifier in the xxxxxxxx-xxxxx-xxxxxxxx-xxxxxxxx format
	 */
	public ReferenceId(String referenceIdStr) {
		String[] parts = referenceIdStr.split("-");
		
		part1 = Long.parseLong(parts[0], 16);
		part2 = Long.parseLong(parts[1], 16);
		part3 = Long.parseLong(parts[2], 16);
		part4 = Long.parseLong(parts[3], 16);
		
		if ((part1 > 0xffffffffL) || (part2 > 0xfffffL) ||  (part3 > 0xffffffffL) || (part4 > 0xffffffffL)) {
			throw new RtStpsRuntimeException("Illegal ReferenceId [" + referenceIdStr + "]");
		}
		
		this.refIdStr = String.format("%08x", (part1)) + "-" + String.format("%05x", (part2)) + "-" + String.format("%08x", (part3)) + "-" + String.format("%08x", (part4));	
		
		if (this.refIdStr.equals(referenceIdStr) == false) {
			throw new RtStpsRuntimeException("Illegal ReferenceId [" + referenceIdStr + "], expecting [" + refIdStr + "]");
		}
	}
	
	
	/**
	 * Return the String representation of the Reference Identifier
	 */
	public String toString() {
		return refIdStr;
	}
	
	/***
	public static void main(String[] args) {
		ReferenceId ref1 = new ReferenceId();
		
		System.out.println("Ref1 = " + ref1.toString());
		
		ReferenceId ref2 = new ReferenceId(ref1.toString());
		
		System.out.println("Ref2 = " + ref2.toString());
	}
	****/
}
