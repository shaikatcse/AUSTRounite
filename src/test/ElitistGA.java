//  gGA.java
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


import jmetal.core.*;
import jmetal.encodings.variable.Permutation;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveAndConstraintsComparator;
import jmetal.util.comparators.ObjectiveComparator;

import java.lang.annotation.Repeatable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import aust.cse.routine.problem.singleObjective.AUSTCSERoutineSingleObjectiveProblem;

/** 
 * Class implementing a generational genetic algorithm
 */
public class ElitistGA extends Algorithm {
  
 /**
  *
  * Constructor
  * Create a new GGA instance.
  * @param problem Problem to solve.
  */
  public ElitistGA(Problem problem){
    super(problem) ;
  } // GGA
  
  
  
 /**
  * Execute the GGA algorithm
 * @throws JMException 
  */
  public SolutionSet execute() throws JMException, ClassNotFoundException {
    int populationSize ;
    int maxEvaluations ;
    int evaluations    ;

    SolutionSet population          ;
    SolutionSet offspringPopulation ;
    SolutionSet tempPopulation ;

    Operator    mutationOperator  ;
    Operator    crossoverOperator ;
    Operator    selectionOperator ;
    
    Comparator  comparator        ;
    comparator = new ObjectiveAndConstraintsComparator(0) ; // Single objective comparator
    
    // Read the params
    populationSize = ((Integer)this.getInputParameter("populationSize")).intValue();
    maxEvaluations = ((Integer)this.getInputParameter("maxEvaluations")).intValue();                
   
    // Initialize the variables
    population          = new SolutionSet(populationSize) ;   
    offspringPopulation = new SolutionSet(populationSize) ;
    tempPopulation          = new SolutionSet(2*populationSize) ;   
    
    
    
    evaluations  = 0;                

    // Read the operators
    mutationOperator  = this.operators_.get("mutation");
    crossoverOperator = this.operators_.get("crossover");
    selectionOperator = this.operators_.get("selection");  

    // Create the initial solutionSet
    Solution newSolution;
   
   
    for (int i = 0; i < populationSize; i++) {
      newSolution = new Solution(problem_);
      problem_.repair(newSolution);
      problem_.evaluate(newSolution);
      problem_.evaluateConstraints(newSolution);
      evaluations++;
      population.add(newSolution);
    } //for       
     
     
    while (evaluations < maxEvaluations) {
           
      // Reproductive cycle
      for (int i = 0 ; i < (populationSize / 2 ) ; i ++) {
        // Selection
        Solution [] parents = new Solution[2];
        Solution [] offspring;
        
        parents[0] = (Solution)selectionOperator.execute(population);
        parents[1] = (Solution)selectionOperator.execute(population);
 
        // Crossover
        offspring = (Solution []) crossoverOperator.execute(parents);                
          
        // Mutation
        mutationOperator.execute(offspring[0]);
        mutationOperator.execute(offspring[1]);

   
        // Evaluation of the new individual
        problem_.evaluate(offspring[0]);            
        problem_.evaluate(offspring[1]);            
        
      
        problem_.evaluateConstraints(offspring[0]);
        problem_.evaluateConstraints(offspring[1]);
        
        offspringPopulation.add(offspring[0]);
        offspringPopulation.add(offspring[1]);
        
        evaluations +=2;
    
      } // for
      
     // copy all the solution from population and offspringpopulation
      for(int i=0;i<populationSize;i++) {
    	  tempPopulation.add(population.get(i));
      }
      for(int i=0;i<populationSize;i++) {
    	  tempPopulation.add(offspringPopulation.get(i));
      }
      //sort the tempPopulation
      tempPopulation.sort(comparator);
      
      population.clear();
      for (int i = 0; i < populationSize; i++) {
        population.add(tempPopulation.get(i)) ;
      }
      offspringPopulation.clear();
      tempPopulation.clear();
    } // while
    
    // Return a population with the best individual
    SolutionSet resultPopulation = new SolutionSet(1) ;
    resultPopulation.add(population.get(0)) ;
    
    System.out.println("Evaluations: " + evaluations ) ;
    return resultPopulation ;
  } // execute
} // gGA