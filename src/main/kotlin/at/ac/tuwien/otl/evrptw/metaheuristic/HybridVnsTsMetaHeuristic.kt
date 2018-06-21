package at.ac.tuwien.otl.evrptw.metaheuristic

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import java.util.*
import java.util.logging.Logger

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 1.0.0
 * @since 1.0.0
 */
class HybridVnsTsMetaHeuristic(private val logEnabled: Boolean = true) : IMetaHeuristic {
    private val log: Logger = Logger.getLogger(this.javaClass.name)
    private val random: Random = Random(123456)
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
        val routes: MutableList<MutableList<EVRPTWInstance.Node>> = mutableListOf()

        if (solution.routes.size < neighbourhoodStructure.numberOfInvolvedRoutes) {
            log("ERROR: fewer routes than involved routes")
        }

        for (i in 0 until neighbourhoodStructure.numberOfInvolvedRoutes) {
            val index = random.nextInt(solution.routes.size)

            routes.add(solution.routes[index])
            solution.routes.removeAt(index)
        }

        val exchangeSequences = mutableListOf<ExchangeSequence>()

        for (route in routes) {
            val upperBound = Math.min(neighbourhoodStructure.maxVertices, route.size - 2)
            val numberOfSuccessiveVertices = if (upperBound == 1) {
                1
            } else {
                random.nextInt(upperBound) + 1
            }
            val startIndex = random.nextInt((route.size - numberOfSuccessiveVertices - 1)) + 1

            val exchangeSequence = ExchangeSequence(route.subList(startIndex, startIndex + numberOfSuccessiveVertices), startIndex)
            exchangeSequences.add(exchangeSequence)

            route.subList(startIndex, startIndex + numberOfSuccessiveVertices).clear()
        }

        for (i in 0 until routes.size) {
            val startIndex = exchangeSequences[i].startIndex
            val sequence: List<EVRPTWInstance.Node> = if (i == 0) {
                exchangeSequences[exchangeSequences.size - 1].nodes
            } else {
                exchangeSequences[i - 1].nodes
            }

            routes[i].addAll(startIndex, sequence)
        }

        solution.routes.addAll(routes)

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

    private fun log(message: String) {
        if (logEnabled) {
            log.info(message)
        }
    }
}