package at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
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
class InterIntraRouteExchangeExplorer : INeighbourhoodExplorer<EVRPTWSolution> {

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

            for (secondRouteIndex in routeIndex until endAtExcl) {
                val secondRoute = initialSolution.routes[secondRouteIndex]
                for (nodeOfFirstRoute in 1 until route.size - 1) { // start at 1 and end -1 before due to depot
                    if (route[nodeOfFirstRoute] is EVRPTWInstance.RechargingStation) {
                        // skip exchange for Recharging stations
                        continue
                    }
                    for (nodeOfSecondRoute in 1 until secondRoute.size - 1) {
                        if (secondRoute[nodeOfSecondRoute] is EVRPTWInstance.RechargingStation) {
                            // skip exchange for Recharging stations
                            continue
                        }
                        if (secondRouteIndex != routeIndex || nodeOfFirstRoute != nodeOfSecondRoute) {
                            val neighbourSolution = performNodeSwap(
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

    private fun performNodeSwap(
        initialSolution: EVRPTWSolution,
        routeIndex: Int,
        nodeOfFirstRoute: Int,
        secondRouteIndex: Int,
        nodeOfSecondRoute: Int
    ): EVRPTWSolution {
        val routes = initialSolution.copyOfRoutes()

        val node1 = routes[routeIndex][nodeOfFirstRoute]
        val node2 = routes[secondRouteIndex][nodeOfSecondRoute]

        routes[routeIndex][nodeOfFirstRoute] = node2
        routes[secondRouteIndex][nodeOfSecondRoute] = node1

        return EVRPTWSolution(
            initialSolution.instance,
            routes,
            Route.calculateTotalDistance(routes, initialSolution.instance)
        )
    }
}