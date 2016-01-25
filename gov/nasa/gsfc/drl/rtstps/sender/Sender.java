/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.sender;
import gov.nasa.gsfc.drl.rtstps.library.layout.RowLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * This is a graphics controller for a program that sends binary file data to a
 * specified port on a target host.
 * 
 * 
 */
public class Sender extends JFrame
{
    public static void main(String[] args)
    {
        new Sender();
    }

    private JTextField hostField;
    private JTextField portField;
    private JTextField delayField;
    private JButton fileField;
    private JButton go;
    private JButton stop;
    private File datafile = null;
    private JProgressBar bar;
    private Timer timer;
    private SenderThread senderThread;

    public Sender()
    {
        super("Sender");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        ImageIcon icon = null;
        try {
          icon = new ImageIcon(getClass().getResource("/images/cycle.gif"));
        } catch (Exception e) {
          icon = new ImageIcon("images/cycle.gif");
        }
        icon.setDescription("Sender");
        setIconImage(icon.getImage());

        JLabel hostLabel = new JLabel("Target Host:");
        hostField = new JTextField("localhost",18);
        hostField.setToolTipText("Host name or IP address for target computer");
        hostLabel.setLabelFor(hostField);
        hostLabel.setDisplayedMnemonic('H');
        hostField.getAccessibleContext().setAccessibleName("Target Host");

        JLabel portLabel = new JLabel("Target Port:");
        portField = new JTextField("4935",5);
        portField.setToolTipText("TCP/IP port number of the target server");
        portLabel.setLabelFor(portField);
        portLabel.setDisplayedMnemonic('P');
        portField.getAccessibleContext().setAccessibleName("Target Port");
        portField.setInputVerifier(new NumberCheck(1024,65535));

        JLabel delayLabel = new JLabel("Delay between sends:");
        delayField = new JTextField("0",5);
        delayField.setToolTipText("Delay between sends, milliseconds");
        delayLabel.setLabelFor(delayField);
        delayLabel.setDisplayedMnemonic('D');
        delayField.getAccessibleContext().setAccessibleName("Millisecond Delay");
        delayField.setInputVerifier(new NumberCheck(0,Integer.MAX_VALUE));

        JLabel fileLabel = new JLabel("File:");
        fileField = new JButton("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXxx");
        fileField.setPreferredSize(fileField.getPreferredSize());
        fileField.setText("<File>");
        fileField.setToolTipText("A file to send to the target");
        fileLabel.setLabelFor(fileField);
        fileLabel.setDisplayedMnemonic('F');
        fileField.getAccessibleContext().setAccessibleName("File to Send");
        fileField.addActionListener(new FilePicker(this));

        go = new JButton("Go");
        go.setMnemonic('G');
        go.setToolTipText("Start sending file.");
        go.getAccessibleContext().setAccessibleName("Begin Send");
        go.addActionListener(new Go());

        stop = new JButton("Stop");
        stop.setToolTipText("Stop sending file.");
        stop.getAccessibleContext().setAccessibleName("Stop Send");
        stop.setEnabled(false);
        stop.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                senderThread.terminate();
            }
        });

        bar = new JProgressBar();
        bar.setStringPainted(true);
        bar.setForeground(Color.red.darker().darker());
        bar.getAccessibleContext().setAccessibleName("Progress");
        timer = new Timer(1000,new TimerListener());

        RowLayout layout = new RowLayout(6,6,12);
        RowLayout.Constraint lc = new RowLayout.Constraint();
        layout.setInsets(new Insets(12,12,12,12));
        JPanel main = new JPanel(layout);
        lc.set(0,0f); main.add(hostLabel,lc);
        lc.set(0,1f); main.add(hostField,lc);
        lc.set(1,0f); main.add(portLabel,lc);
        lc.set(1,1f); main.add(portField,lc);
        lc.set(2,0f); main.add(delayLabel,lc);
        lc.set(2,1f); main.add(delayField,lc);
        lc.set(2,0f); main.add(new JLabel("ms"),lc);
        lc.set(3,0f); main.add(fileLabel,lc);
        lc.set(3,1f); main.add(fileField,lc);
        lc.set(4,1f); main.add(Box.createGlue(),lc);
        lc.set(4,0f); main.add(go,lc);
        lc.set(4,0f); main.add(stop,lc);
        lc.set(5,1f); main.add(bar,lc);
        setContentPane(main);

        pack();
        //This fragment centers the frame on the screen.
        Dimension size = getSize();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (screen.width-size.width)/2;
        int h = (screen.height-size.height)/2;
        setLocation(w,h);
        setVisible(true);
    }

    class Go implements java.awt.event.ActionListener
    {
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
            if (datafile == null) return;
            go.setEnabled(false);
            stop.setEnabled(true);
            int port = getInteger(portField.getText());
            int delay = getInteger(delayField.getText());
            bar.setMinimum(0);
            int max = (int)(datafile.length() / SenderThread.PROGRESS_SIZE);
            bar.setMaximum(max);
            bar.setValue(0);
            bar.setString(null);

            try
            {
                senderThread = new SenderThread(Sender.this,hostField.getText(),port,datafile,delay);
                senderThread.start();
                timer.start();
            }
            catch (java.io.IOException ioe)
            {
                timer.stop();
                go.setEnabled(true);
                stop.setEnabled(false);
                String msg = ioe.getMessage();
                if (msg == null) msg = "Transmission stopped.";
                JOptionPane.showMessageDialog(Sender.this, msg, "Sender: Alert",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private int getInteger(String value)
        {
            int v = 0;
            try { v = Integer.parseInt(value); }
            catch (NumberFormatException nfe) { }
            return v;
        }
    }

    class TimerListener implements java.awt.event.ActionListener
    {
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
            bar.setValue(senderThread.getProgress());
            if (senderThread.isDone())
            {
                timer.stop();
                go.setEnabled(true);
                stop.setEnabled(false);
                bar.setString("Done");
            }
        }
    }

    class FilePicker extends JFileChooser implements
            java.awt.event.ActionListener
    {
        private Frame frame;

        FilePicker(Frame parent)
        {
            frame = parent;
            String baseDirectory = System.getProperty("directory",".");
            setCurrentDirectory(new File(baseDirectory));
            setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
            setDialogTitle("Sender: Choose File");
        }

        public void actionPerformed(java.awt.event.ActionEvent event)
        {
            fileField.setEnabled(false);
            rescanCurrentDirectory();
            int x = showOpenDialog(frame);
            if (x == JFileChooser.APPROVE_OPTION)
            {
                datafile = getSelectedFile();
                fileField.setText(datafile.getName());
                fileField.repaint();
            }
            fileField.setEnabled(true);
        }
    
        private static final long serialVersionUID = 1L;		
    }

    class NumberCheck extends javax.swing.InputVerifier
    {
        private int min;
        private int max;

        NumberCheck(int min, int max)
        {
            this.min = min;
            this.max = max;
        }

        public boolean verify(JComponent input)
        {
            boolean ok = true;
            JTextField tf = (JTextField)input;
            String x = tf.getText().trim();
            int v = 0;
            try
            {
                v = Integer.parseInt(x);
                ok = (v >= min) && (v <= max);
            }
            catch (NumberFormatException nfe)
            {
                ok = false;
            }
            return ok;
        }
    }
    
    private static final long serialVersionUID = 1L;			
}
