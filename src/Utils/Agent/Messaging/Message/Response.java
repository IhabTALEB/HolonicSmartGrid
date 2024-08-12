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
public class Response implements Serializable{

    public String senderId;
    public String targetId;
    public int timestep;
    public int type;
    public int bestPhase;

    public double PVRatio;
    public double WindRatio;
    public double DemandRatio;
    public double ImpactCarbon;
    
    public Map<String, String> GenNAT;//ServiceId, to whom you frowrd
    public Map<String, String> StoNAT;//ServiceId, to whom you frowrd

    public Map<String, Double> GenResp;//ServiceId, response value
    public Map<String, Double> StoResp;//ServiceId, response value

    public Response() {
    }
    
    
    
    public Response(String senderId, String targetID, int timestep, int bestPhase) {
        
        this.senderId = senderId;
        this.targetId = targetId;
        this.timestep = timestep;
        this.bestPhase = bestPhase;
        
        PVRatio = 0;
        WindRatio = 0;
        
        GenNAT = new HashMap();
        StoNAT = new HashMap();
        
        GenResp = new HashMap();
        StoResp = new HashMap();
        
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
        sb.append("PVRatio: ").append(PVRatio).append("\n");
        sb.append("WindRatio: ").append(WindRatio).append("\n");
        sb.append("DemandRatio: ").append(DemandRatio).append("\n");
        sb.append("ImpactCarbon: ").append(ImpactCarbon).append("\n");
        sb.append("Best phase: ").append(bestPhase).append("\n");
        
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

        sb.append("GenResp: ").append("\n");
        for (Map.Entry<String, Double> entry : GenResp.entrySet()) {
            sb.append("  Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue().toString()).append("\n");
        }
        
        sb.append("StoResp: ").append("\n");
        for (Map.Entry<String, Double> entry : StoResp.entrySet()) {
            sb.append("  Key: ").append(entry.getKey())
              .append(", Value: ").append(entry.getValue().toString()).append("\n");
        }
        
        sb.append("------------------------------------------ \n");
        
        return sb.toString();
    }
    
    
}
