package at.ac.tuwien.otl.evrptw.metaheuristic

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.dto.NeighbourhoodStructure
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_DIST
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_FEAS
import at.ac.tuwien.otl.evrptw.metaheuristic.tabusearch.TabuSearch
import at.ac.tuwien.otl.evrptw.verifier.EVRPTWRouteVerifier
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
    private val neighbourSolutionGenerator = ShakingNeighbourSolutionGenerator()
    private val tabuSearch = TabuSearch(logEnabled)

    override fun improveSolution(evrptwSolution: EVRPTWSolution): EVRPTWSolution {
        var bestSolution = evrptwSolution
        var k = 1
        var i = 0
        var feasibilityPhase = true

        while (feasibilityPhase || (!feasibilityPhase && i < N_DIST)) {
            val newSolution = neighbourSolutionGenerator.generateRandomPoint(bestSolution, k)
            val optimizedNewSolution = tabuSearch.apply(newSolution)

            if (acceptSimulatedAnnealing(optimizedNewSolution, bestSolution)) {
                bestSolution = optimizedNewSolution
                k = 1
            } else {
                k = (k % NeighbourhoodStructure.STRUCTURES.size) + 1
            }

            if (feasibilityPhase) {
                if (!feasible(bestSolution)) {
                    if (i == N_FEAS) {
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

        return bestSolution
    }

    private fun acceptSimulatedAnnealing(optimizedNewSolution: EVRPTWSolution, bestSolution: EVRPTWSolution): Boolean {
        return true
    }

    private fun feasible(solution: EVRPTWSolution): Boolean {
        // return solution.fitnessValue.fitness == solution.cost
        return EVRPTWRouteVerifier(solution.instance).verify(solution.routes, solution.cost, false)
    }

    private fun addVehicle(solution: EVRPTWSolution) {
    }

    private fun log(message: String) {
        if (logEnabled) {
            log.info(message)
        }
    }
}