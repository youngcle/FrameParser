/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.bulbs;
import gov.nasa.gsfc.drl.rtstps.core.status.IntegerStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * The data light is a visual component that shows a light bulb with a label.
 * It shows three colors:<br>
 * gray - The server is not receiving data
 * yellow - The server is receiving data, but it cannot detect frame sync.
 * green - The server has detected frame sync and is in lock mode.
 * 
 */
public class DataLight extends AbstractBulbGadget implements
        gov.nasa.gsfc.drl.rtstps.viewer.status.StatusListener
{
    public static final String STATUSITEM_1 = "frame_status.Lock Frames";
    public static final String STATUSITEM_2 = "frame_sync.Search Buffers";
    private static final int MAXCHARS = 6;

    /**
     * I set locks and buffers to -1 to synchronize startup. Otherwise,
     * the ready light can flash green, yellow, and gray.
     */
    private long locks = -1L;
    private long buffers = -1L;
    private long xbuffers = -1L;


    /**
     * Create a DataLight object.
     */
    public DataLight(java.awt.Font font)
    {
        super(AbstractBulbGadget.GRAY,"No Data",MAXCHARS,font);
        setForeground(java.awt.Color.black);
        setToolTipText("No Data/Search/Lock");
        loadBulb(AbstractBulbGadget.YELLOW,"Search");
        loadBulb(AbstractBulbGadget.GREEN,"Lock");
    }

    /**
     * The listener gets a status item from the Distributor.
     * DataLight expects two different StatusItem objects:
     * "frame_status.Lock Frames" and "frame_sync.Search Buffers".
     */
    public synchronized void processStatusItem(StatusItem item, String fullName)
    {
        if (fullName.equals(STATUSITEM_1))
        {
            LongStatusItem lsi = (LongStatusItem)item;
            long xlocks = lsi.getLongValue();
            if (locks == -1) locks = xlocks;
            String bulbColor = null;

            if ((locks == xlocks) && (buffers == xbuffers))
            {
                bulbColor = AbstractBulbGadget.GRAY;
            }
            else
            {
                if (xlocks > locks)
                {
                    bulbColor = AbstractBulbGadget.GREEN;
                }
                else if (xbuffers > buffers)
                {
                    bulbColor = AbstractBulbGadget.YELLOW;
                    buffers = xbuffers;
                }

                locks = xlocks;
                buffers = xbuffers;
            }

            if (bulbColor != null)
            {
                final String bcolor = bulbColor;
                java.awt.EventQueue.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        setBulb(bcolor);
                    }
                } );
            }
        }
        else if (fullName.equals(STATUSITEM_2))
        {
            IntegerStatusItem lsi = (IntegerStatusItem)item;
            xbuffers = lsi.getIntValue();
            if (buffers == -1) buffers = xbuffers;
        }
    }
    
    private static final long serialVersionUID = 1L;			
}

