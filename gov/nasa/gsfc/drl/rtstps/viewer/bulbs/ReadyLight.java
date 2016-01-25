/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.bulbs;
import gov.nasa.gsfc.drl.rtstps.viewer.commands.CommandStateListener;

/**
 * The ready light is a visual component that shows a light bulb with a label.
 * It shows three colors:<br>
 * gray - The server is unloaded.
 * yellow - The server is loaded but stopped.
 * green - The server is loaded and enabled.
 * 
 * 
 */
public class ReadyLight extends AbstractBulbGadget implements
        gov.nasa.gsfc.drl.rtstps.viewer.commands.CommandStateListener
{
    private static final int MAXCHARS = 9;
    

    /**
     * Create a ReadyLight object.
     */
    public ReadyLight(java.awt.Font font)
    {
        super(AbstractBulbGadget.GRAY,"Not Ready",MAXCHARS,font);
        setForeground(java.awt.Color.black);
        setToolTipText("Not Ready/Load/Go Indicator");
        loadBulb(AbstractBulbGadget.YELLOW,"Loaded");
        loadBulb(AbstractBulbGadget.GREEN,"Ready");
    }

    /**
     * ReadyLight is a CommandStateListener.
     */
    public synchronized void commandStateChange(int state)
    {
        String bulbColor = null;
        switch (state)
        {
            case CommandStateListener.UNLOADED:
                bulbColor = AbstractBulbGadget.GRAY;
                break;

            case CommandStateListener.LOADED_STOPPED:
                bulbColor = AbstractBulbGadget.YELLOW;
                break;

            case CommandStateListener.LOADED_GO:
                bulbColor = AbstractBulbGadget.GREEN;
                break;
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
    
    private static final long serialVersionUID = 1L;			
}

