/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.fs;
import gov.nasa.gsfc.drl.rtstps.core.Frame;
import gov.nasa.gsfc.drl.rtstps.core.FrameAnnotation;

import java.util.ArrayList;

/**
 * This class manages a sequential list of frames. When given a Buffer,
 * it uses it to fill frames, advancing to the next frame or creating one
 * when necessary. It resets the annotation on any frame it fills, but it
 * does not set set annotation fields.
 * <p>
 * This class reuses its Frame objects, so subsequent users must not
 * cache frames.
 * 
 * 
 */
class Frames
{
    private int frameLength;
    private ArrayList<Frame> frames = new ArrayList<Frame>();
    private Frame currentFrame;
    private int currentFrameIndex = 0;
    private FrameCaddy caddy;

    /**
     * Create a Frames object.
     * @param frameLength The frame length in bytes. All frames must have the
     *          same length.
     */
    Frames(int frameLength)
    {
        this.frameLength = frameLength;
        currentFrame = new Frame(frameLength);
        frames.add(currentFrame);
        caddy = new FrameCaddy(currentFrame);
    }

    /**
     * Reset the frame list so it contains no full or partial frames.
     */
    void flushAllData()
    {
        currentFrameIndex = 0;
        currentFrame = (Frame)frames.get(0);
        caddy.setFrame(currentFrame);
    }

    void emptyFrameList(){
    	frames.clear();
    }

    /**
     * Remove all complete frames from the list but keep the last partial
     * frame if it exists. One does this after sending the complete frames
     * to a FrameReceiver, so they are no longer needed.
     */
    void flushCompleteFrames()
    {
        if (caddy.isFullFrame())
        {
            /**
             * When the caddy's frame is full, there are no part-filled
             * frames, so I can simply reset to the beginning.
             */
            flushAllData();
        }
        else if (currentFrameIndex > 0)
        {
            /**
             * When the index > 0 and the caddy has a partial frame, it means
             * I have a part-filled frame following a bunch of filled ones.
             * I want to put the last one first and to forget about the rest.
             * I swap the first frame and part-filled frame positions.
             */
            Frame x = frames.get(0);					
            frames.set(0,currentFrame);
            frames.set(currentFrameIndex,x);
            currentFrameIndex = 0;
        }
        /**
         * The remaining case is a part-filled frame in the first position,
         * which is what I want, so I do nothing.
         */
    }

    /**
     * Get a list of complete frames in time order.
     * @return null if there are no complete frames in the list.
     */
    Frame[] getFrameList()
    {
        int completedFrames = currentFrameIndex;

        /**
         * I don't advance the frame index until I need a new frame, so
         * I must count the caddy's frame if it is full.
         */
        if (caddy.isFullFrame()) ++completedFrames;
        Frame[] list = null;
        if (completedFrames > 0)
        {
            list = new Frame[completedFrames];
            for (int n = 0; n < completedFrames; n++)
            {
                list[n] = (Frame)frames.get(n);
            }
        }
        return list;
    }

    /**
     * Get the FrameAnnotation object from the current frame.
     */
    FrameAnnotation getCurrentFrameAnnotation()
    {
        return currentFrame.getFrameAnnotation();
    }

    /**
     * Copy as much of buffer as needed or as possible to fill the current
     * frame. It gets a new frame if the current one is already full.
     * @param buffer The buffer to copy to the frame
     * @param invert If true, invert the buffer's bits while copying
     * @return The number of bytes yet to fill in the current frame.
     */
    int copyBufferToFrame(Buffer buffer, boolean invert)
    {
        if (caddy.isFullFrame())
        {
            ++currentFrameIndex;
            if (currentFrameIndex < frames.size())
            {
                currentFrame = (Frame)frames.get(currentFrameIndex);
            }
            else
            {
                currentFrame = new Frame(frameLength);
                frames.add(currentFrame);
            }
            caddy.setFrame(currentFrame);
        }
        return caddy.copyToFrame(buffer,invert);
    }


    /**
     * This class holds a Frame and is responsible for filling it with data.
     * It resets the frame's annotation, but it does not set annotaion fields.
     */
    class FrameCaddy
    {
        private Frame frame;
        private int index;
        private int leftShiftBits;
        private int rightShiftBits;
        private byte[] data;
        private int bytesToFill;
        private boolean isHalfByte;

