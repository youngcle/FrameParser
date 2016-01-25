/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;

/**
 * AbstractTextGadget shows a StatusItem label and value.
 * 
 */
public abstract class AbstractTextGadget extends javax.swing.JComponent
        implements gov.nasa.gsfc.drl.rtstps.viewer.status.StatusListener
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Font font = new Font("SansSerif",0,12);
    protected static final Color FOREGROUND_COLOR = Color.black;
    protected Dimension myPreferredSize = new Dimension();
    protected int valueDigits = 6;
    protected int textDigits = 16;
    protected String statusItemText = "";
    protected String statusItemValue = "0";
    private String fullStatusItemId;

    /**
     * Create a AbstractTextGadget.
     */
    protected AbstractTextGadget(String label, String fullStatusItemLabel)
    {
        fullStatusItemId = fullStatusItemLabel;
        statusItemText = label;
        setFont(font);
        int len = label.length();
        if (len > textDigits) textDigits = len;
        setBorder(BorderFactory.createEmptyBorder(1,2,1,2));
        setForeground(FOREGROUND_COLOR);
        configureSize();
    }

    /**
     * Set the label and value lengths. This controls the component's
     * size.
     */
    public void setFieldLengths(int labelLength, int valueLength)
    {
        textDigits = labelLength;
        valueDigits = valueLength;
        configureSize();
    }

    /**
     * Get the number of characters in the label.
     */
    public final int getLabelDigits()
    {
        return textDigits;
    }

    /**
     * Get the number of characters in the value string.
     */
    public final int getValueDigits()
    {
        return valueDigits;
    }

    /**
     * Configure the sizes of the this component.
     */
    private void configureSize()
    {
        Insets i = getInsets();
        Font font = getFont();
        FontMetrics fm = getFontMetrics(font);
        myPreferredSize.height = fm.getHeight() + i.top + i.bottom;
        int tpixels = fm.charWidth('0') * textDigits;
        int vpixels = fm.charWidth('0') * valueDigits;
        myPreferredSize.width = tpixels + vpixels + i.left + i.right;
        setPreferredSize(myPreferredSize);
        setMinimumSize(myPreferredSize);
        setMaximumSize(myPreferredSize);
    }

    /**
     * Paint the label and value.
     */
    protected void paintValues(Graphics g)
    {
        g = g.create();  //make a copy
        FontMetrics fm = g.getFontMetrics();
        Insets i = getInsets();
        int y = fm.getAscent() + i.top;
        g.setColor(FOREGROUND_COLOR);
        g.drawString(statusItemText,i.left,y);
        int x = myPreferredSize.width - (i.left + i.right +
                fm.stringWidth(statusItemValue));
        g.drawString(statusItemValue,x,y);
        g.dispose();
    }

    /**
     * Get the full status item name (example: "path.vc42.CADUs").
     */
    public final String getFullStatusItemId()
    {
        return fullStatusItemId;
    }

    /**
     * A text gadget is a status listener.
     */
    public abstract void processStatusItem(StatusItem item, String fullName);
}
