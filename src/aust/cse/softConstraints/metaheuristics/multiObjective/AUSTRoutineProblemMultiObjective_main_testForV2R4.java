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

package aust.cse.softConstraints.metaheuristics.multiObjective;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import aust.cse.routine.metaheuristics.singleObjective.geneticAlgorithm.ssGA;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.singleObjective.TSP;
import jmetal.util.JMException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;


import aust.cse.routine.problem.multiObjective.AUSTCSERoutineMultiObjectiveProblem;
import aust.cse.routine.problem.multiObjective.AUSTCSERoutineMultiObjectiveProblemV1;
import aust.cse.routine.problem.multiObjective.withoutDataBase.AUSTCSERoutineMultiObjectiveProblemV2;
import aust.cse.softConstraints.problem.AUSTCSERoutineMultiObjectiveProblemV2WithSC;

/**
 * This class runs a single-objective genetic algorithm (GA). The GA can be 
 * a steady-state GA (class SSGA) or a generational GA (class GGA). The TSP
 * is used to test the algorithms. The data files accepted as in input are from
 * TSPLIB.
 */
public class AUSTRoutineProblemMultiObjective_main_testForV2R4 {

  public static void main(String [] args)  throws FileNotFoundException, 
                                                  IOException, JMException, 
                                                  ClassNotFoundException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
    Operator  selection ;         // Selection operator

    HashMap  parameters ; // Operator parameters
        
    for(int i=6;i<8;i++) {

    	String resultPath=".\\results\\multiObjective\\SmartInitWithSCBinaryTournament\\run"+i;
    problem = new AUSTCSERoutineMultiObjectiveProblemV2WithSC("Permutation");
    
    algorithm = new NSGAII(problem, resultPath);
    //algorithm = new gGA(problem) ;
    
    // Algorithm params
    algorithm.setInputParameter("populationSize",300);
    algorithm.setInputParameter("maxEvaluations",900000);
    
    // Mutation and Crossover for Real codification
    parameters = new HashMap() ;
    parameters.put("probability", 0.90) ;
    crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover", parameters);
    //crossover = CrossoverFactory.getCrossoverOperator("PMXCrossover");
    
    parameters = new HashMap() ;
    parameters.put("probability", 0.2) ;
    mutation = MutationFactory.getMutationOperator("SwapMutation", parameters);                    
  
    /* Selection Operator */
    parameters = null;
    selection = new aust.cse.softConstraints.operators.selection.BinaryTournamentSC(parameters); 
    
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
    population.printObjectivesToFile(resultPath+"\\FUN_NSGAII");
    System.out.println("Variables values have been writen to file VAR");
    population.printVariablesToFile(resultPath+"\\VAR_NSGAII");          
    population.printFeasibleVAR(resultPath+"\\FIS_VAR_NSGAII");
    population.printFeasibleFUN(resultPath+"\\FIS_FUN_NSGAII");
    
   /* System.out.println("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN_NSGAII");
    System.out.println("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR_NSGAII");          
    population.printFeasibleVAR("FIS_VAR_NSGAII");
    population.printFeasibleFUN("FIS_FUN_NSGAII");
    */

}
  }//main
} // TSPGA_main
