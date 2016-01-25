/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.drl.rtstps.core.output.rdr;

import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

import java.util.List;

public class GapFinder {
	private int[] sequenceNumberTracker = new int[16384];
	private int totalSequences = 0;
	private int startSequences = -1;
	
	public GapFinder(List<Packet> packets) {
		for (Packet packet : packets) {
			int seqCount = packet.getSequenceCounter();
			//System.out.println("SequenceCounter: " + seqCount);
			sequenceNumberTracker[packet.getSequenceCounter()] += 1;
			++totalSequences;
			if (startSequences  < 0) {
				startSequences = packet.getSequenceCounter();
			}
		}
	}

	public void printSequenceNumberIssues() {
		for (int i = 0; i < totalSequences; i++) {
			int index = (i + startSequences) % 16384;
			if (sequenceNumberTracker[index] == 0) {
				System.out.println("#" + i + " Sequence Gap at # -- " + index + " -- start sequences: " + startSequences + " -- totalSequences: " + totalSequences);
			} else if (sequenceNumberTracker[index] > 1) {
				System.out.println("#" + i + " Sequence Dup at # -- " + index + " -- start sequences: " + startSequences + " -- totalSequences: " + totalSequences);
			}
		}
	}
}
