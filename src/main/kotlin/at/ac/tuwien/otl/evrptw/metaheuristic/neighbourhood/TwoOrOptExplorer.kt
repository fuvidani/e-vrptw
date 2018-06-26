package at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.dto.Operator
import at.ac.tuwien.otl.evrptw.dto.Route

/**
 * <h4>About this class</h4>
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 26.06.2018
 */
class TwoOrOptExplorer : INeighbourhoodExplorer<EVRPTWSolution> {
    /**
     * Explores (enumerates) every possible solution in the search space
     * of this neighbourhood based on the initial solution and returns them
     * in a list.
     *
     * @param initialSolution an initial solution for which the neighbours should
     * be calculated
     * @return a list of neighbour solutions, not necessarily sorted
     */
    override fun exploreEverySolution(
        initialSolution: EVRPTWSolution,
        startAtIncl: Int,
        endAtExcl: Int
    ): List<EVRPTWSolution> {
        val result = mutableListOf<EVRPTWSolution>()
        for (routeIndex in startAtIncl until endAtExcl) {
            val route = initialSolution.routes[routeIndex]
            if (route.size >= 5) {
                for (nodeIndex in 1 until route.size - 2) {
                    if (route[nodeIndex] is EVRPTWInstance.Customer && route[nodeIndex + 1] is EVRPTWInstance.Customer) {
                        if (nodeIndex > 1) {
                            for (nodeBefore in 1 until nodeIndex) {
                                result.add(performOrOptBackwards(initialSolution, routeIndex, nodeIndex, nodeBefore))
                            }
                        }
                        if (nodeIndex + 1 != route.size - 2) {
                            for (nodeAfter in nodeIndex + 2 until route.size - 1) {
                                result.add(performOrOptForwards(initialSolution, routeIndex, nodeIndex, nodeAfter))
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    private fun performOrOptBackwards(solution: EVRPTWSolution, routeIndex: Int, indexOfChainFirstNode: Int, insertBeforeIndex: Int): EVRPTWSolution {
        val routes = solution.copyOfRoutes()
        val route = routes[routeIndex]
        val node1 = route[indexOfChainFirstNode]
        val node2 = route[indexOfChainFirstNode + 1]
        val chain = listOf(node1, node2)
        route.removeAll(chain)
        route.addAll(insertBeforeIndex, chain)

        return EVRPTWSolution(
                solution.instance,
                routes,
                Route.calculateTotalDistance(routes, solution.instance),
                Operator.TWO_OR_OPT
        )
    }

    private fun performOrOptForwards(solution: EVRPTWSolution, routeIndex: Int, indexOfChainFirstNode: Int, insertAfterIndex: Int): EVRPTWSolution {
        val routes = solution.copyOfRoutes()
        val route = routes[routeIndex]
        val node1 = route[indexOfChainFirstNode]
        val node2 = route[indexOfChainFirstNode + 1]
        val chain = listOf(node1, node2)
        route.removeAll(chain)
        route.addAll(insertAfterIndex - 1, chain)

        return EVRPTWSolution(
                solution.instance,
                routes,
                Route.calculateTotalDistance(routes, solution.instance),
                Operator.TWO_OR_OPT
        )
    }
}