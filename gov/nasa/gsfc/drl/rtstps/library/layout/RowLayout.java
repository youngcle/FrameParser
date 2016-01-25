/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.library.layout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;

/**
 * This class is a layout manager. It forms rows of components.
 * It appends components to rows, left-justifying them in each row.
 * <p>
 * RowLayout lays out a row similar to FlowLayout, except it does not wrap
 * components to a new row. It sets components to their preferred widths.
 * but if the container is not wide enough, it uses minimum widths instead.
 * <p>
 * When you give a component to RowLayout, you also assign it a row number and
 * a weight, which is a floating point number between 0.0 and 1.0. The weight
 * tells RowLayout how to distribute extra space in a row. If all components
 * in a row have zero weight, then RowLayout left-justifies all of them, and
 * it puts all extra space at the row's end. If one or more components has
 * weight, then RowLayout assigns all extra space to those components by
 * expanding their horizontal cell size. It gives a component a percentage of
 * the extra space equal to the component's weight divided by the sum of all
 * weights in that row. It ignores a component's maximum size. The horizontal
 * layout of a row does not affect any other row.
 * <p>
 * You may add glue or horizontal glue to a row. (It ignores the vertical
 * aspects of glue.) It treats glue as an invisible component with weight set
 * to 1.0. It ignores any passed value for glue's weight and unconditionally
 * uses 1.0 instead. (Set javax.swing.Box for glue components. You can also
 * insert a blank panel with 1.0 weight.)
 * <p>
 * RowLayout sets the height of a row to the maximum preferred size of all
 * components in a row. If the container is not tall enough, it uses minimum
 * sizes instead. This means that all components in a row are expanded
 * vertically to be the same size. RowLayout ignores a component's maximum
 * size. You do not have to put components in every row.
 * <p>
 * You may assign weights to rows. The default row weight is 0.0. If all rows
 * have zero weight, then RowLayout evenly distributes extra space to the
 * vertical gaps and margins. If one or more rows have weight, then it gives
 * those rows all extra space by expanding the vertical heights of weighted
 * rows. It gives a row a percentage of the extra space equal to the row's
 * weight divided by the sum of all row weights.
 * <p>
 * You may not add vertical glue to RowLayout. Instead, you should set a
 * row's weight to a non-zero value (usually 1.0). You do not need to put
 * components in that row or any row.
 * <p>
 * Pass a constraint object when you add a component to its container. The
 * constraint class is RowLayout.Constraint { int row; float weight; } It
 * has the convenience method void set(int row, float weight). RowLayout
 * copies the Constraint object, so you only need to create one,
 * <p>
 * You may set hgap and vgap, which are the gaps between components. The
 * default gap size is zero pixels. It does not apply to the border. You may
 * set insets, which is the border.<p>
 * <pre>
 * RowLayout layout = new RowLayout(5); //five rows, no gaps
 * JPanel jp = new JPanel(layout);
 * RowLayout.Constraint lc = new RowLayout.Constraint();
 * layout.setRowWeight(0,1f);      //vertical glue
 * lc.set(1,1f); jp.add(comp1,lc);
 * lc.set(1,1f); jp.add(Box.createGlue(),lc);
 * lc.set(1,0f); jp.add(comp2,lc);
 * lc.set(2,0f); jp.add(comp3,lc);
 * lc.set(3,1f); jp.add(comp4,lc);
 * layout.setRowWeight(4,1f);      //vertical glue
 * </pre>
 *
 * 
 * @version 1.1 07/05/2001
 * 
 */
public final class RowLayout implements LayoutManager2, Serializable
{
    private WidthManager[] _xxxx;
    private HeightManager _yyyy;
    private boolean _valid = false;
    private int _minLayoutWidth;    //minus container insets
    private int _prefLayoutWidth;   //minus container insets

    /**
     * RowLayout's component constraint object. You must pass it as the
     * constraint when you add a component to a container.
     */
    public static class Constraint
    {
        /**
         * The component's row number.
         */
        public int row;

        /**
         * The component's weight. If zero, the component will not grow
         * horizontally. If greater than zero, the component will get empty
         * space proportional to its weight divided by the sum of all weights
         * in the same row. The default weight of any component is 0.0f.
         */
        public float weight = 0f;

        /**
         * A convenience method to set the constraints.
         * @param row The row number, starting at 0.
         * @param weight A weight value between 0f and 1f.
         */
        public void set(int row, float weight)
        {
            this.row = row;
            this.weight = weight;
        }
    }


    /**
     * Construct a RowLayout layout manager.
     * @param rows The number of rows must be greater than zero.
     * @param hgap The horizontal gap between columns. Default is zero.
     * @param vgap The vertical gap between rows. Default is zero.
     */
    public RowLayout(int rows, int hgap, int vgap)
    {
        if (rows < 1) rows = 1;
        if (hgap < 0) hgap = 0;
        if (vgap < 0) vgap = 0;
        _yyyy = new HeightManager(rows,vgap);
        _xxxx = new WidthManager[rows];
        for (int r = 0; r < rows; r++)
        {
            _xxxx[r] = new WidthManager(hgap);
        }
    }

