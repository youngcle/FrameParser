/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.library.layout;
import java.awt.Component;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This is a utility class that layout managers use. Its job is to determine
 * the start pixels and widths of a row of components. You may add glue or
 * horizontal glue.
 * 
 * @version 1.1 07/05/2001
 * 
 */
final class WidthManager
{
    private LinkedList<Item> _itemList = new LinkedList<Item>();
    private boolean _valid = false;
    private int _hgap = 0;
    private int _leftMargin = 0;
    private int _rightMargin = 0;
    private float _totalWeight = 0f;
    private boolean _distributeFatToGaps = false;

    /** the min and preferred sizes minus the margins. */
    private int _minimumHeight;
    private int _preferredHeight;
    private int _minimumWidth;
    private int _preferredWidth;


    /**
     * Construct a WidthManager object.
     */
    WidthManager(int gap)
    {
        _hgap = gap;
    }

    /**
     * Add a component with a weight. The component may be glue or
     * horizontal glue, in which case the weight is unconditionally
     * set to 1f, and the passed weight is ignored.
     */
    void addComponent(Component c, float weight)
    {
        Item item = new Item(c,weight);
        _itemList.add(item);
        _totalWeight += item.weight;
        _valid = false;
    }

    /**
     * Remove a component.
     */
    void removeComponent(Component c)
    {
        if (_itemList.remove(new Item(c)))
            _valid = false;
    }

    /**
     * Determines how to distribute extra space when all components have zero
     * weight and there is no glue. If true, extra space is evenly distributed
     * among the gaps and margins, which causes a center alignment. If false,
     * extra space is put at the end of the row, which causes a left alignment.
     * The default behavior is false, left alignment.
     */
    void setDistributeSpaceToGaps(boolean c)
    {
        _distributeFatToGaps = c;
    }

    /**
     * Set the left and right margins in pixels. The defaults are (0,0).
     */
    void setMargins(int leftMargin, int rightMargin)
    {
        _leftMargin = leftMargin;
        _rightMargin = rightMargin;
        _valid = false;
    }

    /**
     * Set a horizontal gap. The default is zero pixels.
     */
    void setGap(int gap)
    {
        _hgap = gap;
        _valid = false;
    }

    /**
     * Get the preferred height for the row, which is the largest preferred
     * height of all components in the row.
     */
    int getPreferredHeight()
    {
        return _preferredHeight;
    }

    /**
     * Get the minimum height for the row, which is the largest minimum
     * height of all components in the row.
     */
    int getMinimumHeight()
    {
        return _minimumHeight;
    }

    /**
     * Get the preferred width of the row. This includes the gaps and the
     * margins but does not include the container's insets.
     */
    int getPreferredWidth()
    {
        if (!_valid) validate();
        return _preferredWidth + _leftMargin + _rightMargin;
    }

    /**
     * Get the minimum width of the row. This includes the gaps and the
     * margins but does not include the container's insets.
     */
    int getMinimumWidth()
    {
        if (!_valid) validate();
        return _minimumWidth + _leftMargin + _rightMargin;
    }

    /**
     * Validate the layout.
     */
    void validate()
    {
        _valid = true;
        _totalWeight = 0f;
        _minimumWidth = _preferredWidth = -_hgap;
        _minimumHeight = _preferredHeight = 0;

        Iterator<Item> i = _itemList.iterator();
        while (i.hasNext())
        {
            Item item = (Item)i.next();
            _totalWeight += item.weight;

            java.awt.Dimension d = item.component.getMinimumSize();
            _minimumWidth += d.width + _hgap;
            if (d.height > _minimumHeight)
            {
                _minimumHeight = d.height;
            }

            d = item.component.getPreferredSize();
            _preferredWidth += d.width + _hgap;
            if (d.height > _preferredHeight)
            {
                _preferredHeight = d.height;
            }
        }
    }

