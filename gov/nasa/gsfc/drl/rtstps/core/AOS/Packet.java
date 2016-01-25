/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.AOS;
import gov.nasa.gsfc.drl.rtstps.core.Unit;

/**
 * This class contains a single CCSDS packet.
 * The software usually reuses all Packet objects, so you should not save
 * a reference to a Packet in the expectations that the contents will not
 * change.
 * 
 * 
 */
public final class Packet extends Unit implements PacketI
{
    public static final int PRIMARY_HEADER_LENGTH = 6;
    public static final int LENGTH_OFFSET = 4;
    public static final int IDLE_PACKET = 0x07ff;

    /**
     * This is packet annotation associated with this packet.
     */
    private Annotation annotation = new Annotation();



    /**
     * Constructor to creating a "copy" packet.  A user of this constructor intends to copy the data
     * to this objects data byte array.   The reset method may be used if this class
     * is used in this manner.  Throughout RT-STPS this is the main if not only way it is
     * being used.
     * @param length the packet length in bytes
     */
    public Packet(int length)
    {
        super(length);
    }

    /**
     * Alternate constructor for creating a "no copy" packet.  This is a special case of this class
     * and it should not be mixed with the other main cases or things will break.  The intent of this
     * class is for it to be used in situations where a full copy is not needed and the user simply needs
     * to momentarily wrap a data buffer with this class to get at the details of the packet.  The reset
     * method should not be used if the class is being used in this manner.
     * @param offset offset into the data buffer the packet data starts
     * @param data the data byte array
     */
    public Packet(int offset, byte[] data) {
    	this.startOffset = offset;
    	this.data = data;
    }

    public void setStartOffset(int offset) {
    	this.startOffset = offset;
    }
    public void setLength(int length) {
    	this.length = length;
    }

    /**
     * Reset this packet so that it may be used to contain a different packet.
     * @param length The length of the new packet
     */
    public void reset(int length)
    {
        deleted = false;
        super.length = length;
        annotation.reset();
        if (data.length < length)
        {
            data = new byte[length];
        }
    }

    /**
     * Get this packet's application ID.
     */
    public final int getApplicationId()
    {
        int x = ((int)data[startOffset + 0] << 8) | ((int)data[startOffset + 1] & 0x0ff);
        return x & 0x07ff;
    }

    /**
     * Static version to get a packet's application ID from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return application ID field
     */
    public final static int getApplicationId(int offset, byte[] data)
    {
        int x = ((int)data[offset + 0] << 8) | ((int)data[offset + 1] & 0x0ff);
        return x & 0x07ff;
    }

    /**
     * Get this packet's sequence counter.
     */
    public final int getSequenceCounter()
    {
        int x = ((int)data[startOffset + 2] << 8) | ((int)data[startOffset + 3] & 0x0ff);
        return x & 0x3fff;
    }

    /**
     * Get this packet's sequence counter from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return sequence counter field
     */
    public final static int getSequenceCounter(int offset, byte[] data)
    {
        int x = ((int)data[offset + 2] << 8) | ((int)data[offset + 3] & 0x0ff);
        return x & 0x3fff;
    }

    /**
     * Check secondary header flags
     * @return true or false
     */
	public final boolean hasSecondaryHeader() {
		int flag = (data[startOffset + 0] >>> 3) & 0x01;

		if (flag == 1) return true;
		return false;
	}

    /**
     * Check secondary header flags from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
 	 * @return true or false
     */
	public final static boolean hasSecondaryHeader(int offset, byte[] data) {
		int flag = (data[offset + 0] >>> 3) & 0x01;

		if (flag == 1) return true;
		return false;
	}

	/**
	 * Check that this packet is the first in a series
	 * @return true or false
	 */
	public final boolean isFirstPacketInSequence() {
		return (((data[startOffset + 2] >>> 6) & 0x003)==1);
	}

	/**
	 * Check that this packet is the first in a series from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
 	 * @return true or false
	 */
	public final static boolean isFirstPacketInSequence(int offset, byte[] data) {
		return (((data[offset + 2] >>> 6) & 0x003)==1);
	}