    /**
     * Construct a RowLayout layout manager.
     * @param rows The number of rows must be greater than zero.
     */
    public RowLayout(int rows)
    {
        this(rows,0,0);
    }

    /**
     * Set the horizontal gap, which is the gap between columns. The default
     * is zero pixels. It does not apply to the outside border.
     */
    public final void setHgap(int gap)
    {
        _valid = false;
        for (int n = 0; n < _xxxx.length; n++)
        {
            _xxxx[n].setGap(gap);
        }
    }

    /**
     * Set the vertical gap, which is the gap between rows. The default
     * is zero pixels. It does not apply to the outside border.
     */
    public final void setVgap(int gap)
    {
        _yyyy.setGap(gap);
        _valid = false;
    }

    /**
     * Set a border.
     */
    public final void setInsets(Insets inset)
    {
        _yyyy.setMargins(inset.top,inset.bottom);
        for (int n = 0; n < _xxxx.length; n++)
        {
            _xxxx[n].setMargins(inset.left,inset.right);
        }
        _valid = false;
    }

    /**
     * Set a row weight for all rows. The default weight of any row is 0.0f.
     */
    public final void setRowWeight(float weight)
    {
        _yyyy.setRowWeight(weight);
    }

    /**
     * Set a row's weight. The default weight of any row is 0.0f.
     */
    public final void setRowWeight(int row, float weight)
    {
        _yyyy.setRowWeight(row,weight);
    }

    /**
     * Get the horizontal alignment. RowLayout does not use alignment.
     */
    public final float getLayoutAlignmentX(Container parent)
    {
        return 0.5f;
    }

    /**
     * Get the vertical alignment. RowLayout does not use alignment.
     */
    public final float getLayoutAlignmentY(Container parent)
    {
        return 0.5f;
    }

    /**
     * Invalidate the layout.
     */
    public final void invalidateLayout(Container parent)
    {
        _valid = false;
    }

    /**
     * Remove a component from the layout.
     */
    public void removeLayoutComponent(Component comp)
    {
        _valid = false;
        for (int r = 0; r < _xxxx.length; r++)
        {
            _xxxx[r].removeComponent(comp);
        }
    }

    /**
     * Add a component to the layout. This is not a valid method for this
     * layout manager.
     */
    public void addLayoutComponent(String name, Component comp)
    {
        addLayoutComponent(comp, name);
    }

    /**
     * Add a component to the layout.
     * @param comp A component
     * @param constraint This must be a RowLayout.Constraint object.
     */
    public void addLayoutComponent(Component comp, Object constraint)
    {
        _valid = false;
        if (constraint instanceof Constraint)
        {
            Constraint r = (Constraint)constraint;
            if (r.row >= _xxxx.length)
            {
                throw new IllegalArgumentException("Bad constraint row number");
            }
            else
            {
                WidthManager wm = _xxxx[r.row];
                wm.addComponent(comp,r.weight);
            }
        }
        else
        {
            throw new IllegalArgumentException("The constraint must be RowLayout.Constraint");
        }
    }

    private void validate()
    {
        _valid = true;
        _minLayoutWidth = 0;
        _prefLayoutWidth = 0;

        for (int r = 0; r < _xxxx.length; r++)
        {
            WidthManager wm = _xxxx[r];
            wm.validate();

            _yyyy.setPreferredRowHeight(r,wm.getPreferredHeight());
            int w = wm.getPreferredWidth();
            if (w > _prefLayoutWidth) _prefLayoutWidth = w;

            _yyyy.setMinimumRowHeight(r,wm.getMinimumHeight());
            w = wm.getMinimumWidth();
            if (w > _minLayoutWidth) _minLayoutWidth = w;
        }

        _yyyy.validate();
    }

    /**
     * Get the container's minimum layout size.
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        if (!_valid) validate();
        Insets insets = parent.getInsets();
        int height = insets.top + insets.bottom + _yyyy.getMinimumHeight();
        int width = insets.left + insets.right + _minLayoutWidth;
        return new Dimension(width,height);
    }

    /**
     * Get the container's preferred layout size.
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        if (!_valid) validate();
        Insets insets = parent.getInsets();
        int height = insets.top + insets.bottom + _yyyy.getPreferredHeight();
        int width = insets.left + insets.right + _prefLayoutWidth;
        return new Dimension(width,height);
    }

    /**
     * Get the container's maximum layout size.
     */
    public Dimension maximumLayoutSize(Container parent)
    {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Lay out the container's components.
     */
    public void layoutContainer(Container parent)
    {
        Dimension parentSize = parent.getSize();
        if (parentSize.height <= 0 || parentSize.width <= 0) return;

        if (!_valid) validate();

        _yyyy.doLayout(parent);

        for (int r = 0; r < _xxxx.length; r++)
        {
            int ystart = _yyyy.getRowStart(r);
            int yheight = _yyyy.getRowHeight(r);
            _xxxx[r].doLayout(parent,ystart,yheight);
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
