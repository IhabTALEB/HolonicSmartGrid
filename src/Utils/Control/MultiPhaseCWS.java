/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils.Control;

import Utils.Agent.Messaging.Message.Offer;
import Utils.Agent.Messaging.Message.Response;
import Utils.Agent.Messaging.Message.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.algorithm.pso.OMOPSO;
import org.moeaframework.algorithm.single.GeneticAlgorithm;
import org.moeaframework.algorithm.single.LinearDominanceComparator;
import org.moeaframework.algorithm.single.SimulatedAnnealing;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Settings;
import org.moeaframework.core.Solution;
import org.moeaframework.core.initialization.RandomInitialization;
import org.moeaframework.core.spi.OperatorFactory;

/**
 *
 * @author ihab
 */
public class MultiPhaseCWS {
        Offer offer;
        int NumVar;
        int StoStart=0;
        double []econCosts;
        double []envCosts;
        
        Map<String,Integer> IdToIndex;
        ArrayList<String> IndexToId;
        
        double Wecon, Wenv, Weq, Wsto;
        
        Solution bestResult;
        int targetPhase;
        int bestPhase;


        public MultiPhaseCWS(Offer offer) {
            this.offer = offer;
            prepareindices();
            this.Weq=0.75; this.Wenv=0.20; this.Wecon=0.05*0.5; this.Wsto=0.05*0.5;
            bestResult=null;
            targetPhase = 1;
            bestPhase = 0;
            
        }
        public Response decide(Offer offer){
            Response finalResponse = new Response();
                finalResponse = optimize(offer);
            return finalResponse;
        }
        
    
        public Response useAll(Offer offer, double totalGen){
            Response response = new Response("DecisionAlgorithm", "ControlAgent", offer.timestep, 0);
            response.DemandRatio = totalGen / offer.totalDemands;
            response.PVRatio = offer.totalPVs > 0 ? 1 : 0;
            response.WindRatio = offer.totalWinds > 0 ? 1 : 0;
            response.GenNAT = offer.GenNAT;
            response.StoNAT = offer.StoNAT;
            for (Map.Entry<String, Service> entry : offer.GenList.entrySet()) {
                response.GenResp.put(entry.getKey(),entry.getValue().max);
            }
            for (Map.Entry<String, Service> entry : offer.StoList.entrySet()) {//System.out.println("%%testnew"+entry.getKey()+"..."+entry.getValue().min);
                response.StoResp.put(entry.getKey(),entry.getValue().min);
            }
            
            return response;
        }
        
