/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs.clock;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.text.SimpleDateFormat;

/**
 * This class defines how the system creates timestamp annotation for frames.
 * It is a configuration class. It holds setup data only.
 * 
 */
public class TimeStamp
{
    /**
     * This field defines the epoch time. All timestamps will be marked as
     * an offset from this epoch time. The default is Aug 10, 1995 00:00:00.
     */
    public java.util.Date epoch;

    /**
     * This is the session start time. Leave this field blank if you want the
     * scenario start time to be the current wall clock time. If you set this
     * field, then the frame annotation timetags will show that the session
     * occurred at your selected date and time.
     */
    public java.util.Date sessionStart = null;

    /**
     * Normally the time difference between frames will be real time, after
     * adjusting for epoch and the session start time. If you set this field
     * to a positive value, then the annotation timestamps of successive
     * frames will differ by this step size (in milliseconds), and the wall
     * clock is ignored. For example, if you set stepsize to 100, then each
     * frame's time will differ from the preceding one by 100 milliseconds.
     * Warning: The frame synchronizer will not adjust the time to account
     * for sync dropouts, so do not rely on the step size to detect lost
     * frames. It will adjust for flywheel frames, dropped or not, however.
     */
    public long stepsize = 0;


    public TimeStamp()
    {
        try
        {
            if (sdf == null)
            {
                sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            }
            epoch = sdf.parse("19950810000000");
        }
        catch (java.text.ParseException e)
        {
            System.err.println(e);
        }
    }

    /**
     * Load fields from an XML element.
     */
    public void load(org.w3c.dom.Element element) throws RtStpsException
    {
        String value = element.getAttribute("epoch");
        if (value.length() > 0)
        {
            try
            {
                epoch = sdf.parse(value);
            }
            catch (java.text.ParseException pe)
            {
                throw new RtStpsException("epoch has bad format: " +
                        pe.getMessage());
            }
        }

        value = element.getAttribute("sessionStart");
        if (value.length() > 0)
        {
            try
            {
                sessionStart = sdf.parse(value);
            }
            catch (java.text.ParseException pe2)
            {
                throw new RtStpsException("sessionStart has bad format: " +
                        pe2.getMessage());
            }
        }

        stepsize = (long)Convert.toInteger(element,"stepSize",(int)stepsize,0);
    }

    private static SimpleDateFormat sdf = null;
}
