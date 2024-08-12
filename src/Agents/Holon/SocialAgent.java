/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Agents.Holon;

import Utils.Agent.Messaging.Message.Offer;
import Utils.Agent.Messaging.Message.Response;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import Utils.Agent.Messaging.Reader;
import Utils.Agent.Messaging.Sender;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ihab
 */
public class SocialAgent extends Agent{

    Object[] args;
    String id;
    String upperHolon;
    List<String> children;
    int receivedvalues = 0;
    Timer timer;
    int level;
    int differenceDelay;
    int normalDelay;
    Reader reader;
    Sender sender;
    
    
    protected void setup(){

        timer = new Timer();
        
        args = getArguments();
        id = args[0].toString();
        upperHolon = args[1].toString();
        level = Integer.parseInt(args[2].toString());
        //delay = (int) (60*1000*Math. pow((1-0.6), level));
        differenceDelay = (int) (30*1000*Math. pow((1-0.1), 0)) - (int) (30*1000*Math. pow((1-0.1), 1));//level 0 - level 1
        normalDelay = (int) (30*1000*Math. pow((1-0.1), level));
        children = new ArrayList<String>();
        if(upperHolon.equals("none")) upperHolon = null;
        
        reader = new Reader();
        sender = new Sender(id);
        
        if(upperHolon != null){
            addBehaviour(new OneShotBehaviour(){ 
                @Override
                public void action(){
                    send(sender.reset().prepare(upperHolon+"_soc", ACLMessage.SUBSCRIBE));
                }
            });
        }
        else{
            send(sender.reset().put("state", "start").prepare(id+"_cont", ACLMessage.INFORM));
            setTimer(normalDelay);
        }
        
        addBehaviour(new CyclicBehaviour(){
           @Override
           public void action(){
               
                double totalsubdemands = 0;

                ACLMessage receivedMsg = receive();
                //ACLMessage receivedMsg = blockingReceive();
                String msg;
               if(receivedMsg!=null){
                   
                Offer receivedOffer;
                Response receivedResponse;
                Offer offer;
                Response response;
                   
                   String senderId;
                   JSONObject receivedJSON;
                    
                   try{
                        msg = receivedMsg.getContent();
                        receivedJSON = JSONParse(msg);

                        senderId = (String) receivedJSON.get("senderId");
                    }catch(Exception e){
                        receivedJSON = new JSONObject();
                        senderId="";
                    }
                    String targetId;
                    int type;
                    int timestep;
                    switch(receivedMsg.getPerformative()){
                        case ACLMessage.SUBSCRIBE:
                            children.add(senderId);
                            send(sender.reset().put("state", "confirm").prepare(senderId+"_soc", ACLMessage.CONFIRM));
                            break;
                        case ACLMessage.REQUEST:
                    
                        try {
                            receivedOffer = (Offer) receivedMsg.getContentObject();
                        } catch (UnreadableException ex) {
                            Logger.getLogger(SocialAgent.class.getName()).log(Level.SEVERE, null, ex);
                            receivedOffer = new Offer("","",0);
                        }
                    
                            senderId = receivedOffer.senderId;
                            targetId = receivedOffer.targetId;
                            if (children.contains((senderId))){
                                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                                message.addReceiver(new AID(id+"_cont", AID.ISLOCALNAME));
                                try {
                                    message.setContentObject((Serializable) receivedOffer);
                                } catch (IOException ex) {
                                    Logger.getLogger(ControlAgent.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                send(message);
                            }
                            else{
                            }
                            break;

                        case ACLMessage.INFORM:
                           try {
                               // verify if from or to and if child or parent
                            if(receivedMsg.getContentObject() instanceof Response){
                                receivedResponse = (Response) receivedMsg.getContentObject();
                                senderId = receivedResponse.senderId;
                                targetId = receivedResponse.targetId;
                                timestep = receivedResponse.timestep;
                                type = receivedResponse.type;
                                if(targetId.equals("children")){
                                    setTimer(normalDelay);
                                    Map<String, String> GenNAT = receivedResponse.GenNAT;
                                    if (children != null && !children.isEmpty()){
                                        for(String child : children){
                                            response = new Response();
                                            response.senderId = id;
                                            response.targetId = child;
                                            response.timestep = receivedResponse.timestep;
                                            response.type = receivedResponse.type;
                                            response.PVRatio = receivedResponse.PVRatio;
                                            response.WindRatio = receivedResponse.WindRatio;
                                            response.DemandRatio = receivedResponse.DemandRatio;
                                            response.ImpactCarbon = receivedResponse.ImpactCarbon;
                                            response.bestPhase = receivedResponse.bestPhase;
                                            
                                            response.GenResp= new HashMap();
                                            response.GenNAT= new HashMap();
                                            response.StoResp= new HashMap();
                                            response.StoNAT= new HashMap();
                                            
                                            if(child.equals("Mamoudzou") && id.equals("Mayotte")) System.out.println("-----TEST-----TEST-----SOCIAL2");
                                            if(child.equals("Mamoudzou") && id.equals("Mayotte")) System.out.println(receivedResponse.toString());
                                            if(child.equals("Mamoudzou") && id.equals("Mayotte")) System.out.println("-----TEST-----TEST-----SOCIAL2");

                                            for (Map.Entry<String, String> entry : receivedResponse.GenNAT.entrySet()) {
                                                if((child).equals(entry.getValue())){//+"_thermal"
                                                    if(child.equals("Mamoudzou")) System.out.println("TESTkey SOCIAL is : " + entry.getKey() +" value is :"+ entry.getValue());
                                                    response.GenResp.put(entry.getKey(), receivedResponse.GenResp.get(entry.getKey()));
                                                    response.GenNAT.put(entry.getKey(), entry.getValue());
                                                }
                                            }
                                            for (Map.Entry<String, String> entry : receivedResponse.StoNAT.entrySet()) {
                                                if((child).equals(entry.getValue())){//+"_Storage"
                                                    if(child.equals("Mamoudzou")) System.out.println("TESTkey SOCIAL is : " + entry.getKey() +" value is :"+ entry.getValue());
                                                    response.StoResp.put(entry.getKey(), receivedResponse.StoResp.get(entry.getKey()));
                                                    response.StoNAT.put(entry.getKey(), entry.getValue());
                                                }
                                            }
                                            ACLMessage message = new ACLMessage(type);
                                            message.addReceiver(new AID(child+"_soc", AID.ISLOCALNAME));
                                            try {
                                                message.setContentObject((Serializable) response);
                                            } catch (IOException ex) {
                                                Logger.getLogger(ControlAgent.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                             send(message);
                                        }
                                    }
                                }
                                else{
                                    //System.out.println(id+ "_soc : forwarding to targetId : " + targetId + " wtih msg is :" + msg);
                                }
                            }    
                           } catch (UnreadableException ex) {
                               
                               
                           }
                       //}
                            try{
                            if(receivedMsg.getContentObject() instanceof Offer){
                                receivedOffer = (Offer) receivedMsg.getContentObject();
                                senderId = receivedOffer.senderId;
                                targetId = receivedOffer.targetId;
                                timestep = receivedOffer.timestep;
                                type = receivedOffer.type;
                                if (targetId.equals("upper")){
                                    ACLMessage message = new ACLMessage(type);
                                    message.addReceiver(new AID(upperHolon+"_soc", AID.ISLOCALNAME));
                                    try {
                                        message.setContentObject((Serializable) receivedOffer);
                                    } catch (IOException ex) {
                                        Logger.getLogger(ControlAgent.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                     send(message);
                                }
                            }
                            } catch (UnreadableException ex) {
                               
                               
                           }
                            break;

                        case ACLMessage.PROPOSE:
                    
                        try {
                            //check then forward
                            receivedResponse = (Response) receivedMsg.getContentObject();
                        } catch (UnreadableException ex) {
                            Logger.getLogger(SocialAgent.class.getName()).log(Level.SEVERE, null, ex);
                            receivedResponse = new Response();
                        }
                    
                            senderId = receivedResponse.senderId;
                            if (senderId.equals(upperHolon)){
                                    ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
                                    message.addReceiver(new AID(id+"_cont", AID.ISLOCALNAME));
                                    try {
                                        message.setContentObject((Serializable) receivedResponse);
                                    } catch (IOException ex) {
                                        Logger.getLogger(ControlAgent.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                     send(message);
//                                send(sender.reset(receivedJSON).prepare(id+"_cont", ACLMessage.PROPOSE));
                                //setTimer();msg
                                break;
                            }
                            else{
                                System.out.println(id +"_soc : Unexpcted sender: Expected from parent (upper) Holon but received from: " + senderId + receivedMsg);
                                break;
                            }

                        case ACLMessage.CONFIRM:
                            send(sender.reset().put("senderId", id).put("state", "start").prepare(id+"_cont", ACLMessage.INFORM));
//                            System.out.println(id + "_soc : (start) sent");
                            setTimer(normalDelay);
                            break;
                        case ACLMessage.REFUSE://received wrong timestep from child holon
                                send(sender.reset(receivedJSON).prepare(id+"_cont", ACLMessage.REFUSE));
                            break;
                        case ACLMessage.AGREE://agree to the timestep demanded
                                receivedvalues++;
                                if(receivedvalues == children.size()){
                                }
                                else{
                                    //error
                                }
                            break;
                    }
               }else{
                   block();
               }
           }
        });
    }
    
    
        public void forward(String msg, String targetId, int msgType){
        
        String final_target;
        if(targetId.equals(id)){
            final_target = targetId+"_cont";
        }
        else{
            final_target = targetId+"_soc";
        }
        ACLMessage message = new ACLMessage(msgType);
        message.addReceiver(new AID(final_target, AID.ISLOCALNAME));
        message.setContent(msg);
        send(message);
        System.out.println(id + "_soc : sending to " + final_target + ", message type is : " + msgType);
    }
        
        

        
        
    private JSONObject JSONParse(String jsonString){
        JSONObject  jsonObject=new JSONObject();
        JSONParser jsonParser=new  JSONParser();
        if ((jsonString != null) && !(jsonString.isEmpty()) && !(jsonString.contains("Utils.Agent.Messaging"))) {
            try {
                jsonObject=(JSONObject) jsonParser.parse(jsonString);
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("error from : " + jsonString);
            }
            
        }
        return jsonObject;
    }
        
    
        
    public void setTimer(int delay){
        
        
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                JSONObject jsonstart = new JSONObject();
                jsonstart.put("senderId", id);
                jsonstart.put("state", "allReady");
                String msgStart = JSONValue.toJSONString(jsonstart);
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.addReceiver(new AID(id+"_cont", AID.ISLOCALNAME));
                message.setContent(msgStart);
                send(message);
            }
          }, delay);
    }
        
    
}
