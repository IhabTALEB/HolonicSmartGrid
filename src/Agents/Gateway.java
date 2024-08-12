/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Agents;

import Utils.Agent.Messaging.Reader;
import Utils.Agent.Messaging.Sender;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.java_websocket.WebSocket;
import Utils.SocketServer;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



/**
 *
 * @author ihab
 */
public class Gateway extends Agent{
   
    Sender sender =new Sender("Gateway");
    Reader reader =new Reader();
    
    protected void setup(){
        

        
        SocketServer websocket = new SocketServer(this);
        websocket.start();
        //System.out.println("Address is : "+websocket.getAddress());
        //System.out.println("GateWay Started");
        
        if (Desktop.isDesktopSupported()) {
        //System.out.println("Condition accepted");
        Desktop dt = Desktop.getDesktop();
            if (dt.isSupported(Desktop.Action.BROWSE)) {  
                try {
                    File f = new File("./src/visualizer/main.html");
                    dt.browse(f.toURI());
                    //System.out.println("Started");
                } catch (IOException ex) {
                    Logger.getLogger(Gateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }  
        }
        
        addBehaviour(new CyclicBehaviour(){
            @Override
            public void action() {
                ACLMessage receivedMsg = blockingReceive();
                if(receivedMsg!=null){
                     switch(receivedMsg.getPerformative()){
                        case ACLMessage.REQUEST:
                            break;
                        case ACLMessage.INFORM:
                            
                            for (WebSocket sock : websocket.conns.keySet()) {
                                sock.send(receivedMsg.getContent());
                            }
                            break;
                     }
                }else{
                    block();
                }
             }
            
        });
    }
    
    public void read(WebSocket client, String message){
        System.out.println(message);
        JSONObject json = JSONParse(message);
        String request = (String) json.get("request");
        String holon = (String) json.get("target");
        if(request != null) switch (request){
            case "vis":
                send(sender.reset().put("state", "link").prepare(holon + "_data", ACLMessage.REQUEST_WHENEVER));
                break;
            case "mod":
                send(sender.reset(json).prepare(holon + "_cont", ACLMessage.INFORM));
                break;
            case "info":
                send(sender.reset(json).prepare("master", ACLMessage.REQUEST));
                break;
            case "logs":
                //send(sender.reset(json).prepare(holon+"_loger", ACLMessage.REQUEST_WHENEVER));
                break;
            default:
                break;
        }
        
    }
    
/*    public void link(WebSocket client, String holon){
        JSONObject jsonlink = new JSONObject();
        jsonlink.put("senderId", "Gateway");
        jsonlink.put("state", "link");
        String msglink = JSONValue.toJSONString(jsonlink);
        ACLMessage messageLink = new ACLMessage(ACLMessage.REQUEST_WHENEVER);
        messageLink.addReceiver(new AID(holon+"_data", AID.ISLOCALNAME));
        messageLink.setContent(msglink);
        send(messageLink);
    }*/
 
    
    private static JSONObject JSONParse(String jsonString){
        JSONObject  jsonObject=new JSONObject();
        JSONParser jsonParser=new  JSONParser();
        if ((jsonString != null) && !(jsonString.isEmpty())) {
            try {
                jsonObject=(JSONObject) jsonParser.parse(jsonString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
            
            
}
