/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.library;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * This is a shell for a custom dialog. You create a custom Component, such
 * as a JPanel derivative, and you give it to DialogShell. DialogShell will
 * show the Component in the primary display area, and it will put "ok" and
 * "cancel" buttons at the bottom.
 * <p>
 * When a user presses the "ok" button, DialogShell will verify the fields in
 * your custom Component if you have set it up to do so. To create a verifier,
 * first either create a new class that implements DialogShell.Verifier or
 * have your custom Component implement DialogShell.Verifier itself. You can
 * then implement verify() any way you wish. Throw an exception if you detect
 * an error. DialogShell will show the user your error message, and it will
 * return to the dialog. Don't forget to use DialogShell's setVerifier()
 * method to establish your verifier.
 * <p>
 * Another advantage to using DialogShell is that it lets you reuse the dialog.
 * If you do reuse it, you may need to reset the component values in the custom
 * panel. DialogShell does NOT dispose itself. It uses setVisible(false), which
 * means you must dispose it yourself.
 * <p>
 * You must invent your own methods to retrieve data from your component.
 * <p>
 * In the following example, assume you have created a custom component that
 * implement DialogShell.Verifier.
 * <pre>
 * DialogShell dialog = new DialogShell(frame,myJPanel,"Enter Name and Address");
 * dialog.setVerifier(myJPanel);
 * dialog.setLocationRelativeTo(this);
 * dialog.show();
 * if (dialog.pressedOk()) get fields . . .
 * dialog.dispose();
 * </pre>
 * 
 * 
 */
public class DialogShell extends javax.swing.JDialog
{
    private JButton _ok;
    private JButton _cancel;
    private boolean _okSelected;
    private java.awt.Component _component;
    private Verifier _verifier = null;

    /**
     * The DialogShell constructor.
     * @param frame The parent frame. You may pass null.
     * @param component The component (usually a JPanel) that is the primary focus
     *            of this dialog.
     * @param title The dialog title.
     */
    public DialogShell(java.awt.Frame frame, java.awt.Component component,
            String title)
    {
        super(frame,true); //modal
        setTitle(title);
        _component = component;
        java.awt.Container cp = getContentPane();
        cp.setLayout(new java.awt.BorderLayout(8,8));
        cp.add(component,"Center");
        cp.add(new Buttons(),"South");
        pack();
    }

    /**
     * Implement this interface to verify the component's fields. To
     * indicate an error, throw an exception with an appropriate error
     * message. DialogShell will then return to the dialog.
     */
    public static interface Verifier
    {
        public void verify() throws Exception;
    }

    /**
     * Tell the DialogShell who will verify the user's input values in the
     * component. The usual practice is to have the original DialogShell
     * component also implement the DialogShell.Verifier interface.
     */
    public void setVerifier(Verifier verifier)
    {
        _verifier = verifier;
    }

    /**
     * Get the component that is the primary focus of this dialog.
     */
    public java.awt.Component getComponent()
    {
        return _component;
    }

    /**
     * Determine if the user pressed the OK button to close the dialog.
     * It returns false if he/she pressed the cancel button.
     */
    public boolean pressedOk()
    {
        return _okSelected;
    }

    class Buttons extends JPanel implements java.awt.event.ActionListener
    {
        Buttons()
        {
            _ok = new JButton("Ok");
            _ok.setMnemonic('O');
            _ok.addActionListener(this);

            _cancel = new JButton("Cancel");
            _cancel.setMnemonic('C');
            _cancel.addActionListener(this);

            add(_ok);
            add(_cancel);
        }

        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
            _okSelected = (evt.getSource() == _ok);
            if (_okSelected)
            {
                try
                {
                    if (_verifier != null) _verifier.verify();
                    DialogShell.this.setVisible(false);
                }
                catch (Exception ex)
                {
                    String msg = ex.getMessage();
                    if (msg == null) msg = ex.toString();
                    JOptionPane.showMessageDialog(DialogShell.this,
                            msg,"Error",JOptionPane.ERROR_MESSAGE);
                }
            }
            else
            {
                DialogShell.this.setVisible(false);
            }
        }
    
        private static final long serialVersionUID = 1L;			
    }
    
    private static final long serialVersionUID = 1L;			
}
