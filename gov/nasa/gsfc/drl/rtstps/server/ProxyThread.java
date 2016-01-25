/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.drl.rtstps.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class defines a socket interface in which a client can send commands to the
 * RT-STPS server.
 * 
 */
final class ProxyThread extends Thread
{
    private AbstractServer stpsServer;
    private ServerSocket serverSocket;
    private boolean running = true;

    ProxyThread(AbstractServer server, int port)
    {
        stpsServer = server;
        try
        {
            serverSocket = new ServerSocket(port);
            setDaemon(true);
            start();
        }
        catch (IOException ioe)
        {
            stpsServer.log("ProxyThread error: "+ioe.getMessage());
        }
    }

    public void run()
    {
        while (running)
        {
            try
            {
                Socket socket = serverSocket.accept();
                ProxyReader r = new ProxyReader(socket);
                r.start();
            }
            catch (IOException ioe)
            {
                stpsServer.log("ProxyThread error: "+ioe.getMessage());
            }
        }
        try { serverSocket.close(); } catch (IOException ce) {}
    }

    class ProxyReader extends Thread
    {
        private final BufferedReader reader;
        private final Socket socket;

        ProxyReader(Socket socket) throws IOException
        {
            this.socket = socket;
            InputStream is = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            setDaemon(true);
        }

        public void run()
        {
            try
            {
                while (true)
                {
                    String line = reader.readLine();
                    if (line == null) break;
                    line = line.trim();
                    stpsServer.log("ProxyThread received: " + line);
                    if (line.equals("quit")) break;

                    try
                    {
                        if (line.startsWith("loadgo"))
                        {
                            String configfile = line.substring(6).trim();
                            stpsServer.loadFile(configfile);
                            stpsServer.setEnabled(true);
                        }
                        else if (line.startsWith("rloadgo"))
                        {
                            String config = line.substring(6);
                            stpsServer.load(config);
                            stpsServer.setEnabled(true);
                        }
                        else if (line.equals("shutdown"))
                        {
                            stpsServer.unload();
                        }
                        else
                        {
                            stpsServer.log("unknown: "+line);
                        }
                    }
                    catch (java.rmi.RemoteException re)
                    {
                        stpsServer.log("ProxyThread: "+re.getMessage());
                    }
                }
            }
            catch (IOException ioe)
            {
                stpsServer.log("ProxyThread: "+ioe);
            }
            try
            {
                reader.close();
                socket.close();
            }
            catch (IOException ioe2)
            {
                stpsServer.log("ProxyThread: "+ioe2);
            }
        }
    }
}