	/**
	 * Check that this packet is the middle in a series
	 * @return true or false
	 */
	public final boolean isMiddlePacketInSequence() {
		return (((data[startOffset + 2] >>> 6) & 0x003)==0);
	}

	/**
	 * Check that this packet is the middle in a series from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return true or false
	 */
	public final static boolean isMiddlePacketInSequence(int offset, byte[] data) {
		return (((data[offset + 2] >>> 6) & 0x003)==0);
	}

	/**
	 * Check that this packet is the last in a series
	 * @return true or false
	 */
	public final boolean isLastPacketInSequence() {
		return (((data[startOffset + 2] >>> 6) & 0x003)==2);
	}

	/**
	 * Check that this packet is the last in a series from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return true or false
	 */
	public final boolean isLastPacketInSequence(int offset, byte[] data) {
		return (((data[offset + 2] >>> 6) & 0x003)==2);
	}

	/**
	 * Check that this packet is the last in a series
	 * @return true or false
	 */
	public final boolean isStandalonePacket() {
		return (((data[startOffset + 2] >>> 6) & 0x003)==3);
	}

	/**
	 * Check that this packet is the last in a series from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return true or false
	 */
	public final boolean isStandalonePacket(int offset, byte[] data) {
		return (((data[offset + 2] >>> 6) & 0x003)==3);
	}

	/**
	 * Get the packet length field from the header
	 * @return length field bytes
	 */
    @Override
	public final int getPacketLength() {
		int len = (((int)data[startOffset + 4] << 8) & 0x0ff00);
		len |= ((int)data[startOffset + 5] & 0x0ff);
		len &= 0x0ffff; // paranoid...

		return len;
	}

	/**
	 * Static version of getPacketLength(), requires data and index into to be passed in.
	 * Intended to be used to "peek" into a data buffer that contains a packet but that has not
	 * been processed further.
	 * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return length field
	 */
	public final static int getPacketLength(int offset, byte[] data) {
		int len = (((int)data[offset + 4] << 8) & 0x0ff00);
		len |= ((int)data[offset + 5] & 0x0ff);
		len &= 0x0ffff; // paranoid...

		return len;
	}

	/**
	 * Get the total packet size, including header and body
	 *
	 * @return total bytes
	 */
	@Override
	public final int getPacketSize() {
		int totalLen = getPacketLength();
		totalLen += PRIMARY_HEADER_LENGTH + 1;
		return totalLen;
	}

	/**
	 * Static version of getPacketSize(), requires data and index into to be passed in.
	 * Intended to be used to "peek" into a data buffer that contains a packet but that has not
	 * been processed further.
	 * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return total size in bytes
	 */
	public final static int getPacketSize(int offset, byte[] data) {
		int totalLen = getPacketLength(offset, data);

		totalLen += PRIMARY_HEADER_LENGTH + 1;
		return totalLen;
	}

	/**
	 * Get sequence flags...
	 * @return 0,1,2 or 3
	 */
	public final int getSequenceFlags() {
		int scf = (data[startOffset + 2] >>> 6) & 0x03;
		return scf;
	}

	/**
	 * Get sequence flags from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return 0, 1, 2 or 3
	 */
	public final static int getSequenceFlags(int offset, byte[] data) {
		int scf = (data[offset + 2] >>> 6) & 0x03;
		return scf;
	}


	/**
	 * Get the type field...
	 * @return 0 or 1
	 */
	public final int getType() {
		int type = (data[startOffset + 0] >>> 4) & 0x01;
		return type;
	}

	/**
	 * Get the type field from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return 0 or 1
	 */
	public final int getType(int offset, byte[] data) {
		int type = (data[offset + 0] >>> 4) & 0x01;
		return type;
	}

	/**
	 * Get the version field...
	 * @return 0,1,2 or 3...
	 */
	public final int getVersion() {
		int version = (data[startOffset + 0] >>> 5) & 0x07;
		return version;
	}

	/**
	 * Get the version field from a data buffer
     * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @return 0, 1, 2 or 3...
	 */
	public final int getVersion(int offset, byte[] data) {
		int version = (data[offset + 0] >>> 5) & 0x07;
		return version;
	}

