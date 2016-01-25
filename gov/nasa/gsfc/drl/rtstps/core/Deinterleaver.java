/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;

import gov.nasa.gsfc.drl.rtstps.core.AOS.AOS;

import java.util.LinkedList;

/**
 * This class performs byte deinterleave of the frames or blocks
 * of data.
 * 
 */
public class Deinterleaver extends FrameSenderNode implements FrameReceiver,
        Sender, Cloneable
{
    /**
     * This is a class name for this RT-STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "deinterleaver";


    private int syncLength = 8;

    private Frame frameI;
    private Frame frameQ;
    private LinkedList<Frame> frameListI = new LinkedList<>();
    private LinkedList<Frame> frameListQ = new LinkedList<>();



    private int componentI;
    private int componentQ;

    /**
     * A null constructor.
     */
    public Deinterleaver()
    {
        /**
         * There is only one PnDecoder object, so the class name is the same as
         * the link/object name.
         */
        super(CLASSNAME,CLASSNAME);
    }

    public void dlsetup(int i,int q){
        componentI = i;
        componentQ = q;
    }
    /**
     * Set up this RT-STPS node with a configuration. PN does not have an element.
     * It is created in the Frame Sync, so this required method does nothing.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        componentI = Convert.toInteger(element,"componentI",
                0);

        componentQ = Convert.toInteger(element,"componentQ",
                1);

    }

    /**
     * Set the sync pattern length. The default length is 4 bytes.
     */
    public void setSyncLength(int length)
    {
        syncLength = length;
    }

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
        for (int n = 0; n < frames.length; n++)
        {
            Frame frame = frames[n];
            if (!frame.isDeleted())
            {
                deinterleave(frame);
            }
        }

        for(Frame f:frameListI) {
            output.putFrame(f);
        }
        for(Frame f:frameListQ){
            output.putFrame(f);
        }
        frameListI.clear();
        frameListQ.clear();

    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        if (!frame.isDeleted())
        {
            deinterleave(frame);
        }
        for(Frame f:frameListI)
            output.putFrame(f);
        for(Frame f:frameListQ)
            output.putFrame(f);
    }

   /**
    * do byte deinterleave
   */
  public void deinterleave(Frame inputframe)
  {
      int singlelength = inputframe.length /2;
      Frame frame1 = new Frame(singlelength);
      Frame frame2 = new Frame(singlelength);

      for(int i=0;i<singlelength;i++){
          frame1.getData()[i]= inputframe.data[i*2];
          frame2.getData()[i]= inputframe.data[i*2+1];
      }

      AOS aos = new AOS();
      aos.setFrame(frame1);
      if(aos.getIQFlag()==aos.IQFLAG_I) {
          frameI = frame1;
          frameQ = frame2;
      }
      else {
          frameI = frame2;
          frameQ = frame1;
      }
      frameListI.add(frameI);
      frameListQ.add(frameQ);
  }
}
