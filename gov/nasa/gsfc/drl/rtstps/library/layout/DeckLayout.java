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
import java.util.ArrayList;

/**
 * This layout manager is a CardLayout substitute. If fixes one bug in
 * removeComponent() and has enhanced capabilities. When you add components
 * to a container that is using DeckLayout, you must add a constraint, which
 * is a string that is a unique name for the component. DeckLayout stores the
 * components in a list in the order in which they are added to the layout.
 * 
 * @version 1.1 07/05/2001
 * 
 */
public class DeckLayout implements LayoutManager2, java.io.Serializable
{
    private static final Dimension MAXLAYOUTSIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    /** A list of all components (Info) controlled by this layout manager. */
    private ArrayList<Info> _data = new ArrayList<Info>();		
    /** Index of the current visible component. */
    private int _showing = 0;
    /** My insets. */
    private Insets _insets = new Insets(0,0,0,0);
    /** The container I am laying out. */
    private Container _container;

    /**
     * An internal class with no expected public access. Each instance is one
     * component that I am laying out.
     */
    final class Info
    {
        String name;
        Component comp;

        Info(String nm, Component c)
        {
            name = nm;
            comp = c;
        }
    }

    /**
     * The DeckLayout constructor.
     * @param parent The container that uses this layout manager.
     */
    public DeckLayout(Container parent)
    {
        _container = parent;
    }

    /**
     * Add a component to the layout.
     * @param comp The component to be added
     * @param constraint This must be a string. It is a label by which
     *      DeckLayout references the component. It must be unique.
     */
    public void addLayoutComponent(Component comp, Object constraint)
    {
        if (constraint instanceof String)
        {
            String name = (String)constraint;
            if (find(name) != -1)
            {
                throw new IllegalArgumentException("Duplicate component name");
            }
            /** All components but the first are invisible until shown. */
            if (_data.size() > 0) comp.setVisible(false);
            _data.add(new Info(name,comp));
        }
        else
        {
            throw new IllegalArgumentException("constraint must be String");
        }
    }

    /**
     * Add a component to the layout.
     * @param name This string is a label by which DeckLayout references the
     *          component. It must be unique.
     * @param comp The component to be added
     */
    public final void addLayoutComponent(String name, Component comp)
    {
        addLayoutComponent(comp,name);
    }

    /**
     * Find a component by name in my internal list.
     * @return the index of the component or -1 if the name is not a component
     * name.
     */
    private final int find(String name)
    {
        int index = -1;
        for (int n = 0; n < _data.size(); n++)
        {
            Info info = (Info)_data.get(n);
            if (name.equals(info.name))
            {
                index = n;
                break;
            }
        }
        return index;
    }

    /**
     * Set a blank border.
     */
    public final void setInsets(Insets i)
    {
        _insets = i;
    }

    /**
     * Remove a component from the layout. DeckLayout will not remove a
     * component if it is visible or the last one in the layout.
     */
    public void removeLayoutComponent(Component comp)
    {
        if (_data.size() <= 1)
        {
            throw new IllegalArgumentException("Cannot remove last component");
        }

        if (comp.isVisible())
        {
            throw new IllegalArgumentException("Cannot remove visible component");
        }

        for (int n = 0; n < _data.size(); n++)
        {
            Info info = (Info)_data.get(n);
            if (comp == info.comp)
            {
                _data.remove(n);
                break;
            }
        }
    }

    /**
     * Get the preferred layout size.
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        Insets insets = parent.getInsets();
        int ncomponents = parent.getComponentCount();
        int w = 0;
        int h = 0;

        for (int i = 0; i < ncomponents; i++)
        {
            Component comp = parent.getComponent(i);
            Dimension d = comp.getPreferredSize();
            if (d.width > w) w = d.width;
            if (d.height > h) h = d.height;
        }

        w += (insets.left + insets.right + _insets.left + _insets.right);
        h += (insets.top + insets.bottom + _insets.top + _insets.bottom);
        return new Dimension(w,h);
    }

    /**
     * Get the minimum layout size.
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        Insets insets = parent.getInsets();
        int ncomponents = parent.getComponentCount();
        int w = 0;
        int h = 0;

        for (int i = 0; i < ncomponents; i++)
        {
            Component comp = parent.getComponent(i);
            Dimension d = comp.getMinimumSize();
            if (d.width > w) w = d.width;
            if (d.height > h) h = d.height;
        }

        w += (insets.left + insets.right + _insets.left + _insets.right);
        h += (insets.top + insets.bottom + _insets.top + _insets.bottom);
        return new Dimension(w,h);
    }

    /**
     * Get the maximum layout size.
     */
    public Dimension maximumLayoutSize(Container parent)
    {
        return MAXLAYOUTSIZE;
    }

