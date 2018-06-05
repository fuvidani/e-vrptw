package at.ac.tuwien.otl.evrptw.construction

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWRouteVerifier
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.dto.Route

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 1.0.0
 * @since 1.0.0
 */
class TimeOrientedNearestNeighbourHeuristic : IConstructionHeuristic {
    override fun generateSolution(instance: EVRPTWInstance): EVRPTWSolution {

        var totalCost = 0.0

        val remainingCustomers = instance.customers.toMutableList()
        val routes = mutableListOf<MutableList<EVRPTWInstance.Node>>()

        var route = Route(instance)
        while (remainingCustomers.isNotEmpty()) {
            val map = calculateMetric(remainingCustomers, route.visitedNodes.last(), instance)
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
//                    println("added route to list")
                    route = Route(instance)
                } else {

                    val insertedStations = mutableListOf<EVRPTWInstance.RechargingStation>()
                    var lastNode: EVRPTWInstance.Customer? = null
                    for (station in newSortedList) {
                        if (route.addNode(station)) {
                            break
                        }
                        lastNode = route.visitedNodes.last() as EVRPTWInstance.Customer
                        route = Route(instance)
                        val newStationSorted = instance.rechargingStations
                                .sortedWith(compareBy({ instance.getTravelDistance(lastNode, it) }))
                        for (newStation in newStationSorted) {
                            if (route.addNode(newStation)) {
                                break
                            }
                        }

                        var insertedCustomer = route.addNode(lastNode)
                        if (insertedCustomer) {
                            lastNode = null
                            insertedStations.clear()
                            break
                        } else {
//                            while (!insertedCustomer) {
//                                if (addStation(instance, lastNode!!,route)) {
//                                    insertedCustomer = true
//                                    lastNode = null
//                                    insertedStations.clear()
//                                }
//                            }
                        }

                    }
                    if (remainingCustomers.isEmpty()) {
                        while (true) {
                            if (route.addNode(instance.depot)) {
                                routes.add(route.visitedNodes)
                                totalCost += route.currentTravelDistance
//                    println("added route to list")
                                route = Route(instance)
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

        return EVRPTWSolution(instance, routes, totalCost)
    }

    private fun addStation(instance : EVRPTWInstance, lastNode: EVRPTWInstance.Node, route: Route) : Boolean {
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

        for (newStation in newSortedList) {
            if (route.addNode(newStation)) {
                break
            }
        }

        return route.addNode(lastNode)
    }

    private fun calculateMetric(customers: List<EVRPTWInstance.Customer>, lastNode: EVRPTWInstance.Node, instance: EVRPTWInstance): Map<EVRPTWInstance.Customer, Double> {
        val map = HashMap<EVRPTWInstance.Customer, Double>()

        customers.forEach { customer -> map.put(customer, calculateDistance(customer, lastNode, instance)) }

        return map
    }

    private fun calculateDistance(customer: EVRPTWInstance.Customer, lastNode: EVRPTWInstance.Node, instance: EVRPTWInstance): Double {
        val delta1 = 0.9
        val delta2 = 0.05
        val delta3 = 0.05

        val dij = instance.getTravelDistance(lastNode, customer)
        val Tij = instance.getTimewindow(customer).start - (instance.getTimewindow(lastNode).start + instance.getServiceTime(lastNode))
        val vij = instance.getTimewindow(customer).start - (instance.getTimewindow(lastNode).start + instance.getServiceTime(lastNode) + instance.getTravelTime(lastNode, customer))

        return delta1 * dij + delta2 * Tij + delta3 * vij
    }
}