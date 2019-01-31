  // Algorithm params
    algorithm.setInputParameter("populationSize",200);
    algorithm.setInputParameter("maxEvaluations",400000);
    
    // Mutation and Crossover for Real codification
    parameters = new HashMap() ;
    parameters.put("probability", 0.90) ;
    crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover", parameters);
    //crossover = CrossoverFactory.getCrossoverOperator("PMXCrossover");
    
    parameters = new HashMap() ;
    parameters.put("probability", 0.1) ;
    mutation = MutationFactory.getMutationOperator("SwapMutation", parameters);                    
  
    /* Selection Operator */
    parameters = null;
    selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;                            
  
run 5 times, FIS_* contains feasible solutions.