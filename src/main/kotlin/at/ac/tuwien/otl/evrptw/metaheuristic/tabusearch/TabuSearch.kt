package at.ac.tuwien.otl.evrptw.metaheuristic.tabusearch

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_TABU
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.StationInReExplorer
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.TwoOptArcExchangeExplorer
import at.ac.tuwien.otl.evrptw.verifier.EVRPTWRouteVerifier
import java.util.logging.Logger

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class TabuSearch(private val logEnabled: Boolean = true) {
    private val log: Logger = Logger.getLogger(this.javaClass.name)
    private val explorers = listOf(TwoOptArcExchangeExplorer(), StationInReExplorer())

    fun apply(solution: EVRPTWSolution): EVRPTWSolution {
        var overallBestSolution = EVRPTWSolution(solution) // create deep copy
        var bestCandidate = overallBestSolution
        val tabuMap = mutableMapOf(bestCandidate to 1) // add incoming solution to prevent its reversal
        var iteration = 0

        while (iteration < N_TABU) {
            bestCandidate = bestSolutionOfNeighbourhoods(bestCandidate, tabuMap)
            updateTabuMap(bestCandidate, tabuMap)

            if (evaluateSolution(bestCandidate) < evaluateSolution(overallBestSolution)) {
                overallBestSolution = bestCandidate
            }
            iteration++
        }
        log("Tabu search done")
        return overallBestSolution
    }

    private fun bestSolutionOfNeighbourhoods(
        solution: EVRPTWSolution,
        tabuList: Map<EVRPTWSolution, Int>
    ): EVRPTWSolution {
        val solutionsOfAllNeighbourhoods = mutableListOf<EVRPTWSolution>()
        for (explorer in explorers) {
            solutionsOfAllNeighbourhoods.addAll(explorer.exploreEverySolution(solution))
        }
        return solutionsOfAllNeighbourhoods
            .filter { !tabuList.contains(it) } // todo we should accept a "tabu" solution if it is feasible
            .sortedBy { it.cost }[0]
    }

    private fun updateTabuMap(solution: EVRPTWSolution, tabuMap: MutableMap<EVRPTWSolution, Int>) {
        // remove elements older than some constant
        // don't forget to consider aspiration criteria
        tabuMap
            .filter { it.value >= 15 }
            .keys.toList()
            .forEach { tabuMap.remove(it) }
        tabuMap[solution] = 0
        tabuMap.entries.forEach { tabuMap[it.key] = it.value + 1 }
    }

    private fun evaluateSolution(solution: EVRPTWSolution): Double {
        return EVRPTWRouteVerifier(solution.instance).calculateTotalCost(solution.routes, false)
    }

    private fun log(message: String) {
        if (logEnabled) {
            log.info(message)
        }
    }
}