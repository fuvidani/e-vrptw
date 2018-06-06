package at.ac.tuwien.otl.evrptw.construction

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.dto.Route
import java.util.logging.Logger

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 1.0.0
 * @since 1.0.0
 */
class TimeOrientedNearestNeighbourHeuristic(private val logEnabled: Boolean = true) : IConstructionHeuristic {

    private val log: Logger = Logger.getLogger(this.javaClass.name)

    override fun generateSolution(instance: EVRPTWInstance): EVRPTWSolution {
        val solutions = mutableListOf<EVRPTWSolution>()
        for (deltas in listOf(listOf(0.9, 0.05, 0.05), listOf(0.99, 0.005, 0.005))) {
            var totalCost = 0.0
            val remainingCustomers = instance.customers.toMutableList()
            val routes = mutableListOf<MutableList<EVRPTWInstance.Node>>()

            var route = Route(instance, logEnabled)
            while (remainingCustomers.isNotEmpty()) {
                val map = calculateMetric(remainingCustomers, route.visitedNodes.last(), instance, deltas)
                val sortedMap = map.toList().sortedBy { (_, value) -> value }.toMap()

                var inserted = false
                for (customer in sortedMap.keys.toList()) {
                    if (route.addNode(customer)) {
                        inserted = true
                        remainingCustomers.remove(customer)
                        break
                    }
                }

                if (!inserted || remainingCustomers.isEmpty()) {
                    val sortedList = instance.rechargingStations
                            .sortedWith(compareBy({ instance.getTravelDistance(instance.depot, it) }))

                    var index = -1
                    for (node: EVRPTWInstance.Node in route.visitedNodes.reversed()) {
                        if (!instance.isRechargingStation(node)) {
                            index = route.visitedNodes.indexOf(node)
                            break
                        }
                    }
                    val blacklistStations = route.visitedNodes.subList(index + 1, route.visitedNodes.size)
                    val newSortedList = sortedList.filter {
                        !blacklistStations.contains(it)
                    }

                    if (route.addNode(instance.depot)) {
                        routes.add(route.visitedNodes)
                        totalCost += route.currentTravelDistance
                        log("added route to list")
                        route = Route(instance, logEnabled)
                    } else {

                        for (station in newSortedList) {
                            if (route.addNode(station)) {
                                break
                            }
                            val lastNode = route.visitedNodes.last() as EVRPTWInstance.Customer
                            route = Route(instance, logEnabled)
                            val newStationSorted = instance.rechargingStations
                                    .sortedWith(compareBy({ instance.getTravelDistance(lastNode, it) }))
                            for (newStation in newStationSorted) {
                                if (route.addNode(newStation)) {
                                    break
                                }
                            }

                            if (route.addNode(lastNode)) {
                                break
                            } else {
                                route.addNodeToRoute(lastNode)
                            }
                        }
                        if (remainingCustomers.isEmpty()) {
                            while (true) {
                                if (route.addNode(instance.depot)) {
                                    routes.add(route.visitedNodes)
                                    totalCost += route.currentTravelDistance
                                    log("added route to list")
                                    route = Route(instance, logEnabled)
                                    break
                                } else {
                                    val newStationSorted = instance.rechargingStations
                                            .sortedWith(compareBy({ instance.getTravelDistance(route.visitedNodes.last(), it) }))
                                    for (newStation in newStationSorted) {
                                        if (route.addNode(newStation)) {
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            solutions.add(EVRPTWSolution(instance, routes, totalCost))
        }

        return solutions.minBy { it.cost }!!
    }

    private fun calculateMetric(customers: List<EVRPTWInstance.Customer>, lastNode: EVRPTWInstance.Node, instance: EVRPTWInstance, deltas: List<Double>): Map<EVRPTWInstance.Customer, Double> {
        val map = HashMap<EVRPTWInstance.Customer, Double>()
        customers.forEach { customer -> map[customer] = calculateDistance(customer, lastNode, instance, deltas) }
        return map
    }

    private fun calculateDistance(customer: EVRPTWInstance.Customer, lastNode: EVRPTWInstance.Node, instance: EVRPTWInstance, deltas: List<Double>): Double {
        val delta1 = deltas[0]
        val delta2 = deltas[1]
        val delta3 = deltas[2]

        val dij = instance.getTravelDistance(lastNode, customer)
        val tij =
                instance.getTimewindow(customer).start - (instance.getTimewindow(lastNode).start + instance.getServiceTime(
                        lastNode
                ))
        val vij = instance.getTimewindow(customer).start - (instance.getTimewindow(lastNode).start + instance.getServiceTime(lastNode) + instance.getTravelTime(lastNode, customer))

        return delta1 * dij + delta2 * tij + delta3 * vij
    }

    private fun log(message: String) {
        if (logEnabled) {
            log.info(message)
        }
    }
}