/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.core.output;
import gov.nasa.gsfc.drl.rtstps.core.Convert;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * This device writes units to a tcp/ip socket.
 * 
 */
class SocketDevice extends AbstractOutputDevice
{
    private java.net.Socket socket;

    SocketDevice(org.w3c.dom.Element element) throws RtStpsException
    {
        String host = element.getAttribute("host");
        int port = Convert.toInteger(element,"port",4000);
        int bufferSize = Convert.toInteger(element,"bufferSize",8192);

        try
        {
            socket = new java.net.Socket(host,port);
            int dsize = socket.getSendBufferSize();
            if (bufferSize > dsize)
            {
                socket.setSendBufferSize(bufferSize);
                bufferSize = socket.getSendBufferSize();
            }
            //System.out.println("tcp send buffer size. set="+
            //        bufferSize + " default=" + dsize);
            OutputStream os = socket.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os,bufferSize);
            output = new DataOutputStream(bos);
        }
        catch (Exception e)
        {
            throw new RtStpsException(e);
        }
    }

    void shutdown() throws RtStpsException
    {
        try
        {
            output.flush();
            socket.close();
            output.close();
        }
        catch (java.io.IOException ioe)
        {
            throw new RtStpsException(ioe);
        }
    }
}