    	public Response optimize(Offer offer) {
            
                this.offer = offer;
                
                boolean stop = false;
                int NumVar = 2+offer.GenList.size()+offer.StoList.size();
                int NumCons = 0;//3
                ArrayList<String> IdList = new ArrayList();
                int tries = 0;
                int triesMax = 10;
                double impactCarbon = 0;

                
                
//            while (!stop){
		CWS problem = new CWS(offer, NumVar, NumCons, IdToIndex, IndexToId, this.Weq, this.Wenv, this.Wecon, this.Wsto);
		
//		OMOPSO algorithm = new OMOPSO(problem, 10000);
//		NSGAII algorithm = new NSGAII(problem);
//		GeneticAlgorithm algorithm = new GeneticAlgorithm(problem);
		GeneticAlgorithm algorithm = new GeneticAlgorithm(problem,
				Settings.DEFAULT_POPULATION_SIZE,
                                //100,
				new LinearDominanceComparator(),
				new RandomInitialization(problem),
				OperatorFactory.getInstance().getVariation(problem));
		//SimulatedAnnealing algorithm = new SimulatedAnnealing(problem);
                algorithm.run(10000);
		
                IdList = problem.getIndices();
                
		algorithm.getResult().display();
               System.out.println("___________________________");
               for(var result : algorithm.getResult().asList())// This should give 1 single result => 1 single iteration
               {
                   double sum=0;
                   double ratioRES=0;
                   double sumSto=0;
                   double ratioSto=0;
                   double maxSto=0;
                   double resultSto=0;
                   double econCost = offer.DemandEconCost*offer.totalDemands;
                   sum += Double.parseDouble(result.getVariable(0).toString()) + Double.parseDouble(result.getVariable(1).toString());
                   for(int j=0; j<IndexToId.size();j++){
                       if(j<StoStart){
                            sum += Double.parseDouble(result.getVariable(j+2).toString());
                           
                            System.out.println("GEN IndexToId.get(j) test "+IndexToId.get(j));
                           
                            econCost += offer.GenList.get(IndexToId.get(j)).econCost + Double.parseDouble(result.getVariable(j+2).toString());
                       }
                       else {
                            sum -= Double.parseDouble(result.getVariable(j+2).toString());
                       
                            System.out.println("STO IndexToId.get(j) test "+IndexToId.get(j));
                       
                            resultSto += Double.parseDouble(result.getVariable(j+2).toString());
                            maxSto += offer.StoList.get(IndexToId.get(j)).max;
                       
                            econCost += offer.StoList.get(IndexToId.get(j)).econCost + Double.parseDouble(result.getVariable(j+2).toString());
                       }
                   }
                    
                    if(offer.totalPVs==0 && offer.totalWinds==0) ratioRES=-1;
                    else ratioRES = (Double.parseDouble(result.getVariable(0).toString()) + Double.parseDouble(result.getVariable(1).toString())) /
                                     offer.totalPVs + offer.totalWinds;
                    
                    ratioSto = resultSto/maxSto;
                    
                    if(sum/offer.totalDemands<0.95){//Phase 1 not achieved (less than 95% demands satisfied
                        if(bestPhase<1){// if got first result then save it 
                            bestResult = result;
                            //bestPhase = 1;
                            targetPhase = 0;
                        }// Wether you have better previous result or not, stop either way
                                stop = true;
                                break;
                    } 
                    else {//Phase 1 achieve
                       if (bestPhase < 2) {
                           bestPhase = 1;
                           bestResult = result;
                       }
                       //CurrentPhase = 2;
                       //updateWeights();
                       if(ratioRES<0.9 && ratioRES!=-1 && (offer.totalPVs + offer.totalWinds) > 500){//phase 2 not achieved
                           if(targetPhase >= 2) {// targetPhase 1 ensured, save best result and move on to targetPhase 2
                                    stop = true;
                                    break;
                           }
                           else{//already have a acceptable phae 2 result, stop and keep old result
                            targetPhase = 2;
                            updateWeights(targetPhase);
                            continue;
                           }
                       }
                       else{//phase 2 achieved
                           if (bestPhase < 3) {
                                bestPhase = 2;
                                bestResult = result;
                           }
                           if(ratioSto>0.9 || maxSto<2000){
                                bestPhase = 3;
                                bestResult = result;
                                stop = true; break;
                           }
                           else{
                               targetPhase = 3;
                               updateWeights(targetPhase);
                               continue;
                           }
                       }
                    }
               }
            System.out.println("-----------------------------------------------------------------------");
            double printsum = Double.parseDouble(bestResult.getVariable(0).toString()) + Double.parseDouble(bestResult.getVariable(1).toString());
            System.out.println(offer.totalDemands);
            System.out.println(offer.totalPVs);
            System.out.println(offer.totalWinds);
            impactCarbon = Double.parseDouble(bestResult.getVariable(0).toString())*offer.PVEnvCost + Double.parseDouble(bestResult.getVariable(1).toString())*offer.WindEnvCost;
            System.out.println("impact carbon " + impactCarbon);
            for(int j=0; j<IndexToId.size();j++){
                   if(j<StoStart){
                       printsum+=Double.parseDouble(bestResult.getVariable(j+2).toString());
                        System.out.println("**GEN "+IndexToId.get(j)+" test "+ bestResult.getVariable(j+2));
                        impactCarbon += Math.abs(Double.parseDouble(bestResult.getVariable(j+2).toString()))*offer.GenList.get(IndexToId.get(j)).envCost;
                        System.out.println("**GEN "+IndexToId.get(j)+" test "+ offer.GenList.get(IndexToId.get(j)).envCost + " and new impact carbon " + impactCarbon);
                   }
                   else {
                       printsum-=Double.parseDouble(bestResult.getVariable(j+2).toString());
                        System.out.println("**STO "+IndexToId.get(j)+" test "+bestResult.getVariable(j+2));
                        impactCarbon += Math.abs(Double.parseDouble(bestResult.getVariable(j+2).toString()))*offer.StoList.get(IndexToId.get(j)).envCost;
                        System.out.println("***STO "+IndexToId.get(j)+" test "+offer.StoList.get(IndexToId.get(j)).envCost + " and new impact carbon " + impactCarbon);
                   }
                   
               }
            
            
            // return result...
            Response response = new Response("DecisionAlgorithm", "ControlAgent", offer.timestep, bestPhase);
            response.GenNAT = offer.GenNAT;
            response.StoNAT = offer.StoNAT;
            
            if(offer.totalPVs>0) response.PVRatio = Double.parseDouble(bestResult.getVariable(0).toString()) / offer.totalPVs;
            else response.PVRatio = 0;
            if(offer.totalWinds>0) response.WindRatio = Double.parseDouble(bestResult.getVariable(1).toString()) / offer.totalWinds;
            else response.WindRatio = 0;
            
            double totalreceived=Double.parseDouble(bestResult.getVariable(0).toString()) + Double.parseDouble(bestResult.getVariable(1).toString());
            
            for(int j=0; j<IndexToId.size();j++){
                       if(j<StoStart){
                           response.GenResp.put(IndexToId.get(j), Double.parseDouble(bestResult.getVariable(j+2).toString()));
                           totalreceived += Double.parseDouble(bestResult.getVariable(j+2).toString());
                       }
                       else{
                           response.StoResp.put(IndexToId.get(j), Double.parseDouble(bestResult.getVariable(j+2).toString()));
                           totalreceived -= Double.parseDouble(bestResult.getVariable(j+2).toString());
                       }
            }
            
            response.DemandRatio = printsum/offer.totalDemands;//totalreceived
            response.ImpactCarbon = impactCarbon/1000000;// gCO2/kWh -> tCO2/kWh
            
            
            return response;
            
        }
        
        
        void prepareindices(){
            IdToIndex = new HashMap();
            IndexToId = new ArrayList();
            int i=0;
            for (Map.Entry<String, Service> entry : offer.GenList.entrySet()) {
                IdToIndex.put(entry.getKey(),i);
                IndexToId.add(entry.getKey());
                i++;
            }
            StoStart = i;
            for (Map.Entry<String, Service> entry : offer.StoList.entrySet()) {
                IdToIndex.put(entry.getKey(),i);
                IndexToId.add(entry.getKey());
                i++;
            }
        }
        
        void updateWeights(int phase){
            //QLearning
            switch(phase){
                case 0:
                    break;
                case 1:
                    this.Weq=1; this.Wenv=0; this.Wecon=0; this.Wsto=0;
                case 2:
                    this.Weq=0.65; this.Wenv=0.35; this.Wecon=0; this.Wsto=0;
                case 3:
                    this.Weq=0.60; this.Wenv=0.30; this.Wecon=0.10*0.5; this.Wsto=0.10*0.5;
            }
        }
        
    
}
