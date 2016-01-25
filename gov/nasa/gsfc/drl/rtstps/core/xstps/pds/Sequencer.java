/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.xstps.pds;

/**
 * This class checks for packet sequence errors.
 * 
 */
final class Sequencer
{
    /** The current correct count. */
    private int count = 0;
    private static int MASK = 0x03fff;
    private int stepsize = 1;
    private boolean firstTime = true;
    private int expectedCount = 0;

    /**
     * Create s Sequencer with a non-zero stepsize.
     */
    Sequencer(int step)
    {
        if (step == 0) step = 1;
        stepsize = step;
    }

    /**
     * Get the next expected sequence count.
     */
    final int getExpectedNextCount()
    {
        return expectedCount;
    }

    /**
     * Check a packet for a sequence error. Note that this class uses the
     * first packet it sees to establish a sequence seed.
     */
    int check(gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet packet)
    {
        int actual = packet.getSequenceCounter();
        int missing = 0;

        if (firstTime)
        {
            expectedCount = count = actual;
            firstTime = false;
        }
        else
        {
            expectedCount = (count + stepsize) & MASK;
            count = actual;

            if (expectedCount != actual)
            {
                if (stepsize == 1)
                {
                    missing = (actual - expectedCount) & MASK;
                }
                else if (stepsize == -1)
                {
                    missing = (expectedCount - actual) & MASK;
                }
                else if (stepsize > 1)
                {
                    missing = getGapWithUnusualStepsize(actual,expectedCount,stepsize);
                }
                else if (stepsize < -1)
                {
                    missing = getGapWithUnusualStepsize(expectedCount,actual,-stepsize);
                }
            }
        }
        return missing;
    }

    /**
     * In this function I compute the gap assuming the step value > 1.
     * I do step < -1 by calling it with actual and expectedCount swapped.
     */
    private int getGapWithUnusualStepsize(int actual, int expected, int step)
    {
        actual -= expected;
        actual &= MASK;
        int missing = 0;
        for (int n = 0; n < step; n++)
        {
            int x = actual + (MASK + 1) * n;
            if ((x % step) == 0)
            {
                missing = x / step;
                break;
            }
        }
        if (missing == 0) missing = MASK;
        return missing;
    }
}


