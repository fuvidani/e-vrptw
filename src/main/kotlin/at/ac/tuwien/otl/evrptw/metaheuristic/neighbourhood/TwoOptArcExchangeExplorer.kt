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
class TwoOptArcExchangeExplorer :
    INeighbourhoodExplorer<EVRPTWSolution> {

    override fun exploreEverySolution(initialSolution: EVRPTWSolution, startAtIncl: Int, endAtExcl: Int): List<EVRPTWSolution> {
        val result = mutableListOf<EVRPTWSolution>()
        for (routeIndex in startAtIncl until endAtExcl) {
            val route = initialSolution.routes[routeIndex]

            for (secondRouteIndex in (routeIndex + 1) until endAtExcl) {
                val secondRoute = initialSolution.routes[secondRouteIndex]
                for (nodeOfFirstRoute in 1 until route.size - 1) { // start at 1 and end -1 before due to depot
                    for (nodeOfSecondRoute in 1 until secondRoute.size - 1) {
                        val neighbourSolution = performTwoOptExchange(
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
        return result
    }

    private fun performTwoOptExchange(
        solution: EVRPTWSolution,
        firstRouteIndex: Int,
        firstRouteNodeIndex: Int,
        secondRouteIndex: Int,
        secondRouteNoteIndex: Int
    ): EVRPTWSolution {
        val routes = solution.copyOfRoutes()

        val firstRoute = routes[firstRouteIndex]
        val secondRoute = routes[secondRouteIndex]

        val newFirstRoute = firstRoute.subList(0, firstRouteNodeIndex + 1).toList() + secondRoute.subList(
            secondRouteNoteIndex,
            secondRoute.size
        ).toList()
        val newSecondRoute = secondRoute.subList(0, secondRouteNoteIndex).toList() + firstRoute.subList(
            firstRouteNodeIndex + 1,
            firstRoute.size
        ).toList()

        routes[firstRouteIndex].clear()
        routes[firstRouteIndex].addAll(newFirstRoute)

        routes[secondRouteIndex].clear()
        routes[secondRouteIndex].addAll(newSecondRoute)

        if (newFirstRoute.size <= 2) {
            routes.removeAt(firstRouteIndex)
        }
        if (newSecondRoute.size <= 2) {
            routes.removeAt(secondRouteIndex)
        }

        return EVRPTWSolution(solution.instance, routes, Route.calculateTotalDistance(routes, solution.instance))
    }
}