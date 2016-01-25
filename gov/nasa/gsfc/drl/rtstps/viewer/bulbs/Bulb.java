/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.bulbs;
import javax.swing.ImageIcon;

/**
 * This class represents a bulb icon and a label that could be displayed in a
 * bulb gadget. It loads bulb images.
 * <p>
 * I have cached the image icons in a static map so that I don't waste time
 * and resources loading them multiple times. I defer creating an ImageIcon
 * until someone actually demands it.
 * <p>
 * Bulb colors are passes as lowercase text string ("red","green", etc).
 * This class then loads the image file, which it expects to find in the
 * images directory. The file name must have the form:<br>
 * colorTextString + "Bulb.gif"
 * <p>
 * This class is an AbstractBulbGadget utility. It exists primarily to
 * provide a value object in a map.
 *
 * 
 * 
 */
class Bulb
{
    /**
     * This is a static map of bulb images, (String,ImageIcon). I store them
     * here so that I only have to load them once.
     */
    private static java.util.Map<String,ImageIcon> imageMap = new java.util.HashMap<String,ImageIcon>(16);	

    /** The image icon for this bulb color. */
    private ImageIcon icon;

    /** The label that accompanies the icon. It is NOT the bulb color. */
    private String label;


    /**
     * Create a bulb.
     * @param color A color such as "red", "yellow", "green", and "gray"
     * @param label A label that is printed next to the bulb
     */
    Bulb(String color, String label)
    {
        this.label = label;
        icon = (ImageIcon)imageMap.get(color);
        if (icon == null)
        {
            boolean ok = createImageIcon(color);
            if (!ok)
            {
                throw new IllegalArgumentException(color +
                        " ImageIcon not created");
            }
        }
    }

    /**
     * Get the icon.
     */
    final javax.swing.ImageIcon getIcon()
    {
        return icon;
    }

    /**
     * Get the label. It may be null.
     */
    final String getLabel()
    {
        return label;
    }

    private boolean createImageIcon(String color)
    {
        boolean ok = false;
        /**
         * All viewer images are contained in "images", which is a
         * subdirectory of the viewer's package.
         */
        /*								
        String bfile = "/nasa/stps/viewer/images/" + color + "Bulb.gif";
        java.net.URL url = getClass().getResource(bfile);
        if (url != null)
        {
            icon = new ImageIcon(url,color + " light");
            if (icon != null)
            {
                imageMap.put(color,icon);
                ok = true;
            }
        }
        */
        
        icon = null;
        String bfile = "images/" + color + "Bulb.gif";
        try {
          icon = new ImageIcon(getClass().getResource("/"+bfile));
        } catch (Exception e1) {
          try {
            icon = new ImageIcon(bfile);
          } catch (Exception e2) {
            // Give up.
          }
        }
        if (icon != null) {
          imageMap.put(color,icon);
          ok = true;
        }
        return ok;
    }
}
