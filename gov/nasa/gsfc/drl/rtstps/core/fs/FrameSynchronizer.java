/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;
import gov.nasa.gsfc.drl.rtstps.core.*;
import gov.nasa.gsfc.drl.rtstps.core.fs.clock.FrameClock;
import gov.nasa.gsfc.drl.rtstps.core.fs.clock.FrameClockFactory;
import gov.nasa.gsfc.drl.rtstps.core.status.IntegerStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.LongStatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;
import gov.nasa.gsfc.drl.rtstps.core.status.TextStatusItem;

/**
 * This class produces frames from buffers of bits, which it gives to a
 * FrameReceiver object. It does not do CRC checking, frame inversion, or any
 * other frame level processing. A FrameSynchronizer object is reuseable. To
 * use, you must give it a FrameReceiver object to handle its frame output,
 * and you must give it an setup object for configuration via the load method.
 * You give data to it with repeated calls to <code>putBuffer</code>. You
 * should call <code>shutdown</code> at the session's conclusion to eliminate
 * any partial frame.
 * 
 * 
 */
public class FrameSynchronizer extends FrameSenderNode implements FrameReceiver
{
    /**
     * This is a class name for this RT-STPS node type, which is also the element name.
     * It is not necessarily the link name, which is the name of one particular
     * object.
     */
    public static final String CLASSNAME = "frame_sync";

    /**
     * The following parameters are FrameSynchronizer states. In SEARCH, the
     * FS is looking for a synchronization (sync) pattern.
     */
    private static final int SEARCH = 0;

    /**
     * Locked to sync. The frame is split across two buffers, and the FS is
     * waiting for the second buffer.
     */
    private static final int LOCK_SPLIT_FRAME = 1;

    /**
     * The FS expects the sync pattern to be split across two buffers. It is
     * waiting for the second buffer. Also see FLYWHEEL_SPLIT_SYNC.
     */
    private static final int SPLIT_SYNC = 2;

    /**
     * The FS is flywheeling. A "frame" is split across two buffers, and the
     * FS is waiting for the second one.
     */
    private static final int FLYWHEEL = 3;

    /**
     * This is a special split-sync scenario. The FS is just coming out of
     * flywheel, and the first sync it sees is split. In this scenario, if
     * that sync turns out to be invalid, the FS drops to search immediately.
     * It does not begin flywheel (again), which is what happens in the
     * standard split-sync scenario.
     */
    private static final int FLYWHEEL_SPLIT_SYNC = 4;

    /**
     * This is search mode at the very beginning of a pass for the first buffer.
     * The difference is there are no "crossover" bytes that straddle buffers,
     * which is a consideration when looking for potential split sync patterns.
     */
    private static final int FIRST_SEARCH = 5;

    /**
     * An internal state only. The FS is never in this state between buffers.
     * It is locked and moving frames.
     */
    static final int LOCK = 10;

    /**
     * An internal state only. The FS is never in this state between buffers.
     * It has just lost sync and next will either flywheel or drop to search.
     */
    private static final int LOST_SYNC = 20;

    /**
     * The current FrameSynchronizer state. The state is critical to proper
     * processing across buffers.
     */
    private int state = FIRST_SEARCH;

    /**
     * This object handles true sync detection. It skips true sync detection
     * if trueSync is null.
     */
    private AbstractSynchronizer trueSync = null;

    /**
     * This object handles inverted sync detection. It inverted true sync
     * detection if trueSync is null.
     */
    private AbstractSynchronizer invertedSync = null;

    /**
     * I use a Buffer object to hold an input buffer. The Buffer class helps
     * me keep track of where I am.
     */
    private Buffer workBuffer;

    /**
     * I use the crossover buffer to hold a small fragment of the end of one
     * buffer and the beginning of the next buffer. I must do this to detect
     * split sync and slip.
     */
    private Buffer crossover;

    /**
     * The current sync pattern is true, not inverted. This is meaningful only
     * when sync is detected.
     */
    private boolean isTrueSync = true;

    /**
     * The current sync pattern is slipped. This is meaningful only when sync
     * is detected.
     */
    private boolean isSlipped = false;

