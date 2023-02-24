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

/**
 *
 * @author ihab
 */
public class Launcher extends Agent{
    protected void setup(){
                //createSimulation2("/home/ihab/EDM/final/24-30 2020 prod pv et conso.xlsx");
    //}
    
    
    
    
    
    //public void createSimulation2(String FileName){
//        ArrayList<Double>[] arr = new ArrayList[17];
//        
//        
//        for(int i=0;i<17;i++){
//            arr[i] = new ArrayList<Double>();
//        }
//        addBehaviour(new OneShotBehaviour(){
//            @Override
//            public void action(){
//            }});
        String upperId;
        String holonId;
        int level =0;
        //double value;
        //String newregion;
        double regionvalue;
        //ArrayList<String> children = new ArrayList<String>();
        //ArrayList<String> regions = new ArrayList<String>();
        //double pvRates[] = new double[17];
        double PVRatio;
        double DemandRatio;
        double thermal;
        Row row;
        Cell cell;
        String filename = "/home/ihab/EDM/final/final pred hourly.xlsx"; //24-30 2020 prod pv et conso
//        Boolean test=false;
        int rownum = 0;
        
        try {

            FileInputStream file = new FileInputStream(new File("/home/ihab/EDM/final/PV and Demand/PV and populations - edited version.xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            
            AddHolon("none", "Mayotte", filename, 0, 0, 0, level);

            for (int sheetNumber = 0; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
            
                level++;
                
                System.out.println("sheet number : " + sheetNumber);
                
                XSSFSheet sheet = workbook.getSheetAt(sheetNumber);
                
//                test=false;
                
                int i = 0;
                regionvalue=0;
                rownum = 0;
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();

                    cell = cellIterator.next();
                    upperId = cell.getStringCellValue();
                    
                    cell = cellIterator.next();
                    holonId = cell.getStringCellValue();

                    cell = cellIterator.next();
                    DemandRatio = cell.getNumericCellValue();
//                    i++;
                    
                    cell = cellIterator.next();
                    PVRatio = cell.getNumericCellValue();
                    
                    cell = cellIterator.next();
                    thermal = cell.getNumericCellValue();
//                    i++;

                    //if(holonId.equals("Dzaoudzi")) thermal=0;
                    //if(holonId.equals("Koungou")) thermal = thermal;
                    

//                    if (!test){
//                    if((sheetNumber==0 && rownum==1) || (sheetNumber==1 && rownum==3))
//                    if((sheetNumber==0 && rownum==1) || (sheetNumber==1 && (rownum>1 && rownum<7)))
                        AddHolon(upperId, holonId, filename, DemandRatio, PVRatio, thermal, level);
//                        test=true;
//                    }
                    rownum++;
                    
                    
                    
                }
            }
        file.close();
        AddAgent("Gateway");
        } catch (Exception e) {
            e.printStackTrace();
            //return null;
        }
    }
    
    
    public static void AddHolon(String upperId, String holonId, String FileName, double popValue, double pvValue, double thermal, int level){//, ArrayList<String> children
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
             AgentController cont = otherContainer.createNewAgent(holonId+"_cont", "Agents.Holon.ControlAgent", new Object[] {holonId, upperId, thermal});//params, children
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