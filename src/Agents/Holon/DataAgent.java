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
    
    
    Reader reader;
    Sender sender;

    
    
    protected void setup(){
    
        JSONObject Data = new JSONObject();
        String Visualization;        
        Visualization = null;

        Object[] args = getArguments();
        String id = args[0].toString();
        
        
        reader = new Reader();
        sender = new Sender(id);
        
        addBehaviour(new CyclicBehaviour(){
            
            JSONObject jsondata;
            String msg;
            ACLMessage message;
            
            
            String Visualization = null;        
            //Visualization = null;
            
            String data;
            ArrayList PVs =new ArrayList();
            ArrayList Demands =new ArrayList();
            int reqTimestep=0;
            Boolean waiting = false;
            ArrayList<Double> demandRequests = new ArrayList<Double>();
            ArrayList<Double> demandResults = new ArrayList<Double>();
            ArrayList<Double> batStored = new ArrayList<Double>();
            ArrayList<Double> DemandRatios = new ArrayList<Double>();
            ArrayList<Double> PVRatios = new ArrayList<Double>();
            ArrayList<Double> impactCarbons = new ArrayList<Double>();
            ArrayList<Integer> bestPhases = new ArrayList<Integer>();
            
           @Override
           public void action(){    
               
               
               ACLMessage receivedMsg = blockingReceive();//blockingReceive
               
               if(receivedMsg!=null){
                   
                    String type;
                    int timestep;

                    double value;
                    
                    data = receivedMsg.getContent();
                    JSONObject receivedJSON = JSONParse(data);
                    
                    switch(receivedMsg.getPerformative()){
                        case ACLMessage.REQUEST:
                            //System.out.println(id + "_data : request received not sent yet");
                            this.reqTimestep = ((Long) receivedJSON.get("timestep")).intValue();
                            if(this.reqTimestep < PVs.size() && this.reqTimestep < Demands.size()){
                                send(sender.reset().put("senderId", id + "_data").put("PV", PVs.get(reqTimestep)).put("Demand", Demands.get(reqTimestep)).put("timestep", reqTimestep).prepare(id+"_cont", ACLMessage.INFORM));
                            }
                            else{
                                this.waiting = true;
                            }
                            break;
                        case ACLMessage.INFORM:
                            type = (String) receivedJSON.get("type");
                            timestep = ((Long) receivedJSON.get("timestep")).intValue();
                            value = ((Number)receivedJSON.get("value")).doubleValue();
                            if(type.equals("PV")){
                                this.PVs.add(value);
                            }else{
                                this.Demands.add(value);
                            }
                            if((this.reqTimestep == PVs.size()-1) && (this.reqTimestep == Demands.size()-1)){// && this.waiting
                                send(sender.reset().put("senderId", id + "_data").put("PV", PVs.get(this.reqTimestep)).put("Demand", Demands.get(this.reqTimestep)).put("timestep", this.reqTimestep).prepare(id+"_cont", ACLMessage.INFORM));
                                this.waiting = false;
//                                if(id.equals("Mamoudzou")) System.out.println("testthedataagent with wating "+timestep);
                            }
                            break;
                        
                        case ACLMessage.INFORM_IF:
                            
                            double allReceived =((Number)receivedJSON.get("allReceived")).doubleValue();
                            double allDemand=0;
                            try{
                                 allDemand =((Number)receivedJSON.get("allDemand")).doubleValue();
                            }
                            catch(Exception e){
//                                System.out.println("--ERROORR-- : " + receivedJSON);
                            }
                            double allBatteryStored =((Number)receivedJSON.get("LocalBatteryStocked")).doubleValue();
                            double DemandRatio =((Number)receivedJSON.get("DemandRatio")).doubleValue();
                            double PVRatio =((Number)receivedJSON.get("PVRatio")).doubleValue();
                            double impactCarbon =((Number)receivedJSON.get("impactCarbon")).doubleValue();
                            int bestPhase =((Number)receivedJSON.get("bestPhase")).intValue();

//                            System.out.println("alldemand is " + allDemand + "for json : " + receivedJSON);
                            demandRequests.add(allDemand);
                            demandResults.add(allReceived);
                            batStored.add(allBatteryStored);
                            DemandRatios.add(DemandRatio);
                            PVRatios.add(PVRatio);
                            impactCarbons.add(impactCarbon);
                            bestPhases.add(bestPhase);
                            if(id.equals(this.Visualization)){
                                send(sender.reset()
                                        .put("senderId", id)
                                        .put("index", ((Long) receivedJSON.get("timestep")).intValue())
                                        .put("rec_" + ((Long) receivedJSON.get("timestep")).intValue(), allReceived)
                                        .put("dem_" + ((Long) receivedJSON.get("timestep")).intValue(), allDemand)
                                        .put("bat_" + ((Long) receivedJSON.get("timestep")).intValue(), allBatteryStored)
                                        .put("demandratio_" + ((Long) receivedJSON.get("timestep")).intValue(), DemandRatio)
                                        .put("pvratio_" + ((Long) receivedJSON.get("timestep")).intValue(), PVRatio)
                                        .put("impactCarbon_" + ((Long) receivedJSON.get("timestep")).intValue(), impactCarbon)
                                        .put("bestPhase_" + ((Long) receivedJSON.get("timestep")).intValue(), bestPhase)
//                                        .put("feedbackdemand", ((Number)receivedJSON.get("feedbackdemand")).doubleValue())
//                                        .put("feedbackstorage", ((Number)receivedJSON.get("feedbackstorage")).doubleValue())
                                        .prepare("Gateway", ACLMessage.INFORM));
                            }
                            
                            break;

                        case ACLMessage.REQUEST_WHENEVER:
                            
                            this.Visualization = id;
                    
                            sender.reset().put("senderId", id).put("number_dem", demandRequests.size()).put("number_rec", demandResults.size());
                            
                            for (int i = 0; i<demandRequests.size(); i++){
                                sender.put("dem_" + i, demandRequests.get(i));
                            }
                            
                            for (int i = 0; i<demandResults.size(); i++){
                                sender.put("rec_" + i, demandResults.get(i));
                            }
                            
                            for (int i = 0; i<demandResults.size(); i++){
                                sender.put("bat_" + i, batStored.get(i));
                            }
                            
                            for (int i = 0; i<DemandRatios.size(); i++){
                                sender.put("demandratio_" + i, DemandRatios.get(i));
                            }
                            
                            for (int i = 0; i<PVRatios.size(); i++){
                                sender.put("pvratio_" + i, PVRatios.get(i));
                            }
                            
                            for (int i = 0; i<impactCarbons.size(); i++){
                                sender.put("impactCarbon_" + i, impactCarbons.get(i));
                            }
                            
                            for (int i = 0; i<bestPhases.size(); i++){
                                sender.put("bestPhase_" + i, bestPhases.get(i));
                            }
                            
                            send(sender.prepare("Gateway", ACLMessage.INFORM));
                            break;
                    }
               }else{
                   //block();
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
