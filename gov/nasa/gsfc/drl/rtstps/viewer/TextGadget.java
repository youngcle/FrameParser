/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;

import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.awt.Graphics;

/**
 * TextGadget is a visual component that shows a status item label
 * and its value.
 * 
 * 
 */
class TextGadget extends AbstractTextGadget
{
    /**
     * Create a TextGadget with a label.
     * @param label A label to be shown on the screen.
     * @param fullIdentity The full status identification of the item.
     */
    TextGadget(String label, String fullIdentity)
    {
        super(label, fullIdentity);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        paintValues(g);
    }

    public synchronized void processStatusItem(StatusItem item, String fullName)
    {
        String value = item.getValue();
        if (!statusItemValue.equals(value))
        {
            statusItemValue = value;
            repaint();
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