    /**
     * This is where the FrameSynchronizer collects frames from a single
     * buffer. It usually sends them to the FrameReceiver in a bunch.
     */
    private Frames frameList = null;

    /**
     * The configuration for the current pass.
     */
    private FsSetup setup;

    /**
     * The FrameSynchronizer saves status information here.
     */
    private LongStatusItem totalFrames;
    private LongStatusItem dropToSearches;
    private TextStatusItem currentMode;
    private static final String LOCK_STATUS = "lock";
    private static final String FLYWHEEL_STATUS = "flywheel";
    private static final String SEARCH_STATUS = "search";

    /**
     * The number of buffers searched without finding lock. Useful to
     * indicate the FrameSynchronizer is receiving data packets in a
     * server setting.
     */
    private IntegerStatusItem searchBuffers;

    /**
     * This class handles all flywheeling.
     */
    private Flywheeler flywheeler = null;

    /**
     * This class is the annotation timestamp clock.
     */
    private FrameClock frameClock;



    /**
     * Create a Frame Synchronizer node.
     */
    public FrameSynchronizer()
    {
        /**
         * There is only one FrameSynchronizer object, so the class name is
         * the same as the link/object name.
         */
        super(CLASSNAME,CLASSNAME);
    }

    public void setupFS(long pattern,int synclength,int framelength,Configuration configuration) throws RtStpsException {
        totalFrames = new LongStatusItem("Total Frames");
        searchBuffers = new IntegerStatusItem("Search Buffers");
        dropToSearches = new LongStatusItem("Lost Sync Count");
        currentMode = new TextStatusItem("Mode",SEARCH_STATUS);
        LongStatusItem flywheels = new LongStatusItem("Flywheels");

        statusItemList = new java.util.ArrayList<StatusItem>(5);
        statusItemList.add(currentMode);
        statusItemList.add(searchBuffers);
        statusItemList.add(dropToSearches);
        statusItemList.add(flywheels);
        statusItemList.add(totalFrames);

        workBuffer = new Buffer();
        setup = new FsSetup(pattern,synclength,framelength);

        if (setup.isPnEncoded)
        {
            RtStpsNode pn = configuration.getNodeFactory().create("pn");
            configuration.getStpsNodes().put(pn.getLinkName(),pn);
        }

        frameClock = FrameClockFactory.getClock(setup.timestamp);
        crossover = new Buffer(2 * setup.syncLength);
        frameList = new Frames(setup.frameLength);
        if (setup.flywheelDuration > 0)
        {
            flywheeler = new Flywheeler(setup,crossover,frameList,frameClock,
                    flywheels);
        }

        /**
         * If true sync is enabled, I create a true synchronizer.
         * Synchronizer throws an exception if the pattern is ambiguous.
         * XSynchronizer handles ambiguous patterns. See
         * AbstractSynchronizer for an explanation of what this means.
         */
        if (setup.trueSyncEnabled)
        {
            try
            {
                trueSync = new Synchronizer(setup.syncPattern,
                        setup.frameLength);
            }
            catch (FsException fse)
            {
                trueSync = new XSynchronizer(setup.syncPattern,
                        setup.frameLength);
            }
            trueSync.setSlip(setup.slippage);
        }

        /**
         * If inverted sync is enabled, I create an inverted synchronizer.
         * Synchronizer throws an exception if the pattern is ambiguous.
         * XSynchronizer handles ambiguous patterns.
         * See AbstractSynchronizer for an explanation of what this means.
         */
        if (setup.invertedSyncEnabled)
        {
            byte[] isync = new byte[setup.syncPattern.length];
            for (int n = 0; n < isync.length; n++)
            {
                isync[n] = (byte)(~setup.syncPattern[n]);
            }

            try
            {
                invertedSync = new Synchronizer(isync, setup.frameLength);
            }
            catch (FsException fse)
            {
                invertedSync = new XSynchronizer(isync, setup.frameLength);
            }
            invertedSync.setSlip(setup.slippage);
        }
    }

