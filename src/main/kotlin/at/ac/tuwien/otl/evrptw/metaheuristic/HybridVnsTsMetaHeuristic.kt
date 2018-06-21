package at.ac.tuwien.otl.evrptw.metaheuristic

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 1.0.0
 * @since 1.0.0
 */
class HybridVnsTsMetaHeuristic(private val logEnabled: Boolean = true) : IMetaHeuristic {
    private val neighbourhoodStructures = mapOf(
            1 to NeighbourhoodStructure(2, 1),
            2 to NeighbourhoodStructure(2, 2),
            3 to NeighbourhoodStructure(2, 3),
            4 to NeighbourhoodStructure(2, 4),
            5 to NeighbourhoodStructure(2, 5),
            6 to NeighbourhoodStructure(3, 1),
            7 to NeighbourhoodStructure(3, 2),
            8 to NeighbourhoodStructure(3, 3),
            9 to NeighbourhoodStructure(3, 4),
            10 to NeighbourhoodStructure(3, 5),
            11 to NeighbourhoodStructure(4, 1),
            12 to NeighbourhoodStructure(4, 2),
            13 to NeighbourhoodStructure(4, 3),
            14 to NeighbourhoodStructure(4, 4),
            15 to NeighbourhoodStructure(4, 5)
    )

    override fun improveSolution(evrptwSolution: EVRPTWSolution): EVRPTWSolution {

        var bestSolution = evrptwSolution
        var k = 1
        var i = 0
        var feasibilityPhase = true

        val n_dist = 200
        val n_feas = 500
        val n_tabu = 100

        while (feasibilityPhase || (!feasibilityPhase && i < n_dist)) {
            val neighbourhoodStructure = neighbourhoodStructures[k]
            val newSolution = generateRandomPoint(bestSolution, neighbourhoodStructure!!)
            val optimizedNewSolution = applyTabuSearch(newSolution, n_tabu)

            if (acceptSimulatedAnnealing(optimizedNewSolution, bestSolution)) {
                bestSolution = optimizedNewSolution
                k = 1
            } else {
                k = (k % neighbourhoodStructures.size) + 1
            }

            if (feasibilityPhase) {
                if (!feasible(bestSolution)) {
                    if (i == n_feas) {
                        addVehicle(bestSolution)
                        i = -1
                    }
                } else {
                    feasibilityPhase = false
                    i = -1
                }
            }
            i++
        }
        return evrptwSolution
    }

    private fun generateRandomPoint(solution: EVRPTWSolution, neighbourhoodStructure: NeighbourhoodStructure): EVRPTWSolution {
        return solution
    }

    private fun applyTabuSearch(solution: EVRPTWSolution, n_tabu: Int): EVRPTWSolution {
        return solution
    }

    private fun acceptSimulatedAnnealing(optimizedNewSolution: EVRPTWSolution, bestSolution: EVRPTWSolution): Boolean {
        return true
    }

    private fun feasible(solution: EVRPTWSolution): Boolean {
        return true
    }

    private fun addVehicle(solution: EVRPTWSolution) {

    }


}