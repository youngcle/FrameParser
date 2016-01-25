/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.bulbs;
import java.awt.Dimension;

/**
 * This base class is a visual component that shows a light bulb (in one of
 * several defined colors) with an optional label, which is associated with
 * each color. After creating a bulb gadget, you should define additional
 * bulbs.
 * 
 * 
 */
abstract class AbstractBulbGadget extends javax.swing.JLabel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * Common bulb colors.
     */
    public static final String RED = "red";
    public static final String YELLOW = "yellow";
    public static final String GREEN = "green";
    public static final String GRAY = "gray";


    /** A map that stores bulbs (Color,Bulb) */
    private java.util.Map<String,Bulb> bulbMap = new java.util.HashMap<String,Bulb>(16);	
    private Bulb currentBulb = null;

    /**
     * Construct an abstract bulb gadget.
     * @param initialBulbColor A bulb color such as "red", "green", etc.
                    This is the starting bulb color.
     * @param initialBulbLabel A label that is attached to the initial bulb.
     *              It may be null.
     * @param maxLabelChars Maximum number of characters allocated to the
     *              label. It is used to estimate the gadget size. It may
     *              be zero, in which case the size comes from the initial
     *              bulb text.
     * @param font A font for the label. It may be null.
     */
    protected AbstractBulbGadget(String initialBulbColor,
            String initialBulbLabel, int maxLabelChars, java.awt.Font font)
    {
        currentBulb = new Bulb(initialBulbColor,initialBulbLabel);
        bulbMap.put(initialBulbColor,currentBulb);

        setIcon(currentBulb.getIcon());
        setHorizontalAlignment(javax.swing.JLabel.LEFT);
        setBorder(null);
        setAlignmentX(0.5f);
        setAlignmentY(0.5f);
        setIconTextGap(1);
        if (font != null) setFont(font);

        /**
         * I derive the label length from the initial text if the client
         * did not pass maxLabelChars.
         */
        if (initialBulbLabel != null && maxLabelChars <= 0)
        {
            maxLabelChars = initialBulbLabel.length();
        }

        /**
         * I compute the preferred size from a string template.
         */
        Dimension d;
        if (maxLabelChars > 0)
        {
            StringBuffer sb = new StringBuffer(maxLabelChars);
            for (int n = 0; n < maxLabelChars; n++) sb.append('m');
            setText(sb.toString());

            d = new Dimension(getPreferredSize());

            if (initialBulbLabel == null)
            {
                setText(" ");
            }
            else
            {
                setText(initialBulbLabel);
            }
        }
        else
        {
            javax.swing.Icon icon = currentBulb.getIcon();
            d = new Dimension(icon.getIconWidth(),icon.getIconHeight());
        }

        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    /**
     * Load an additional color bulb and label. The label may be null.
     */
    public final void loadBulb(String color, String label)
    {
        bulbMap.put(color,new Bulb(color,label));
    }

    /**
     * Set the current bulb and label.
     */
    public synchronized void setBulb(String color)
    {
        Bulb bulb = (Bulb)bulbMap.get(color);
        if (bulb == null)
        {
            throw new RuntimeException("Undefined bulb color: " + color);
        }
        if (!bulb.equals(currentBulb))
        {
            currentBulb = bulb;
            setText(bulb.getLabel());
            setIcon(bulb.getIcon());
        }
    }
}
