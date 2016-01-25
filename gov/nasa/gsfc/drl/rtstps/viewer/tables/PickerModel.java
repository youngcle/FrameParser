/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.tables;

/**
 * This class is a list selection model. I use it in a JList to decide
 * which status elements I will show in a status table. The differences
 * between this model and the default one are (1) the user can select
 * and deselect single items by just clicking on them without needing
 * the control key. It adds or removes the element to the current
 * selection. The default model always clears all other selections and
 * requires that you use the control key to add selections. (2) This
 * model has a "select all" method, which you can wire to a button.
 * 
 * 
 */
class PickerModel extends javax.swing.DefaultListSelectionModel
{
    private int listSize;

    PickerModel(int listSize)
    {
        this.listSize = listSize;
    }

    final void setListSize(int size)
    {
        listSize = size;
    }

    /**
     * Select all elements.
     */
    final void selectAll()
    {
        super.setSelectionInterval(0,listSize-1);
    }

    /**
     * When a single item is selected, this method changes it to a
     * add/remove toggle.
     */
    public void setSelectionInterval(int i0, int i1)
    {
        if (i0 == i1)
        {
            if (isSelectedIndex(i0))
            {
                super.removeSelectionInterval(i0,i1);
            }
            else
            {
                super.addSelectionInterval(i0,i1);
            }
        }
        else
        {
            super.setSelectionInterval(i0,i1);
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
