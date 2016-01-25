/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.server;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

/**
 * This class logs server events. It writes timetagged messages to the monitor
 * screen and to the file stps.log if it is enabled.
 * 
 */
class LoggerSTAN
{
    private SimpleDateFormat dateFormat = new SimpleDateFormat();
    private java.io.PrintWriter log;

    LoggerSTAN()
    {
        try
        {
            log = new PrintWriter(new FileOutputStream("stps.log"),true);
        }
        catch (FileNotFoundException fnfe)
        {
            System.out.println("Cannot open stps.log");
            System.exit(1);
        }
    }

    void print(String message)
    {
        String now = dateFormat.format(new java.util.Date());
        String text = now + "    " + message;
        System.out.println(text);
        log.println(text);
    }

    void close()
    {
        print("The server exits.");
        log.close();
    }
}
