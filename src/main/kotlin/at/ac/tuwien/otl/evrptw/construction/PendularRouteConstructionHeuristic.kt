package at.ac.tuwien.otl.evrptw.construction

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class PendularRouteConstructionHeuristic : IConstructionHeuristic {

    override fun generateSolution(instance: EVRPTWInstance): EVRPTWSolution {
        var totalCost = 0.0

        val remainingCustomers = instance.customers
        val routes = mutableListOf<MutableList<EVRPTWInstance.Node>>()
        for (customer: EVRPTWInstance.Customer in remainingCustomers) {
            val route = PendularRoute(instance)
            route.addCustomer(customer)
            if (!route.tryAddDepot()) {
                var found = false
                for (chargingStation: EVRPTWInstance.RechargingStation in instance.rechargingStations) {
                    if (route.tryAddRechargingStation(chargingStation)) {
                        found = true
                        break
                    }
                }
                if (!found) {
                    println("OOOOHHH SHIIIIT")
                }
                route.tryAddDepot()
            }
            routes.add(route.visitedNodes)
            totalCost += route.currentTravelDistance
        }
        val customerCopy = instance.customers.toMutableList()
        for (route: List<EVRPTWInstance.Node> in routes) {
            for (visitedNode: EVRPTWInstance.Node in route) {
                if (!instance.isDepot(visitedNode) && !instance.isRechargingStation(visitedNode)) {
                    customerCopy.remove(visitedNode as EVRPTWInstance.Customer)
                }
            }
        }

        if (customerCopy.isNotEmpty()) {
            println("WELL DONE")
        }
        return EVRPTWSolution(instance, routes, totalCost)
    }
}

data class PendularRoute(private val instance: EVRPTWInstance) {

    val depot = instance.depot
    var currentCapacity: Double = 0.0
    var currentBatteryCapacity: Double = instance.vehicleEnergyCapacity
    var currentTravelTime: Double = 0.0
    var currentTravelDistance: Double = 0.0
    val visitedNodes: MutableList<EVRPTWInstance.Node> = mutableListOf(depot)

    fun addCustomer(customer: EVRPTWInstance.Customer): Boolean {
        val travelDistance = instance.getTravelDistance(visitedNodes.last(), customer)

        if ((travelDistance * instance.vehicleEnergyConsumption) > instance.vehicleType.energyCapacity) {
            println("AIN'T NOBODY GOT TIME FOR THAT")
        }

        currentTravelDistance += travelDistance

        val travelTime = instance.getTravelTime(visitedNodes.last(), customer)
        currentTravelTime += travelTime
        val waitingTime = instance.getTimewindow(customer).start - currentTravelTime
        if (waitingTime > 0) {
            currentTravelTime += waitingTime
        }
        currentTravelTime += instance.getServiceTime(customer)

        currentCapacity += instance.getDemand(customer)

        currentBatteryCapacity -= instance.vehicleType.energyConsumption * travelDistance

        visitedNodes.add(customer)
        return true
    }

    fun tryAddDepot(): Boolean {
        val travelTime = instance.getTravelTime(visitedNodes.last(), depot)
        if (travelTime * instance.vehicleType.energyConsumption > currentBatteryCapacity) {
            // println("could not add depot (battery violation)")
            return false
        }

        if (travelTime + currentTravelTime > instance.getTimewindow(depot).end) {
            // println("could not add depot (tour duration violation)")
            return false
        }
        // println("insert DEPOT")
        val travelDistance = instance.getTravelDistance(visitedNodes.last(), depot)
        currentTravelDistance += travelDistance
        currentTravelTime += travelTime
        currentBatteryCapacity -= instance.vehicleType.energyConsumption * travelDistance
        visitedNodes.add(depot)
        return true
    }

    fun tryAddRechargingStation(chargingStation: EVRPTWInstance.RechargingStation): Boolean {
        if (depotIsNotReachableWithChargingStation(chargingStation)) {
            return false
        }

        val travelDistance = instance.getTravelDistance(visitedNodes.last(), chargingStation)
        val travelTime = instance.getTravelTime(visitedNodes.last(), chargingStation)
        if (travelTime * instance.vehicleType.energyConsumption > currentBatteryCapacity) {
            // println("could not add recharge (battery violation)")
            return false
        }
        // println("inserted RECHARGE")
        currentTravelDistance += travelDistance
        currentBatteryCapacity -= instance.vehicleType.energyConsumption * travelDistance
        val rechargingTime = (instance.vehicleEnergyCapacity - currentBatteryCapacity) * chargingStation.rechargingRate
        // println("rechargintime: $rechargingTime")
        currentTravelTime += travelTime + rechargingTime
        currentBatteryCapacity = instance.vehicleEnergyCapacity
        visitedNodes.add(chargingStation)
        return true
    }

    private fun depotIsNotReachableWithChargingStation(chargingStation: EVRPTWInstance.RechargingStation): Boolean {
        val travelTimeFromCustomerToRechargeStation = instance.getTravelTime(visitedNodes.last(), chargingStation)
        val travelTimeFromRechargingStationToDepot = instance.getTravelTime(chargingStation, depot)
        // val arrivingTime = currentTravelTime + travelTimeFromCustomerToRechargeStation

        var fullTravelTime =
            currentTravelTime + travelTimeFromCustomerToRechargeStation + travelTimeFromRechargingStationToDepot

        val consumptionUntilStation = travelTimeFromCustomerToRechargeStation * instance.vehicleType.energyConsumption
        val batteryCapacityAtStation = currentBatteryCapacity - consumptionUntilStation

        val rechargingTime =
            (instance.vehicleEnergyCapacity - batteryCapacityAtStation) * chargingStation.rechargingRate

        fullTravelTime += rechargingTime

        if (fullTravelTime > instance.getTimewindow(depot).end) {
            // println("tour duration violation (recharge)")
            return true
        }

        if (travelTimeFromRechargingStationToDepot * instance.vehicleType.energyConsumption > instance.vehicleType.energyCapacity) {
            // println("battery capacity violation (recharge)")
            return true
        }

        return false
    }
}