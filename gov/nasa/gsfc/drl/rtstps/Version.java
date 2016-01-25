/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps;


/**
 * this static class holds the current RT-STPS version number.
 *
 * 3.01 2/7/2003
 * Occasionally the server would show a traceback from the sorcerer node. It indicated
 * a concurrent modification error in the gap list while it was creating the construction
 * record. It appears that sometimes our scheduler was issuing a shutdown before all packets
 * (gaps, in particular) were recorded. As a consequence, it was creating the CR at the same
 * time that the last gap was being recorded. I've changed the sorcerer code so that now it
 * ignores all packets the instant the shutdown command is issued.
 *
 * 3.02 4/11/2003
 * Some users complained that all PDS files from the same pass did not have the same timestamp.
 * Fixed with a static time variable.
 *
 * 3.03 9/28/2004
 * In the Reed Solomon decoder, the default virtual fill was incorrect for interleave 5.
 * The RS decoder erroneously expected interleave 5 frames to be 1204 bytes instead of 1264.
 *
 * 3.04 6/8/2005
 * In PDS creation, I delete empty data files.
 *
 * 3.05 10/12/2005
 * In the servers, I added -Dsecurity=0 to disable security checking and
 * -Dsecurity=1 to enable it (default)>
 * 
 * 3.06 6/6/2006
 * Added NSLS support.
 * If an output socket connection fails, just don't send to that socket (rather
 * than the load failing).
 *
 */
public final class Version
{
    private static String FVERSION = "5.6";
    private static String IVERSION = Version.FVERSION;
    private static String VERSION = "RT-STPS version " + FVERSION + " Created April 23, 2014";  // alas we should automate this one...

    public static String getVersionNumber()
    {
        return IVERSION;
    }

    public static String getVersion()
    {
        return VERSION;
    }

    public static void main(String[] args)
    {
        System.out.println(VERSION);
    }
}