    /**
     * Get DeckLayout's X alignment. DeckLayout does not use the X alignment,
     * so this value is always 0.5.
     */
    public final float getLayoutAlignmentX(Container parent)
    {
        return 0.5f;
    }

    /**
     * Get DeckLayout's Y alignment. DeckLayout does not use the Y alignment,
     * so this value is always 0.5.
     */
    public final float getLayoutAlignmentY(Container parent)
    {
        return 0.5f;
    }

    /**
     * Invalidate the layout.
     */
    public void invalidateLayout(Container parent)
    {
    }

    /**
     * Lay out the components in the parent container.
     */
    public void layoutContainer(Container parent)
    {
        Insets insets = parent.getInsets();
        Dimension psize = parent.getSize();
        Info info = (Info)_data.get(_showing);
        int x = insets.left + _insets.left;
        int y = insets.top + _insets.top;
        int w = psize.width - x - insets.right - _insets.right;
        int h = psize.height - y - insets.bottom - _insets.bottom;
        info.comp.setBounds(x,y,w,h);
    }

    /**
     * Show the component identified by the name index.
     */
    private void show(int index)
    {
        Info info = (Info)_data.get(_showing);
        info.comp.setVisible(false);
        _showing = index;
        info = (Info)_data.get(_showing);
        info.comp.setVisible(true);
        _container.validate();
    }

    /**
     * Show the first component.
     */
    public void showFirst()
    {
        show(0);
    }

    /**
     * Get the first component.
     */
    public Component getFirstComponent()
    {
        Info info = (Info)_data.get(0);
        return info.comp;
    }

    /**
     * Show the next component after the currently showing one. If the
     * current component is the last one, show the first component.
     */
    public void showNext()
    {
        int index = _showing + 1;
        if (index == _data.size()) index = 0;
        show(index);
    }


    /**
     * Get the next component after the currently showing one. If the
     * current component is the last one, get the first component.
     */
    public Component getNextComponent()
    {
        int index = _showing + 1;
        if (index == _data.size()) index = 0;
        Info info = (Info)_data.get(index);
        return info.comp;
    }

    /**
     * Show the component that precedes the currently showing one. If the
     * current component is the first one, show the last component.
     */
    public void showPrevious()
    {
        int index = _showing - 1;
        if (index == -1) index = _data.size() - 1;
        show(index);
    }


    /**
     * Get the component that precedes the currently showing one. If the
     * current component is the first one, get the last component.
     */
    public Component getPreviousComponent()
    {
        int index = _showing - 1;
        if (index == -1) index = _data.size() - 1;
        Info info = (Info)_data.get(index);
        return info.comp;
    }

    /**
     * Show the last component.
     */
    public void showLast()
    {
        show(_data.size() - 1);
    }

    /**
     * Get the last component.
     */
    public Component getLastComponent()
    {
        Info info = (Info)_data.get(_data.size()-1);
        return info.comp;
    }

    /**
     * Get the currently visible component.
     */
    public Component getVisibleComponent()
    {
        Info info = (Info)_data.get(_showing);
        return info.comp;
    }

    /**
     * Get the name index of the currently visible component.
     */
    public int getVisibleComponentIndex()
    {
        return _showing;
    }

    /**
     * Determine if the last component is currently showing.
     */
    public boolean isLastShowing()
    {
        return _showing == (_data.size()-1);
    }

    /**
     * Determine if the first component is currently showing.
     */
    public boolean isFirstShowing()
    {
        return _showing == 0;
    }

    /**
     * Get the name of the currently visible component.
     */
    public String getVisibleComponentName()
    {
        Info info = (Info)_data.get(_showing);
        return info.name;
    }

    /**
     * Show the component with the passed component name. If the name does not
     * exist, the method throws an IllegalArgumentException.
     */
    public void show(String name)
    {
        int index = find(name);
        if (index == -1)
        {
            throw new IllegalArgumentException(name + " does not exist");
        }
        show(index);
    }

    /**
     * Get the component with the passed name. If the name does not exist,
     * the method throws an IllegalArgumentException.
     */
    public Component getComponent(String name)
    {
        int index = find(name);
        if (index == -1)
        {
            throw new IllegalArgumentException(name + " does not exist");
        }
        Info info = (Info)_data.get(index);
        return info.comp;
    }
    
    private static final long serialVersionUID = 1L;			
}
