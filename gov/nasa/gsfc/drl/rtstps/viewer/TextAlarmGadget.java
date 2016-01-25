/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;

import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.awt.Color;
import java.awt.Graphics;

/**
 * TextAlarmGadget shows a StatusItem label and value. It also shows the
 * component's background in normal, yellow, or red depending on if the
 * value exceeds yellow or red limits. You must configure TextAlarmGadget
 * with a second StatusItem, which provides a denominator when computing
 * percentages for the limits.
 * 
 * 
 */
class TextAlarmGadget extends AbstractTextGadget implements
        gov.nasa.gsfc.drl.rtstps.viewer.status.StatusListener
{
    private static final Color RED = new Color(255,146,146);
    private static final Color YELLOW = new Color(255,255,90);

    private Color backgroundColor = null;
    private float yellowBound = 10f;
    private float redBound = 25f;
    private long longValue = 0L;
    
    private String secondaryStatusItemName;
    private long denominator = 0L;

    /**
     * Create a TextAlarmGadget object.
     */
    TextAlarmGadget(String label, String fullLabel,
            String secondaryStatusItemName,
            float yellowLimit, float redLimit)
    {
        super(label,fullLabel);
        this.secondaryStatusItemName = secondaryStatusItemName;
        yellowBound = yellowLimit;
        redBound = redLimit;
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (backgroundColor != null)
        {
            Color save = g.getColor();
            g.setColor(backgroundColor);
            g.fillRect(0,0,myPreferredSize.width,myPreferredSize.height);
            g.setColor(save);
        }

        paintValues(g);
    }

    public synchronized void processStatusItem(StatusItem item, String fullName)
    {
        if (secondaryStatusItemName.equals(fullName))
        {
            LongStatusItem li = (LongStatusItem)item;
            denominator = li.getLongValue();

            float p = 0f;
            if (denominator > longValue)
            {
                p = 100f * (float)longValue / (float)denominator;
            }
            else if (denominator > 0 || longValue > 0)
            {
                p = 100f;
            }

            Color save = backgroundColor;
            if (p > redBound)
            {
                backgroundColor = RED;
            }
            else if (p > yellowBound)
            {
                backgroundColor = YELLOW;
            }
            else
            {
                backgroundColor = null;
            }

            if (save != backgroundColor)
            {
                repaint();
            }
        }
        else
        {
            statusItemValue = item.getValue();

            long savedValue = longValue;
            LongStatusItem li = (LongStatusItem)item;
            longValue = li.getLongValue();

            if (longValue != savedValue)
            {
                repaint();
            }
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
