/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils.Agent.Messaging;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author ihab
 */
public class Sender{
    String senderId;
    JSONObject json;
    String JSONString;
    ACLMessage message;
    
    public Sender(String senderId){
        json = new JSONObject();
        this.senderId = senderId;
    }
    
    public Sender reset(){
        this.json = new JSONObject();
        this.put("senderId",this.senderId);
        return this;
    }
    
    
    public Sender reset(JSONObject json){
        this.json = new JSONObject();
        this.json = json;
        this.put("senderId",this.senderId);
        return this;
    }
    
    
    public Sender put(Object key, Object value){
        json.put(key, value);
        return this;
    }
    
    public ACLMessage prepare(Object targetId, int performative){
        //this.put("targetId", targetId);
        JSONString=JSONValue.toJSONString(this.json);
        message = new ACLMessage(performative);
        message.addReceiver(new AID(targetId.toString(), AID.ISLOCALNAME));
        System.out.println(targetId.toString()+"targettttttttIDDDDDD*********** :");
        System.out.println("JSON for "+ targetId.toString() +"*********** :" + JSONString);
        if("Dembeni_soc_soc".equals(this.json.get("targetId"))) System.out.println(1/0);
        message.setContent(JSONString);
        return message;
    }
    
    
//    public ACLMessage prepareForward(JSONObject json, Object targetId, int performative){
//        this.json = json;
//        this.put("targetId", targetId);
//        JSONString=JSONValue.toJSONString(json);
//        message = new ACLMessage(performative);
//        message.addReceiver(new AID(targetId.toString(), AID.ISLOCALNAME));
//        message.setContent(JSONString);
//        return message;
//    }
    
    
}
