package at.ac.tuwien.otl.evrptw.metaheuristic.tabusearch

import at.ac.tuwien.otl.evrptw.Executor
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_TABU
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.InterIntraRouteExchangeExplorer
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.StationInReExplorer
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.TwoOptArcExchangeExplorer
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.callable.INeighbourhoodExplorerCallable
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.callable.InterIntraRouteExchangeExplorerCallable
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.callable.StationInReExplorerCallable
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.callable.TwoOptArcExchangeExplorerCallable
import java.util.logging.Logger
import java.util.stream.Collectors

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
                log(
                    "New local optimum found. Cost: ${overallBestSolution.cost}, " +
                            "Cap-Violation: ${overallBestSolution.fitnessValue.totalCapacityViolation}, " +
                            "TW-Violation: ${overallBestSolution.fitnessValue.totalTimeWindowViolation}, " +
                            "Bat-Violation: ${overallBestSolution.fitnessValue.totalBatteryCapacityViolation}"
                )
            }
            iteration++
        }
        log("Tabu search done")
        return overallBestSolution
    }

    private fun bestSolutionOfNeighbourhoods(
        solution: EVRPTWSolution,
        tabuMap: Map<EVRPTWSolution, Int>
    ): EVRPTWSolution {
        val solutionsOfAllNeighbourhoods = parallelExploreNeighbourhoods(solution)

        val solutionsNotInTabu = solutionsOfAllNeighbourhoods.filter { !tabuMap.contains(it) }

        /*val feasibleSolutions = solutionsNotInTabu.filter { it.fitnessValue.fitness == it.cost }

        if (feasibleSolutions.isNotEmpty()) {
            return feasibleSolutions.sortedBy { it.fitnessValue.fitness }.first()
        }*/
        if (solutionsNotInTabu.isEmpty()) {
            log("NO SOLUTIONS AVAILABLE THAT ARE EITHER FEASIBLE OR NOT IN TABU LIST")
            return solution
        }
        return solutionsNotInTabu.sortedBy { it.fitnessValue.fitness }.first()
    }

    private fun parallelExploreNeighbourhoods(solution: EVRPTWSolution): List<EVRPTWSolution> {
        val callableList = mutableListOf<INeighbourhoodExplorerCallable<EVRPTWSolution>>()
        callableList.add(TwoOptArcExchangeExplorerCallable(solution, TwoOptArcExchangeExplorer()))
        callableList.add(StationInReExplorerCallable(solution, StationInReExplorer()))
        callableList.add(InterIntraRouteExchangeExplorerCallable(solution, InterIntraRouteExchangeExplorer()))
        val results = Executor.getExecutorService().invokeAll(callableList)
        return results.stream().flatMap { it.get().stream() }.collect(Collectors.toList()).toList()
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
        return solution.fitnessValue.fitness
    }

    private fun log(message: String) {
        if (logEnabled) {
            log.info(message)
        }
    }
}