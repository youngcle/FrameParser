/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;
import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;
import gov.nasa.gsfc.drl.rtstps.core.fs.clock.FrameClock;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;

/**
 * This class handles flywheeling for the Frame Synchronizer.
 * 
 */
class Flywheeler
{
    private static final boolean INVERT_OPTION = false;
    private int flywheelsCompleted = 0;
    private int flywheelBytesToFill = 0;
    private boolean isSendingFlywheels = false;
    private int flywheelDuration;
    private int frameLength;
    private Buffer crossover;
    private Frames frameList;
    private FrameClock frameClock;
    private LongStatusItem flywheels;

    /**
     * Create a flywheel object.
     * @param setup A configuration. You should not create Flywheeler if
     *          the flywheel duration in setup is zero.
     * @param crossover A buffer used to handle buffer straddle problems.
     * @param frames A list of output frames.
     * @param frameClock A clock to track annotation timestamps
     */
    Flywheeler(FsSetup setup, Buffer crossover, Frames frames,
            FrameClock frameClock, LongStatusItem flywheels)
    {
        this.crossover = crossover;
        this.frameList = frames;
        this.frameClock = frameClock;
        this.flywheels = flywheels;
        isSendingFlywheels = setup.isSendingFlywheels;
        flywheelDuration = setup.flywheelDuration;
        frameLength = setup.frameLength;
    }

    /**
     * Begin a flywheel scenario.
     * @return True if the flywheel scenario finishes.
     */
    boolean start(Buffer buffer)
    {
        flywheelBytesToFill = frameLength;
        flywheelsCompleted = 0;
        return next(buffer);
    }

    /**
     * Continue a flywheel scenario.
     * @return True if the flywheel scenario finishes.
     */
    boolean next(Buffer buffer)
    {
        boolean complete = false;

        if (isSendingFlywheels)
        {
            complete = transmit(buffer);
        }
        else
        {
            complete = discard(buffer);
        }
        return complete;
    }

    /**
     * Discard flywheel frames found in the buffer.
     * @return True if the flywheel scenario finishes.
     */
    private boolean discard(Buffer buffer)
    {
        if (crossover.getRemainingBytes() > 0)
        {
            flywheelBytesToFill -= crossover.getRemainingBytes();
            if (flywheelBytesToFill == 0)
            {
                ++flywheelsCompleted;
                ++flywheels.value;
                flywheelBytesToFill = frameLength;
                frameClock.getTimeStamp(); //to keep clock running
            }
            crossover.empty();
        }

        boolean fullFrame = true;

        while (fullFrame && (flywheelsCompleted < flywheelDuration))
        {
            int bytes = buffer.getRemainingBytes();
            if (flywheelBytesToFill > bytes)
            {
                flywheelBytesToFill -= bytes;
                buffer.advance(bytes);
                fullFrame = false;
            }
            else
            {
                buffer.advance(flywheelBytesToFill);
                flywheelBytesToFill = frameLength;
                frameClock.getTimeStamp(); //to keep clock running
                ++flywheelsCompleted;
                ++flywheels.value;
            }
        }

        return (flywheelsCompleted == flywheelDuration);
    }

    /**
     * Transmit flywheel frames, which are found in the buffer, as if they
     * were lock frames. Note that I set the annotation of completed frames
     * to flywheel (not lock). The other flags have no meaning, except that
     * I turn off the inverted frame bit so that I don't trigger an
     * unnecessary frame inversion somewhere else in the code. When I copy
     * the buffer, I turn off data inversion since I have no idea if the
     * data is inverted or not. (I never look at the sync pattern, which
     * may not even exist.)
     * @return True if the flywheel scenario finishes.
     */
    private boolean transmit(Buffer buffer)
    {
        /**
         * I copy any bytes in the crossover buffer first. This action
         * could finish my flywheel session.
         */
        if (crossover.getRemainingBytes() > 0)
        {
            frameList.copyBufferToFrame(crossover,INVERT_OPTION);
            if (flywheelBytesToFill == 0)
            {
                ++flywheelsCompleted;
                ++flywheels.value;
                flywheelBytesToFill = frameLength;
                FrameAnnotation a = frameList.getCurrentFrameAnnotation();
                a.isLock = false;
                a.isInverted = false;
                a.timestamp = frameClock.getTimeStamp();
            }
            crossover.empty();
        }

        boolean fullFrame = true;

        while (fullFrame && (flywheelsCompleted < flywheelDuration))
        {
            int toFill = frameList.copyBufferToFrame(buffer,INVERT_OPTION);
            fullFrame = (toFill == 0);

            if (fullFrame)
            {
                ++flywheelsCompleted;
                ++flywheels.value;
                FrameAnnotation a = frameList.getCurrentFrameAnnotation();
                a.isLock = false;
                a.isInverted = false;
                a.timestamp = frameClock.getTimeStamp();
            }
        }

        return (flywheelsCompleted == flywheelDuration);
    }
}
