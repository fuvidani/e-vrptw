package at.ac.tuwien.otl.evrptw.metaheuristic.tabusearch

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_TABU

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class TabuSearch {

    fun apply(solution: EVRPTWSolution): EVRPTWSolution {
        var overallBestSolution = solution
        var bestCandidate = overallBestSolution
        val tabuList = mutableListOf(bestCandidate) // add incoming solution to prevent its reversal
        var iteration = 0
        while (iteration < N_TABU) {
            bestCandidate = bestSolutionOfNeighbourhood(bestCandidate, tabuList)
            tabuList.add(bestCandidate)
            refreshTabuList(tabuList)
            if (evaluateSolution(bestCandidate) < evaluateSolution(overallBestSolution)) {
                overallBestSolution = bestCandidate
            }
            iteration++
        }
        return overallBestSolution
    }

    private fun bestSolutionOfNeighbourhood(solution: EVRPTWSolution, tabuList: List<EVRPTWSolution>): EVRPTWSolution {
        return solution
    }

    private fun refreshTabuList(list: MutableList<EVRPTWSolution>) {
        // remove elements older than some constant
    }

    private fun evaluateSolution(solution: EVRPTWSolution): Double {
        return 42.0
    }
}