	/**
	 * Supports 8 bytes or less in the secondary header
	 * @param timeStampSize
	 * @return timestamp copied into a long, right justified
	 */
	public long getTimeStamp(int timeStampSize) {
		/**
		long rawTime = 0L;

		if (timeStampSize > 8) {
			timeStampSize = 8;
		}
		int shift = (timeStampSize - 1) * 8;

		for (int i = 0; i < timeStampSize; i++) {
			rawTime |= (data[startOffset + 6 + i] & 0x0ffL) << shift;
			shift -= 8;
		}
		**/

		return calcTimeStamp(6, timeStampSize);

	}
	public long getTimeStamp(int offset, int timeStampSize) {
		return calcTimeStamp(offset, timeStampSize);
	}
	private long calcTimeStamp(int offset, int timeStampSize) {
	    	//System.out.println("in calcTimeStamp");
		long rawTime = 0L;

		if (timeStampSize > 8) {
			timeStampSize = 8;
		}
		int shift = (timeStampSize - 1) * 8;

		for (int i = 0; i < timeStampSize; i++) {

		    	try
		    	{
		    	    rawTime |= (data[startOffset + offset + i] & 0x0ffL) << shift;
		    	    shift -= 8;
		    	}
		    		catch (ArrayIndexOutOfBoundsException e)
		    	{
		    		    System.out.println("Invalid timestamp.");
		    		    System.out.println(rawTime);
		    		    this.deleted = true;


		    	}

		}


		return rawTime;
	}

	/**
	 * Supports 8 bytes or less in the secondary header from a data buffer
	 * @param offset index into byte[] where packet starts
	 * @param data the byte[] array of the packet values
	 * @param timeStampSize
	 * @return timestamp copied into a long, right justified
	 */
	public static long getTimeStamp(int offset, byte[] data, int timeStampSize) {
	    	System.out.println("in getTimeStamp");
		long rawTime = 0L;

		if (timeStampSize > 8) {
			timeStampSize = 8;
		}
		int shift = (timeStampSize - 1) * 8;

		for (int i = 0; i < timeStampSize; i++) {
			rawTime |= (data[offset + 6 + i] & 0x0ffL) << shift;
			shift -= 8;
		}

		return rawTime;
	}

    /**
     * Get debug information.
     */
    public String toString()
    {
        return "length="+length+" deleted="+deleted+" "+annotation;
    }

	public String hdrToString() {
		/** String output = String.format("AppId[" + %4d getAppId() + "]" +
						" SeqCount[" + %5d getSeqCount() + "]" +
						" Length[" + %5d getPacketLength() + "]" +
						" Size[" + %5d getPacketSize() + "]"; **/

		String output = String.format("AppId[%4d] SeqCount[%5d] Length[%5d] Size[%5d]",
									getApplicationId(), getSequenceCounter(), getPacketLength(),getPacketSize());

		return output;
	}
    /**
     * Get this packet's annotation.
     */
    public final Packet.Annotation getPacketAnnotation()
    {
        return annotation;
    }

    /**
     * Packet annotation. It contains quality information for a single packet.
     */
    public final class Annotation
    {
        /**
         * If true, the packet has an invalid length. An operator will
         * define valid minimum and maximum lengths for each packet stream.
         */
        public boolean isInvalidLength = false;

        /**
         * If true, there was a packet sequence error between this one and
         * the preceding packet.
         */
        public boolean hasSequenceError = false;

        /**
         * If true, the packet could not be constructed in its entirety, so
         * the Path Service appended fill bytes to it to fill it out to its
         * advertised length.
         */
        public boolean isPacketWithFill = false;

        /**
         * The number of "good" bytes in this packet, which is the packet
         * length for most packets. If <code>isPacketWithFill</code> is
         * true, then this field is the index of the first fill byte.
         */
        public int goodByteCount;

        /**
         * Reset this annotation so that it can be used for a different
         * packet.
         */
        public void reset()
        {
            isPacketWithFill = false;
            goodByteCount = length;
            isInvalidLength = false;
            hasSequenceError = false;
        }

        public String toString()
        {
            return "badLen="+isInvalidLength +
                " goodByte="+goodByteCount +
                " withFill="+isPacketWithFill +
                " seqError="+hasSequenceError;
        }
    }

}
