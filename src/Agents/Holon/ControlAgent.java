/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Agents.Holon;

import Utils.Agent.Messaging.Message.Offer;
import Utils.Agent.Messaging.Message.Response;
import Utils.Agent.Messaging.Message.Service;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Arrays;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Utils.Agent.Messaging.Reader;
import Utils.Agent.Messaging.Sender;
import Utils.Control.MultiPhaseCWS;
import Utils.Control.PSO.PSOOptimizer;
import Utils.Control.RLCWS;
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
public class ControlAgent extends Agent{


    Object[] args;

    String id;
    String upperHolonMain;
    String upperHolon;
    Boolean disconnected;
    Boolean no_thermal;
    double theoreticalbatcapacity;
    double batcapacity;
    double batpow;
    double modDemand;
    double modPV;
    double modWind;
    
    double PVEcon=0;
    double PVEnv=0;
    double windEcon=0;
    double windEnv=0;
    double thermalEcon=0;
    double thermalEnv=0;
    double stoEcon=0;
    double stoEnv=0;
    
    List<Object> children;
    JSONObject Data = new JSONObject();
    double thermal;
    double originalThermal;
    
    double resultThermal, resultPV, resultStorage, totalDemands;
    
    
    Reader reader;
    Sender sender;



    protected void setup(){
        
        disconnected = false;
        no_thermal = false;
        
        args = getArguments();
        System.out.println(Arrays.toString(args));
        id = args[0].toString();
        originalThermal = Double.parseDouble(args[4].toString());
        thermal = originalThermal;
        
        upperHolonMain = args[1].toString();
        upperHolon = args[1].toString();
        theoreticalbatcapacity = Double.parseDouble(args[7].toString());
        batcapacity = 0.9*theoreticalbatcapacity;
        batpow = Double.parseDouble(args[8].toString());
    
        
        PVEcon = Double.parseDouble(args[2].toString());
        PVEnv = Double.parseDouble(args[3].toString());
        windEcon = 0;//Double.parseDouble(args[2].toString());
        windEnv = 0;//Double.parseDouble(args[3].toString());
        thermalEcon = Double.parseDouble(args[5].toString());
        thermalEnv = Double.parseDouble(args[6].toString());
        stoEcon = Double.parseDouble(args[9].toString());
        stoEnv = Double.parseDouble(args[10].toString());
        
        
        reader = new Reader();
        sender = new Sender(id);
        
        double demandCost = -0.1;
        double battCost = (65.406*1000000)/(365*3600);
        double PVCost = (0.364*1000000)/(365*3600);
        double thermalCost = (63.406*1000000)/(365*3600);

        double[] prod = new double[2];
        double[] sto = new double[2];
        
        

        
        
        

        addBehaviour(new CyclicBehaviour(){
            
            double PVs=0;
            double Demands = 0;
            double request = 0;
            double totallocal;
            int timestep = 0;
            int receivedTimestep = 0;
            double allDemand = 0;
            double allPV = 0;
            
            double PV = 0;
            double localPV = 0;
            double Demand = 0;
            double localDemand = 0;
            int nummm =0;
            
            double batrequested = (batpow < batcapacity) ? batpow : batcapacity;
            double batavailable = 0;
            double batstocked = 0;
            
            Service thermalInfo;// = new Service(id, 0, thermal, 0, 0);
            Service storageInfo;// = new Service(id, batavailable, batrequested, 0, 0);// min is -available and max is +energy requested 
            
            Offer offer;// = new Offer();
            Offer receivedOffer;
            Response receivedResponse;
            
            
            Map<String, String> subGen = new HashMap();
            Map<String, Service> substo = new HashMap();

            Map<String, String> localGenNAT;// = new HashMap();
            Map<String, String> localStoNAT;// = new HashMap();
            
            //Map<String, String> tempGenNAT;// = new HashMap();
            //Map<String, String> tempStoNAT;// = new HashMap();

            
            @Override
            public void action(){
                String senderId= id;
                JSONObject receivedJSON;

                ACLMessage receivedMsg = blockingReceive();//
                String data;
                if(receivedMsg!=null){

                    try{
                        String content = receivedMsg.getContent();
                        data = receivedMsg.getContent();
                        receivedJSON = JSONParse(data);
                        senderId = (String) receivedJSON.get("senderId");
                        // Handle string content
                    }catch(Exception e){
                        receivedJSON = new JSONObject();
                    }
                    
                    switch(receivedMsg.getPerformative()){
                        case ACLMessage.REQUEST:
                        
                            try {
                                receivedOffer = (Offer) receivedMsg.getContentObject();
                            } catch (UnreadableException ex) {
                                Logger.getLogger(ControlAgent.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        
                            senderId = receivedOffer.senderId;
                            
                            //receivedTimestep = ((Long) receivedJSON.get("timestep")).intValue();
                            this.receivedTimestep = receivedOffer.timestep;
                            
                            if(this.receivedTimestep == timestep)
                            {
                                offer.totalPVs += receivedOffer.totalPVs;
                                offer.totalDemands += receivedOffer.totalDemands;
                                offer.totalWinds += receivedOffer.totalWinds;
                                
                                receivedOffer.GenNAT.forEach(localGenNAT::putIfAbsent);
                                receivedOffer.StoNAT.forEach(localStoNAT::putIfAbsent);
                                
                                receivedOffer.GenList.forEach(offer.GenList::putIfAbsent);
                                receivedOffer.StoList.forEach(offer.StoList::putIfAbsent);
                                
                                for (Map.Entry<String, String> entry : receivedOffer.GenNAT.entrySet()) {
                                    offer.GenNAT.put(entry.getKey(), id);
                                }
                                
                                for (Map.Entry<String, String> entry : receivedOffer.StoNAT.entrySet()) {
                                    offer.StoNAT.put(entry.getKey(), id);
                                }
                                
                                send(sender.reset().put("timestep",this.timestep).put("senderId",id).put("targetId",senderId).put("type", ACLMessage.AGREE).prepare(id+"_soc", ACLMessage.AGREE));
                            }
                            else{//if wrong timestep
                                System.out.println("wrong timestep " + nummm + " and condition is " + (this.receivedTimestep == this.timestep) + " for receivedTimestep = " + this.receivedTimestep +" and timestep = " + this.timestep + " sender is " + receivedOffer.senderId + " target is " + receivedOffer.targetId + " receiver is " + id);
                            }
                            break;

                        case ACLMessage.INFORM:
                            if (senderId!=null && senderId.equals(id + "_data")){//"_meas"
                                this.receivedTimestep = ((Long) receivedJSON.get("timestep")).intValue();// is it needed ??
                                    localPV = (Double) receivedJSON.get("PV");
                                    localDemand = (Double) receivedJSON.get("Demand");
                                    offer.totalPVs += localPV;
                                    offer.totalDemands += localDemand;
                                    offer.totalWinds = 0;
                            }
                            else{
                                String state = (String) receivedJSON.get("state");
                                String request = (String) receivedJSON.get("request");
                                if ("allReady".equals(state)){
                                    allDemand = localDemand + Demands;
                                    allPV = localPV + PVs;

                                    if(upperHolon.equals("none")){
                                        
                                        MultiPhaseCWS control = new MultiPhaseCWS(offer);
                                        Response response = control.decide(offer);
                                        
                                        
                                        resultThermal=0;
                                        resultStorage = 0;
                                        for (Map.Entry<String, Double> entry : response.GenResp.entrySet()) {
                                            resultThermal += entry.getValue();
                                        }
                                        for (Map.Entry<String, Double> entry : response.StoResp.entrySet()) {
                                            resultStorage += entry.getValue();
                                                batstocked+=entry.getValue();
                                                batrequested = (batpow < batcapacity-batstocked) ? batpow : batcapacity-batstocked;
                                                batavailable = (batstocked < batpow) ? batstocked : batpow;
                                            System.out.println(entry.getKey() + " : " + entry.getValue());
                                        }
                                        
                                        
                                        send(sender.reset()
                                            .put("timestep",this.timestep)
                                            .put("senderId",id)//.put("allDemand", allDemand)
                                            .put("allDemand", offer.totalDemands)
                                            .put("LocalBatteryStocked",0.1*theoreticalbatcapacity+batstocked)
                                            .put("allReceived", response.DemandRatio*offer.totalDemands)
                                            .put("DemandRatio", response.DemandRatio)
                                            .put("PVRatio", response.PVRatio)
                                            .put("impactCarbon", response.ImpactCarbon)
                                            .put("bestPhase", response.bestPhase)
                                            .prepare(id+"_data", ACLMessage.INFORM_IF));
                                        
                                        
                                        
                                        offer = new Offer(id,  "upper", timestep+1);

                                        offer.PVEconCost = PVEcon;
                                        offer.PVEnvCost = PVEnv;
                                        offer.WindEconCost = windEcon;
                                        offer.WindEnvCost = windEnv;
                                        
                                        if(thermal>0){
                                            thermalInfo = new Service(id, 0, thermal, thermalEcon, thermalEnv);
                                            offer.GenList.put("thermal_"+id, thermalInfo);
                                            offer.GenNAT.put("thermal_"+id, id);//id to be changed with th value of the receiving holon before sending (like NAT)
                                        }
                                        else
                                            ;//thermalInfo = null;
                                        if(batpow>0){
                                            storageInfo = new Service(id, -batavailable, batrequested, stoEcon, stoEnv);// min is -available and max is rrequested storage...with negative cost
                                            offer.StoList.put("Storage_"+id, storageInfo);
                                            offer.StoNAT.put("Storage_"+id, id);//id to be changed with th value of the receiving holon before sending (like NAT)
                                        }
                                        else
                                            ;//storageInfo = null;
                                        
                                        //batstocked += (LS_Sto[1] < 0) ? (LS_Sto[1] * batavailable) : (LS_Sto[1] * batrequested);
                                        
                                        this.timestep++;
                                        localDemand = 0;
                                        Demands = 0;
                                        localPV = 0;
                                        PVs = 0;
                                        allDemand = 0;
                                        allPV = 0;
                                        nummm=0;
                                        
                                        
                                        send(sender.reset().put("senderId", id).put("data", "request").put("timestep", this.timestep).prepare(id+"_data",ACLMessage.REQUEST));
                                        //send result to children
                                        
                                        
                                        
                                        
                                        response.senderId = id;
                                        response.targetId = "children";
                                        response.GenNAT.putAll(localGenNAT);
                                        response.StoNAT.putAll(localStoNAT);
//                                        localGenNAT.forEach(response.GenNAT::putIfAbsent);
//                                        localStoNAT.forEach(response.StoNAT::putIfAbsent);
                                        response.type = ACLMessage.PROPOSE;
                                        
                                        
                                        
                                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                                        message.addReceiver(new AID(id+"_soc", AID.ISLOCALNAME));
                                        try {
                                            message.setContentObject((Serializable) response);
                                        } catch (IOException ex) {
                                            Logger.getLogger(ControlAgent.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                         send(message);
                                        
                                         response = new Response();
                                         
                                         
                                        if(!disconnected) upperHolon = upperHolonMain;
                                        send(sender.reset().put("senderId", id).put("data", "request").put("timestep", this.timestep).prepare(id+"_data",ACLMessage.REQUEST));
//                                        if(id.equals("Mamoudzou") && (timestep > 14)) upperHolon = "Mayotte";
                                    }
                                    else{
                                        offer.senderId = id;
                                        offer.targetId = "upper";
                                        offer.type = ACLMessage.REQUEST;
                                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                                        message.addReceiver(new AID(id+"_soc", AID.ISLOCALNAME));
                                        try {
                                            message.setContentObject((Serializable) offer);
                                        } catch (IOException ex) {
                                            Logger.getLogger(ControlAgent.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                         send(message);
                                         
                                        nummm=0;
                                    }
                                }else{
                                    if("start".equals(state)){
                                        send(sender.reset().put("senderId", id).put("data", "request").put("timestep", this.timestep).prepare(id+"_data", ACLMessage.REQUEST));
                                        offer = new Offer(id, "upper", timestep);
                                        
                                        offer.PVEconCost = PVEcon;
                                        offer.PVEnvCost = PVEnv;
                                        offer.WindEconCost = windEcon;
                                        offer.WindEnvCost = windEnv;
                                        
                                        localGenNAT = new HashMap();
                                        localStoNAT = new HashMap();
                                        if(thermal>0){
                                            thermalInfo = new Service(id, 0, thermal, thermalEcon, thermalEnv);
                                            offer.GenList.put("thermal_"+id, thermalInfo);
                                            offer.GenNAT.put("thermal_"+id, id);//id to be changed with th value of the receiving holon before sending (like NAT)
                                        }
                                        else
                                            ;//thermalInfo = null;
                                        if(batpow>0){
                                            storageInfo = new Service(id, -batavailable, batrequested, stoEcon, stoEnv);// min is -available and max is rrequested storage...with negative cost
                                            offer.StoList.put("Storage_"+id, storageInfo);
                                            offer.StoNAT.put("Storage_"+id, id);//id to be changed with th value of the receiving holon before sending (like NAT)
                                        }
                                        else
                                            ;//storageInfo = null;
                                    }
                                    else{
                                        if("mod".equals(request)){
                                            //apply changes here
                                            String type = (String) receivedJSON.get("type");
                                            double value;
                                            double powerInput;
                                            double capacityInput;
                                            switch(type){
                                                case "disconnection":
                                                    disconnected = !disconnected;
                                                    break;
                                                case "no_thermal":
                                                    no_thermal = !no_thermal;
                                                    break;
                                                case "demandRatio":
                                                    modDemand = (Double) receivedJSON.get("value");
                                                    break;
                                                case "pvRatio":
                                                    modPV = (Double) receivedJSON.get("value");
                                                    PVEcon = (Double) receivedJSON.get("ecoCost");
                                                    PVEnv = (Double) receivedJSON.get("envCost");
                                                    break;
                                                case "windValue":
                                                    modWind = (Double) receivedJSON.get("value");
                                                    windEcon = (Double) receivedJSON.get("ecoCost");
                                                    windEnv = (Double) receivedJSON.get("envCost");
                                                    break;
                                                case "thermalGeneration":
                                                    originalThermal = (Double) receivedJSON.get("value");
                                                    thermalEcon = (Double) receivedJSON.get("ecoCost");
                                                    thermalEnv = (Double) receivedJSON.get("envCost");
                                                    break;
                                                case "storage":
                                                    batpow = (Double) receivedJSON.get("powerInput");
                                                    batcapacity = (Double) receivedJSON.get("capacityInput");
                                                    stoEcon = (Double) receivedJSON.get("ecoCost");
                                                    stoEnv = (Double) receivedJSON.get("envCost");
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case ACLMessage.PROPOSE:
                            if(!upperHolon.equals("none")){
                        try {
                            receivedResponse = (Response) receivedMsg.getContentObject();
                        } catch (UnreadableException ex) {
                            Logger.getLogger(ControlAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                                senderId = receivedResponse.senderId;
                                receivedTimestep = receivedResponse.timestep;
                                if((this.receivedTimestep==timestep)&&(!receivedResponse.equals(null))){

                                    senderId = receivedResponse.senderId;


                                    resultThermal=0;
                                    resultStorage = 0;
                                    for (Map.Entry<String, Double> entry : receivedResponse.GenResp.entrySet()) {
                                        resultThermal += entry.getValue();
                                    }
                                    for (Map.Entry<String, Double> entry : receivedResponse.StoResp.entrySet()) {
                                        resultStorage += entry.getValue();
//                                        if(!localStoNAT.containsKey(entry.getKey())){
                                            batstocked+=entry.getValue();
                                            batrequested = (batcapacity-batstocked < batpow) ? batcapacity-batstocked : batpow;
                                            batavailable = (batstocked < batpow) ? batstocked : batpow;
                                    }


                                    send(sender.reset()
                                            .put("timestep",this.timestep)
                                            .put("senderId",id)//.put("allDemand", allDemand)
                                            .put("allDemand", offer.totalDemands)
                                            .put("LocalBatteryStocked",0.1*theoreticalbatcapacity+batstocked)
                                            .put("allReceived", receivedResponse.DemandRatio*offer.totalDemands)
                                            .put("DemandRatio", receivedResponse.DemandRatio)
                                            .put("PVRatio", receivedResponse.PVRatio)
                                            .put("impactCarbon", receivedResponse.ImpactCarbon)
                                            .put("bestPhase", receivedResponse.bestPhase)
                                            .prepare(id+"_data", ACLMessage.INFORM_IF));
                                    
                                    
                                    

                                    
                                    offer = new Offer(id,  "upper", timestep+1);

                                    offer.PVEconCost = PVEcon;
                                    offer.PVEnvCost = PVEnv;
                                    offer.WindEconCost = windEcon;
                                    offer.WindEnvCost = windEnv;
                                    

                                    if(thermal>0){
//                                        if(!(id.equals("Koungou") && (timestep+1 > 5) && (timestep+1 < 14))){//5 14
//                                            thermalInfo = new Service(id, 0, thermal, thermalEcon, thermalEnv);
//                                            offer.GenList.put("thermal_"+id, thermalInfo);
//                                            offer.GenNAT.put("thermal_"+id, id);//id to be changed with th value of the receiving holon before sending (like NAT)
//                                        }
                                        if(!no_thermal){//5 14
                                            thermalInfo = new Service(id, 0, thermal, thermalEcon, thermalEnv);
                                            offer.GenList.put("thermal_"+id, thermalInfo);
                                            offer.GenNAT.put("thermal_"+id, id);//id to be changed with th value of the receiving holon before sending (like NAT)
                                        }
                                        else{
                                            thermalInfo = new Service(id, 0, 1, thermalEcon, thermalEnv);
                                            offer.GenList.put("thermal_"+id, thermalInfo);
                                            offer.GenNAT.put("thermal_"+id, id);//id to be changed with th value of the receiving holon before sending (like NAT)
                                        }
                                    }
                                    else
                                        ;//thermalInfo = null;
                                    if(batpow>0){
                                        storageInfo = new Service(id, -batavailable, batrequested, stoEcon, stoEnv);// min is -available and max is rrequested storage...with negative cost
                                        offer.StoList.put("Storage_"+id, storageInfo);
                                        offer.StoNAT.put("Storage_"+id, id);//id to be changed with th value of the receiving holon before sending (like NAT)
                                    }
                                    else
                                        ;

                                    this.timestep++;
                                    localDemand = 0;
                                    Demands = 0;
                                    localPV = 0;
                                    PVs = 0;
                                    allDemand = 0;
                                    allPV = 0;
                                    this.nummm++;
                                    
                                    
                                    receivedResponse.senderId = id;
                                    receivedResponse.targetId = "children";
                                    receivedResponse.GenNAT.putAll(localGenNAT);
                                    receivedResponse.StoNAT.putAll(localStoNAT);
                                    receivedResponse.type = ACLMessage.PROPOSE;
                                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);//ACLMessage.PROPOSE
                                    message.addReceiver(new AID(id+"_soc", AID.ISLOCALNAME));
                                    try {
                                        message.setContentObject((Serializable) receivedResponse);
                                    } catch (IOException ex) {
                                        Logger.getLogger(ControlAgent.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                     send(message);
                                     
                                     
                                    receivedResponse = new Response();
                                    
                                    if(disconnected) upperHolon = "none";
                                    send(sender.reset().put("senderId", id).put("data", "request").put("timestep", this.timestep).prepare(id+"_data",ACLMessage.REQUEST));
                                }
                                else {
                                }
                            }
                                break;
                            
                        case ACLMessage.REFUSE:
                            break;
                     }
                }else{
                    block();
                }
            }
         });
    }
    
    

    
    private static JSONObject JSONParse(String jsonString){
        JSONObject  jsonObject=new JSONObject();
        JSONParser jsonParser=new  JSONParser();
        if ((jsonString != null) && !(jsonString.isEmpty()) && !(jsonString.contains("Utils.Agent.Messaging"))) {
            try {
                jsonObject=(JSONObject) jsonParser.parse(jsonString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
    
    
}