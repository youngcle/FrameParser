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
import java.awt.Rectangle;
import java.io.Serializable;

/**
 * This class is a layout manager that lays out components in an adjustable
 * grid. It is similar to GridLayout with several improvements. The rows and
 * columns are not the same size by default. GridLayout determines the width
 * of a column or the height of a row by using the maximum preferred size
 * of all components in that row or column. You can configure ScaledGridLayout
 * so that it does not grow a component beyond its maximum size. If a component
 * is smaller than its cell, it sizes it to its maximum size and places in the
 * cell based on the layout manager's X and Y alignments. Use
 * <code>honorMaxSize(true)</code> to enable this feature. ScaledGridLayout
 * adds components in a specific order -- across a column before progressing to
 * the next row.
 * <p>
 * You may set hgap and vgap, which is the gap between components. The default
 * is five pixels each. It does not apply to the border.
 * <p>
 * You may fix the size of columns or rows, which then work like GridLayout.
 * All rows or columns are then the same size.
 * <p>
 * Use Box's struts, glue, and rigid to insert blank components.
 * <p>
 * You may set an inset, which is a blank border.
 * <p>
 * <code>
 * JPanel jp = new JPanel(new ScaledGridLayout(3,4)); //3 rows, 4 columns
 * jp.add(comp1);
 * jp.add(comp2);
 * </code>
 * 
 * @version 1.1 07/05/2001
 * 
 */
public final class ScaledGridLayout implements LayoutManager2, Serializable
{
    private Axis _rows = new Axis();
    private Axis _columns = new Axis();
    private Insets _insets = new Insets(0,0,0,0);
    private boolean _valid = false;
    private int _hgap = 5;
    private int _vgap = 5;
    private boolean _honorMaxSize = false;
    private float _alignX = 0.5f;
    private float _alignY = 0.5f;

    /**
     * Create a ScaledGridLayout object with one row and hgap=5, vgap=5.
     */
    public ScaledGridLayout()
    {
        this(1,0,5,5,false);
    }

    /**
     * Create a ScaledGridLayout object with hgap=5 and vgap=5.
     * @param rows Number of rows
     * @param columns Number of columns
     */
    public ScaledGridLayout(int rows, int columns)
    {
        this(rows,columns,5,5,false);
    }

    /**
     * Create a ScaledGridLayout object.
     * @param rows Number of rows
     * @param columns Number of columns
     * @param hgap Horizontal gap between units in a row.
     * @param vgap Vertical gap between units in a column.
     */
    public ScaledGridLayout(int rows, int columns, int hgap, int vgap)
    {
        this(rows,columns,hgap,vgap,false);
    }

    /**
     * Create a ScaledGridLayout object.
     * @param rows Number of rows
     * @param columns Number of columns
     * @param hgap Horizontal gap between units in a row.
     * @param vgap Vertical gap between units in a column.
     * @param honorMaximumSize If true, the layout will not resize any
     *          component above its maximum size.
     */
    public ScaledGridLayout(int rows, int columns, int hgap, int vgap,
            boolean honorMaximumSize)
    {
        if (rows <= 0 && columns <= 0)
        {
            throw new IllegalArgumentException("both rows and columns cannot be zero");
        }

        /** rows or columns may be zero, so I cannot construct anything else yet. */
        _hgap = hgap;
        if (_hgap < 0) _hgap = 0;
        _vgap = vgap;
        if (_vgap < 0) _vgap = 0;
        _rows.count = rows;
        _columns.count = columns;
        _honorMaxSize = honorMaximumSize;
    }

    /**
     * Set the horizontal gap between units on a row. It does not apply
     * to the border.
     */
    public final void setHgap(int gap)
    {
        _hgap = gap;
    }
    /**
     * Set the vertical gap between units in a column. It does not apply
     * to the border.
     */
    public final void setVgap(int gap)
    {
        _vgap = gap;
    }

    /**
     * If set to true, the layout manager will not increase any component's
     * size above its maximum. If the cell is bigger than a component, the
     * manager will locate it in the cell depending on the manager X and Y
     * alignment. (0f = top or left; 1f = right or bottom.) The default
     * alignment is centered (.5f, .5f).
     */
    public final void honorMaxSize(boolean b)
    {
        _honorMaxSize = b;
    }

    /**
     * Set insets, which is a blank border. The default is no border.
     */
    public final void setInsets(Insets in)
    {
        _insets = in;
    }

    /**
     * Get the insets.
     * @return Return the insets.
     */
    public final Insets getInsets()
    {
        return _insets;
    }

    /**
     * Set the horizontal alignment. The default is 0f.
     * @param x Floating point between 0f and 1f.
     */
    public final void setLayoutAlignmentX(float x)
    {
        _alignX = x;
    }

