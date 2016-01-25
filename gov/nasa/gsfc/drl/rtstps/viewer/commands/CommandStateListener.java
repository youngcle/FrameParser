/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.viewer.commands;

/**
 * Classes that implement this interface listen for command state change
 * notification.
 * 
 */
public interface CommandStateListener
{
    public static final int UNLOADED = 0;
    public static final int LOADED_STOPPED = 1;
    public static final int LOADED_GO = 2;

    public void commandStateChange(int state);
}
