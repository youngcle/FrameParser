/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
/******************************************************************************
*
*  NISGS/RT-STPS
*
*  History:
*
*   9-Jun-06, J Love, GST	Original version.
*   14-Aug-09 K Rice, GST   Changed logger to read site property file or proceed
*                           with traditional log creation.
*
******************************************************************************/

package gov.nasa.gsfc.drl.rtstps.server;
import gov.nasa.gsfc.nisgs.nsls.Log;
import gov.nasa.gsfc.nisgs.nsls.NSLS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Logger creates either a NSLS log by reading a site.properties file or 
 * creates a log based on the value in the certain -D property options.
 * The site.properties file takes precedence over the command-line options
 * even if they are present.
 * 
 *
 */
class Logger {
  private Log log;
 
  /**
   * Create a log
   */
  public Logger () {
    createLog();
    log.setDefaultSource(new NSLS.RTSTPS());
    log.setToStdErr(true);
    return;
  }
  /**
   * The new default behavior is to read a properties file.
   * The properties file is found in the directory above where
   * RT-STPS is installed. 
   * If it does not exist, the traditional method 
   * is used to create the log... 
   */
  private void createLog() {

	  boolean tryTraditional = false; // default
	  
	  Properties properties = new Properties();
	  try {
		  // first look where it should be if this is a JSW run
		  // program...
		  File firstTry = new File ( "../site.properties");
		  
		  // otherwise look where it MIGHT be if this is run from
		  // a shell script
		  File secondTry = new File ( "../../site.properties");
		  
		  // If all else fails, try the traditional command line options
		  //
		  
		  if (firstTry.exists()) {
			  properties.load(new FileInputStream(firstTry));
			  createLogFromPropertiesFile(properties);
		  } else if (secondTry.exists()) {
			  properties.load(new FileInputStream(secondTry));
			  createLogFromPropertiesFile(properties);
		  } else {
			 tryTraditional = true; 
		  }
		  
	  } catch (IOException e) {
		  // if there are any IO exceptions, print out the message
		  // and then try the traditional method...
		  // NOTE: it's NOT CLEAR any exceptions will every be thrown
		  // to get here ...
		  System.out.println(e.getMessage());
		  tryTraditional = true;
	  }
	  
	  // tryTraditional should be false if the site.properties is found
	  // other wise go with the -D args from the cmd line
	  if (tryTraditional) {
		  createTraditionalLog();
	  }
	  
	  // unfortunately it seems possible (but not likely) that all log creation 
	  // options could fail, so check that.
	  assert(log != null);
  }
  /**
   * Get the NSLS configuration from the properties file... hopefully
   * @param properties
   */
  private void createLogFromPropertiesFile(Properties properties) {
	  
      System.out.println("Configuring RT-STPS for IPOPP Mode.");  
      
      int nslsPort = Integer.parseInt(properties.getProperty("NSLS_SERVER_PORT"));
      String nslsAddress = properties.getProperty("NSLS_SERVER_HOST");
     
      log = new Log(nslsAddress, nslsPort, null);
  }
  /**
   * The traditional method, check individually configured -D
   * properties, either log to a file, stdout or an NSLS server
   */
  private void createTraditionalLog () {
	  
	System.out.println("Configuring RT-STPS for Standalone Mode.");
	
    if (System.getProperty("log.server") != null) {
      StringTokenizer byColon = new StringTokenizer(System.getProperty("log.server"),":");
      String host = byColon.nextToken();
      int port = Integer.parseInt(byColon.nextToken());
      String tmpDir = byColon.nextToken();
      log = new Log(host,port,tmpDir);
      if (System.getProperty("log.file") != null) {
        log.setToLogFile(new File(System.getProperty("log.file")));
      }
      if (System.getProperty("log.stdout") != null) {
        log.setToStdOut(true);
      }
      return;
    }
    if (System.getProperty("log.file") != null) {
      log = new Log(new File(System.getProperty("log.file")));
      if (System.getProperty("log.stdout") != null) {
        log.setToStdOut(true);
      }
      return;
    }
    if (System.getProperty("log.stdout") != null) {
      log = new Log(true);
      return;
    }
    log = new Log(new File("./rt-stps-server.log"));
    log.setToStdOut(true);
  }
  
 
  /****************************************************************************
  * print.
  ****************************************************************************/
  public void print (String message) {	
    	log.info(message);
  }
}