    /**
     * Load the FrameSynchronizer with a configuration.
     */
    public void load(org.w3c.dom.Element element, Configuration configuration)
            throws RtStpsException
    {
        totalFrames = new LongStatusItem("Total Frames");
        searchBuffers = new IntegerStatusItem("Search Buffers");
        dropToSearches = new LongStatusItem("Lost Sync Count");
        currentMode = new TextStatusItem("Mode",SEARCH_STATUS);
        LongStatusItem flywheels = new LongStatusItem("Flywheels");

        statusItemList = new java.util.ArrayList<StatusItem>(5);
        statusItemList.add(currentMode);
        statusItemList.add(searchBuffers);
        statusItemList.add(dropToSearches);
        statusItemList.add(flywheels);
        statusItemList.add(totalFrames);

        workBuffer = new Buffer();
        setup = new FsSetup(element);

        if (setup.isPnEncoded)
        {
            RtStpsNode pn = configuration.getNodeFactory().create("pn");
            pn.load(element,configuration);
            configuration.getStpsNodes().put(pn.getLinkName(),pn);
        }

        frameClock = FrameClockFactory.getClock(setup.timestamp);
        crossover = new Buffer(2 * setup.syncLength);
        frameList = new Frames(setup.frameLength);
        if (setup.flywheelDuration > 0)
        {
            flywheeler = new Flywheeler(setup,crossover,frameList,frameClock,
                    flywheels);
        }

        /**
         * If true sync is enabled, I create a true synchronizer.
         * Synchronizer throws an exception if the pattern is ambiguous.
         * XSynchronizer handles ambiguous patterns. See
         * AbstractSynchronizer for an explanation of what this means.
         */
        if (setup.trueSyncEnabled)
        {
            try
            {
                trueSync = new Synchronizer(setup.syncPattern,
                            setup.frameLength);
            }
            catch (FsException fse)
            {
                trueSync = new XSynchronizer(setup.syncPattern,
                            setup.frameLength);
            }
            trueSync.setSlip(setup.slippage);
        }

        /**
         * If inverted sync is enabled, I create an inverted synchronizer.
         * Synchronizer throws an exception if the pattern is ambiguous.
         * XSynchronizer handles ambiguous patterns.
         * See AbstractSynchronizer for an explanation of what this means.
         */
        if (setup.invertedSyncEnabled)
        {
            byte[] isync = new byte[setup.syncPattern.length];
            for (int n = 0; n < isync.length; n++)
            {
                isync[n] = (byte)(~setup.syncPattern[n]);
            }

            try
            {
                invertedSync = new Synchronizer(isync, setup.frameLength);
            }
            catch (FsException fse)
            {
                invertedSync = new XSynchronizer(isync, setup.frameLength);
            }
            invertedSync.setSlip(setup.slippage);
        }
    }

    /**
     * Get the frame length. Do not use this method until after this object
     * is loaded.
     */
    public final int getFrameLength()
    {
        return setup.frameLength;
    }

    /**
     * Get the sync pattern length in bytes. Do not use this method until
     * after this object is loaded.
     */
    public final int getSyncPatternLength()
    {
        return setup.syncLength;
    }

    /**
     * End a session. The FrameSynchronizer discards any partial frame and
     * will put itself into a search state.
     */
    public void shutdown() throws RtStpsException
    {
        frameList.flushAllData();
        output.flush();
        state = FIRST_SEARCH;
        currentMode.value = SEARCH_STATUS;
    }

    //实现FrameReceiver接口，以便实现，处理链条中，可以在后面继续进行帧同步
    @Override
    public void putFrame(Frame frame) throws RtStpsException {
        putBuffer(frame.getData(),frame.getSize());

    }

    //实现FrameReceiver接口（多帧接口），以便实现，处理链条中，可以在后面继续进行帧同步
    @Override
    public void putFrames(Frame[] frames) throws RtStpsException {
        for(Frame singleframe:frames)
            putBuffer(singleframe.getData(),singleframe.getSize());
    }

    /**
     * Flush the pipeline. This behaves the same as shutdown.
     */
    public void flush() throws RtStpsException
    {
        shutdown();
    }

