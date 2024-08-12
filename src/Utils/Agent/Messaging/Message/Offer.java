/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils.Agent.Messaging.Message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ihab
 */
public class Offer implements Serializable{

    public double DemandEconCost = (0.364*1000000)/(365*3600);
    
    
    public String senderId;
    public String targetId;
    public int timestep;
    public int type;
    
    public double totalDemands;
    public double totalPVs;
    public double totalWinds;
    
    public double PVEconCost;
    public double PVEnvCost;
    public double WindEconCost;
    public double WindEnvCost;

    
    public Map<String, String> GenNAT;//ServiceId, to whom you frowrd
    public Map<String, String> StoNAT;//ServiceId, to whom you frowrd
    
    public Map<String, Service> GenList;//ServiceId, ServiceInfo
    public Map<String, Service> StoList;//ServiceId, ServiceInfo
    
    public Offer(String senderId, String targetID, int timestep) {
        
        this.senderId = senderId;
        this.targetId = targetId;
        this.timestep = timestep;
        
        totalDemands = 0;
        totalPVs = 0;
        totalWinds = 0;
        
        GenNAT = new HashMap();
        StoNAT = new HashMap();
        GenList = new HashMap();
        StoList = new HashMap();
        
        
    }
    
    
         // Override toString method
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("------------------------------------------ \n");
        
        sb.append("senderId: ").append(senderId).append("\n");
        sb.append("targetId: ").append(targetId).append("\n");
        sb.append("timestep: ").append(timestep).append("\n");
        sb.append("type: ").append(type).append("\n");
        sb.append("totalDemands: ").append(totalDemands).append("\n");
        sb.append("totalPVs: ").append(totalPVs).append("\n");
        sb.append("totalWinds: ").append(totalWinds).append("\n");
        sb.append("PVEconCost: ").append(PVEconCost).append("\n");
        sb.append("PVEnvCost: ").append(PVEnvCost).append("\n");
        sb.append("WindEconCost: ").append(WindEconCost).append("\n");
        sb.append("WindEnvCost: ").append(WindEnvCost).append("\n");
        
        sb.append("GenNAT: ").append("\n");
        for (Map.Entry<String, String> entry : GenNAT.entrySet()) {
            sb.append("  Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue()).append("\n");
        }
        
        sb.append("StoNAT: ").append("\n");
        for (Map.Entry<String, String> entry : StoNAT.entrySet()) {
            sb.append("  Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue()).append("\n");
        }

        sb.append("GenList: ").append("\n");
        for (Map.Entry<String, Service> entry : GenList.entrySet()) {
            sb.append("  Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue().toString()).append("\n");
        }
        
        sb.append("StoList: ").append("\n");
        for (Map.Entry<String, Service> entry : StoList.entrySet()) {
            sb.append("  Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue().toString())
              .append(", Id: ").append(entry.getValue().Id.toString())
              .append(", Min: ").append(entry.getValue().min)
              .append(", Max: ").append(entry.getValue().max)
              .append(", econCost: ").append(entry.getValue().econCost)
              .append(", envCost: ").append(entry.getValue().envCost).append("\n");
        }
        
        sb.append("------------------------------------------ \n");
        
        return sb.toString();
    }
    
    
    
}
