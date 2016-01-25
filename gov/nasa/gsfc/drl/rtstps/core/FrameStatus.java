/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

/**
 * This class holds Frame level status. It is also a FrameReceiver, so you
 * may plug it into a pipeline. It examines frame annotation for each frame
 * and counts occurrences. You do not have to set an output FrameReceiver,
 * in which case it becomes a terminal.
 * 
 * 
 */
public class FrameStatus extends FrameSenderNode implements FrameReceiver,
        Sender, Cloneable
{
    /**
     * This is a class name for this RT-STPS node type, which is also the element
     * name. It is not necessarily the link name, which is the name of one
     * particular object.
     */
    public static final String CLASSNAME = "frame_status";

    /** The number of lock frames. */
    private LongStatusItem locks;

    /** The number of transmitted flywheel frames. */
    private LongStatusItem flywheels;

    /**
     * The number of slip frames. Slip frames are ones that the Frame
     * Synchronizer found out of position (long or short) by 1 or 2 bits.
     */
    private LongStatusItem slips;

    /** The number of inverted frames the Frame Synchronizer detected. */
    private LongStatusItem invertedFrames;

    /**
     * The number of true frames the Frame Synchronizer detected. Frames will
     * originally be either true or inverted.
     */
    private LongStatusItem trueFrames;

    /**
     * The number of frames that have CRC errors. The frames must contain CRC
     * parity and CRC detection must be enabled for this counter to be
     * meaningful.
     */
    private LongStatusItem crcErrors;

    /**
     * The number of frames that had Reed Solomon errors, which were corrected.
     * The frames must contain Reed Solomon parity and Reed Solomon block
     * correction must be enabled for this counter to be meaningful.
     */
    private LongStatusItem rsCorrected;

    /**
     * The number of frames that had Reed Solomon uncorrectable errors.
     * The frames must contain Reed Solomon parity and Reed Solomon block
     * detection must be enabled for this counter to be meaningful.
     */
    private LongStatusItem rsUncorrectables;

    /** The number of frames marked for deletion. */
    private LongStatusItem deleted;

    /** The number of passed frames, i.e. not marked for deletion. */
    private LongStatusItem passed;



    /**
     * Create a FrameStatus node.
     */
    public FrameStatus()
    {
        /**
         * There is only one FrameStatus object, so the class name is
         * the same as the link/object name.
         */
        super(CLASSNAME,CLASSNAME);
        setOutputIsRequired(false);
    }

    /**
     * Set up this RT-STPS node with a configuration. It does not have an element,
     * so this required method initializes status only.
     */
    public void load(org.w3c.dom.Element element, Configuration config)
            throws RtStpsException
    {
        locks = new LongStatusItem("Lock Frames");
        flywheels = new LongStatusItem("Flywheels");
        slips = new LongStatusItem("Slipped Frames");
        trueFrames = new LongStatusItem("True Frames");
        invertedFrames = new LongStatusItem("Inverted Frames");
        crcErrors = new LongStatusItem("CRC Error Frames");
        rsCorrected = new LongStatusItem("RS-Corrected Frames");
        rsUncorrectables = new LongStatusItem("RS-Uncorrectable Frames");
        deleted = new LongStatusItem("Deleted Frames");
        passed = new LongStatusItem("Passed Frames");

        statusItemList = new java.util.ArrayList<StatusItem>(10);	
        statusItemList.add(locks);
        statusItemList.add(flywheels);
        statusItemList.add(slips);
        statusItemList.add(trueFrames);
        statusItemList.add(invertedFrames);
        statusItemList.add(crcErrors);
        statusItemList.add(rsCorrected);
        statusItemList.add(rsUncorrectables);
        statusItemList.add(deleted);
        statusItemList.add(passed);
    }

    /**
     * Give an array of frames to this FrameReceiver.
     */
    public void putFrames(Frame[] frames) throws RtStpsException
    {
        for (int n = 0; n < frames.length; n++)
        {
            count(frames[n]);
        }

        if (output != null)
        {
            output.putFrames(frames);
        }
    }

    /**
     * Give a frame to this FrameReceiver.
     */
    public void putFrame(Frame frame) throws RtStpsException
    {
        count(frame);
        if (output != null)
        {
            output.putFrame(frame);
        }
    }

    /**
     * Count occurrences of the frame's annotation fields.
     */
    public void count(Frame frame)
    {
        if (frame.isDeleted())
        {
            ++deleted.value;
        }
        else
        {
            ++passed.value;
        }

        FrameAnnotation a = frame.getFrameAnnotation();
        if (a.isSlipped) ++slips.value;
        if (a.hasCrcError) ++crcErrors.value;
        if (a.isRsUncorrectable) ++rsUncorrectables.value;
        if (a.isRsCorrected) ++rsCorrected.value;

        if (a.isLock)
        {
            ++locks.value;
        }
        else
        {
            ++flywheels.value;
        }

        if (a.isInverted)
        {
            ++invertedFrames.value;
        }
        else
        {
            ++trueFrames.value;
        }
    }
}
