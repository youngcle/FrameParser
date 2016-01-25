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
import java.io.File;
import java.io.FileOutputStream;

/**
 * This device writes units to a file.
 * auto-generated file name format:
 * "t" + code + userLabel + currentDateTime + ".dat"
 * code = 'p':packet, 'f':frame, "g":generic unit
 * 
 */
class FileDevice extends AbstractOutputDevice
{
    private static java.text.SimpleDateFormat sdf = null;

    FileDevice(String unitType,String userLable) throws RtStpsException{
        String directory = "/home/youngcle";
        String userLabel = userLable;
        String filename = "";

        boolean autoGenerateFilename = true;

        if (autoGenerateFilename || (filename.length() == 0))
        {
            if (sdf == null)
            {
                sdf = new java.text.SimpleDateFormat("yyyyDDDHHmmss");
            }
            String fdate = sdf.format(new java.util.Date());
            char type = unitType.toLowerCase().charAt(0);
            filename = "t" + type + fdate + userLabel + ".dat";
        }

        try
        {
            File file = new File(directory,filename);
            FileOutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os,8192);
            output = new DataOutputStream(bos);
        }
        catch (java.io.IOException ioe)
        {
            throw new RtStpsException(ioe);
        }

    }

    FileDevice(org.w3c.dom.Element element, String unitType)
            throws RtStpsException
    {
        String directory = element.getAttribute("directory");
        String userLabel = element.getAttribute("userLabel");
        String filename = element.getAttribute("filename");

        boolean autoGenerateFilename = Convert.toBoolean(element,
                "autoGenerateFilename",true);

        if (autoGenerateFilename || (filename.length() == 0))
        {
            if (sdf == null)
            {
                sdf = new java.text.SimpleDateFormat("yyyyDDDHHmmss");
            }
            String fdate = sdf.format(new java.util.Date());
            char type = unitType.toLowerCase().charAt(0);
            filename = "t" + type + fdate + userLabel + ".dat";
        }

        try
        {
            File file = new File(directory,filename);
            FileOutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os,8192);
            output = new DataOutputStream(bos);
        }
        catch (java.io.IOException ioe)
        {
            throw new RtStpsException(ioe);
        }
    }

    void shutdown() throws RtStpsException
    {
        try
        {
            output.close();
        }
        catch (java.io.IOException ioe)
        {
            throw new RtStpsException(ioe);
        }
    }
}
