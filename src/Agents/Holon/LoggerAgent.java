/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Agents.Holon;

import Utils.Agent.Messaging.Reader;
import Utils.Agent.Messaging.Sender;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ihab
 */
public class LoggerAgent extends Agent{
    
    ArrayList<JSONObject> logList;
    Reader reader;
    Sender sender;
    String Request="Last";

    
    @Override
    protected void setup(){
        
        logList = new ArrayList<>();
        Object[] args = getArguments();
        String id = args[0].toString();

        int start = 0;
        int n = 0;
        String requested = "last";
        
        
        reader = new Reader();
        sender = new Sender(id);
        
        
        addBehaviour(new CyclicBehaviour(){
            @Override
            public void action() {
                ACLMessage receivedMsg = blockingReceive();//blockingReceive
               
               if(receivedMsg!=null){
                   
                   String data = receivedMsg.getContent();
                   JSONObject receivedJSON = JSONParse(data);
                   
                   switch(receivedMsg.getPerformative()){
                        case ACLMessage.INFORM:
                            
                            logList.add(receivedJSON);
                            
                            break;
                        case ACLMessage.REQUEST:
                            
                            //READ JSON
                            //PREAPARE REQUESTED LOGS
                            //SEND LOGS
                            //if READ FORWARD

                            
                            if(requested.equals("first")){
                                // Get the first 3 elements of the list stating at start
                                for (int i = start; i < start + n && i < logList.size(); i++) {
                                    
                                }
                            }
                            else{
                                //IF READ BAKWARD
                                // Get the last 2 elements of the list
                                for (int i = logList.size() - n; i < logList.size(); i++) {

                                }
                            }
                            
                            break;
                    }
                }
            }
        });
    }
    
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
