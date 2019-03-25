The results are generated using NSGAII, the smart intilzation and soft constraints binary tourament.
population size: 300
max Evaluations: 12,00,000
crossover probability: 0.90
mutation probability: 0.20

There is two special files: initP, trackingSoftConstraints
initP: first fornt after initial population
trackingSoftConstraints: contains 3 columns. first column contains the generation, second column contain average soft constraint violation and the last colum contain the number of individuals that violate soft constraints
The question is why the soft constraint violation reported from a perticular generation. The starting generation is the first generation when all hard constriants are met.
