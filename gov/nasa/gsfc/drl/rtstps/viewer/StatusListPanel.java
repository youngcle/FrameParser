/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;
import gov.nasa.gsfc.drl.rtstps.viewer.status.Distributor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * This class is a visual component that shows a list of status items.
 * 
 * 
 */
public class StatusListPanel extends javax.swing.JPanel
{
    private static final Dimension maxSize = new Dimension(0x0ffff,0x0ffff);
    private static final Font BORDER_FONT =
            new Font("SansSerif",Font.ITALIC|Font.BOLD,10);
    private int digitsInValueField;
    private Distributor distributor;

    /**
     * Create an AbstractStatusListPanel with a title.
     * @param title The panel title.
     * @param digitsInValueField Number of digits allocated to the value field
     * @param distributor The distributor
     */
    public StatusListPanel(String title,int digitsInValueField,
            Distributor distributor)
    {
        this.distributor = distributor;
        this.digitsInValueField = digitsInValueField;
        BoxLayout layout = new BoxLayout(this,BoxLayout.Y_AXIS);
        setLayout(layout);
        setMaximumSize(maxSize);
        Border ob = new EtchedBorder(EtchedBorder.RAISED);
        ob = BorderFactory.createTitledBorder(ob,title,
                TitledBorder.CENTER,TitledBorder.TOP,BORDER_FONT,Color.black);
        Border ib = BorderFactory.createEmptyBorder(6,6,5,5);
        setBorder(BorderFactory.createCompoundBorder(ob,ib));
    }

    /**
     * Create an AbstractStatusListPanel without a title.
     * @param distributor The distributor
     * @param digitsInValueField Number of digits allocated to the value field
      */
    public StatusListPanel(Distributor distributor, int digitsInValueField)
    {
        this.distributor = distributor;
        this.digitsInValueField = digitsInValueField;
        BoxLayout layout = new BoxLayout(this,BoxLayout.Y_AXIS);
        setLayout(layout);
        setMaximumSize(maxSize);
        Border ob = new EtchedBorder(EtchedBorder.RAISED);
        Border ib = BorderFactory.createEmptyBorder(6,6,5,5);
        setBorder(BorderFactory.createCompoundBorder(ob,ib));
    }

    /**
     * Add a TextAlarmGadget to the panel.
     * @param label A label to be shown on the screen.
     * @param item The full status identification of the item.
     * @param item2 A second item used to compute red and yellow limits.
     * @param yellowLimit A yellow limit ratio boundary (0.0..1.0)
     * @param redLimit A red limit ratio boundary.
     */
    public void addAlarmGadget(String label, String item, String item2,
            float yellowLimit, float redLimit)
    {
        TextAlarmGadget tag = new TextAlarmGadget(label,item,item2,
                yellowLimit, redLimit);
        distributor.requestStatusItemDelivery(tag,item);
        distributor.requestStatusItemDelivery(tag,item2);
        add(tag);
    }

    /**
     * Add a TextGadget to the panel.
     * @param label A label to be shown on the screen.
     * @param item The full status identification of the item.
     */
    public void addTextGadget(String label, String item)
    {
        TextGadget tg = new TextGadget(label, item);
        distributor.requestStatusItemDelivery(tg,item);
        add(tg);
    }

    /**
     * Set all gadets to the same size.
     */
    public void setGadgetsToSameSize()
    {
        java.awt.Component[] cl = getComponents();
        int labelLength = 0;
        int valueLength = digitsInValueField;

        for (int n = 0; n < cl.length; n++)
        {
            AbstractTextGadget g = (AbstractTextGadget)cl[n];
            int len = g.getLabelDigits();
            if (len > labelLength) labelLength = len;
            len = g.getValueDigits();
            if (len > valueLength) valueLength = len;
        }

        for (int n = 0; n < cl.length; n++)
        {
            AbstractTextGadget g = (AbstractTextGadget)cl[n];
            g.setFieldLengths(labelLength,valueLength);
        }
    }

    /**
     * Disconnect all gadgets from the distributor.
     */
    public void disconnect()
    {
        java.awt.Component[] cl = getComponents();

        for (int n = 0; n < cl.length; n++)
        {
            AbstractTextGadget g = (AbstractTextGadget)cl[n];
            String fullName = g.getFullStatusItemId();
            distributor.cancelStatusItemDelivery(g,fullName);
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
