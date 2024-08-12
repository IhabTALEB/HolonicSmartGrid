/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils.Agent.Messaging.Message;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 *
 * @author ihab
 */
public class Service implements Serializable{
    public String Id;// Id of the component
    public double min;
    public double max;
    public double econCost;
    public double envCost;

    
    
    public Service(String Id, double min, double max, double econCost, double envCost) {
        this.Id = Id;
        this.min = min;
        this.max = max;
        this.econCost = econCost;
        this.envCost = envCost;
    }
    
    
}