    /**
     * Give a buffer of bits to the FrameSynchronizer. It will find all frames
     * and send them to the FrameReceiver.<p>
     * When the FrameSynchronizer is in search mode, then the buffer size must
     * be at least as large as twice the sync pattern length (i.e. at least 8
     * bytes for a 4-byte pattern). The searcher cannot reliably find sync if
     * the buffer is smaller than the minimum. This limitation does not hold
     * when it is in any other mode.
     */
    public void putBuffer(byte[] data, int dataLength) throws RtStpsException
    {
        workBuffer.setData(data,dataLength);

        switch (state)
        {
            case SEARCH:
                doSearchScenario(workBuffer);
                break;

            case LOCK_SPLIT_FRAME:
                doLockScenario(workBuffer);
                break;

            case SPLIT_SYNC:
            case FLYWHEEL_SPLIT_SYNC:
                doSplitSyncScenario(workBuffer);
                break;

            case FLYWHEEL:
                doFlywheelScenario(workBuffer);
                break;

            case FIRST_SEARCH:
                doFirstSearchScenario(workBuffer);
                break;
        }

        /**
         * I send the complete frames from this buffer to the FrameReceiver.
         * I reuse the frames, so the downstream users should not cache them.
         */
        Frame[] frames = frameList.getFrameList();
        if (frames != null)
        {
            totalFrames.value += frames.length;

            output.putFrames(frames);

            /**
             * This tells frameList to remove the completed frames from its
             * collection.
             */
            frameList.flushCompleteFrames();
        }
    }

    /**
     * The FS begins this buffer in a search state. This case is the first
     * buffer of a pass, so there are no crossover bytes from a previous
     * buffer. When it returns, there is no more processing to be done on
     * the buffer.
     * <p>
     * The buffer size (remaining bytes) must be at least as large as twice
     * the sync pattern length (i.e. at least 8 bytes for a 4-byte pattern).
     * The searcher cannot reliably find sync if buffers are smaller than
     * the minimum. If you pass a too-small buffer, the searcher will simply
     * return still in search mode.
     */
    private void doFirstSearchScenario(Buffer buffer)
    {
        /** Is the buffer big enough to do search processing? */
        if (buffer.getRemainingBytes() >= crossover.getLength())
        {
            state = doSearch(buffer);

            if (state == LOCK)
            {
                frameClock.start();
                doLockScenario(buffer);
            }
        }
    }

    /**
     * The FS begins this buffer in a search state. Unlike a first search,
     * it is holding some bytes from the previous buffer. When it returns,
     * there is no more processing to be done on the buffer.
     * <p>
     * The buffer size (remaining bytes) must be at least as large as twice
     * the sync pattern length (i.e. at least 8 bytes for a 4-byte pattern).
     * The searcher cannot reliably find sync if buffers are smaller than
     * the minimum. If you pass a too-small buffer, the searcher will simply
     * return still in search mode.
     */
    private void doSearchScenario(Buffer buffer)
    {
        /** Is the buffer big enough to do search processing? */
        if (buffer.getRemainingBytes() >= crossover.getLength())
        {
            /**
             * I copy some bytes to the crossover buffer, which I then
             * search for sync. I append these bytes to the end of the
             * last bytes from the previous buffer.
             */
            crossover.append(buffer);

            /**
             * I begin sync searching at the beginning of the crossover.
             */
            crossover.setLocation(0);

            state = doSearch(buffer);

            if (state == LOCK)
            {
                doLockScenario(buffer);
            }
            else
            {
                ++searchBuffers.value;
            }
        }
    }

    /**
     * The FS begins this buffer in a flywheel state. When it returns,
     * there is no more processing to be done on the buffer.
     */
    private void doFlywheelScenario(Buffer buffer)
    {
        state = FLYWHEEL;
        currentMode.value = FLYWHEEL_STATUS;

        boolean complete = flywheeler.next(buffer);

        if (complete)
        {
            state = verifySync(buffer);
            if (state == LOST_SYNC)
            {
                state = SEARCH;
                currentMode.value = SEARCH_STATUS;
                ++dropToSearches.value;
            }
            if (state == LOCK)
            {
                doLockScenario(buffer);
            }
        }
    }

