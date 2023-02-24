/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Agents.Holon;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
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
public class DataAgent extends Agent{
    
    


    
    protected void setup(){
    Reader reader;
    Sender sender;
        
        //ArrayList<JSONObject> Data = new ArrayList<JSONObject>();
        //HashMap<String, JSONObject> Data = new HashMap<String, JSONObject>();
        
        JSONObject Data = new JSONObject();
        
        //map.put("dog", "type of animal");
        //System.out.println(map.get("dog"));
        Object[] args = getArguments();
        String id = args[0].toString();
        
        
        
        reader = new Reader();
        sender = new Sender(id);
        
        
//        addBehaviour(new OneShotBehaviour(){
//           @Override
//           public void action(){
//               
//               
//           }
//        });
        
        addBehaviour(new CyclicBehaviour(){
            
            JSONObject jsondata;
            String msg;
            ACLMessage message;
            
            String data;
            ArrayList PVs =new ArrayList();
            ArrayList Demands =new ArrayList();
            int reqTimestep=0;
            Boolean waiting = false;
            ArrayList<Double> demandRequests = new ArrayList<Double>();
            ArrayList<Double> demandResults = new ArrayList<Double>();
            //ArrayList<Double> PVResults = new ArrayList<Double>();
            Boolean Visualization = false;
            
           @Override
           public void action(){    
               //ACLMessage receivedMsg = receive();//blockingReceive
               ACLMessage receivedMsg = blockingReceive();//blockingReceive
               
               if(receivedMsg!=null){
                   
                   // System.out.println(id + "_data : meas received");

                   
                    String type;
                    int timestep;

                    double value;
                    
                    data = receivedMsg.getContent();
                    JSONObject receivedJSON = JSONParse(data);
//                            System.out.println("??????????????????????????????????????? "+receivedMsg.getPerformative()+" ??????????????????????????????????????????????,");
                    
                    switch(receivedMsg.getPerformative()){
                        case ACLMessage.REQUEST:
                            //System.out.println(id + "_data : request received not sent yet");
                            this.reqTimestep = ((Long) receivedJSON.get("timestep")).intValue();
                            if(this.reqTimestep < PVs.size() && this.reqTimestep < Demands.size()){
//                                JSONObject jsondata = new JSONObject();
//                                jsondata.put("senderId", id + "_data");
//                                jsondata.put("PV", PVs.get(reqTimestep));
//                                jsondata.put("Demand", Demands.get(reqTimestep));
//                                jsondata.put("timestep", reqTimestep);
//                                String msg = JSONValue.toJSONString(jsondata);
//                                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
//                                message.addReceiver(new AID(id+"_cont", AID.ISLOCALNAME));
//                                message.setContent(msg);
//                                send(message);
                                send(sender.reset().put("senderId", id + "_data").put("PV", PVs.get(reqTimestep)).put("Demand", Demands.get(reqTimestep)).put("timestep", reqTimestep).prepare(id+"_cont", ACLMessage.INFORM));
                                //System.out.println(id + "_data : data sent");
                            }
                            else{
                                this.waiting = true;
                                System.out.println(id + "_data : data not available yet ! waiting for Measurement Agent");
                            }
                            break;
                        case ACLMessage.INFORM:
                            //System.out.println(id + "_data : measurement received");
                            //System.out.println("**********************"+ receivedJSON);
                            type = (String) receivedJSON.get("type");
                            timestep = ((Long) receivedJSON.get("timestep")).intValue();
                            //value = (Double) receivedJSON.get("value");
                            //try{
                            value = ((Number)receivedJSON.get("value")).doubleValue();
                            //}catch(Exception e){
                            //    System.out.println("******************************************************************"+receivedJSON);
                            //}
                            if(type.equals("PV")){
                                this.PVs.add(value);
                                //System.out.println(id + "_data : PVS size is " + PVs.size() + " and Demands size is " + Demands.size());
                            }else{
                                this.Demands.add(value);
                            }
                            if((this.reqTimestep == PVs.size()-1) && (this.reqTimestep == Demands.size()-1)){
                                //System.out.println(id + "_data : data sending");
//                                JSONObject jsondata = new JSONObject();
//                                jsondata.put("senderId", id + "_meas"); //IT SHOULD BE DATA AND NOT MEAS ////////////////////////////////////////// ATTENTIONNNNNNNNNNNNNNNNNNN ////////////////////////////
//                                jsondata.put("PV", PVs.get(this.reqTimestep));
//                                jsondata.put("Demand", Demands.get(this.reqTimestep));
//                                jsondata.put("timestep", this.reqTimestep);
//                                String msg = JSONValue.toJSONString(jsondata);
//                                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
//                                message.addReceiver(new AID(id+"_cont", AID.ISLOCALNAME));
//                                message.setContent(msg);
//                                send(message);
                                send(sender.reset().put("senderId", id + "_data").put("PV", PVs.get(this.reqTimestep)).put("Demand", Demands.get(this.reqTimestep)).put("timestep", this.reqTimestep).prepare(id+"_cont", ACLMessage.INFORM));
                                //System.out.println(id + "_data : data sent");
                            }
//                            String[] addressArr = receivedMsg.getSender().getAddressesArray();
//                            //System.out.println(addressArr[0]);
//                            String sender =addressArr[0];
//                            if ( sender == (id + "_meas")){
//                                sender = "self";
//                            }
//                            else{
//                                sender = (String) receivedJSON.get("senderId");
//                            }
//                            JSONObject JSONObj = new JSONObject();
//                            if(Data.get(sender)==null){
//                                int NextIndex = 1;
//                                ArrayList<Double> PV = new ArrayList<Double>();
//                                ArrayList<Double> Demand = new ArrayList<Double>();
//                                JSONObj.put("NextIndex", NextIndex);
//                                JSONObj.put("PV", PV);
//                                JSONObj.put("Demand", Demand);
//                                Data.put("sender", JSONObj);
//                            }
//                            JSONObj = (JSONObject) Data.get("sender");
//                            JSONObj.put("NextIndex", (Double)JSONObj.get("NextIndex")+1);
//                            JSONObj.put("PV", (Double) JSONObj.get("NextIndex")+1);
//                            JSONObj.put("Demand", (Double)JSONObj.get("NextIndex")+1);
//                            if (type == "PV"){
//                                ArrayList<Double> PV = (ArrayList)JSONObj.get("PV");
//                                PV.add((Double) receivedJSON.get("PV"));
//                            }
//                            else{
//                                ArrayList<Double> Demand = (ArrayList)JSONObj.get("Demand");
//                                Demand.add((Double) receivedJSON.get("Demand"));
//                            }


                            
                            
                            

                            //Data.add(JSONObj);
                            break;
                        
                        case ACLMessage.INFORM_IF:
                            
                            double allReceived =((Number)receivedJSON.get("allReceived")).doubleValue();
                            double allDemand =((Number)receivedJSON.get("allDemand")).doubleValue();
                            System.out.println("alldemand is " + allDemand + "for json : " + receivedJSON);
                            demandRequests.add(allDemand);
                            demandResults.add(allReceived);
                            if(id.equals("Handréma")) System.out.println("???????????????????????????????????????????????"+this.Visualization);
                            if(this.Visualization){
                                
//                                jsondata = new JSONObject();
//                                jsondata.put("senderId", id);
//                                jsondata.put("index", ((Long) receivedJSON.get("timestep")).intValue());
//                                //jsondata.put("max", demandRequests.size()-1);
//
//                                //for (int i = 0; i<demandRequests.size()-1; i++){
//                                    jsondata.put("rec_" + ((Long) receivedJSON.get("timestep")).intValue(), allReceived);
//                                    jsondata.put("dem_" + ((Long) receivedJSON.get("timestep")).intValue(), allDemand);
//                                    jsondata.put("feedback", ((Number)receivedJSON.get("feedback")).doubleValue());
//                                //}
//
//                                msg = JSONValue.toJSONString(jsondata);
//                                message = new ACLMessage(ACLMessage.INFORM);
//                                message.addReceiver(new AID("Gateway", AID.ISLOCALNAME));
//                                message.setContent(msg);
//                                send(message);
//                                
                                send(sender.reset()
                                        .put("senderId", id)
                                        .put("index", ((Long) receivedJSON.get("timestep")).intValue())
                                        .put("rec_" + ((Long) receivedJSON.get("timestep")).intValue(), allReceived)
                                        .put("dem_" + ((Long) receivedJSON.get("timestep")).intValue(), allDemand)
                                        .put("feedback", ((Number)receivedJSON.get("feedback")).doubleValue())
                                        .prepare("Gateway", ACLMessage.INFORM));
                            }
                            
                            break;
                        case ACLMessage.REQUEST_WHENEVER:
                            
                            this.Visualization = true;
                            //if(Visualization) System.out.println("???????????????????????????????????????????????? received new INFORM.IF" + Visualization);
//                            jsondata = new JSONObject();
//                            jsondata.put("senderId", id);
//                            jsondata.put("number_dem", demandRequests.size());
//                            jsondata.put("number_rec", demandResults.size());
                    
                            sender.reset().put("senderId", id).put("number_dem", demandRequests.size()).put("number_rec", demandResults.size());
                            
                            for (int i = 0; i<demandRequests.size(); i++){
//                                jsondata.put("dem_" + i, demandRequests.get(i));
                                sender.put("dem_" + i, demandRequests.get(i));
                            }
                            
                            for (int i = 0; i<demandResults.size(); i++){
//                                jsondata.put("rec_" + i, demandResults.get(i));
                                sender.put("rec_" + i, demandResults.get(i));
                            }
                            
//                            msg = JSONValue.toJSONString(jsondata);
//                            message = new ACLMessage(ACLMessage.INFORM);
//                            message.addReceiver(new AID("Gateway", AID.ISLOCALNAME));
//                            message.setContent(msg);
//                            send(message);
                            send(sender.prepare("Gateway", ACLMessage.INFORM));
                            break;
                    }
               }else{
                   //block();
               }
               
               
           }
        });
    }
    
    

    
//    public String checkMessages(){
//        ACLMessage receivedMsg = blockingReceive();
//               if(receivedMsg!=null){
//                 
//                    
//                   
//                    switch(receivedMsg.getPerformative()){
//                        case ACLMessage.REQUEST:
//                            break;
//                        case ACLMessage.INFORM:
//                            return receivedMsg.getContent();
//                    }
//               }else{
//                   //block();
//               }
//    };
    
    
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
