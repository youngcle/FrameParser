/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.library;
import javax.swing.event.ListDataListener;

/**
 * This class promotes the model AListModel (a JList model) to a combo box
 * model. It uses the decorator pattern to enhance AListModel. Typically you
 * construct an AListModel first. The code looks like this:
 * <code>
 * AListModel alm = new AListModel();
 * AComboModel acm = new AComboModel(alm);
 * JComboBox combo = new JComboBox(acm);
 * </code>
 * 
 * @version 2.0 08/13/2001
 * 
 */
public class AComboModel implements javax.swing.ComboBoxModel
{
    /**
     * The underlying list model.
     */
    protected AListModel listModel;
    private Object selected = null;

    /**
     * Construct an AComboModel object from an AListModel object.
     * @param alm An AListModel object.
     */
    public AComboModel(AListModel alm)
    {
        listModel = alm;
    }

    /**
     * Add a ListDataListener to the model.
     */
    public void addListDataListener(ListDataListener l)
    {
        listModel.addListDataListener(l);
    }

    /**
     * Remove a ListDataListener.
     */
    public void removeListDataListener(ListDataListener l)
    {
        listModel.removeListDataListener(l);
    }

    /**
     * Get the number of items in the list.
     * @return the list size
     */
    public int getSize()
    {
        return listModel.getSize();
    }

    /**
     * Get the object at list index <code>index</code>
     * @return the object at the specified index
     */
    public Object getElementAt(int index)
    {
        return listModel.getElementAt(index);
    }

    /**
     * Determine if the model contains the object via the equals method.
     */
    public boolean contains(Object obj)
    {
        return listModel.contains(obj);
    }

    /**
     * Append an object to the model but only if it is not a duplicate (via
     * isEquals). It ignores duplicates. Property change listeners will
     * receive an event message if the state changes from empty to non-empty.
     */
    public void add(String obj)						
    {
        /** listModel ignores duplicates. */
        listModel.add(obj);
    }

    /**
     * Set the selected object. If the object is null, it shows a blank
     * entry in the combo box input cell.
     * @param obj The selected item
     */
    public void setSelectedItem(Object obj)
    {
        selected = obj;
        listModel.fireContentsChanged();
    }

    /**
     * Get the selected item object.
     * @return Returns the selected item. It returns null if there is no
     * selected object.
     */
    public Object getSelectedItem()
    {
        return selected;
    }
}