        /**
         * Create a FrameCaddy with a starting frame to fill.
         */
        FrameCaddy(Frame f)
        {
            setFrame(f);
        }

        /**
         * Change the frame this caddy is filling.
         */
        void setFrame(Frame f)
        {
            frame = f;
            frame.reset();
            index = 0;
            bytesToFill = frame.getSize();
            leftShiftBits = -1;
            rightShiftBits = -1;
            isHalfByte = false;
            data = frame.getData();
        }

        /**
         * Is this frame full?
         */
        boolean isFullFrame()
        {
            return bytesToFill == 0;
        }

        /**
         * Copy buffer data to the frame.
         * @param buffer The buffer to copy to a frame
         * @param invert If true, invert the buffer's bits while copying
         * @return The number of bytes yet to fill in the frame.
         */
        int copyToFrame(Buffer buffer, boolean invert)
        {
            /**
             * "leftShiftBits: and "rightShiftBits" tell me how many bits
             * I need to shift the data to get the frame aligned. With
             * each new frame, they are negative, so I use the values from
             * the buffer. They never change during a frame even if I cross
             * buffers.
             */
            if (leftShiftBits == -1)
            {
                leftShiftBits = buffer.index.bit;
                rightShiftBits = 8 - leftShiftBits;
            }
            else
            {
                buffer.index.bit = leftShiftBits;
            }

            /**
             * When the shift is zero, I do not need to align the data, so
             * it is a straight copy.
             */
            if (leftShiftBits == 0)
            {
                int bytes = buffer.getRemainingBytes();
                if (bytes > bytesToFill) bytes = bytesToFill;

                if (invert)
                {
                    int b = buffer.index.offset;
                    int d = index;
                    byte[] bdata = buffer.data;
                    for (int n = 0; n < bytes; n++)
                    {
                        data[d++] = (byte)(~bdata[b++]);
                    }
                }
                else
                {
                    System.arraycopy(buffer.data, buffer.index.offset,
                                data, index, bytes);
                }
                index += bytes;
                bytesToFill -= bytes;
                buffer.advance(bytes);
            }
            else
            {
                /**
                 * "isHalfByte" means that the frames are not aligned to
                 * byte boundaries, and I have to handle a buffer byte that
                 * contains pieces of two frames.
                 */
                if (isHalfByte)
                {
                    int offset = buffer.index.offset;
                    int x = data[index] & 0x0ff;
                    int y = buffer.data[offset] & 0x0ff;
                    x |= (y >> rightShiftBits);
                    if (invert) x = ~x;
                    data[index] = (byte)x;
                    isHalfByte = false;
                    ++index;
                    --bytesToFill;
                    if ((bytesToFill == 0) || (buffer.getRemainingBytes() == 0))
                    {
                        return bytesToFill;
                    }
                }

                /**
                 * "willFill" is the number of bytes I will move into the
                 * current frame, which is the remaining frame size or the
                 * remaining buffer size -- whichever is smaller.
                 */
                int willFill = bytesToFill;
                if (willFill >= buffer.getRemainingBytes())
                {
                    willFill = buffer.getRemainingBytes() - 1;
                    isHalfByte = true;
                }

                byte a[] = buffer.data;
                int n = buffer.index.offset;

                /**
                 * I copy from buffer to frame with alignment.
                 */
                if (invert)
                {
                    int x = a[n] & 0x0ff;
                    for (int k = 0; k < willFill; k++)
                    {
                        int r = x << leftShiftBits;
                        ++n;
                        x = a[n] & 0x0ff;
                        r |= (x >> rightShiftBits);
                        data[index++] = (byte)(~r);
                    }
                }
                else
                {
                    int x = a[n] & 0x0ff;
                    for (int k = 0; k < willFill; k++)
                    {
                        int r = x << leftShiftBits;
                        ++n;
                        x = a[n] & 0x0ff;
                        r |= (x >> rightShiftBits);
                        data[index++] = (byte)r;
                    }
                }

                buffer.setLocation(n);
                bytesToFill -= willFill;

                /**
                 * I append the half-byte piece.
                 */
                if (isHalfByte)
                {
                    int x = a[n] << leftShiftBits;
                    if (invert) x = ~x;
                    data[index] = (byte)x;
                    buffer.advance(1);
                }
            }

            return bytesToFill;
        }
    }
}
