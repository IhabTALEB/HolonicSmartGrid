/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Agents;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;

/**
 *
 * @author ihab
 */
public class Master extends Agent{
    

    
    protected void setup(){

    JSONObject fulljson;
    JSONObject currentleveljson;
    
    
    fulljson = new JSONObject();

        String upperId;
        String holonId;
        int level =0;
        double regionvalue;
        double PVRatio;
        double DemandRatio;
        double thermal;
        double BatteryCapacity;
        double BatteryPower;
        
        double PVRatioParam=1;
        double DemandRatioParam=1;
        double thermalParam=1;
        double BatteryCapacityParam=1;
        double BatteryPowerParam=1;
        
        
        double PVEcon=0;
        double PVEnv=0;
        double thermalEcon=0;
        double thermalEnv=0;
        double stoEcon=0;
        double stoEnv=0;
        
        Row row;
        Cell cell;
        String filename = "/home/ihab/EDM/final/final pred hourly.xlsx"; //24-30 2020 prod pv et conso
        int rownum = 0;
        
        try {

            FileInputStream file = new FileInputStream(new File("/home/ihab/EDM/final/PV and Demand/PV and populations - edited version.xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            
            for (int sheetNumber = 0; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
            
                currentleveljson = new JSONObject();
                
                System.out.println("sheet number : " + sheetNumber);
                
                XSSFSheet sheet = workbook.getSheetAt(sheetNumber);
                
                int i = 0;
                regionvalue=0;
                rownum = 0;
                Iterator<Row> rowIterator = sheet.iterator();
                System.out.println("LAST ROW IS: "+ sheet.getLastRowNum());
                while (rowIterator.hasNext()) {
                    row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();

                    if(rownum==0){
                        System.out.println("rownum is: "+rownum);
                        cellIterator.next();
                        cellIterator.next();
                        
                        cell = cellIterator.next();
                        DemandRatioParam = cell.getNumericCellValue();

                        cell = cellIterator.next();
                        PVRatioParam = cell.getNumericCellValue();
                        
                        cell = cellIterator.next();
                        cell = cellIterator.next();
                        
                        cell = cellIterator.next();
                        thermalParam = cell.getNumericCellValue();

                        cell = cellIterator.next();
                        cell = cellIterator.next();

                        cell = cellIterator.next();
                        BatteryCapacityParam = cell.getNumericCellValue();


                        cell = cellIterator.next();
                        BatteryPowerParam = cell.getNumericCellValue();
                        
                        cell = cellIterator.next();
                        cell = cellIterator.next();
                    }
                    else{
                        System.out.println("rownum is: "+rownum);


                        cell = cellIterator.next();
                        upperId = cell.getStringCellValue();
                        System.out.println("testtest"+upperId);

                        cell = cellIterator.next();
                        holonId = cell.getStringCellValue();

                        cell = cellIterator.next();
                        DemandRatio = cell.getNumericCellValue();

                        cell = cellIterator.next();
                        PVRatio = cell.getNumericCellValue();

                        cell = cellIterator.next();
                        PVEcon = cell.getNumericCellValue();

                        cell = cellIterator.next();
                        PVEnv = cell.getNumericCellValue();

                        
                        cell = cellIterator.next();
                        thermal = cell.getNumericCellValue();
                        
                        
                        cell = cellIterator.next();
                        thermalEcon = cell.getNumericCellValue();

                        cell = cellIterator.next();
                        thermalEnv = cell.getNumericCellValue();
                    
                    
                        cell = cellIterator.next();
                        BatteryCapacity = cell.getNumericCellValue();


                        cell = cellIterator.next();
                        BatteryPower = cell.getNumericCellValue();

                        
                        cell = cellIterator.next();
                        stoEcon = cell.getNumericCellValue();

                        cell = cellIterator.next();
                        stoEnv = cell.getNumericCellValue();
                        
                        AddHolon(upperId, holonId, filename, DemandRatio*DemandRatioParam, 
                                PVRatio*PVRatioParam, PVEcon, PVEnv,
                                thermal*thermalParam, thermalEcon, thermalEnv,
                                BatteryCapacity*BatteryCapacityParam, BatteryPower*BatteryPowerParam, stoEcon, stoEnv,
                                level);
                        
                        currentleveljson.put(upperId, holonId);
                        

                        fulljson.put(level, currentleveljson);
                    }
                    rownum++;
                    
                    
                    
                    
                    
                }
                level++;
            }
        file.close();
        AddAgent("Gateway");
        } catch (Exception e) {
            e.printStackTrace();
            //return null;
        }
    }
    
    
    public static void AddHolon(String upperId, String holonId, String FileName, double popValue, double pvValue, double pvEcon, double pvEnv, double thermal, double thermalEcon, double thermalEnv, double batcapacity, double batpow, double stoEcon, double stoEnv, int level){//, ArrayList<String> children
        //add the container with its five agent
        
        System.out.println("adding holon : " + holonId);
        
        jade.core.Runtime rt = jade.core.Runtime.instance();

        Profile profile = new ProfileImpl();
         // With the Profile you can set some options for the container
        profile.setParameter(Profile.PLATFORM_ID, "192.168.56.130:1099/JADE");
        profile.setParameter(Profile.CONTAINER_NAME, holonId);
        
        AgentContainer otherContainer = rt.createAgentContainer(profile);

         try {
                 // Here I create an agent in the main container and start it.
             AgentController data = otherContainer.createNewAgent(holonId+"_data", "Agents.Holon.DataAgent", new Object[] {holonId});//params
             AgentController meas = otherContainer.createNewAgent(holonId+"_meas", "Agents.Holon.MeasurementAgent", new Object[] {holonId, FileName, popValue, pvValue});//params
             AgentController pred = otherContainer.createNewAgent(holonId+"_pred", "Agents.Holon.PredictionAgent", new Object[] {holonId});//params
             AgentController cont = otherContainer.createNewAgent(holonId+"_cont", "Agents.Holon.ControlAgent", new Object[] {holonId, upperId, pvEcon, pvEnv, thermal, thermalEcon, thermalEnv, batcapacity, batpow, stoEcon, stoEnv});//params, children
             AgentController soc = otherContainer.createNewAgent(holonId+"_soc", "Agents.Holon.SocialAgent", new Object[] {holonId, upperId, level});//params
             meas.start();
             data.start();
             pred.start();
             soc.start();
             cont.start();
         } catch(StaleProxyException e) {
             e.printStackTrace();
             System.out.println(e);
         }
    }
    
    
    public static void AddAgent(String name){//, ArrayList<String> children
        //add the container with its five agent
        
        jade.core.Runtime rt = jade.core.Runtime.instance();

        Profile profile = new ProfileImpl();
         // With the Profile you can set some options for the container
        profile.setParameter(Profile.PLATFORM_ID, "192.168.56.130:1099/JADE");
        profile.setParameter(Profile.CONTAINER_NAME, "GateWay");
        
        AgentContainer otherContainer = rt.createAgentContainer(profile);

         try {
                 // Here I create an agent in the main container and start it.
             AgentController gateway = otherContainer.createNewAgent(name, "Agents.Gateway", new Object[] {});//params
             gateway.start();
         } catch(StaleProxyException e) {
             e.printStackTrace();
             System.out.println(e);
         }
    }
    
    
}