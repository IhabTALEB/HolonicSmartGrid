/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Agents.Holon;

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

/**
 *
 * @author ihab
 */
public class ControlAgent extends Agent{


    Object[] args;// = getArguments();

    String id;// = args[0].toString();
    String upperHolon;
    List<Object> children;
    JSONObject Data = new JSONObject();
    double thermal;
    //String upperHolon;
    
    Reader reader;
    Sender sender;



    protected void setup(){
        
        args = getArguments();
        System.out.println(Arrays.toString(args));
        id = args[0].toString();
        thermal = Double.parseDouble(args[2].toString());
        
        //upperHolon = args[1].toString();
        upperHolon = args[1].toString();
//        children = Arrays.asList(args[2]);

//        JSONObject jsonreq = new JSONObject();
//        jsonreq.put("senderId", id);
//        jsonreq.put("data", "request");
//        jsonreq.put("timestep", 0);
//        String msg = JSONValue.toJSONString(jsonreq);
//        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
//        message.addReceiver(new AID(id+"_meas", AID.ISLOCALNAME));
//        message.setContent(msg);
//        send(message);
//        System.out.println(id + "_cont : data requested");

        

        reader = new Reader();
        sender = new Sender(id);


        addBehaviour(new CyclicBehaviour(){
            
            double PVs=0;
            double Demands = 0;
            double request = 0;
            double totallocal;
            int timestep = 0;
            int receivedTimestep = 0;
            double allDemand = 0;
            double allPV = 0;
            //double thermal;
            
            double PV = 0;
            double localPV = 0;
            double Demand = 0;
            double localDemand = 0;
            int nummm =0;

              //////////////////////////////////////////////////////////////////////////////////
             ///    We need three variables, * 2 : local, locaglobal with children,all      ///
            //////////////////////////////////////////////////////////////////////////////////
            
            @Override
            public void action(){


                double totalsubdemands = 0;

                //ACLMessage receivedMsg = receive();
                ACLMessage receivedMsg = blockingReceive();//
                String data;
                if(receivedMsg!=null){

                    if(id.equals("Handréma")){
                        System.out.println("$$$$$$$$$$$$$$$$$$ MESSAGEEEEEEEE in timestep : " + timestep + "  msg is" + receivedMsg);
                    }
                    
                    data = receivedMsg.getContent();
                    JSONObject receivedJSON = JSONParse(data);
                    String senderId= "";
                    
                    //System.out.println("??????????????????????????????????????? "+receivedMsg.getPerformative()+" ??????????????????????????????????????????????,");
                    senderId = (String) receivedJSON.get("senderId");
                    
                    
                    switch(receivedMsg.getPerformative()){
                        case ACLMessage.REQUEST:
                            System.out.println(id + "_cont received REQUEST from sender : "+ receivedJSON);
                            System.out.println("received REQUEST from sender : "+ senderId + ", and value is "+ senderId.equals(id + "_data"));//"_meas"
                            receivedTimestep = ((Long) receivedJSON.get("timestep")).intValue();
                            if(id.equals("Mayotte")){
                                System.out.println("_______________________________nummm : " + nummm + " and condition is " + (receivedTimestep == timestep) + " for receivedTimestep = " + receivedTimestep +" and timestep = " + timestep + "json is" + receivedJSON);
                            }
                            if(this.receivedTimestep == timestep)
                            {
                                PV = (Double) receivedJSON.get("PV");
                                if(id.equals("Mayotte")){
                                    System.out.println("_______________________________OLD Demands in REQUEST is : " + Demands);
                                }
                                Demand = (Double) receivedJSON.get("Demand");
                                //request = (Double) receivedJSON.get("Demand");
                                PVs+= PV;
                                allPV += (Double) receivedJSON.get("allPV");
                                Demands+=Demand;
                                if(id.equals("Mayotte")){
                                    System.out.println("______________________________NEW Demands : " + Demands + "WHile Demand from REQUEST is :" + Demand);
                                }
                                allDemand += (Double) receivedJSON.get("allDemand");
                                if(id.equals("Mayotte")){
                                    System.out.println("______________________________NEW allDemand : " + allDemand + "WHile Demand  from REQUEST is :" + Demand);
                                }
                                //confirm right timestep
//                                JSONObject JSONObj;
//
//                                JSONObj=new JSONObject();
//
//                                JSONObj.put("timestep",this.timestep);//timestep
//                                JSONObj.put("senderId",id);//
//                                JSONObj.put("targetId",senderId);//
//
//                                JSONObj.put("type", ACLMessage.AGREE);
//
//                                String JSONString=JSONValue.toJSONString(JSONObj);
//
//                                ACLMessage message = new ACLMessage(ACLMessage.AGREE);
//                                message.addReceiver(new AID(id+"_soc", AID.ISLOCALNAME));
//                                message.setContent(JSONString);
//                                send(message);
                                
                                send(sender.reset().put("timestep",this.timestep).put("senderId",id).put("targetId",senderId).put("type", ACLMessage.AGREE).prepare(id+"_soc", ACLMessage.AGREE));
                                
                                nummm++;
                            }
                            else{//wrong timestep
                                System.out.println("_______________________________nummm : wrong  " + nummm + " and condition is " + (this.receivedTimestep == this.timestep) + " for receivedTimestep = " + this.receivedTimestep +" and timestep = " + this.timestep + "json is" + receivedJSON);
//                                JSONObject JSONObj;
//
//                                JSONObj=new JSONObject();
//
//                                JSONObj.put("timestep",this.timestep);//timestep
//                                JSONObj.put("senderId",id);//
//                                JSONObj.put("targetId",senderId);//
//
//                                JSONObj.put("type", ACLMessage.REFUSE);
//
//                                String JSONString=JSONValue.toJSONString(JSONObj);
//
//                                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
//                                message.addReceiver(new AID(id+"_soc", AID.ISLOCALNAME));
//                                message.setContent(JSONString);
//                                send(message);
                            }
                            break;
                        case ACLMessage.INFORM:
                            //senderId = (String) receivedJSON.get("senderId");
                            //String[] addressArr = receivedMsg.getSender().getAddressesArray();
                            //System.out.println(addressArr[0]);
                            //String sender =addressArr[1];
                            System.out.println(id + "_cont received INFORM from sender : "+ receivedJSON);
                            System.out.println(id + "_cont : received INFORM from sender : "+ senderId + ", and condition (data) is "+ senderId.equals(id + "_data"));//"_meas"
                            if (senderId!=null && senderId.equals(id + "_data")){//"_meas"
                                System.out.println(id + " _cont :  data received ; " + receivedJSON);
                                // Inform can only be sent inside the same Holon!
                                this.receivedTimestep = ((Long) receivedJSON.get("timestep")).intValue();
                                //if (receivedTimestep == timestep){
                                    //System.out.println("*************************** :" + receivedJSON);
                                    localPV = (Double) receivedJSON.get("PV");
                                    allPV += localPV;
                                    PVs+= localPV;
                                    localDemand = (Double) receivedJSON.get("Demand");
                                    if(id.equals("Mayotte")){
                                        System.out.println("______________________________NEW localDemand from INFORM is: " + localDemand);
                                    }
                                    if(id.equals("Mayotte")){
                                        System.out.println("______________________________OLD allDemand : " + allDemand );
                                    }
                                    allDemand += localDemand;
                                    if(id.equals("Mayotte")){
                                        System.out.println("______________________________NEW allDemand : " + allDemand + "WHile localDemand  from INFORM is :" + localDemand);
                                    }
                                    if(id.equals("Mayotte")){
                                        System.out.println("______________________________OLD Demands : " + Demands );
                                    }
                                    Demands += localDemand;
                                    if(id.equals("Mayotte")){
                                        System.out.println("______________________________NEW Demands : " + Demands + "WHile localDemand  from INFPORM is :" + localDemand);
                                    }
                                    if(id.equals("Mayotte")){
                                        System.out.println("______________________________OLDtotallocal : " + totallocal);
                                    }
                                    totallocal = localPV - localDemand;
                                    if(id.equals("Mayotte")){
                                        System.out.println("______________________________NEW totallocal : " + totallocal + "WHile Demand  from localDemand is :" + localDemand);
                                    }
                                //}
                            }
                            else{
                                String state = (String) receivedJSON.get("state");
                                System.out.println(id + "_cont : state is : " + state + " " + upperHolon + "allReady".equals("state"));
                                if ("allReady".equals(state)){
                                    
                                    if(id.equals("Handréma")){
                                            System.out.println("$$$$$$$$$$$$$$$$$$ allReady in timestep : " + timestep);
                                        }
                                    
                                    //if(upperHolon == null){
                                    if(upperHolon.equals("none")){
                                        
                                        System.out.println(id + "_cont : 'allready' receieved from " + id + "_soc");
                                        sendMsg(this.timestep, id, thermal + allPV, allDemand, thermal + allPV, allDemand, "children", ACLMessage.PROPOSE);//localPV, localDemand, PVs ,Demands //localPV + //localDemand + 
                                        System.out.println(id + "_cont : feedback sent from upper holon with allDemand is : "+ allDemand + " and allgeneration is : " + (thermal + allPV));
                                        
                                        
                                        double energytransmitted=0;
                                        if((thermal + allPV) > allDemand){
                                            energytransmitted = allDemand;
                                        }
                                        else{
                                            energytransmitted = thermal + allPV;
                                        }
                                        
                                        
//                                        JSONObject JSONObj =new JSONObject();
//
//                                        JSONObj.put("timestep",this.timestep);//timestep
//                                        JSONObj.put("senderId",id);
//                                        JSONObj.put("allDemand", allDemand);// can be positive or negative  //* (1-feedback))
//                                        JSONObj.put("allReceived",energytransmitted);// can be positive or negative
//                                        JSONObj.put("allRenw", energytransmitted);// can be positive or negative //thermal + allPV - 
//                                        JSONObj.put("feedback", -1);// can be positive or negative
//
//                                        String JSONString=JSONValue.toJSONString(JSONObj);
//
//                                        ACLMessage message = new ACLMessage(ACLMessage.INFORM_IF);
//                                        message.addReceiver(new AID(id+"_data", AID.ISLOCALNAME));
//                                        message.setContent(JSONString);
//                                        send(message);
                                        
                                        
                                        send(sender.reset()
                                                .put("timestep",this.timestep)
                                                .put("senderId",id)
                                                .put("allDemand", allDemand)
                                                .put("allReceived",energytransmitted)
                                                .put("allRenw", energytransmitted)
                                                .put("feedback", -1)
                                                .prepare(id+"_data", ACLMessage.INFORM_IF));
                                        
                                        
                                        System.out.println("*********************************************************************allReceived"+allPV);
                                        System.out.println("*********************************************************************allRenw"+ (allPV - allDemand));
                                        
                                        this.timestep++;
                                        localDemand = 0;
                                        Demands = 0;
                                        localPV = 0;
                                        PVs = 0;
                                        allDemand = 0;
                                        allPV = 0;
                                        nummm=0;
                                    }
                                    else{
                                        if(id.equals("Koungou")){
                                            System.out.println("$$$$$$$$$$$$$$$$$$ ThERMAL in timestep : " + timestep + " is " + thermal);
                                        }
                                        sendMsg(this.timestep, id, thermal + allPV, allDemand, thermal+allPV, allDemand, "upper", ACLMessage.REQUEST);//sendMsg //localDemand + 
                                        System.out.println(id + "_cont : Request sent to upper holons");
                                        nummm=0;
                                        //timestep++;
                                    }
                                }else{
                                    if("start".equals(state)){
                                        
                                        //System.out.println("??????????????????????????????????????? "+ state +" ??????????????????????????????????????????????,");
//                                        JSONObject jsonreq = new JSONObject();
//                                        jsonreq.put("senderId", id);
//                                        jsonreq.put("data", "request");
//                                        jsonreq.put("timestep", this.timestep);
//                                        String msg = JSONValue.toJSONString(jsonreq);
//                                        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
//                                        message.addReceiver(new AID(id+"_data", AID.ISLOCALNAME));
//                                        message.setContent(msg);
//                                        send(message);
                                        
                                        send(sender.reset().put("senderId", id).put("data", "request").put("timestep", this.timestep).prepare(id+"_data", ACLMessage.REQUEST));
                                        
                                        System.out.println(id + "_cont : data requested from "+ id+"_data");
                                    }
                                }
                            }
                            break;
                        case ACLMessage.PROPOSE:
                            // We don't have PROPOSE in IoE, it is only requests and demands. 
                            
                            ////
                            // (Send request to data and then) receive values from data and then send request and wait for response and/or directly send broadcast feedback to lowers
                            ////

                              ////////////////////////////////////////////////////////
                             /// WE NEED IT ANYWAY, BUT NO NEGOTITATIONS AT FIRST ///
                            ////////////////////////////////////////////////////////

                            
                            
                            
                            //System.out.println("received PROPOSE from sender : "+ senderId + ", and value is "+ senderId.equals(id + "_meas"));
                            senderId = (String) receivedJSON.get("senderId");
                            // if senderId == upperId
                            double feedback=0;
                            try{
                                feedback = ((Number)receivedJSON.get("feedback")).doubleValue();
                            }
                            catch(Exception e){
                                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ " + receivedJSON);
                            }
                            double totalPV = localPV + PVs;
                            double totaldemand = localDemand + Demands;
                            double totalrequested = totaldemand - totalPV;
                            System.out.println("["+ id +" : "+ timestep +"] Available Renewable Energy: " + totalPV);
                            System.out.println("["+ id +" : "+ timestep +"] Requested Energy: " +  totalrequested);
                            System.out.println("["+ id +" : "+ timestep +"] Receieved Energy: " + (totalrequested * (1-feedback)));
                            
                            
                            
//                            JSONObject JSONObj;
//
//                            JSONObj=new JSONObject();
//
//                            JSONObj.put("timestep",this.timestep);//timestep
//                            JSONObj.put("senderId",id);//
//                            JSONObj.put("targetId","children");//
//                            JSONObj.put("feedback",feedback);// can be positive or negative
//                            
//                            JSONObj.put("type", ACLMessage.PROPOSE);//PROPOSE
//
//                            String JSONString=JSONValue.toJSONString(JSONObj);
//
//                            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
//                            message.addReceiver(new AID(id+"_soc", AID.ISLOCALNAME));
//                            message.setContent(JSONString);
//                            send(message);
                            
                            send(sender.reset().put("timestep",this.timestep).put("senderId",id).put("targetId","children").put("feedback",feedback).put("type", ACLMessage.PROPOSE).prepare(id+"_soc", ACLMessage.INFORM));
                            
//                            JSONObj=new JSONObject();
//
//                            JSONObj.put("timestep",this.timestep);//timestep
//                            JSONObj.put("senderId",id);
//                            JSONObj.put("allDemand", allDemand);// can be positive or negative  //* (1-feedback))
//                            JSONObj.put("allReceived", allDemand * feedback);// can be positive or negative
//                            JSONObj.put("feedback", feedback);// can be positive or negative
//                            
//                            JSONString=JSONValue.toJSONString(JSONObj);
//
//                            message = new ACLMessage(ACLMessage.INFORM_IF);
//                            message.addReceiver(new AID(id+"_data", AID.ISLOCALNAME));
//                            message.setContent(JSONString);
//                            send(message);
                            
                            send(sender.reset().put("timestep",this.timestep).put("senderId",id).put("allDemand", allDemand).put("allReceived", allDemand * feedback).put("feedback",feedback).prepare(id+"_data", ACLMessage.INFORM_IF));
                            
                            
                            this.timestep++;
                            totalPV = 0;
                            totaldemand = 0;
                            totalrequested = 0;
                            allDemand = 0;
                            allPV = 0;
                            
                            Demands =0;
                            PVs=0;
                            
                            
                            //////////////////////////////////////
                            //show and forward
                            ///////////////////

                            
//                            JSONObject jsonreq = new JSONObject();
//                            jsonreq.put("senderId", id);
//                            jsonreq.put("data", "request");
//                            jsonreq.put("timestep", this.timestep);
//                            String msg = JSONValue.toJSONString(jsonreq);
//                            ACLMessage reqMessage = new ACLMessage(ACLMessage.REQUEST);
//                            reqMessage.addReceiver(new AID(id+"_data", AID.ISLOCALNAME));
//                            reqMessage.setContent(msg);
//                            send(reqMessage);
                            
                            send(sender.reset().put("senderId", id).put("data", "request").put("timestep", this.timestep).prepare(id+"_data",ACLMessage.REQUEST));
                            
                            System.out.println(id + "_cont : data requested from "+ id+"_data");
                            
                            break;
                        case ACLMessage.REFUSE:
                            
//                            JSONObj=new JSONObject();
//                            
//                            JSONObj.put("timestep",this.timestep);//timestep
//                            JSONObj.put("senderId",id);
//                            JSONObj.put("allReceived", 0);// can be positive or negative  //* (1-feedback))
//                            JSONObj.put("allRenw", 0);// can be positive or negative
//                            JSONObj.put("feedback", -1);// can be positive or negative
//                            
//                            JSONString=JSONValue.toJSONString(JSONObj);
//
//                            message = new ACLMessage(ACLMessage.INFORM_IF);
//                            message.addReceiver(new AID(id+"_data", AID.ISLOCALNAME));
//                            message.setContent(JSONString);
//                            send(message);
                            
                            send(sender.reset().put("timestep",this.timestep).put("senderId",id).put("allReceived", 0).put("allRenw", 0).put("feedback", -1).prepare(id+"_data", ACLMessage.INFORM_IF));
                            
                            this.timestep = ((Long) receivedJSON.get("timestep")).intValue();;
                            totalPV = 0;
                            totaldemand = 0;
                            totalrequested = 0;
                            allDemand = 0;
                            allPV = 0;
                            
                            Demands =0;
                            PVs=0;
                            
                            
//                            jsonreq = new JSONObject();
//                            jsonreq.put("senderId", id);
//                            jsonreq.put("data", "request");
//                            jsonreq.put("timestep", this.timestep);
//                            msg = JSONValue.toJSONString(jsonreq);
//                            reqMessage = new ACLMessage(ACLMessage.REQUEST);
//                            reqMessage.addReceiver(new AID(id+"_data", AID.ISLOCALNAME));
//                            reqMessage.setContent(msg);
//                            send(reqMessage);
                            
                            send(sender.reset().put("senderId", id).put("data", "request").put("timestep", this.timestep).prepare(id+"_data",ACLMessage.REQUEST));
                            
                            System.out.println(id + "_cont : data requested from "+ id+"_data");
                            break;
                     }
                }else{
                    block();
                }
            }
         });
        
        
//        addBehaviour(new TickerBehaviour(this, 5000){
//            int timestep = 0;
//            @Override
//            protected void onTick() {
//                
//           }
//        });
        
        
    }
    
    
    
