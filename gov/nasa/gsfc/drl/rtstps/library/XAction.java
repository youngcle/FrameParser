/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.library;

/**
 * This class is an extension to <code>AbstractAction</code>. It adds some
 * useful methods that are not in <code>AbstractAction</code>, and I take
 * advantage of them when I make toolbars and menus.
 * <code>AbstractAction</code> uses the technique of storing attributes
 * with text keys in a map, which always seemed silly to me when you have
 * a perfectly good convention of set/get method calls.
 * 
 * @version 1.1 07/05/2001
 */
public abstract class XAction extends javax.swing.AbstractAction
        implements java.awt.event.ActionListener
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String label;

    /**
     * Define an Action object with the specified label and icon.
     */
    protected XAction(String label, javax.swing.Icon icon)
    {
        super(label,icon);
        this.label = label;
    }

    /**
     * Define an Action object with the specified label and a default icon.
     */
    protected XAction(String label)
    {
        super(label);
        this.label = label;
    }

    /**
     * Get the text associated with this action.
     * @return label
     */
    public final String getText()
    {
        return label;
    }

    /**
     * Get the mnemonic character associated with the action label.
     * @return the mnemonic character or null if there is none.
     */
    public abstract char getMnemonic();

    /**
     * Get the action's tooltip.
     * @return a tooltip string or null if there is none.
     */
    public abstract String getToolTip();

    /**
     * Get a hotkey combination that activates the action. Here's an example
     * that links control-A to the action.
     * <pre>
     *  public KeyStroke getKeyStroke()
     *  {
     *      return KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,
     *               java.awt.event.InputEvent.CTRL_MASK);
     *  }
     * </pre>
     * @return the associated keystroke combination
     */
    public abstract javax.swing.KeyStroke getKeyStroke();

    /**
     * The action that is performed when the button is pressed or the menu
     * item is selected.
     */
    public abstract void actionPerformed(java.awt.event.ActionEvent e);
}
