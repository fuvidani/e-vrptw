package at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.dto.Route

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class InterIntraRouteRelocateExplorer : INeighbourhoodExplorer<EVRPTWSolution> {
    /**
     * Explores (enumerates) every possible solution in the search space
     * of this neighbourhood based on the initial solution and returns them
     * in a list.
     *
     * @param initialSolution an initial solution for which the neighbours should
     * be calculated
     * @return a list of neighbour solutions, not necessarily sorted
     */
    override fun exploreEverySolution(initialSolution: EVRPTWSolution, startAtIncl: Int, endAtExcl: Int): List<EVRPTWSolution> {
        val result = mutableListOf<EVRPTWSolution>()
        for (routeIndex in startAtIncl until endAtExcl) {
            val route = initialSolution.routes[routeIndex]
            if (route.size > 3) {
                for (secondRouteIndex in routeIndex until endAtExcl) {
                    val secondRoute = initialSolution.routes[secondRouteIndex]
                    for (nodeOfFirstRoute in 1 until route.size - 1) { // start at 1 and end -1 before due to depot
                        for (nodeOfSecondRoute in 1 until secondRoute.size - 1) {
                            if (routeIndex == secondRouteIndex && nodeOfSecondRoute <= nodeOfFirstRoute) {
                                continue
                            }
                            val neighbourSolution = performRelocation(
                                initialSolution,
                                routeIndex,
                                nodeOfFirstRoute,
                                secondRouteIndex,
                                nodeOfSecondRoute
                            )
                            result.add(neighbourSolution)
                        }
                    }
                }
            }
        }
        return result
    }

    private fun performRelocation(
        initialSolution: EVRPTWSolution,
        routeIndex: Int,
        nodeOfFirstRoute: Int,
        secondRouteIndex: Int,
        nodeOfSecondRoute: Int
    ): EVRPTWSolution {
        val routes = initialSolution.copyOfRoutes()

        val node = routes[routeIndex][nodeOfFirstRoute]

        routes[secondRouteIndex].add(nodeOfSecondRoute, node)
        routes[routeIndex].removeAt(nodeOfFirstRoute)

        return EVRPTWSolution(
            initialSolution.instance,
            routes,
            Route.calculateTotalDistance(routes, initialSolution.instance)
        )
    }
}