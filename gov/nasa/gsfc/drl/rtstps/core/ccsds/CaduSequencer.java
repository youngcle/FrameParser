/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.ccsds;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * This class performs sequence field checking for version 2 CCSDS frames.
 * 
 * 
 */
public class CaduSequencer
{
    private static final int SEQUENCE_MASK = 0x00ffffff;
    private static final int UNINITIALIZED = -100;

    private int expected = UNINITIALIZED;

    /**
     * Number of frame sequence errors.
     */
    private LongStatusItem sequenceErrors;

    /**
     * Number of missing frames due to frame sequence errors.
     */
    private LongStatusItem missingCADUs;

    /**
     * The total number of CADUs this service has processed.
     */
    private LongStatusItem cadus;


    /**
     * Create a Sequencer object.
     * @param statusItemList A CCSDS status collector.
     */
    public CaduSequencer(java.util.Collection<StatusItem> statusItemList)	
    {
        cadus = new LongStatusItem("CADUs");
        sequenceErrors = new LongStatusItem("CADU Seq Errors");
        missingCADUs = new LongStatusItem("Missing CADUs");

        statusItemList.add(cadus);
        statusItemList.add(sequenceErrors);
        statusItemList.add(missingCADUs);
    }

    /**
     * Perform a sequence check on the CADU. The method marks the frame
     * annotation if it detects a sequence error.
     */
    public void check(Cadu cadu)
    {
        if (!cadu.isFillFrame())
        {
            int actual = cadu.getSequenceCount();

            if (actual == expected)
            {
                ++expected;
            }
            else if (expected == UNINITIALIZED)
            {
                expected = actual + 1;
            }
            else
            {
                ++sequenceErrors.value;
                int missing = (actual - expected) & SEQUENCE_MASK;
                missingCADUs.value += missing;
                cadu.getFrameAnnotation().hasSequenceError = true;
                expected = actual + 1;
            }

            expected &= SEQUENCE_MASK;
            ++cadus.value;
        }
    }
}
