package com.networkcontacts.servicelayer.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.jetty.server.Server;

import com.networkcontacts.servicelayer.core.utils.Const;


/**
 * Listens for stop commands and causes jetty to stop by stopping the server instance.
 *
 * @see <a href="https://github.com/eclipse/jetty.project/tree/master/jetty-maven-plugin">https://github.com/eclipse/jetty.project/tree/master/jetty-maven-plugin</a>
 */
public class ContextMonitor extends Thread {

    private Server[] servers;
    private ServerSocket serverSocket;

    public ContextMonitor(int port, Server[] servers) throws IOException {
        if (port <= 0) {
            throw new IllegalStateException(Const.Info.BAD_PORT);
        }
        this.servers = servers;
        setDaemon(true);
        setName(Const.Operations.OPERATION_STOP);
        serverSocket = new ServerSocket(port, 1, InetAddress.getByName(Const.Properties.LOCAL_ADDRESS));
        serverSocket.setReuseAddress(true);
    }

    public void run() {
        while (serverSocket != null) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                socket.setSoLinger(false, 0);
                LineNumberReader lin = new LineNumberReader(new InputStreamReader(
                        socket.getInputStream()));
                String cmd = lin.readLine();
                if ("stop".equals(cmd)) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                    try {
                        serverSocket.close();
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                    serverSocket = null;

                    for (int i = 0; servers != null && i < servers.length; i++) {
                        try {
                        	System.out.println(Const.Info.STOPPING_SERVER + i);
                            servers[i].stop();
                        } catch (Exception e) {
                        	e.printStackTrace();
                        }
                    }

                } else{
                    System.out.println(Const.Log.UNSUPPORTED_MONITOR);
                }
            } catch (Exception e) {
            	e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                }
            }
        }
    }
}