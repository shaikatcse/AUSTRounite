//  TSPGA_main.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package aust.cse.routine.main;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import aust.cse.routine.metaheuristics.singleObjective.geneticAlgorithm.ElitistGA;

import aust.cse.routine.problem.singleObjective.AUSTCSERoutineSingleObjectiveProblem;
import aust.cse.routine.problem.singleObjective.AUSTCSERoutineSingleObjectiveProblemV1;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 * This class runs a single-objective genetic algorithm (GA). The GA can be 
 * a steady-state GA (class SSGA) or a generational GA (class GGA). The TSP
 * is used to test the algorithms. The data files accepted as in input are from
 * TSPLIB.
 */
public class AUSTRoutineProblem_main {

  public static void main(String [] args)  throws FileNotFoundException, 
                                                  IOException, JMException, 
                                                  ClassNotFoundException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
    Operator  selection ;         // Selection operator

    HashMap  parameters ; // Operator parameters
        
    
    for(int i=0;i<5;i++) {
    problem = new AUSTCSERoutineSingleObjectiveProblemV1("Permutation");
    
    algorithm = new ElitistGA(problem, ".\\results\\singleObjective\\singleObjetiveGeneration_"+i);
    //algorithm = new gGA(problem) ;
    
    // Algorithm params
    algorithm.setInputParameter("populationSize",200);
    algorithm.setInputParameter("maxEvaluations",500000);
    
    // Mutation and Crossover for Real codification
    parameters = new HashMap() ;
    parameters.put("probability", 0.95) ;
    crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover", parameters);
    //crossover = CrossoverFactory.getCrossoverOperator("PMXCrossover");
    
    parameters = new HashMap() ;
    parameters.put("probability", 0.1) ;
    mutation = MutationFactory.getMutationOperator("SwapMutation", parameters);                    
  
    /* Selection Operator */
    parameters = null;
    selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;                            
    
    /* Add the operators to the algorithm*/
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);

    /* Execute the Algorithm */
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;
    System.out.println("Total time of execution: "+estimatedTime);

    /* Log messages */
    System.out.println("Objectives values have been writen to file FUN");
    population.printObjectivesToFile(".\\results\\singleobjective\\FUN_Elitist_"+i);
    System.out.println("Variables values have been writen to file VAR");
    population.printVariablesToFile(".\\results\\singleobjective\\VAR_Elitist_"+i);
    
    population.printFeasibleFUN(".\\results\\singleobjective\\FIS_FUN_Elitist"+i);
    population.printFeasibleVAR(".\\results\\singleobjective\\FIS_VAR_Elitist"+i);
    
    }
  }//main
} // TSPGA_main
