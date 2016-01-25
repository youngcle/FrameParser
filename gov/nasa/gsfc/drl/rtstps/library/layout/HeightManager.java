/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.library.layout;

/**
 * This is a utility class that layout managers use. Its job is to determine
 * the start pixel and height of each row in a list of rows. It does not need
 * components. You tell it the minimum and preferred sizes of each row as well
 * as a row weighting factor. You may also give it top and bottom margins and
 * a row gap.
 * <p>
 * HeightManager usually sets each row to its preferred size. If no row has a
 * non-zero weight, then it distributes extra space to the gaps and margins.
 * If one or more rows has non-zero weight, it gives those rows all extra
 * space proportional to the weight.
 * <p>
 * If the container is smaller than HeightManager's preferred size, then it
 * sets each row to its minimum size. It then distributes extra space as it
 * did above depending on whether or not any row has non-zero weight.
 * 
 * @version 1.1 07/05/2001
 */
final class HeightManager
{
    private boolean _valid = false;
    private Row[] _row;
    private int _vgap = 0;
    private int _topMargin = 0;
    private int _bottomMargin = 0;
    private int _minimumHeight;      //minus margins
    private int _preferredHeight;    //minus margins
    private float _totalWeight = 0f;

    /**
     * Construct a HeightManager object.
     */
    HeightManager(int rows, int vgap)
    {
        _vgap = vgap;
        _row = new Row[rows];
        for (int n = 0; n < rows; n++)
        {
            _row[n] = new Row();
        }
    }

    /**
     * Set a weight for all rows. The default is 0.0f.
     */
    void setRowWeight(float w)
    {
        for (int r = 0; r < _row.length; r++)
        {
            _row[r].weight = w;
        }
        _valid = false;
    }

    /**
     * Set a weight for an indicated row. The default is 0.0f.
     */
    void setRowWeight(int rowIndex, float w)
    {
        _row[rowIndex].weight = w;
        _valid = false;
    }

    /**
     * Set a minimum pixel height for an indicated row.
     */
    void setMinimumRowHeight(int rowIndex, int h)
    {
        _row[rowIndex].minimumHeight = h;
        _valid = false;
    }

    /**
     * Set a preferred pixel height for an indicated row.
     */
    void setPreferredRowHeight(int rowIndex, int h)
    {
        _row[rowIndex].preferredHeight = h;
        _valid = false;
    }

    /**
     * Set a pixel gap between rows. It does not affect the margin.
     */
    void setGap(int g)
    {
        _vgap = g;
        _valid = false;
    }

    /**
     * Set the top and bottom margins.
     */
    void setMargins(int top, int bottom)
    {
        _topMargin = top;
        _bottomMargin = bottom;
        _valid = false;
    }

    /**
     * Get the total preferred height of all rows. This includes gaps and
     * margins but excludes container insets.
     */
    int getPreferredHeight()
    {
        if (!_valid) validate();
        return _preferredHeight + _topMargin + _bottomMargin;
    }

    /**
     * Get the total minimum height of all rows. This includes gaps and
     * margins but excludes container insets.
     */
    int getMinimumHeight()
    {
        if (!_valid) validate();
        return _minimumHeight + _topMargin + _bottomMargin;
    }

    /**
     * Validate the layout.
     */
    void validate()
    {
        _valid = true;
        _minimumHeight = -_vgap;
        _preferredHeight = -_vgap;
        for (int n = 0; n < _row.length; n++)
        {
            Row r = _row[n];
            _preferredHeight += r.preferredHeight + _vgap;
            _minimumHeight += r.minimumHeight + _vgap;
            _totalWeight += r.weight;
        }
    }

    /**
     * Lay out the rows.
     */
    void doLayout(java.awt.Container parent)
    {
        int containerHeight = parent.getSize().height;
        java.awt.Insets insets = parent.getInsets();
        containerHeight -= (insets.top + insets.bottom + _topMargin + _bottomMargin);
        int fat = 0;

        if (!_valid) validate();

        if (containerHeight >= _preferredHeight)
        {
            for (int n = 0; n < _row.length; n++)
            {
                _row[n].height = _row[n].preferredHeight;
            }
            fat = containerHeight - _preferredHeight;
            if (fat > 0)
            {
                fat = giveFatToComponents(fat);
            }
        }
        else
        {
            for (int n = 0; n < _row.length; n++)
            {
                _row[n].height = _row[n].minimumHeight;
            }
            fat = containerHeight - _minimumHeight;
            if (fat > 0)
            {
                fat = giveFatToComponents(fat);
            }
            else
            {
                fat = 0;
            }
        }

        int count = _row.length + 1;
        int afat = fat / count;
        int bfat = fat % count;
        int y = insets.top + _topMargin + afat;
        if (bfat > 0)
        {
            ++y;
            --bfat;
        }

        for (int n = 0; n < _row.length; n++)
        {
            Row r = _row[n];
            r.start = y;
            y += r.height + _vgap + afat;
            if (bfat > 0)
            {
                ++y;
                --bfat;
            }
        }
    }

    private int giveFatToComponents(int fat)
    {
        int work = fat;
        int count = 0;
        for (int n = 0; n < _row.length; n++)
        {
            Row r = _row[n];
            if (r.weight > 0f && work > 0)
            {
                int nfat = Math.round(r.weight / _totalWeight * (float)fat);
                if (nfat > work) nfat = work;
                r.height += nfat;
                work -= nfat;
                ++count;
            }
        }

        if (count > 0 && work > 0)
        {
            int afat = work / count;
            int bfat = work % count;
            work = 0;
            for (int n = 0; n < _row.length; n++)
            {
                Row r = _row[n];
                if (r.weight > 0)
                {
                    r.height += afat;
                    if (bfat > 0)
                    {
                        ++r.height;
                        --bfat;
                    }
                }
            }
        }

        return work;
    }

    /**
     * Get a row's start pixel. This is meaningful only after doLayout().
     */
    final int getRowStart(int rowIndex)
    {
        return _row[rowIndex].start;
    }

    /**
     * Get a row's pixel height. This is meaningful only after doLayout().
     */
    final int getRowHeight(int rowIndex)
    {
        return _row[rowIndex].height;
    }

    void print()
    {
        for (int n = 0; n < _row.length; n++)
        {
            Row r = _row[n];
            System.out.println("n="+n+" y="+r.start+" h="+r.height);
        }
    }

    final class Row
    {
        int start;
        int height;
        int minimumHeight = 0;
        int preferredHeight = 0;
        float weight = 0f;
    }
}
