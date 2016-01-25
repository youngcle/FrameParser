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
import java.util.Vector;

import javax.swing.Box;

/**
 * This class is a layout manager. It arranges components in a vertical list
 * similar to a vertical Box. Unlike boxes and BoxLayout however, you may
 * specify vertical gaps and indentations. It also grows and shrinks
 * differently.
 * <p>
 * You may set a vertical gap, which is a mandatory spacing between components.
 * The default is 0 pixels.
 * <p>
 * You may add an indentation as a constraint (Integer(pixels)) when you add a
 * component to a container. ListLayout will then indent the component from the
 * left margin. ListLayout contains a static TAB=20 pixels and TABTAB=40 for
 * your convenience.
 * <p>
 * If you do not add glue to the container, then each component is given a cell
 * that is proportional to its preferred size. It grows to fill its cell unless
 * the cell is larger than the component's maximum size. In that case, the
 * manager uses its own x and y alignments (not the component's) to place the
 * component within the cell. By default, components are vertically centered and
 * left-justified. (x-alignment = 0f, y-alignment = 0.5f)
 * <p>
 * You may add Box's filler components. If you add glue, then the glue grabs
 * all extra space, and components grow no bigger than their preferred sizes.
 * <p>
 * <pre>
 * Integer tab = new Integer(10); //10 pixels indentation
 * Integer tab1 = new Integer(25); //25 pixels indentation
 * int gap = 5;
 * JPanel jp = new JPanel(new ListLayout(gap));
 * jp.add(comp1);  //no indentation
 * jp.add(comp2, tab);
 * jp.add(comp2, tab2);
 * jp.add(comp2, ListLayout.TAB);
 * jp.add(Box.createGlue());
 * </pre>
 * 
 * @version 1.1 07/05/2001
 * 
 */
public class ListLayout implements LayoutManager2, Serializable
{
    /** A default indentation. Use it as a constraint argument. */
    public static final Integer TAB = new Integer(20);

    /** A default indentation. Use it as a constraint argument. */
    public static final Integer TABTAB = new Integer(40);

    private static final Integer ZERO = new Integer(0);
    private static final Integer GLUE = new Integer(-1);
    private Vector<Integer> _indentation = new Vector<Integer>();	
    private Vector<Component> _comp = new Vector<Component>();		
    private Insets _insets = new Insets(0,0,0,0);
    private int _glues = 0;
    private int _gap = 0;
    private float _alignX = 0f;
    private float _alignY = 0.5f;
    private boolean _valid = false;

    /** These layout values do not include gaps and insets. */
    private int _minLayoutWidth;
    private int _maxLayoutWidth;
    private int _prefLayoutWidth;
    private int _minLayoutHeight;
    private int _maxLayoutHeight;
    private int _prefLayoutHeight;


    /**
     * Create a ListLayout object.
     * @param vgap Gap in pixels between components. The gap does not affect
     *          the border. The default is 0 pixels.
     */
    public ListLayout(int vgap)
    {
        _gap = vgap;
        if (_gap < 0) _gap = 0;
    }

    /**
     * Create a ListLayout object. There are no gaps between components.
     */
    public ListLayout()
    {}

    /**
     * Set a gap between components. The gap does not affect the border.
     */
    public final void setGap(int gap)
    {
        _gap = gap;
    }

    /**
     * Set a blank border.
     */
    public final void setInsets(Insets in)
    {
        _insets = in;
    }

    /**
     * Set the X alignment. The default value is 0.0, which causes the
     * components to left justified.
     */
    public final void setLayoutAlignmentX(float a)
    {
        _alignX = a;
    }

