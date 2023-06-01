/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import Agents.Gateway;

/**
 *
 * @author ihab
 */
public class SocketServer  extends WebSocketServer {
        private static int TCP_PORT = 4444;
    Gateway gateway;
    
    //private Set<WebSocket> conns;
    
    public HashMap<WebSocket, String> conns;

    public SocketServer(Gateway gtw) {
        super(new InetSocketAddress(TCP_PORT));
//        conns = new HashSet<>();
        conns = new HashMap<WebSocket, String>();
        gateway = gtw;
        //System.out.println("************************************************************** Address is : "+getAddress());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        //conns.add(conn);
        conns.put(conn, null);
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        //System.out.println("************************************************************** other Address is : " + super.getLocalSocketAddress(conn));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        conns.remove(conn);
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);
        gateway.read(conn, message);
//        for (WebSocket sock : conns.keySet()) {
//            sock.send(message);
//        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        //ex.printStackTrace();
        if (conn != null) {
            conns.remove(conn);
            // do some thing if required 
        }
        System.out.println("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onStart() {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
