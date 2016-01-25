/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.xstps.pds;
import gov.nasa.gsfc.drl.rtstps.core.ccsds.Packet;

/**
 * This class contains fragments of packet information that Sorcerer needs.
 * 
 * 
 */
final class PacketKernel
{
    /**
     * This is the Terra ESH time, which comes from the frame annotation.
     * The format is PB5:
     * <pre>
     *   8 bits      zero. my fill to extend it to 64 bits.
     *   1 bit       1
     *   14 bits     truncated julian day (0-9999) epoch: 8-10-95.
     *   17 bits     seconds of day (0-86,399)
     *   10 bits     milliseconds of second (0-999)
     *   10 bits     micro of milli (0-999)
     *   4 bits      zero.
     * </pre>
     */
    private long eshTime;

    /**
     * The packet time comes from the packet secondary header.
     * Some appids use an unsegmented format that I don't document here.
     * The format for day-segmented packets is:
     * <pre>
     *    Uint16 day since 1/1/1958
     *    Uint32 millisecond of day
     *    Uint16 microsecond of millisecond
     * </pre>
     * The secondary header may have a 9th byte. If it exists, it either
     * precedes the timecode or follows it.
     *
     * <pre>
     *    Byte   flags (0x80 = quicklook on)
     * </pre>
     */
    private long packetTime;

    /**
     * If true, the packet time is in CUC format.
     */
    private boolean isCUC = false;

    /**
     * Location of time in packet secondary header. It is a byte offset
     * from the start of the packet. It is usually 6 or 7.
     */
    



    public String toString()
    {
        return Long.toHexString(packetTime)+":cuc="+isCUC;
    }

    /**
     * Load this object with a different packet.
     * @param packet the packet
     * @param isCUC if true, this packet is an Aqua one that has
     *      unsegmented time in the secondary header. If false, it is
     *      one that has day segmented time in the secondary header.
     * @param timeOffset offset in bytes to timecode from start of packet.
     */
    void set(Packet packet, boolean isCUC, int timeOffset)
    {
        byte[] data = packet.getData();
        eshTime = makeEshTime(packet.getFrameAnnotation().timestamp);
        packetTime = makePacketTime(data,timeOffset);
        this.isCUC = isCUC;
        if (isCUC)
        {
            packetTime &= 0x0000ffffffffffffL;
        }
    }

    /**
     * Copy a packet kernel into this object.
     */
    void copy(PacketKernel pk)
    {
        packetTime = pk.packetTime;
        eshTime = pk.eshTime;
        isCUC = pk.isCUC;
    }

    /**
     * Does this packet kernel contain a packet?
     */
    final boolean isEmpty()
    {
        return eshTime == 0;
    }

    /**
     * Get the ESH time.
     */
    final long getEshTime()
    {
        return eshTime;
    }

    /**
     * Get the packet time.
     */
    long getPacketTime()
    {
        return isCUC? cucToDaySegmented(packetTime) : packetTime;
    }

    /**
     * Compare the packet times from two PacketKernel objects.
     * @return Zero if they are equal, a positive value if this time is
     *          greater than the passed object's time, and a negative
     *          value otherwise.
     */
    final int comparePacketTime(PacketKernel pk)
    {
        return (int)(packetTime - pk.packetTime);
    }

    /**
     * Make an ESH-format longword.
     * @param msFromEpoch Milliseconds from epoch.
     */
    private long makeEshTime(long msFromEpoch)
    {
        long days = msFromEpoch / 86400000L;
        long ms = msFromEpoch - 86400000L * days;
        long sec = ms / 1000L;
        ms -= sec * 1000L;
        days %= 9999L;
        return 0x0080000000000000L | (days << 41) | (sec << 24) | (ms << 14);
    }

    /**
     * Make a packet time longword.
     * @param data The packet data
     * @param start Array index, where the time is
     */
    private long makePacketTime(byte[] data, int start)
    {
        long t = 0;
        int shift = 56;
        for (int n = 0; n < 8; n++)
        {
            t |= ((long)data[start+n] & 0x0ffL) << shift;
            shift -= 8;
        }
        return t;
    }

    /**
     * Convert unsegmented time to day segmented format.
     * <pre>
     * aqua s/c bus tlm pkt format
     *      epoch=1/1/58
     *      16 bits pfield (I zero this)
     *      32 bits coarse time, seconds since epoch
     *      16 bits fine time, subseconds, 15.2 microseconds (1 mil micro / ffff)
     *                                      15.2587890625 micro per tick.
     *
     *      terra day segmented format (epoch = 1/1/58)
     *      16 bits day
     *      32 bits ms of day
     *      16 bits micro of ms
     * </pre>
     */
    public static long cucToDaySegmented(long cuc)
    {
        long secsSinceEpoch = (cuc >> 16) & 0x0ffffffffL;
        long fineTime = cuc & 0x0ffffL;

        //There have been complaints about the accuracy of the time conversion.
        //To avoid roundoff errors as much as possible, I changed the following
        //code to it uses longs instead of floats. That is, instead of 15.2f, I
        //now use 152L. micro10 is thus micro*10 instead of just microseconds.
        //long micro = (long)(15.2f * (float)fineTime);
        //long milliFromMicro = micro / 1000L;
        //long microOfMilli = micro - milliFromMicro * 1000L;

        long micro10 = 152L * fineTime;
        long milliFromMicro = micro10 / 10000L;
        long microOfMilli = (micro10 % 10000L) / 10L;

        long days = secsSinceEpoch / 86400L;
        long secsOfDay = secsSinceEpoch - days * 86400L;
        days &= 0x0ffffL;
        long milliOfDay = secsOfDay * 1000L + milliFromMicro;
        return (days << 48) | (milliOfDay << 16) | microOfMilli;
    }
}
