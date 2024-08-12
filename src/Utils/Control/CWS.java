/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils.Control;

import Utils.Agent.Messaging.Message.Offer;
import Utils.Agent.Messaging.Message.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.moeaframework.core.Constraint;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

/**
 *
 * @author ihab
 */
public class CWS extends AbstractProblem {
    
                int NumVar, NumCons;
                Offer offer;
                ArrayList<String> IdList;
                double Fecon, Fenv, Feq, Fcws;
                double Fecon_min, Fenv_min, Feq_min, Fecon_max, Fenv_max, Feq_max;
                double Fecon_norm, Fenv_norm, Feq_norm, Fsto_norm;
                double Wecon, Wenv, Weq, Wsto;
                
                Map<String,Integer> IdToIndex;//DOES IT REQUIRE QN INSTANCE ??
                ArrayList<String> IndexToId;//DOES IT EQUIRE A INSTANCE ??
    
		public CWS(Offer offer, int NumVar, int NumCons, Map<String,Integer> IdToIndex, ArrayList<String> IndexToId, double Weq, double Wenv, double Wecon, double Wsto) {//int NumVar, int NumObj, int Const
                    //super(2, 2, 2);
                    super(2+offer.GenList.size()+offer.StoList.size(), 1, NumCons);//decision variables, objectives, and constraints
                    this.offer = offer;
                    this.NumVar = 2+offer.GenList.size()+offer.StoList.size();// 2 instead of 4//(DemandSatisfied adn Load Curtailement), number of thermal generators, number of storage... ALSO ADDED : PV AND WIND
                    this.NumCons = NumCons;//this.NumVar;
                    this.IdToIndex = IdToIndex;
                    this.IndexToId = IndexToId;
                    this.Wecon = Wecon;
                    this.Wenv = Wenv;
                    this.Weq = Weq;
                    this.Wsto = Wsto;
		}
                
                
		@Override
		public void evaluate(Solution solution) {
                    
                        
                    IdList = new ArrayList();
                        double[] x = EncodingUtils.getReal(solution);

                        double DemandCost = (0.364*1000000)/(365*3600);//demand cost
                        
                        double Fecon = -DemandCost*offer.totalDemands + offer.PVEconCost*x[0] + offer.WindEconCost*x[1];// Demand and load shifting
                        double Fecon_min = -DemandCost*offer.totalDemands + offer.PVEconCost*0 + offer.WindEconCost*0;// Demand and load shifting
                        double Fecon_max = -DemandCost*offer.totalDemands + offer.PVEconCost*offer.totalPVs + offer.WindEconCost*offer.totalWinds;// Demand and load shifting
                        
                        double Fenv = offer.PVEnvCost*x[0] + offer.WindEnvCost*x[1];
                        double Fenv_min = offer.PVEnvCost*0 + offer.WindEnvCost*0;
                        double Fenv_max = offer.PVEnvCost*offer.totalPVs + offer.WindEnvCost*offer.totalWinds;
                        
                        double Feq = -offer.totalDemands+x[0]+x[1];
                        double Feq_min = -offer.totalDemands+0+0;
                        double Feq_max = -offer.totalDemands+offer.totalPVs+offer.totalWinds;
                        
                        double Fsto=0;
                        double Fsto_min=0;
                        double Fsto_max=0;
                        
                        double ceq;
                        double cres = (offer.totalPVs + offer.totalWinds) > 0 ? (x[0] + x[1])/(offer.totalPVs+offer.totalWinds) : 1;
                        
                        double maxprod = offer.totalPVs + offer.totalWinds;
                        double maxsto = 0;
                        double minsto = 0;
                        double csto=0;
                        
                        
                        double cvar;
                        
                        
                        ceq = -offer.totalDemands + offer.totalPVs + offer.totalWinds;//-offer.totalDemands + 
                            
                        int i=0;//0 for PVs and 1 for WInds... THus, add 2 to all indices
                        //int j=0;
                        for (Map.Entry<String, Service> entry : offer.GenList.entrySet()) {
                            i = 2 + IdToIndex.get(entry.getKey());
                            Fecon += entry.getValue().econCost*x[i];
                            Fecon_min += entry.getValue().econCost*entry.getValue().min;
                            Fecon_max += entry.getValue().econCost*entry.getValue().max;
                            Fenv += entry.getValue().envCost*x[i];
                            Fenv_min += entry.getValue().envCost*entry.getValue().min;
                            Fenv_max += entry.getValue().envCost*entry.getValue().max;
                            Feq += x[i];
                            Feq_min += entry.getValue().min;
                            Feq_max += entry.getValue().max;
                            ceq -= x[i];
                            maxprod += entry.getValue().max;
                        }

                        for (Map.Entry<String, Service> entry : offer.StoList.entrySet()) {
                                    i = 2 + IdToIndex.get(entry.getKey());                                    
                                    Fecon -= entry.getValue().econCost*x[i];
                                    Fecon_min -= entry.getValue().econCost*entry.getValue().max;
                                    Fecon_max -= entry.getValue().econCost*entry.getValue().min;
                                    Fenv -= entry.getValue().envCost*x[i];
                                    Fenv_min -= entry.getValue().envCost*entry.getValue().max;
                                    Fenv_max -= entry.getValue().envCost*entry.getValue().min;
                                    Feq -= x[i];
                                    Feq_min -= entry.getValue().max;
                                    Feq_max -= entry.getValue().min;
                                    ceq -= x[i];
                                    csto += x[i];
                                    maxsto += entry.getValue().max;
                                    minsto += entry.getValue().min;
                                    Fsto-=x[i];
                                    Fsto_min -= entry.getValue().max;
                                    Fsto_max -= entry.getValue().min;
                        }
                        Feq = Math.abs(Feq);
                        Feq_norm = (Feq - Feq_min)/(Feq_max - Feq_min);
                        Fenv_norm = (Fenv - Fenv_min)/(Fenv_max - Fenv_min);
                        Fecon_norm = (Fecon - Fecon_min)/(Fecon_max - Fecon_min);
                        Fsto_norm = (Fsto - Fsto_min)/(Fsto_max - Fsto_min);

                        Fcws = Weq*Feq_norm + Wenv*Fenv_norm + Wecon*Fecon_norm + Wsto*Fsto_norm;
                        
                        solution.setObjective(0, Fcws);
		}

		@Override
		public Solution newSolution() {
			Solution solution = new Solution(NumVar, 1, NumCons);

			solution.setVariable(0, new RealVariable(0.0, offer.totalPVs));
			solution.setVariable(1, new RealVariable(0.0, offer.totalWinds));
                        
                        int i=2;//0 for PVs and 1 for Winds... thus, add 2 to all indices
                        for (Map.Entry<String, Service> entry : offer.GenList.entrySet()) {
                                    i = 2+IdToIndex.get(entry.getKey());
                                    solution.setVariable(i, new RealVariable(entry.getValue().min, entry.getValue().max));
                                    i++;
                        }

                        for (Map.Entry<String, Service> entry : offer.StoList.entrySet()) {
                                    i = 2+IdToIndex.get(entry.getKey());                                    
                                    solution.setVariable(i, new RealVariable(entry.getValue().min, entry.getValue().max));
                                    i++;
                        }
			return solution;
		}
                
                
                public ArrayList getIndices(){
                    return IdList;
                }
		
}