    /**
     * Set the Y alignment. The default value is 0.5, which causes the
     * components to centered.
     */
    public final void setLayoutAlignmentY(float a)
    {
        _alignY = a;
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
     * Invalidate the layout.
     */
    public final void invalidateLayout(Container parent)
    {
        _valid = false;
    }

    /**
     * Add a component to the layout. ListLayout does not support this method.
     */
    public void addLayoutComponent(String name, Component comp)
    {
        addLayoutComponent(comp, name);
    }

    /**
     * Remove a component from the layout. ListLayout ignores this method.
     */
    public void removeLayoutComponent(Component comp)
    {}

    /**
     * Add a component to the layout.
     * @param comp The component to be added to the layout.
     * @param constraint This object identifies the indentation. It may be null,
     *          in which case there is no indentation. Otherwise, it must be of
     *          type Integer, and it is the indentation in pixels. If the
     *          component is a Box filler type (glue, for example), the
     *          constraint is ignored.
     */
    public void addLayoutComponent(Component comp, Object constraint)
    {
        Integer i = ZERO;
        _valid = false;
        if ((comp instanceof Box.Filler) || (constraint == null))
        {
            Dimension d = comp.getPreferredSize();
            if (d.width == 0 && d.height == 0)
            {
                i = GLUE;
            }
            else
            {
                i = ZERO;
            }
        }
        else if (constraint instanceof Integer)
        {
            i = (Integer)constraint;
            if (i.intValue() < 0) i = ZERO;
        }
        else
        {
            throw new IllegalArgumentException("Constraint must be Integer");
        }
        _indentation.addElement(i);
        _comp.addElement(comp);
    }

    /**
     * Lay out the components in the container.
     */
    public void layoutContainer(Container parent)
    {
        Dimension parentSize = parent.getSize();
        if ((parentSize.height <= 0) || (parentSize.width <= 0)) return;

        if (!_valid) validate(parent);
        Insets insets = parent.getInsets();

        int count = _comp.size();
        int realCount = count - _glues;

        int pwidth = parentSize.width - (insets.left + insets.right +
                _insets.left + _insets.right);
        int pheight = parentSize.height - (insets.top + insets.bottom +
                _insets.top + _insets.bottom + (realCount - 1) * _gap);

        int x = _insets.left + insets.left;
        int y = _insets.top + insets.top;

        if ((_glues > 0) && (pheight > _prefLayoutHeight))
        {
            int space = (pheight - _prefLayoutHeight) / _glues;

            for (int n = 0; n < count; n++)
            {
                Integer iindent = (Integer)_indentation.elementAt(n);
                if (iindent == GLUE)
                {
                    y += space;
                    continue;
                }

                int indent = iindent.intValue();
                Component comp = (Component)_comp.elementAt(n);
                Dimension d = comp.getPreferredSize();

                Rectangle r = new Rectangle(x+indent,y,pwidth-indent,d.height);
                locateInCell(comp,r);
                y += (_gap + d.height);
            }
        }
        else
        {
            float vf = (float)pheight / (float)_prefLayoutHeight;
            for (int n = 0; n < count; n++)
            {
                Integer iindent = (Integer)_indentation.elementAt(n);
                if (iindent == GLUE) continue;

                int indent = iindent.intValue();
                Component comp = (Component)_comp.elementAt(n);
                Dimension d = comp.getPreferredSize();

                int h = (int)(vf * (float)d.height);
                Rectangle r = new Rectangle(x+indent,y,pwidth-indent,h);
                locateInCell(comp,r);
                y += (_gap + h);
            }
        }
    }

    /**
     * Get the minimum layout size.
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        if (!_valid) validate(parent);
        Insets insets = parent.getInsets();
        int width = addHorizontalBaggage(_minLayoutWidth,insets);
        int height = addVerticalBaggage(_minLayoutHeight,insets);
        return new Dimension(width,height);
    }

    /**
     * Get the preferred layout size.
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        if (!_valid) validate(parent);
        Insets insets = parent.getInsets();
        int width = addHorizontalBaggage(_prefLayoutWidth,insets);
        int height = addVerticalBaggage(_prefLayoutHeight,insets);
        return new Dimension(width,height);
    }

    /**
     * Get the maximum layout size.
     */
    public final Dimension maximumLayoutSize(Container parent)
    {
        if (!_valid) validate(parent);
        Insets insets = parent.getInsets();
        int width = addHorizontalBaggage(_maxLayoutWidth,insets);
        int height = addVerticalBaggage(_maxLayoutHeight,insets);
        return new Dimension(width,height);
    }

    private void validate(Container parent)
    {
        _valid = true;
        
        _minLayoutWidth = 0;
        _maxLayoutWidth = 0;
        _prefLayoutWidth = 0;
        _minLayoutHeight = 0;
        _maxLayoutHeight = 0;
        _prefLayoutHeight = 0;
        _glues = 0;

        int count = _comp.size();

        for (int n = 0; n < count; n++)
        {
            Integer iindent = (Integer)_indentation.elementAt(n);
            if (iindent == GLUE)
            {
                ++_glues;
                continue;
            }

            int indent = iindent.intValue();
            Component comp = (Component)_comp.elementAt(n);

            Dimension d = comp.getPreferredSize();
            int w = d.width + indent;
            if (w > _prefLayoutWidth) _prefLayoutWidth = w;
            _prefLayoutHeight += d.height;

            d = comp.getMinimumSize();
            w = d.width + indent;
            if (w > _minLayoutWidth) _minLayoutWidth = w;
            _minLayoutHeight += d.height;

            d = comp.getMaximumSize();
            if (_maxLayoutWidth != Integer.MAX_VALUE)
            {
                if (d.width == Integer.MAX_VALUE)
                {
                    _maxLayoutWidth = Integer.MAX_VALUE;
                }
                else
                {
                    w = d.width + indent;
                    if (w > _maxLayoutWidth) _maxLayoutWidth = w;
                }
            }
            if (_maxLayoutHeight != Integer.MAX_VALUE)
            {
                if (d.height == Integer.MAX_VALUE)
                {
                    _maxLayoutHeight = Integer.MAX_VALUE;
                }
                else
                {
                    _maxLayoutHeight += d.height;
                }
            }
        }
    }

    private int addHorizontalBaggage(int width, Insets insets)
    {
        int ww = insets.left + insets.right + _insets.left + _insets.right;
        return width + ww;
    }

    private int addVerticalBaggage(int height, Insets insets)
    {
        int hh = (_comp.size() - _glues - 1) * _gap;
        hh += (insets.top + insets.bottom + _insets.top + _insets.bottom);
        return height + hh;
    }

    private void locateInCell(Component comp, Rectangle r)
    {
        Dimension m = comp.getMaximumSize();
        if (m.width < r.width)
        {
            int a = (int)(_alignX * (float)m.width);
            int b = (int)(_alignX * (float)r.width);
            r.x += (b - a);
            r.width = m.width;
        }

        if (m.height < r.height)
        {
            int a = (int)(_alignY * (float)m.height);
            int b = (int)(_alignY * (float)r.height);
            r.y += (b - a);
            r.height = m.height;
        }
        comp.setBounds(r);
    }
    
    private static final long serialVersionUID = 1L;			
}