    /**
     * Lay out the container.
     * @param parent The container.
     * @param ystart The row's y pixel location.
     * @param yheight The row's height.
     */
    void doLayout(java.awt.Container parent, int ystart, int yheight)
    {
        int containerWidth = parent.getSize().width;
        java.awt.Insets insets = parent.getInsets();
        containerWidth -= (insets.left + insets.right + _leftMargin + _rightMargin);
        int fat = 0;

        if (!_valid) validate();

        if (containerWidth >= _preferredWidth)
        {
            Iterator<Item> i = _itemList.iterator();
            while (i.hasNext())
            {
                Item item = (Item)i.next();
                item.width = item.component.getPreferredSize().width;
            }
            fat = containerWidth - _preferredWidth;
            if (fat > 0)
            {
                fat = giveFatToComponents(fat);
            }
        }
        else
        {
            Iterator<Item> i = _itemList.iterator();
            while (i.hasNext())
            {
                Item item = (Item)i.next();
                item.width = item.component.getMinimumSize().width;
            }
            fat = containerWidth - _minimumWidth;
            if (fat > 0)
            {
                fat = giveFatToComponents(fat);
            }
            else
            {
                fat = 0;
            }
        }

        int afat = 0;
        int bfat = 0;
        if ((fat > 0) && _distributeFatToGaps)
        {
            int count = _itemList.size() + 1;
            afat = fat / count;
            bfat = fat % count;
        }

        int x = insets.left + _leftMargin + afat;
        if (bfat > 0)
        {
            ++x;
            --bfat;
        }

        Iterator<Item> i = _itemList.iterator();
        while (i.hasNext())
        {
            Item item = (Item)i.next();
            item.start = x;
            x += item.width + _hgap + afat;
            if (bfat > 0)
            {
                ++x;
                --bfat;
            }

            item.component.setLocation(item.start,ystart);
            item.component.setSize(item.width,yheight);
        }
    }

    private int giveFatToComponents(int fat)
    {
        int work = fat;
        int count = 0;
        Iterator<Item> i = _itemList.iterator();

        while (i.hasNext())
        {
            Item item = (Item)i.next();
            if ((item.weight > 0f) && (work > 0))
            {
                int nfat = Math.round(item.weight / _totalWeight * (float)fat);
                if (nfat > work) nfat = work;
                item.width += nfat;
                work -= nfat;
                ++count;
            }
        }

        if (count > 0 && work > 0)
        {
            int afat = work / count;
            int bfat = work % count;
            work = 0;
            i = _itemList.iterator();
            while (i.hasNext())
            {
                Item item = (Item)i.next();
                if (item.weight > 0)
                {
                    item.width += afat;
                    if (bfat > 0)
                    {
                        ++item.width;
                        --bfat;
                    }
                }
            }
        }

        return work;
    }

    final class Item
    {
        Component component;
        float weight = 0f;
        boolean isGlue = false;
        int start;
        int width;

        Item(Component c, float w)
        {
            if (c instanceof javax.swing.Box.Filler)
            {
                //all glue components have preferred size=(0,0)
                //max size: vglue=(0,0x7fff), hglue=(0x7fff,0),
                //          glue=(0x7fff,0x7fff)
                java.awt.Dimension d = c.getPreferredSize();
                if ((d.width == 0) && (d.height == 0))
                {
                    d = c.getMaximumSize();
                    isGlue = (d.width == 0x07fff);
                    if (!isGlue)
                    {
                        throw new IllegalArgumentException("Wrong glue type in wrong place");
                    }
                    w = 1f;
                }
            }

            component = c;
            weight = w;
        }

        /**
         * I use this constructor to do searches and never to make a real
         * Item object.
         */
        Item(Component c)
        {
            component = c;
        }

        public boolean equals(Object obj)
        {
            boolean match = false;
            if (obj != null)
            {
                Item i = (Item)obj;
                match = (i.component == this.component);
            }
            return match;
        }
    }
}
