/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Agents.Holon;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import Utils.Agent.Messaging.Reader;
import Utils.Agent.Messaging.Sender;

/**
 *
 * @author ihab
 */
public class MeasurementAgent extends Agent{
    
    Reader reader;
    Sender sender;
    
    protected void setup(){
        
        
        Object[] args = getArguments();
        String id = args[0].toString();
        String FileName = args[1].toString();
        double popValue =  Double.parseDouble(args[2].toString());
        double pvValue = Double.parseDouble(args[3].toString());
        
        reader = new Reader();
        sender = new Sender(id);

        double[][] energies = ReadEnergy(FileName);

        double[] totaldemands = new double[168];
        for (int i=0; i< energies[0].length; i++){
            totaldemands[i] = energies[0][i]*popValue;
        }

        double[] totalPVs = new double[168];
        for (int i=0; i< energies[0].length; i++){
            totalPVs[i] = energies[1][i]*pvValue;
        }

//                double[] totaldemands = energies[0]*popValue;//               *value
//                double[] PVs = energies[1];//                        *PV value

//        addBehaviour(new CyclicBehaviour(){
        addBehaviour(new TickerBehaviour(this, 100){
//           @Override
//           public void action(){
            
            int timestep = 0;
            
            
            @Override
            protected void onTick() {
                
                
                //sendMsg(timestep, "Demand", totaldemands[timestep], id, "_data");
                send(sender.reset().put("timestep",timestep).put("type","Demand").put("value",totaldemands[timestep]).prepare(id+"_data", ACLMessage.INFORM));
                //System.out.println(id + "_meas : measurement sent");
                //sendMsg(timestep, "PV", totalPVs[timestep], id, "_data");
                send(sender.reset().put("timestep",timestep).put("type","PV").put("value",totalPVs[timestep]).prepare(id+"_data", ACLMessage.INFORM));
                //System.out.println(id + "_meas : measurement sent");
                
               
                timestep = (timestep+1)%168;
           }
        });
    }
    
    
    
    public void sendMsg(int timestep, String type, double value, String holon, String agent){
                
        JSONObject JSONObj;

        JSONObj=new JSONObject();

        JSONObj.put("timestep",timestep);//timestep
        JSONObj.put("type",type);//
        JSONObj.put("value",value);//

        String JSONString=JSONValue.toJSONString(JSONObj);

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID(holon + agent, AID.ISLOCALNAME));
        message.setContent(JSONString);
        send(message);
    }
    
    
//    public void prepareData(String EnergyPath, double value) throws InterruptedException{ //String PopulationPath
//        //DataReader reader = new DataReader();
//        
//
//        //double[][] energies = reader.ReadEnergy(EnergyPath);//= "/home/ihab/EDM/final/24-30 2020 prod pv et conso.xlsx"
//        //double[] populations = reader.ReadPopulation(PopulationPath);//= "/home/ihab/EDM/final/population pourcentages.xlsx"
//        
//        
//        double[][] energies = ReadEnergy("/home/ihab/EDM/final/24-30 2020 prod pv et conso.xlsx");//= //reader.
//        //double[] populations = reader.ReadPopulation("/home/ihab/EDM/final/population pourcentages.xlsx");//= 
//
//        
//        double[] totaldemands = energies[0];
//        double[] PVs = energies[1];
//        
//        double[] demands = new double [168];
//        //for(int i = 0; i < 168; i++){
//        //    demands[i] = totaldemands[i] - PVs[i];
//        //}
//        
//        double[][] ixdemands = new double[168][17];
//        
//        for(int i =0; i<168; i++){
//            for (int j = 0; j< 17; j++){
//                 ixdemands[i][j] = demands[i]*populations[j];
//            }
//        }
//        double[][][] stockings = new double[168][17][3];
//        
//        
//        for (int i=0;i<168;i++){
//            for(int j=0; j<17;j++){
//                //cost
//                //max
//                //0s
//                stockings[i][j][0] = Math.random()*2-1;
//                if(j==4) stockings[i][j][1] = 14.9;
//                else{
//                    if(j==14) stockings[i][j][1] = 2;
//                    else stockings[i][j][1] = 0;
//                }
//                stockings[i][j][2] = 0;
//                
//            }
//        }
//    }
    
    
        public double[][] ReadEnergy(String FileName) {
        double[][] arr = new double[2][168];
        int i = 0;
        try {
            FileInputStream file = new FileInputStream(new File(FileName));

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();

                //while (cellIterator.hasNext()) {
                //Cell cell = cellIterator.next();
                //Check the cell type and format accordingly
                //switch (cell.getCellType()) 
                //{
                //    case Cell.CELL_TYPE_NUMERIC:
                //System.out.print(cell.getNumericCellValue() + "t");
                //        break;
                //    case Cell.CELL_TYPE_STRING:
                //System.out.print(cell.getStringCellValue() + "t");
                //        break;
                //}
                //System.out.print(cell.getStringCellValue() + "\t");
                //if(i>168){
                    Cell cell = cellIterator.next();
                    arr[0][i] = cell.getNumericCellValue();
                    //System.out.print(arr[0][i] + "\t");
                    cell = cellIterator.next();
                    arr[1][i] = cell.getNumericCellValue();
                    i++;
                //}
                //System.out.print(arr[1][i] + "\t");

                //System.out.println("");
                if(i==168) break;
            }
            file.close();
            //System.out.println("\n ***************************** "+Arrays.deepToString(arr));
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }
}