    /**
     * The FS begins this buffer looking for the second fragment of a sync
     * pattern. When it returns, there is no more processing to be done on
     * the buffer.
     */
    private void doSplitSyncScenario(Buffer buffer)
    {
        int mystate = state;

        /**
         * I copy what I need from the buffer to fill up the crossover
         * buffer.
         */
        crossover.append(buffer);

        /**
         * Is the buffer is too small to even give me complete sync? If so,
         * I exit now and wait for more data.
         */
        if (crossover.getRemainingBytes() == 0)
        {
            /**
             * I position the crossover start index to byte #1, which is
             * where I expect to see the beginning of sync. Byte #0, holds
             * a provisional byte in case I have short slip.
             */
            crossover.setLocation(1);

            /** sync in the crossover buffer? */
            state = verifySync(crossover);

            /**
             * If I lose sync, I try to re-establish it.
             * I should never see SPLIT_SYNC since I have guaranteed that
             * I collected enough bytes to not have split sync.
             */
            if (state == LOST_SYNC)
            {
                /**
                 * I drop to search immediately if I began in a flywheel-
                 * split-sync state because I don't want to do flywheel again.
                 * It didn't work.
                 */
                if (mystate == FLYWHEEL_SPLIT_SYNC)
                {
                    ++dropToSearches.value;
                    state = doSearch(buffer);
                }
                else
                {
                    state = doLostSync(buffer);
                }
            }

            if (state == LOCK)
            {
                doLockScenario(buffer);
            }
        }
    }

    /**
     * In lock. The FrameSynchronizer attempts to collect as many frames as
     * possible. It may drop sync, but it handles it. When it returns, there
     * is no more processing to be done on the buffer.
     */
    private void doLockScenario(Buffer buffer)
    {
        currentMode.value = LOCK_STATUS;
        state = LOCK;

        while (state == LOCK)
        {
            boolean correctPolarity = !isTrueSync && setup.correctInversion;

            /** Copy buffer bytes to a frame. */
            if (crossover.getRemainingBytes() > 0)
            {
                frameList.copyBufferToFrame(crossover,correctPolarity);
            }

            int frameBytesToFill = frameList.copyBufferToFrame(buffer,
                    correctPolarity);

            /** Set the frame's annotation. */
            FrameAnnotation a = frameList.getCurrentFrameAnnotation();
            a.isLock = true;
            a.isInverted = !isTrueSync;
            a.isSlipped = isSlipped;
            a.timestamp = frameClock.getTimeStamp();

            if (frameBytesToFill > 0)   //have partial frame
            {
                state = LOCK_SPLIT_FRAME;
            }
            else
            {
                /** I look at the next bytes for a sync pattern. */
                state = verifySync(buffer);
                if (state == LOST_SYNC)
                {
                    state = doLostSync(buffer);
                }
            }
        }
    }

    /**
     * The FrameSynchronizer has lost sync. It flywheels and then drops
     * back to search if necessary. It attempts to relock.
     * @param buffer The input buffer. Its length must be greater or equal
     *          to the sync pattern length. If not, it remains in search.
     * @return the FrameSynchronizer state
     */
    private int doLostSync(Buffer buffer)
    {
        int xstate = SEARCH;
        currentMode.value = SEARCH_STATUS;

        if (flywheeler != null)
        {
            xstate = FLYWHEEL;
            boolean complete = flywheeler.start(buffer);
            if (complete)
            {
                xstate = verifySync(buffer);
                if (xstate == LOST_SYNC)
                {
                    ++dropToSearches.value;
                    xstate = doSearch(buffer);
                }
                else if (xstate == SPLIT_SYNC)
                {
                    xstate = FLYWHEEL_SPLIT_SYNC;
                }
            }
        }
        else
        {
            ++dropToSearches.value;
            xstate = doSearch(buffer);
        }
        return xstate;
    }

