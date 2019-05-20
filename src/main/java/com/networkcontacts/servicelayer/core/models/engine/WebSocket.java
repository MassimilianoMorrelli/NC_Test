package com.networkcontacts.servicelayer.core.models.engine;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ClientEndpoint
@ServerEndpoint(value="/status/")
public class WebSocket{
	
    @OnOpen
    public void onWebSocketConnect(Session sess, EndpointConfig endpointConfig) {
    System.out.println("Avviata connessione per sessionID: " + sess.getId());
   
    }
    
    @OnMessage
    public void onWebSocketText(String message) throws Throwable
    {
    	System.out.println("Send message updateBroadcast");
    }
    
    @OnClose
    public void onWebSocketClose(CloseReason reason, Session sess)
    {
        System.out.println("Socket Closed: " + reason);
    }
    
    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }
    
}

