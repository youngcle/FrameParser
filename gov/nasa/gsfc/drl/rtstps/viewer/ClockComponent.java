/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * This class is a visual component that shows a date and time.
 * 
 * 
 */
final class ClockComponent extends javax.swing.JLabel implements
        gov.nasa.gsfc.drl.rtstps.viewer.status.StatusListener
{
    private static java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat("yyyy/DDD/HH:mm:ss");
    private java.util.Date clock;

    /**
     * Create a ClockComponent object.
     */
    ClockComponent()
    {
        clock = new java.util.Date(System.currentTimeMillis());
        setText(sdf.format(clock));
    }

    /**
     * Update the visual date and time with the current time value.
     */
    void updateTime()
    {
        clock.setTime(System.currentTimeMillis());
        setText(sdf.format(clock));
    }

    /**
     * As a StatusListener, get a clock status item from the Distributor,
     * which updates the clock.
     */
    public synchronized void processStatusItem(StatusItem item, String fullId)
    {
        /**
         * Clock status items are always longs, which are millisecond
         * measurements.
         */
        LongStatusItem clockItem = (LongStatusItem)item;
        final long value = clockItem.getLongValue();
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                clock.setTime(value);
                setText(sdf.format(clock));
            }
        } );
    }
    
    private static final long serialVersionUID = 1L;			
}
