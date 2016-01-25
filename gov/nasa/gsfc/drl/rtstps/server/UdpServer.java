/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.server;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * This is an RT-STPS server that receives telemetry through UDP datagrams.
 * <p>
 * You may pass one argument when invoking, the server name. The full server
 * name then becomes "RtStpsServices."+name. The default full server name is
 * "RtStpsServices.U".
 * <p>
 * The server understands the following system properties. You set them when
 * you run the server using the -D attribute as in "-Dport=5000".
 * <p>
 * <br/><b>The site.properties file is processed if it is present for configuring the log and
 * it will take precedence over some of these option, even if they are present.</b>
 * </p>
 * <br>
 * -Dport=4395
 * <br>The UDP port number that the server reads for telemetry data. The
 * default port is 4395.
 * <br>
 * -DbufferSizeKb=????
 * <br>The input packet buffer size in kilobytes. The default is the platform's
 * default UDP input buffer size. Some platforms may not receive messages if
 * you set the buffer size to something greater than the platform default.
 * <br>
 * -DblockSizeKb=8
 * <br>This is the number of kilobytes the pipeline process in one chunk. It
 * should evenly divide into bufferSizeKb. I recommend that you set it to 1/2,
 * 1/4, 1/8 etc of bufferSizeKb to create double+ buffering. The default is 4.
 * <br>
 * -DsourceIpAddress=null
 * <br>If you supply a source IP address, then the server rejects any packet
 * from any other source. If you omit it, it accepts all packets.
 * <br>
 * -Dlog.stdout 
 * <br>
 * If specified, log messages are written to the standard output.
 * <br/><b>note: this is overridden by the presence of a site.properties file</b>
 * <br>
 * -Dlog.file=<i>file</i>
 * <br>
 * If specified, log messages are written to <i>file</i>.
 * <br/><b>note: this is overridden by the presence of a site.properties file</b>
 * <br>
 * -Dlog.server=<i>host:port:tmpDir</i>
 * <br>
 * If specified, log messages are sent to the NSLS server at host:port
 * (eg. localhost:3500) and to the temporary directory tmpDir when the NSLS
 * server is unavailable.
 * <br/><b>note: this is overridden by the presence of a site.properties file</b>
 * <p>
 * This class supports only one pipeline thread.
 * 
 * 
 */
public class UdpServer extends AbstractServer
{
    private int blockSize;
    private String sourceIpAddress = null;
    private DatagramSocket socket = null;
    private DatagramPacket packet = null;

    /**
     * This is the main entry point for the RT-STPS server.
     */
    public static void main(String args[])
    {
        final boolean autoFlush = true;
        OutputStream er = new FileOutputStream(FileDescriptor.err);
        PrintStream ps = new PrintStream(er,autoFlush);
        System.setErr(ps);

        String serverName = (args.length == 0)? "U" : args[0];

        String propertyName = "sourceIpAddress";
        String sourceIp = System.getProperty(propertyName);

        int port = 4935;
        int buffersize = 0;
        int blocksize = 0;

        try
        {
            /**
             * Get the port number system property.
             */
            propertyName = "port";
            String x = System.getProperty(propertyName,"4935");
            port = Integer.valueOf(x).intValue();

            /**
             * Get the UDP input buffer size. The default here is 0, which
             * is a later signal to get the real size from the DatagramSocket.
             */
            propertyName = "bufferSizeKb";
            x = System.getProperty(propertyName,"0");
            buffersize = 1024 * Integer.valueOf(x).intValue();

            /**
             * Get the UDP packet size. The default here is 8 kb.
             */
            propertyName = "blockSizeKb";
            x = System.getProperty(propertyName,"8");
            blocksize = 1024 * Integer.valueOf(x).intValue();
        }
        catch (NumberFormatException nfe)
        {
            System.err.println(propertyName + " property is not an integer.");
            System.exit(-3);
        }

        try
        {
            LocateRegistry.createRegistry(1099);
        }
        catch (java.rmi.RemoteException rre)
        {
            System.err.println("Could not create/export the RMI registry.");
            System.exit(-4);
        }

        String security = System.getProperty("security","1");
        if (security.equals("1") && System.getSecurityManager() == null)
        {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }

        try
        {
            UdpServer server = new UdpServer(sourceIp,port,buffersize,blocksize);
            java.rmi.Naming.rebind("RtStpsServices."+serverName, server);
            System.out.println("Ready to serve ...");
        }
        catch (Exception ex)
        {
            System.err.println("SERVER ERROR. " + ex);
            ex.printStackTrace();
            System.exit(-2);
        }
    }

    /**
     * Create a server.
     */
    public UdpServer(String sourceIp, int dataPort, int buffersize, int blocksize)
            throws java.rmi.RemoteException
    {
        super();
        this.blockSize = blocksize;
        sourceIpAddress = sourceIp;

        /**
         * Create the datagram socket. I will use the platform's buffer size
         * unless I have been configured to use a different size.
         */
        try
        {
            socket = new DatagramSocket(dataPort);
            int rsize = socket.getReceiveBufferSize();
            System.out.println("This platform's UDP default input buffer size is "+
                    rsize + " bytes.");
            if (buffersize <= 0)
            {
                buffersize = rsize;
            }
            else
            {
                socket.setReceiveBufferSize(buffersize);
                System.out.println("I set the buffer size to " + buffersize);
            }

            byte[] buffer = new byte[blockSize];
            packet = new DatagramPacket(buffer,blockSize);
        }
        catch (java.net.SocketException se)
        {
            se.printStackTrace();
            System.exit(0);
        }

        /**
         * This thread reads the UDP socket and gives the buffers the the
         * FrameSynchronizer object (if it exists), which is the head of the
         * pipeline. If a pipeline does not exist, which means a configuration
         * has not been loaded, then the thread throws away the input data.
         */
        runner = new Thread(this);
        runner.setDaemon(true);
        runner.setPriority(Thread.MAX_PRIORITY);
        runner.start();
    }

    /**
     * This thread method gets data from the UDP socket and sends it down the
     * pipeline if it exists. It discards the data if the pipeline is null.
     */
    public void run()
    {
        while (running)
        {
            try
            {
                /**
                 * I must reset the packet to its full length every time
                 * I reuse it. Otherwise, the socket will fill it to its
                 * last length only, That is, a short packet would reduce the
                 * maximum packet buffer size unless I reset the length.
                 */
                packet.setLength(blockSize);
                socket.receive(packet);
            }
            catch (java.io.IOException ioe)
            {
                ioe.printStackTrace();
                state.unload();
            }

            /**
             * If I have been configured to accept packets from one source, I
             * discard packets from unknown sources.
             */
            if (sourceIpAddress != null)
            {
                String source = packet.getAddress().getHostAddress();
                if (!sourceIpAddress.equals(source)) continue;
            }

            /**
             * "isEnabled" means I have a loaded configuration (fs is not null),
             * and GO has been sent.
             */
            if (state.isEnabled())
            {
                try
                {
                    fs.putBuffer(packet.getData(), packet.getLength());
                }
                catch (gov.nasa.gsfc.drl.rtstps.core.RtStpsException stpe)
                {
                    System.out.println(stpe.getMessage());
                    state.unload();
                }
            }
        }

        socket.close();
        System.out.println("This server thread is stopping now.");
    }
    
    private static final long serialVersionUID = 1L;
    
    
    public void stopData() throws RemoteException
    {
	
    }
    
}
