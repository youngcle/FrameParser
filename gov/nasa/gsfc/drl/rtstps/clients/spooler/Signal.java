/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.clients.spooler;

/**
 * This class is a monitor object. Its primary use is for the socket reader
 * to awaken the socket writer. It has a secondary use. The socket writer
 * tells the socket reader that it has terminated.<p>
 * Note that the implementation uses two unlocking mechanisms. The counters
 * are used when the reader is not yet waiting to receive. It prevents a
 * deadlock.
 * 
 */
final class Signal
{
    private volatile int sendCount = 0;
    private volatile int receiveCount = 0;
    private boolean socketWriterTerminated = false;

    /**
     * The socket reader signals the socket writer that data is available.
     * It awakens the socket writer.
     */
    synchronized void send()
    {
        ++sendCount;
        notifyAll();
    }

    /**
     * The socket writer tests for a signal and waits if there is none.
     */
    synchronized void receive() throws InterruptedException
    {
        if (receiveCount == sendCount)
        {
            wait();
        }
        receiveCount = sendCount;
    }

    /**
     * The socket writer signals that it is no longer sending data to the
     * output socket.
     */
    final void terminate()
    {
        socketWriterTerminated = true;
    }

    /**
     * Has the socket writer terminated?
     */
    final boolean isTerminated()
    {
        return socketWriterTerminated;
    }
}