    /**
     * Set the vertical alignment. The default is 0f.
     * @param y Floating point between 0f and 1f.
     */
    public final void setLayoutAlignmentY(float y)
    {
        _alignY = y;
    }

    /**
     * Set fixed height rows. If false, the row height can grow. Otherwise,
     * all rows have the same height, which is the same behaviour as
     * <code>GridLayout.</code> The default is false.
     */
    public final void setFixedRows(boolean ena)
    {
        _rows.fixedSize = ena;
        _valid = false;
    }

    /**
     * Set fixed height columns. If false, the column width can grow. Otherwise,
     * all columns have the same width, which is the same behaviour as
     * <code>GridLayout.</code> The default is false.
     */
    public final void setFixedColumns(boolean ena)
    {
        _columns.fixedSize = ena;
        _valid = false;
    }

    /**
     * Cause the layout manager to lay out the components.
     * @param target The container in which the manager is working.
     */
    public final void invalidateLayout(Container target)
    {
        _valid = false;
    }

    /**
     * Adds the specified component with the specified name to the layout.
     * It does not usually apply to this layout manager.
     */
    public final void addLayoutComponent(String name, Component comp)
    {
        addLayoutComponent(comp, name);
    }

    /**
     * Adds the specified component to the layout using the specified
     * constraint object. It does not usually apply to this layout manager.
     */
    public final void addLayoutComponent(Component comp, Object constraints)
    {
        _valid = false;
    }

    /**
     * Remove a component from the layout.
     */
    public final void removeLayoutComponent(Component comp)
    {
        _valid = false;
    }

    /**
     * Get the X alignment.
     */
    public final float getLayoutAlignmentX(Container parent)
    {
        return _alignX;
    }

    /**
     * Get the Y alignment.
     */
    public final float getLayoutAlignmentY(Container parent)
    {
        return _alignY;
    }

    /**
     * Lay out the container.
     */
    public void layoutContainer(Container parent)
    {
        Component[] comp = parent.getComponents();
        if (comp.length == 0) return;

        Dimension parentSize = parent.getSize();
        if (parentSize.height <= 0 || parentSize.width <= 0) return;

        if (!_valid) validate(parent);
        Insets insets = parent.getInsets();

        int pwidth = parentSize.width - insets.left - insets.right -
                _insets.left - _insets.right - (_columns.count - 1) * _hgap;
        int pheight = parentSize.height - insets.top - insets.bottom -
                _insets.top - _insets.bottom - (_rows.count - 1) * _vgap;

        Rectangle cell = new Rectangle();
        cell.y = insets.top + _insets.top;
        cell.height = pheight / _rows.count;
        

        for (int r = 0; r < _rows.count; r++)
        {
            cell.x = insets.left + _insets.left;
            if (!_rows.fixedSize)
            {
                cell.height = (int)((float)pheight * _rows.scale[r]);
            }

            for (int c = 0; c < _columns.count; c++)
            {
                int n = r * _columns.count + c;
                if (n >= comp.length) continue;
                if (!_columns.fixedSize)
                {
                    cell.width = (int)((float)pwidth * _columns.scale[c]);
                }
                if (_honorMaxSize)
                {
                    locateInCell(comp[n],cell);
                }
                else
                {
                    comp[n].setBounds(cell);
                }
                cell.x += cell.width + _hgap;
            }

            cell.y += cell.height + _vgap;
        }
    }

    /**
     * Position a component in a cell.
     */
    private void locateInCell(Component comp, Rectangle cell)
    {
        Dimension m = comp.getMaximumSize();
        Rectangle box = new Rectangle(cell);
        if (m.width < cell.width)
        {
            int a = (int)(_alignX * (float)m.width);
            int b = (int)(_alignX * (float)cell.width);
            box.x += (b - a);
            box.width = m.width;
        }

        if (m.height < cell.height)
        {
            int a = (int)(_alignY * (float)m.height);
            int b = (int)(_alignY * (float)cell.height);
            box.y += (b - a);
            box.height = m.height;
        }
        comp.setBounds(box);
    }