    /**
     * Search the buffer for a sync pattern beginning at its current index.
     * I search the crossover buffer first, but someone else must set it up.
     * @return the FrameSynchronizer state: LOCK or SEARCH.
     */
    private int doSearch(Buffer buffer)
    {
        currentMode.value = SEARCH_STATUS;
        int xstate = SEARCH;
        Location loc = null;

        if (crossover.getRemainingBytes() > 0)
        {
            loc = findSyncPattern(crossover);
            if (loc != null)
            {   /** I found sync. */
                crossover.setLocation(loc);
                xstate = LOCK;
            }
            else
            {
                crossover.empty();
                /**
                 * I start searching buffer at byte 0 because I could not
                 * check the last bytes in the crossover.
                 */
                buffer.setLocation(0);
            }
        }

        if (xstate == SEARCH)
        {
            loc = findSyncPattern(buffer);

            if (loc != null)
            {   /** I found sync. */
                buffer.setLocation(loc);
                xstate = LOCK;
            }
            else
            {
                /**
                 * I did not find sync in this buffer, so I save a
                 * sync-length's worth of bytes in the crossover, which
                 * I will handle with the next buffer.
                 */
                int start = buffer.getLength() - setup.syncLength;
                buffer.setLocation(start);
                crossover.append(buffer);
                xstate = SEARCH;
            }
        }

        return xstate;
    }

    /**
     * Find the first occurrence of a sync pattern in the buffer beginning
     * at its current index but ending within X bytes of the buffer end.
     * (X is the sync pattern length.) It detects both true and inverted sync.
     * It does not change the buffer index.
     */
    private Location findSyncPattern(Buffer buffer)
    {
        Location loc = null;
        Location iloc = null;

        int ending = buffer.getLength() - setup.syncLength;
        if (ending < 0) return null;

        if (trueSync != null)
        {
            loc = trueSync.search(buffer.data, buffer.getLength(),
                    buffer.index.offset, ending);
        }

        if (invertedSync != null)
        {
            iloc = invertedSync.search(buffer.data, buffer.getLength(),
                        buffer.index.offset, ending);
        }

        if (iloc == null)
        {
            isTrueSync = true;
        }
        else if (loc == null)
        {
            isTrueSync = false;
            loc = iloc;
        }
        else if (loc.compareTo(iloc) <= 0)
        {
            /**
             * When I see both true and inverted sync, I use the first one
             * in the buffer.
             */
            isTrueSync = true;
        }
        else
        {
            isTrueSync = false;
            loc = iloc;
        }

        return loc;
    }

    /**
     * Verify there is sync at the current buffer index.
     * @return the FrameSynchronizer state: LOCK, LOST_SYNC, or SPLIT_SYNC.
     */
    private int verifySync(Buffer buffer)
    {
        int xstate = LOST_SYNC;

        /**
         * If there are not enough bytes left in the buffer to hold a complete
         * sync pattern plus a long slip byte, I copy what there is to the
         * crossover buffer and wait for the next buffer. I add an extra byte
         * to account for short slip detection.
         */
        if (buffer.getRemainingBytes() <= setup.syncLength)
        {
            /**
             * Something is seriously wrong if the crossover buffer is not
             * empty here because the passed buffer is almost certainly the
             * crossover buffer itself. Whenever I verify sync on the crossover
             * buffer, I guarantee that a complete sync pattern is containable
             * in it.
             */
            if (crossover.getRemainingBytes() > 0)
            {
                throw new RuntimeException(
                    "Too few bytes in the crossover buffer");
            }

            int start = buffer.index.offset - 1;
            int length = buffer.getRemainingBytes() + 1;
            System.arraycopy(buffer.data, start, crossover.data, 0, length);
            crossover.setLocation(length);
            crossover.index.bit = buffer.index.bit;
            xstate = SPLIT_SYNC;
        }
        else
        {
            boolean lock = false;
            isSlipped = false;

            if (trueSync != null)
            {
                isTrueSync = true;
                lock = trueSync.checkSync(buffer);
            }

            if (!lock && (invertedSync != null))
            {
                isTrueSync = false;
                lock = invertedSync.checkSync(buffer);
            }

            if (!lock && (setup.slippage > 0))
            {
                if (trueSync != null)
                {
                    isTrueSync = true;
                    lock = trueSync.checkSlip(buffer);
                }
                if (!lock && (invertedSync != null))
                {
                    isTrueSync = false;
                    lock = invertedSync.checkSlip(buffer);
                }
                isSlipped = lock;
            }

            xstate = lock? LOCK : LOST_SYNC;
        }

        return xstate;
    }
}
