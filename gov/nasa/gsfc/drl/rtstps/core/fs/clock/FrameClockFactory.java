/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs.clock;
import java.util.Date;

/**
 * This factory creates a FrameClock. You should use the static method and
 * should not instantiate this class.
 * 
 */
public class FrameClockFactory
{
    /**
     * Create a FrameClock object from a timestamp setup.
     */
    public static FrameClock getClock(TimeStamp setup)
    {
        FrameClock clock = null;

        if (setup.stepsize > 0)
        {
            clock = new FrameClock3(setup.epoch, setup.sessionStart,
                            setup.stepsize);
        }
        else if (setup.sessionStart == null)
        {
            clock = new FrameClock2(setup.epoch);
        }
        else
        {
            clock = new FrameClock1(setup.epoch, setup.sessionStart);
        }

        return clock;
    }
}

/**
 * A FrameClock. The user wants the session start to be different from
 * the wall clock. FramClock1 uses the wall clock to space the frames.
 */
final class FrameClock1 implements FrameClock
{
    private long nowStart;
    private long base;

    FrameClock1(Date epoch, Date sessionStart)
    {
        base = sessionStart.getTime() - epoch.getTime();
    }

    public void start()
    {
        nowStart = System.currentTimeMillis();
    }

    public long getTimeStamp()
    {
        return System.currentTimeMillis() - nowStart + base;
    }
}

/**
 * A FrameClock. The session start is the wall clock time, and the frame
 * spacing is real time.
 */
final class FrameClock2 implements FrameClock
{
    private long epoch;

    FrameClock2(Date epoch)
    {
        this.epoch = epoch.getTime();
    }

    public void start()
    {
    }

    public long getTimeStamp()
    {
        return System.currentTimeMillis() - epoch;
    }
}

/**
 * A FrameClock. The session start may be a set time or the current time.
 * The user wants a forced time step size between frames.
 */
class FrameClock3 implements FrameClock
{
    private long epoch;
    private long step;
    private long current;
    private boolean useNow;

    FrameClock3(Date epoch, Date sessionStart, long step)
    {
        this.epoch = epoch.getTime();
        this.step = step;

        useNow = sessionStart == null;

        long start = useNow? System.currentTimeMillis() :
                sessionStart.getTime();

        current = start - this.epoch;
    }

    public void start()
    {
        if (useNow) current = System.currentTimeMillis() - epoch;
    }

    public long getTimeStamp()
    {
        long time = current;
        current += step;
        return time;
    }
}
