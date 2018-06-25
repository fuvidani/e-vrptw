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
class StationInReExplorer : INeighbourhoodExplorer<EVRPTWSolution> {

    override fun exploreEverySolution(initialSolution: EVRPTWSolution, startAtIncl: Int, endAtExcl: Int): List<EVRPTWSolution> {
        val result = mutableListOf<EVRPTWSolution>()
        for (routeIndex in startAtIncl until endAtExcl) {
            val route = initialSolution.routes[routeIndex]
            for (nodeIndex in 1 until route.size) {
                if (route[nodeIndex] is EVRPTWInstance.RechargingStation && route.size > 3) {
                    result.add(performStationRemoval(initialSolution, routeIndex, nodeIndex))
                } else {
                    if (route[nodeIndex - 1] !is EVRPTWInstance.RechargingStation) {
                        for (station in initialSolution.instance.rechargingStations) {
                            val neighbourSolution = performStationInsertion(initialSolution, routeIndex, nodeIndex, station)
                            result.add(neighbourSolution)
                        }
                    }
                }
            }
        }
        return result
    }

    private fun performStationRemoval(
        initialSolution: EVRPTWSolution,
        routeIndex: Int,
        nodeIndex: Int
    ): EVRPTWSolution {
        val routes = initialSolution.copyOfRoutes()
        routes[routeIndex].removeAt(nodeIndex)
        return EVRPTWSolution(
                initialSolution.instance,
                routes,
                Route.calculateTotalDistance(routes, initialSolution.instance)
        )
    }

    private fun performStationInsertion(
        initialSolution: EVRPTWSolution,
        routeIndex: Int,
        nodeIndex: Int,
        station: EVRPTWInstance.RechargingStation
    ): EVRPTWSolution {
        val routes = initialSolution.copyOfRoutes()
        val stationToInsert = EVRPTWInstance.RechargingStation(
                station.id,
                station.name,
                station.location.x,
                station.location.y,
                station.timeWindow.start,
                station.timeWindow.end,
                station.rechargingRate
        )

        routes[routeIndex].add(nodeIndex, stationToInsert)
        return EVRPTWSolution(
                initialSolution.instance,
                routes,
                Route.calculateTotalDistance(routes, initialSolution.instance)
        )
    }
}