    public void sendMsg(int timestep, String id, double totalgeneration, double totalDemand, double allPV, double allDemand, String target, int type){
        

        
        JSONObject JSONObj;

        JSONObj=new JSONObject();

        JSONObj.put("timestep",timestep);//timestep
        JSONObj.put("senderId",id);//
        JSONObj.put("targetId",target);//
        JSONObj.put("type",type);//
        double feedback=0;
        if(type == ACLMessage.PROPOSE){
            if(totalDemand == 0) JSONObj.put("feedback",0);
            else{
                if(totalgeneration > totalDemand) {
                    feedback = 1;
                    JSONObj.put("feedback",feedback);// can be positive or negative
                }
                else{
                    feedback = totalgeneration/totalDemand; ///////////////////////////////////////::::to be  reviewed
                    //if(feedback>=1) System.out.println("____________________________feedback from "+id+"_cont is "+feedback +" with totalgeneration = "+ totalgeneration+" andtotaldemand = "+totalDemand +" and timestep is "+ timestep);
                    JSONObj.put("feedback",feedback);// can be positive or negative
                }
            }
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ feedback is : " + feedback + " with totalgeneration " + totalgeneration + "and totalDemand "+totalDemand);
        }
        else{
            JSONObj.put("PV",totalgeneration);// can be positive or negative
            JSONObj.put("Demand", totalDemand);
            JSONObj.put("allPV", allPV);
            JSONObj.put("allDemand", allDemand);
        }
        //JSONObj.put("PV",totalPV);// can be positive or negative
        //JSONObj.put("Demand", totalDemand);
        JSONObj.put("MessageType", type);
        
        

        String JSONString=JSONValue.toJSONString(JSONObj);

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID(id+"_soc", AID.ISLOCALNAME));
        message.setContent(JSONString);
        send(message);
    }
    
    private static JSONObject JSONParse(String jsonString){
        JSONObject  jsonObject=new JSONObject();
        JSONParser jsonParser=new  JSONParser();
        if ((jsonString != null) && !(jsonString.isEmpty())) {
            try {
                jsonObject=(JSONObject) jsonParser.parse(jsonString);
            } catch (ParseException e) {
                e.printStackTrace();
                //System.out.println(jsonString);
            }
        }
        return jsonObject;
    }
    
    
}