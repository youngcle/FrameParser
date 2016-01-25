/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps;
import gov.nasa.gsfc.drl.rtstps.core.Builder;
import gov.nasa.gsfc.drl.rtstps.core.Configuration;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsException;
import gov.nasa.gsfc.drl.rtstps.core.RtStpsNode;
import gov.nasa.gsfc.drl.rtstps.core.fs.FrameSynchronizer;
import gov.nasa.gsfc.drl.rtstps.core.status.StatusItem;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.xml.sax.InputSource;

/**
 * This class is the main entry for the RT-STPS Batch Processor. Batch is a
 * standalone, comand-line-invoked program that reads a raw telemetry file
 * and produces one or more files of packets, frames, and/or other units.
 * 
 */
public final class Batch
{
    private static final int BUFFERLENGTH = 8192;

    public static void main(String[] args)
    {
        System.out.println(gov.nasa.gsfc.drl.rtstps.Version.getVersion());

        if (args.length != 2)
        {
            System.err.println("Options: [-Droot=<stp.dtdDirectory>] [-Dsetup=<directory>] [-Draw=<directory>]");
            System.err.println("Arguments: [setupFile] [dataFile]");
            System.exit(1);
        }

        FrameSynchronizer fs = null;
        Configuration configuration = null;
        Builder builder = null;
        try {
            builder = new Builder();
            fs = builder.createChain103();
            configuration = builder.getConfiguration();

        } catch (RtStpsException e) {
            e.printStackTrace();
        }

//        try
//        {
//            String directory = System.getProperty("setup");
//            File setupFile = new File(directory,args[0]);
//            BufferedReader br = new BufferedReader(new FileReader(setupFile));
//            InputSource setup = new InputSource(br);
//            String root = System.getProperty("root","file://localhost/");
//            setup.setSystemId(root);
//
//            Builder builder = new Builder();
//            fs = builder.create(setup);
//            configuration = builder.getConfiguration();
//            br.close();
//        }
//        catch (java.io.IOException ifnf)
//        {
//            System.err.println(ifnf.getMessage());
//            System.exit(-1);
//        }
//        catch (RtStpsException be)
//        {
//            System.err.println(be.getMessage());
//            be.printStackTrace();
//            System.exit(-2);
//        }

        BufferedInputStream input = null;
        try
        {
            String directory = "/run/media/youngcle/3178-435E/nasa/rt-stps";
            File dataFile = new File(directory,args[1]);
            FileInputStream fis = new FileInputStream(dataFile);
            input = new BufferedInputStream(fis);
        }
        catch (java.io.FileNotFoundException ofnf)
        {
            ofnf.printStackTrace();
            System.exit(-3);
        }

        long tbytes = 0L;
        long t0 = System.currentTimeMillis();

        byte[] data = new byte[BUFFERLENGTH];

        try
        {
            while (true)
            {
                int bytes = input.read(data,0,BUFFERLENGTH);
                if (bytes == -1) break;
                fs.putBuffer(data,bytes);
                tbytes += bytes;
                System.out.println("writed(KB):"+tbytes/1024);
            }

            fs.shutdown();
        }
        catch (java.io.IOException re)
        {
            re.printStackTrace();
            System.exit(-4);
        }
        catch (RtStpsException stpe)
        {
            stpe.printStackTrace();
            System.exit(-5);
        }

        long t1 = System.currentTimeMillis();
        long t = t1 - t0;
        long bitsPerMs = 8L * tbytes / t;
        long bitsPerSecond = bitsPerMs * 1000L;
        long kilobitsPerSecond = bitsPerSecond / 1024L;
        System.out.println("kilobits per second="+ kilobitsPerSecond);
        System.out.println();

        try
        {
            input.close();
        }
        catch (java.io.IOException ce)
        {
            ce.printStackTrace();
        }

        TreeMap<String, RtStpsNode> nodes = configuration.getStpsNodes();
        Iterator<RtStpsNode> i = nodes.values().iterator();
        while (i.hasNext())
        {
            RtStpsNode node = (RtStpsNode)i.next();
            Collection<StatusItem> status = node.getStatusItems();
            if (status != null)
            {
                System.out.println(node);
                Iterator<StatusItem> ii = status.iterator();
                while (ii.hasNext())
                {
                    StatusItem si = (StatusItem)ii.next();
                    System.out.println("    " + si);
                }
            }
        }
    }
}
