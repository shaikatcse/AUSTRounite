The results are generated using NSGAII, the smart intilzation and binary tourament.
population size: 300
max Evaluations: 18,00,000
crossover probability: 0.90
mutation probability: 0.20

There is two special files: initP, trackingSoftConstraints
initP: first fornt after initial population
trackingSoftConstraints: contains 3 columns. first column contains the generation, second column contain average soft constraint violation and the last colum contain the number of individuals that violate soft constraints
The question is why the soft constraint violation reported from a perticular generation. The starting generation is the first generation when all hard constriants are met.
FIS_FUN_NSGAII_SC: regular feasible solutions with two objectives, last columns show overall soft constrainst vioation.
SoftConstViolationForDesignation: it shows the constraint violation for each designation for each solution. 1 means one of the courses of the designated teacher is outside of preferred slots.
The experiements are conducted to show that without soft constraint binary tournament selection, the average soft constrint violation is not decreased. 