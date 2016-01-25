/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.fs.clock.TimeStamp;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class defines setup fields that are commonly associated with the Frame
 * Synchronizer component.
 * 
 */
public class FsSetup
{
    public static final int SYNC_PATTERN = 0x1acffc1d;

    /**
     * The frame sync pattern. The array length must be "syncLength" long.
     */
    public byte[] syncPattern = {(byte)0x1a, (byte)0xcf, (byte)0xfc, (byte)0x1d};

    /**
     * The sync pattern length. It must be 2, 3, or 4 bytes.
     */
    public int syncLength = 4;

    /**
     * The frame length in bytes.
     */
    public int frameLength = 1024;

    /**
     * If 1 or 2, it permits frames to slip long or short 1 or 2 bits
     * respectively. Only choose 0, 1, or 2.
     */
    public int slippage = 0;

    /**
     * If true, the synchronizer searches for true sync patterns. Either
     * trueSyncEnabled or invertedSyncEnabled (or both) must be enabled.
     */
    public boolean trueSyncEnabled = true;

    /**
     * If true, the synchronizer searches for inverted sync patterns. Either
     * trueSyncEnabled or invertedSyncEnabled (or both) must be enabled.
     */
    public boolean invertedSyncEnabled = false;

    /**
     * If true, the synchronizer will correct the polarity of frames
     * that it determines have an inverted sync pattern. It inverts the
     * entire frame and not just the sync pattern. This should always be
     * true if frames are going to the CRC/RS decoders or to the CCSDS
     * services.
     */
    public boolean correctInversion = true;

    /**
     * If the synchronizer loses sync, then it will flywheel a number of frame
     * lengths equal to the flywheel duration, after which it will then search
     * for sync once again.
     */
    public int flywheelDuration = 0;

    /**
     * If false, the synchronizer discards flywheel data. If true, it treats it
     * as lock frames, eventhough it may not be valid frames. The annotation
     * will mark each flywheel frame as not a lock frame. The synchronizer
     * does not detect inverted data in flywheel frames, so you should
     * probably not send flywheel frames to the decoders or the CCSDS services
     * unless you are certain the input stream does not contain inverted data.
     */
    public boolean isSendingFlywheels = false;

    /**
     * If true, it assumes the frames are encoded with bit transition density
     * encoding, and it will decode the frames.
     */
    public boolean isPnEncoded = false;

    /**
     * This object contains information to configure the clock that stamps every frame
     * with a time.
     */
    TimeStamp timestamp = new TimeStamp();

    public FsSetup(long pattern,int synclength,int framelength) throws RtStpsException
    {

        long i = pattern;
        syncPattern = new byte[synclength];
        if (i != SYNC_PATTERN)
        {
            syncLength = synclength;
            if (syncLength < 2)
            {
                throw new RtStpsException("sync pattern must be at least two bytes");
            }
            int sl = syncLength - 1;
            do
            {
                syncPattern[sl] = (byte)i;
                i >>= 8;
                --sl;
            }
            while (sl >= 0);
        }

        frameLength = framelength;

        slippage = 0;

        flywheelDuration = 0;

        trueSyncEnabled = true;

        invertedSyncEnabled = false;

        correctInversion = false;

        isSendingFlywheels = false;
        isPnEncoded = false;

    }

    public FsSetup(Element element) throws RtStpsException
    {
        NodeList timelist = element.getElementsByTagName("timestamp");
        if (timelist.getLength() > 0)
        {
            Element time = (Element)timelist.item(0);
            timestamp.load(time);
        }

        int i = Convert.toHexInteger(element,"pattern",SYNC_PATTERN);
        if (i != SYNC_PATTERN)
        {
            String value = element.getAttribute("pattern");
            syncLength = (value.length() + 1) / 2;
            if (syncLength < 2)
            {
                throw new RtStpsException("sync pattern must be at least two bytes");
            }
            int sl = syncLength - 1;
            do
            {
                syncPattern[sl] = (byte)i;
                i >>= 8;
                --sl;
            }
            while (sl >= 0);
        }

        frameLength = Convert.toInteger(element,"frameLength",frameLength,16);

        slippage = Convert.toInteger(element,"slip",slippage,0,2);

        flywheelDuration = Convert.toInteger(element,"flywheelDuration",
                flywheelDuration,0);

        trueSyncEnabled = Convert.toBoolean(element,"trueSync",
                trueSyncEnabled);

        invertedSyncEnabled = Convert.toBoolean(element,"invertedSync",
                invertedSyncEnabled);

        correctInversion = Convert.toBoolean(element,"correctPolarity",
                correctInversion);

        isSendingFlywheels = Convert.toBoolean(element,"sendFlywheels",
                isSendingFlywheels);

        isPnEncoded = Convert.toBoolean(element,"PnEncoded",
                isPnEncoded);
    }
}