    /**
     * Get the minimum layout size.
     * @return A dimension object containing the minimum height and width.
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        if (!_valid) validate(parent);
        Insets insets = parent.getInsets();

        int width = (_columns.count - 1) * _hgap + insets.left +
                insets.right + _insets.left + _insets.right +
                _columns.minimumLayoutSize;

        int height = (_rows.count - 1) * _vgap + insets.top +
                insets.bottom + _insets.top + _insets.bottom +
                _rows.minimumLayoutSize;

        return new Dimension(width,height);
    }

    /**
     * Get the preferred layout size.
     * @return A dimension object containing the preferred height and width.
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        if (!_valid) validate(parent);
        Insets insets = parent.getInsets();

        int width = (_columns.count - 1) * _hgap + insets.left +
                insets.right + _insets.left + _insets.right +
                _columns.preferredLayoutSize;

        int height = (_rows.count - 1) * _vgap + insets.top +
                insets.bottom + _insets.top + _insets.bottom +
                _rows.preferredLayoutSize;

        return new Dimension(width,height);
    }

    /**
     * Get the maximum layout size.
     * @return A dimension object containing the maximum height and width.
     */
    public final Dimension maximumLayoutSize(Container parent)
    {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Prepare the container for layout.
     */
    private void validate(Container parent)
    {
        if (_rows.count <= 0 && _columns.count <= 0) return;
        int components = parent.getComponentCount();
        if (components == 0) return;

        if (_rows.count <= 0)
        {
            _rows.count = (components + _columns.count - 1) / _columns.count;
        }
        else if (_columns.count <= 0)
        {
            _columns.count = (components + _rows.count - 1) / _rows.count;
        }

        /** these setups allocate array if it hasn't been done. */
        _rows.setup();
        _columns.setup();

        setLayoutWidth(parent);
        setLayoutHeight(parent);

        _valid = true;
    }

    /**
     * Compute the layout heights -- min, max, and preferred.
     */
    private void setLayoutHeight(Container parent)
    {
        Component[] comp = parent.getComponents();
        if (comp.length == 0) return;

        int psum = 0;   //sum of the max preferred heights
        int ssum = 0;   //sum of the max minimum heights
        int pbig = 0;   //biggest of the max preferred heights
        int sbig = 0;   //biggest of the max minimum heights

        for (int r = 0; r < _rows.count; r++)
        {
            int pmax = 0;  //max of the preferred heights
            int smax = 0;  //max of the minimum heights
            for (int c = 0; c < _columns.count; c++)
            {
                int n = r * _columns.count + c;
                if (n >= comp.length) continue;
                Dimension d = comp[n].getPreferredSize();
                if (d.height > pmax) pmax = d.height;
                d = comp[n].getMinimumSize();
                if (d.width > smax) smax = d.height;
            }
            _rows.size[r] = pmax;
            psum += pmax;
            ssum += smax;
            if (pmax > pbig) pbig = pmax;
            if (smax > sbig) sbig = smax;
        }

        _rows.setup(psum,ssum,pbig,sbig);
    }

    /**
     * Compute the layout widths -- min, max, and preferred.
     */
    private void setLayoutWidth(Container parent)
    {
        Component[] comp = parent.getComponents();
        if (comp.length == 0) return;

        int psum = 0;   //sum of the max preferred widths
        int ssum = 0;   //sum of the max minimum widths
        int pbig = 0;   //biggest of the max preferred widths
        int sbig = 0;   //biggest of the max minimum widths

        for (int c = 0; c < _columns.count; c++)
        {
            int pmax = 0;  //max of the preferred widths
            int smax = 0;  //max of the minimum widths
            for (int r = 0; r < _rows.count; r++)
            {
                int n = r * _columns.count + c;
                if (n >= comp.length) continue;
                Dimension d = comp[n].getPreferredSize();
                if (d.width > pmax) pmax = d.width;
                d = comp[n].getMinimumSize();
                if (d.width > smax) smax = d.width;
            }
            _columns.size[c] = pmax;
            psum += pmax;
            ssum += smax;
            if (pmax > pbig) pbig = pmax;
            if (smax > sbig) sbig = smax;
        }

        _columns.setup(psum,ssum,pbig,sbig);
    }

    /**
     * Manages all rows or all columns.
     */
    final class Axis
    {
        int count = 0;
        int preferredLayoutSize = 0;    //does not include gaps and insets
        int minimumLayoutSize = 0;      //does not include gaps and insets
        int[] size = null;
        float[] scale = null;
        boolean fixedSize = false;

        void setup()
        {
            if (size == null || size.length < count)
            {
                size = new int[count];
                scale = new float[count];
            }
        }

        void setup(int psum, int ssum, int pbig, int sbig)
        {
            if (fixedSize)
            {
                preferredLayoutSize = count * pbig;
                minimumLayoutSize = count * sbig;
            }
            else
            {
                preferredLayoutSize = psum;
                minimumLayoutSize = ssum;
            }

            //This only matters when the axis is not fixed-size.
            if (psum > 0)
            {
                float ftotal = (float)psum;
                for (int c = 0; c < count; c++)
                {
                    scale[c] = (float)size[c] / ftotal;
                }
            }
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
