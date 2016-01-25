/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.library;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.event.SwingPropertyChangeSupport;

/**
 * This class implements a list model to be used by JList and JComboBox.
 * It also possesses features so that it can be used by AComboModel.
 * <p>
 * AListModel combines a java.util.List with an AbstractListModel so that you
 * can use it as a java List and also plug it into a JList. AbstractListModel
 * does not give you these features. This is not an abstract class, so you can
 * use it to create a model.
 * <p>
 * You can add, change, or remove items in the list through AListModel, or you
 * can change the list directly. However, if you modify the list outside of
 * AListModel, you must call AListModel's fireContentsChanged method afterwards
 * to force the graphics component (JList or JComboBox) to update its view.
 * <p>
 * The model has property change methods that tell listeners if the model has
 * switched from empty to non-empty or non-empty to empty state. Although not
 * directly used by JList or JComboBox, other listeners can use the property
 * to enable or disable a JList or JComboBox depending on available list data.
 * Attach a property change listener and wait for the "empty" property to
 * change to true or false.
 *
 * 
 * @version 2.0 08/13/2001
 * 
 */
public class AListModel extends javax.swing.AbstractListModel
{
    protected java.util.List<String> data;				
    private SwingPropertyChangeSupport propSupport =
      new SwingPropertyChangeSupport(this);

    /**
     * An empty constructor. The initial capacity is 10 elements.
     */
    public AListModel()
    {
        data = new java.util.ArrayList<String>(10);			
    }

    /**
     * Construct an AListModel.
     * @param initialCapacity The initial list capacity.
     */
    public AListModel(int initialCapacity)
    {
        data = new java.util.ArrayList<String>(initialCapacity);	
    }

    /**
     * This AListModel constructor creates a model constructed from the
     * passed collection.
     */
    public AListModel(java.util.Collection<String> c)			
    {
        data = new java.util.ArrayList<String>(c);			
    }

    /**
     * Construct an AListModel using the passed list as a container.
     */
    public AListModel(java.util.List<String> list)			
    {
        data = list;
    }

    /**
     * Get the current number of elements in the model. This method satisfies
     * the ListModel interface.
     */
    public int getSize()
    {
        return data.size();
    }

    /**
     * Change the underlying collection to a different list.
     * The model is notified of the change, and property change listeners
     * will receive a property change if the state changes from empty to
     * non-empty or non-empty to empty.
     */
    public void setData(java.util.List<String> list)			
    {
        boolean empty = data.isEmpty();
        List<String> oldData = data;
        data = list;
        super.fireContentsChanged(this,-1,-1);
        propSupport.firePropertyChange("reset",oldData,data);
        if (empty != data.isEmpty())
        {
            propSupport.firePropertyChange("empty",empty,data.isEmpty());
        }
    }

    /**
     * Remove all elements from the model. Property change listeners
     * will receive a property change event if the state changes from
     * non-empty to empty.
     */
    public void clear()
    {
        boolean empty = data.isEmpty();
        data.clear();
        super.fireContentsChanged(this,-1,-1);
        propSupport.firePropertyChange("reset",null,data);
        if (!empty)
        {
            propSupport.firePropertyChange("empty",empty,data.isEmpty());
        }
    }

    /**
     * Get the element at index <code>index</code>. This method satisfies
     * the ListModel interface. It is the same as <code>get(int)</code>.
     */
    public Object getElementAt(int index)
    {
        return data.get(index);
    }

    /**
     * Get the element at index <code>index</code>.
     */
    public Object get(int index)
    {
        return data.get(index);
    }

    /**
     * Notify the model that the contents have changed. If you change this
     * model's list outside of this model, you must call this method after
     * you change the list.
     */
    public void fireContentsChanged()
    {
        super.fireContentsChanged(this,-1,-1);
    }

    /**
     * Determine if the model has no elements.
     */
    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    /**
     * Get the array index of an object. The object matches via the equals
     * method.
     * @return -1 if the object is not in the model.
     */
    public int indexOf(Object obj)
    {
        return data.indexOf(obj);
    }

    /**
     * Determine if the model contains the object via the equals method.
     */
    public boolean contains(Object obj)
    {
        return data.contains(obj);
    }

    /**
     * Find an object in the model via the equals method.
     */
    public Object find(Object obj)
    {
        int n = data.indexOf(obj);
        obj = (n == -1)? null : get(n);
        return obj;
    }

    /**
     * Sort the objects in natural order. The objects must implement the
     * Comparable interface.
     * @see Comparable
     */
    public void sort()
    {
        java.util.Collections.sort(data);
        super.fireContentsChanged(this,0,data.size()-1);
    }

    /**
     * Append an object to the model but only if it is not a duplicate (via
     * isEquals). It ignores duplicates. Property change listeners will
     * receive an event message if the state changes from empty to non-empty.
     */
    public void add(String obj)						
    {
        if (!data.contains(obj))
        {
            boolean empty = data.isEmpty();
            data.add(obj);
            if (empty != data.isEmpty())
            {
                propSupport.firePropertyChange("empty",true,false);
            }
            int index = data.size() - 1;
            fireIntervalAdded(this,index,index);
        }
    }

    /**
     * Add an object to the model but only if it is not a duplicate (via
     * isEquals). It ignores duplicates. Property change listeners will
     * receive an event message if the state changes from empty to non-empty.
     * @param index The object is placed at this index location.
     */
    public void add(int index, String obj)				
    {
        if (!data.contains(obj))
        {
            boolean empty = data.isEmpty();
            data.add(index,obj);
            if (empty != data.isEmpty())
            {
                propSupport.firePropertyChange("empty",true,false);
            }
            fireIntervalAdded(this,index,index);
        }
    }

    /**
     * Remove an object from the model. Property change listeners will receive
     * an event message if the state changes from non-empty to empty.
     * @param index Remove the object at the index location.
     */
    public void remove(int index)
    {
        boolean empty = data.isEmpty();
        Object obj = data.get(index);
        data.remove(index);
        propSupport.firePropertyChange("delete",null,obj);
        if (empty != data.isEmpty())
        {
            propSupport.firePropertyChange("empty",false,true);
        }
        fireIntervalRemoved(this,index,index);
    }

    /**
     * Remove an object from the model. If the object is not in the model,
     * nothing changes. Property change listeners will receive an event
     * message if the state changes from non-empty to empty.
     */
    public void remove(Object obj)
    {
        int index = data.indexOf(obj);
        if (index != -1) remove(index);
    }

    /**
     * Add a property change listener to listen for the "empty" property.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener to listen for the "empty" property.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propSupport.removePropertyChangeListener(listener);
    }
    
    private static final long serialVersionUID = 1L;			
}
