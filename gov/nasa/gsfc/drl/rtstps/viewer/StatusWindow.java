/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer;

/**
 * This class is the base class for many status windows in the viewer.
 * Actions usually create it. It has a window id, which actions use to
 * ensure that there are no duplicate windows.
 * <p>
 * Derivatives should override dispose() to disconnect all connections
 * to the distributor as in this example:
 * <code>
 *   public final void dispose()
 *   {
 *       tableModel.dispose();
 *       super.dispose();
 *   }
 * </code>
 * 
 */
public abstract class StatusWindow extends javax.swing.JDialog
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final boolean MODAL_STATE = false;
    private String windowIdentity;

    /**
     * Create a status window.
     * @param frame the parent frame
     * @param windowId a unique name for this window
     * @param title The window title
     */
    protected StatusWindow(javax.swing.JFrame frame,
            String windowId, String title)
    {
        super(frame,title,MODAL_STATE);
        windowIdentity = windowId;
        setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
        setLocation(frame.getLocation());
        setResizable(true);
    }

    /**
     * Get the window identifier.
     */
    public final String getWindowIdentifier()
    {
        return windowIdentity;
    }
}
