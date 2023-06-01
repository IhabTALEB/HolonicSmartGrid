/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Agents.Holon;

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

/**
 *
 * @author ihab
 */
public class SocialAgent extends Agent{

    Object[] args;
    String id;
    String upperHolon;
    List<String> children;
    //String[] childrenArr;
    int receivedvalues = 0;
    Timer timer;
    int level;
    int delay;
    Reader reader;
    Sender sender;
    
    
    protected void setup(){

        timer = new Timer();
        
        args = getArguments();
        id = args[0].toString();
        upperHolon = args[1].toString();
        level = Integer.parseInt(args[2].toString());
        delay = (int) (10*1000*Math. pow((1-0.2), level));
        children = new ArrayList<String>();
        if(upperHolon.equals("none")) upperHolon = null;
        
        reader = new Reader();
        sender = new Sender(id);
        
        ////////////////////////////////////////////////////////////////////////////////////TO BE REMOVED///////////////////////////////////////////////////////////////////
        //children = Arrays.asList(args[2]);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        

        //childrenArr = new String[children.size()];
        //children.toArray(childrenArr);
        
        if(upperHolon != null){
            addBehaviour(new OneShotBehaviour(){ 
                @Override
                public void action(){
                    send(sender.reset().prepare(upperHolon+"_soc", ACLMessage.SUBSCRIBE));
                    
                    System.out.println(id+"_soc: SUBSRIBE sent to upper holon " + upperHolon);
                }
            });
        }
        else{
            send(sender.reset().put("state", "start").prepare(id+"_cont", ACLMessage.INFORM));
            System.out.println(id + "_soc : (start) sent");
            setTimer();
        }
        ////////////////////////::
        // TIMERRRRRRRRRRRRRRRRRRRRRRRRRRR
        //////////////////////////////
        

        //setTimer();
        
        
        

        
        
        addBehaviour(new CyclicBehaviour(){
           @Override
           public void action(){
               
                double totalsubdemands = 0;

                ACLMessage receivedMsg = receive();
                //ACLMessage receivedMsg = blockingReceive();
                String msg;
               if(receivedMsg!=null){
                   
                    
                   
                    msg = receivedMsg.getContent();
//                    System.out.println("-------------------------------------------------------");
//                    System.out.println(msg);
//                    System.out.println("-------------------------------------------------------");
                    JSONObject receivedJSON = JSONParse(msg);
                    
                    String senderId = (String) receivedJSON.get("senderId");
                    
                    String targetId;
                    int type;
                    int timestep;
                    switch(receivedMsg.getPerformative()){
                        case ACLMessage.SUBSCRIBE:
                            children.add(senderId);
                            send(sender.reset().put("state", "confirm").prepare(senderId+"_soc", ACLMessage.CONFIRM));
                            break;
                        case ACLMessage.REQUEST:
                            if (children.contains((senderId))){
                                System.out.println(id+ "_soc: received from ***" + senderId);
                                targetId = (String) receivedJSON.get("targetId");
                                //check then send...
                                //if(targetId.equals(id)){
                                    //forward(msg, id , ACLMessage.REQUEST);// + "_cont" /:targetId
                                    send(sender.reset(receivedJSON).prepare(id+"_cont", ACLMessage.REQUEST));
                                    System.out.println(id+ "_soc: has received REQUEST from  *** " + senderId);
                            }
                            else{
//                                String[] addressArr = receivedMsg.getSender().getAddressesArray();
//                                String sender =addressArr[0];
//                                if ( sender == (id + "_cont")){
//                                    forwardff
//                                }
//                                else{allReadyallReady
//                                    System.out.println("Unexpected sender: Expected from a Child Holon or from control agent but received from: " + senderId);
//                                }
                            }
                            break;
                        case ACLMessage.INFORM:// verify if from or to and if child or parent
                            System.out.println("-------------------------------------------------------");
                            System.out.println(receivedJSON);
                            System.out.println("-------------------------------------------------------");
                            targetId = (String) receivedJSON.get("targetId");
                            timestep = ((Long) receivedJSON.get("timestep")).intValue();
                            type = ((Long) receivedJSON.get("type")).intValue();
                            System.out.println(id + "_soc : received message of type : " + type + " and it is for : " + targetId);
                            if (targetId.equals("upper")){
                                //receivedJSON.put("targetId", upperHolon)
                                // re-STRINGIFY
                                //forward(msg, upperHolon, type);
                                send(sender.reset(receivedJSON).prepare(upperHolon+"_soc", type));
                            }
                            else{////////////////////////////////////::::        ///////////////////////////:: ://///////////////////////////////////////// //////////////// CONTINUE HERE !
                                //if "childre"
                                if(targetId.equals("children")){
                                    setTimer();
                                    if (children != null && !children.isEmpty()){
                                        //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " +children );
                                        for( String child : children ){
                                            //forward(msg, child, type);
                                            send(sender.reset(receivedJSON).prepare(child+"_soc", type));
                                        }

                                    }
//                                    else{//if sending feedback but there it has no children yet
//                                        JSONObject jsonstart = new JSONObject();
//                                        jsonstart.put("senderId", id);
//                                        jsonstart.put("state", "allReady");
//                                        String msgStart = JSONValue.toJSONString(jsonstart);
//                                        ACLMessage messageConfirm = new ACLMessage(ACLMessage.INFORM);
//                                        messageConfirm.addReceiver(new AID(id+"_cont", AID.ISLOCALNAME));
//                                        messageConfirm.setContent(msgStart);
//                                        send(messageConfirm);
//                                    }
                                }
                                else{
                                    //forward(msg, targetId, type); //////////////////////////////TO BE REVIEWED
                                    //send(sender.reset(receivedJSON).prepare(targetId, type));
                                    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ " + id+ "_soc : forwarding to targetId : " + targetId + " wtih msg is :" + msg);
                                }
                            }
                            //check then send...
                            //forward(msg, targetId, type);
                            break;
                        case ACLMessage.PROPOSE:
                            //check then forward
                            if (senderId.equals(upperHolon)){
                                //targetId = (String) receivedJSON.get("targetId");
                                //check then send...
                                //forward(msg, id , ACLMessage.PROPOSE);//+ "_cont"
                                send(sender.reset(receivedJSON).prepare(id+"_cont", ACLMessage.PROPOSE));
                                //setTimer();msg
                                break;
                            }
                            else{
                                System.out.println(id +"_soc : Unexpcted sender: Expected from parent (upper) Holon but received from: " + senderId + receivedMsg);
                                break;
                            }
                        case ACLMessage.CONFIRM:
//                            JSONObject jsonstart = new JSONObject();
//                            jsonstart.put("senderId", id);
//                            jsonstart.put("state", "start");
//                            String msgStart = JSONValue.toJSONString(jsonstart);
//                            ACLMessage messageConfirm = new ACLMessage(ACLMessage.INFORM);
//                            messageConfirm.addReceiver(new AID(id+"_cont", AID.ISLOCALNAME));
//                            messageConfirm.setContent(msgStart);
//                            send(messageConfirm);
                            send(sender.reset().put("senderId", id).put("state", "start").prepare(id+"_cont", ACLMessage.INFORM));
                            System.out.println(id + "_soc : (start) sent");
                            setTimer();
                            break;
                        case ACLMessage.REFUSE://received wrong timestep from child holon
                            //if (senderId.equals(upperHolon)){
                                //targetId = (String) receivedJSON.get("targetId");
                                //check then send...
                                //forward(msg, id , ACLMessage.REFUSE);//+ "_cont"
                                send(sender.reset(receivedJSON).prepare(id+"_cont", ACLMessage.REFUSE));

//                                if (children != null && !children.isEmpty()){
//                                    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " +children );
//                                    for( String child : children ){
//                                        forward(msg, child, ACLMessage.REFUSE);
//                                    }
//
//                                }
//                            }
//                            else{
//                                System.out.println(id +"_soc : Unexpected timestep, received from: " + senderId + " : " + receivedMsg);
//                            }
                            break;
                        case ACLMessage.AGREE://agree to the timestep demanded
                                receivedvalues++;
                                System.out.println(id+ "_soc: has valid recievedValues *** " + receivedvalues);
                                System.out.println(id+ "_soc: has size *** " + children.size());
                                System.out.println(id+ "_soc has received AGREE with json is : "+ receivedJSON);
                                if(receivedvalues == children.size()){
//                                    System.out.println(id+ "_soc: has *** " + children);
//                                    //timer.cancel();
//                                    //inofrm control
//                                    JSONObject jsonready = new JSONObject();
//                                    jsonready.put("senderId", id);
//                                    jsonready.put("state", "allReady");
//                                    forward(JSONValue.toJSONString(jsonready), id, ACLMessage.INFORM);// + "_cont"
//                                    receivedvalues=0;

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
            
        //String JSONString=JSONValue.toJSONString(JSONObj);

        ACLMessage message = new ACLMessage(msgType);
        message.addReceiver(new AID(final_target, AID.ISLOCALNAME));
        message.setContent(msg);
        send(message);
        System.out.println(id + "_soc : sending to " + final_target + ", message type is : " + msgType);
    }
        
        

        
        
    private JSONObject JSONParse(String jsonString){
        JSONObject  jsonObject=new JSONObject();
        JSONParser jsonParser=new  JSONParser();
        if ((jsonString != null) && !(jsonString.isEmpty())) {
            try {
                jsonObject=(JSONObject) jsonParser.parse(jsonString);
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("error from : " + jsonString);
            }
        }
        return jsonObject;
    }
        
    
        
    public void setTimer(){
        
        
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
                System.out.println(id + "_soc : sent 'allready' by timer to "+ id +"_cont");
                //timer.cancel();
                System.out.println(id +"_soc TIMERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR : " + delay);
            }
          }, delay);//, 10*1000
    }
        
    
}
