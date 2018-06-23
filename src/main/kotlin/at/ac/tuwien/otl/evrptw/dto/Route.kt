package at.ac.tuwien.otl.evrptw.dto

import java.util.logging.Logger

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 0.1.0
 * @since 0.1.0
 */
class Route(
    private val instance: EVRPTWInstance,
    private val logEnabled: Boolean = true
) {
    private val log: Logger = Logger.getLogger(this.javaClass.name)
    private val depot = instance.depot
    private var currentCapacity: Double = 0.0
    private var currentBatteryCapacity: Double = instance.vehicleEnergyCapacity
    private var currentTravelTime: Double = 0.0
    var currentTravelDistance: Double = 0.0
    val visitedNodes: MutableList<EVRPTWInstance.Node> = mutableListOf(depot)

    fun addNode(node: EVRPTWInstance.Node): Boolean {
        if (instance.isRechargingStation(node)) {
            return addRechargeToRoute(node as EVRPTWInstance.RechargingStation)
        } else if (instance.isDepot(node)) {
            return addDepotToRoute(node as EVRPTWInstance.Depot)
        } else if (!nodeViolatesConstraints(node)) {
            log("insert node $node")

            return addNodeToRoute(node as EVRPTWInstance.Customer)
        }
        return false
    }

    private fun nodeViolatesConstraints(node: EVRPTWInstance.Node): Boolean {
        // check capacity constraint
        if (currentCapacity + instance.getDemand(node) > instance.vehicleCapacity) {
            log("load capacity violation")
            return true
        }
        // check if arrival in time window
        val travelTimeFromLastToNode = instance.getTravelTime(visitedNodes.last(), node)
        val travelTimeSum = currentTravelTime + travelTimeFromLastToNode

        if (travelTimeSum > instance.getTimewindow(node).end) {
            log("time window violation")
            return true
        }

        if (visitedNodes.size > 1) {
            if (isDepotUnReachable(node)) return true
            if (isRechargeStationUnReachable(node)) return true
        }

        return false
    }

    private fun addDepotToRoute(depot: EVRPTWInstance.Depot): Boolean {
        val travelTime = instance.getTravelTime(visitedNodes.last(), depot)
        if (travelTime * instance.vehicleType.energyConsumption > currentBatteryCapacity) {
            log("could not add depot (battery violation)")
            return false
        }

        if (travelTime + currentTravelTime > instance.getTimewindow(depot).end) {
            log("could not add depot (tour duration violation)")
            return false
        }

        log("insert DEPOT")
        val travelDistance = instance.getTravelDistance(visitedNodes.last(), depot)
        currentTravelDistance += travelDistance
        currentTravelTime += travelTime
        currentBatteryCapacity -= instance.vehicleType.energyConsumption * travelDistance
        visitedNodes.add(depot)
        return true
    }

    private fun addRechargeToRoute(rechargeStation: EVRPTWInstance.RechargingStation): Boolean {
        val travelDistance = instance.getTravelDistance(visitedNodes.last(), rechargeStation)
        val travelTime = instance.getTravelTime(visitedNodes.last(), rechargeStation)
        if (travelTime * instance.vehicleType.energyConsumption > currentBatteryCapacity) {
            log("could not add recharge (battery violation)")
            return false
        }

        log("inserted RECHARGE")
        currentTravelDistance += travelDistance
        currentBatteryCapacity -= instance.vehicleType.energyConsumption * travelDistance
        val rechargingTime = (instance.vehicleEnergyCapacity - currentBatteryCapacity) * rechargeStation.rechargingRate
        log("rechargintime: $rechargingTime")
        currentTravelTime += travelTime + rechargingTime
        currentBatteryCapacity = instance.vehicleEnergyCapacity
        visitedNodes.add(rechargeStation)
        return true
    }

    private fun isDepotUnReachable(customer: EVRPTWInstance.Node): Boolean {
        val travelTimeFromLastNodeToCustomer = instance.getTravelTime(visitedNodes.last(), customer)
        val travelTimeFromCustomerToDepot = instance.getTravelTime(customer, depot)
        val customerServiceTime = instance.getServiceTime(customer)
        val arrivingTime = currentTravelTime + travelTimeFromLastNodeToCustomer

        var fullTravelTime = currentTravelTime + travelTimeFromLastNodeToCustomer + customerServiceTime + travelTimeFromCustomerToDepot
        val waitingTime = instance.getTimewindow(customer).start - arrivingTime
        if (waitingTime > 0) {
            fullTravelTime += waitingTime
        }

        if (fullTravelTime > instance.getTimewindow(depot).end) {
            log("tour duration violation (depot)")
            return true
        }

        if ((travelTimeFromLastNodeToCustomer + travelTimeFromCustomerToDepot) * instance.vehicleType.energyConsumption > currentBatteryCapacity) {
            log("battery capacity violation (depot)")
            return true
        }
        return false
    }

    private fun isRechargeStationUnReachable(customer: EVRPTWInstance.Node): Boolean {
        val sortedStations = instance.rechargingStations.sortedWith(compareBy({ instance.getTravelDistance(customer, it) }))
        val nearestStation = sortedStations.first { it.id != visitedNodes.last().id }
        val travelTimeFromLastNodeToCustomer = instance.getTravelTime(visitedNodes.last(), customer)
        val travelTimeFromCustomerToRechargeStation = instance.getTravelTime(customer, nearestStation)
        val travelTimeFromRechargingStationToDepot = instance.getTravelTime(nearestStation, depot)
        val customerServiceTime = instance.getServiceTime(customer)
        val arrivingTime = currentTravelTime + travelTimeFromLastNodeToCustomer

        var fullTravelTime = currentTravelTime + travelTimeFromLastNodeToCustomer + customerServiceTime + travelTimeFromCustomerToRechargeStation + travelTimeFromRechargingStationToDepot
        val waitingTime = instance.getTimewindow(customer).start - arrivingTime
        if (waitingTime > 0) {
            fullTravelTime += waitingTime
        }

        val consumptionUntilStation = (travelTimeFromLastNodeToCustomer + travelTimeFromCustomerToRechargeStation) * instance.vehicleType.energyConsumption
        val batteryCapacityAtStation = currentBatteryCapacity - consumptionUntilStation

        val rechargingTime = (instance.vehicleEnergyCapacity - batteryCapacityAtStation) * nearestStation.rechargingRate

        fullTravelTime += rechargingTime

        if (fullTravelTime > instance.getTimewindow(depot).end) {
            log("tour duration violation (recharge)")
            return true
        }

        if (travelTimeFromRechargingStationToDepot * instance.vehicleType.energyConsumption > instance.vehicleType.energyCapacity) {
            log("battery capacity violation (recharge)")
            return true
        }

        return false
    }

    fun addNodeToRoute(node: EVRPTWInstance.Customer): Boolean {
        val travelDistance = instance.getTravelDistance(visitedNodes.last(), node)
        currentTravelDistance += travelDistance

        val travelTime = instance.getTravelTime(visitedNodes.last(), node)
        currentTravelTime += travelTime
        val waitingTime = instance.getTimewindow(node).start - currentTravelTime
        if (waitingTime > 0) {
            currentTravelTime += waitingTime
        }
        currentTravelTime += instance.getServiceTime(node)
        currentCapacity += instance.getDemand(node)
        currentBatteryCapacity -= instance.vehicleType.energyConsumption * travelDistance
        visitedNodes.add(node)
        return true
    }

    private fun log(message: String) {
        if (logEnabled) {
            log.info(message)
        }
    }

    companion object {
        fun calculateTotalDistance(routes: List<List<EVRPTWInstance.Node>>, instance: EVRPTWInstance): Double {
            var result = 0.0

            for (route in routes) {
                for (nodeIndex in 0 until route.size - 1) {
                    result += instance.getTravelDistance(route[nodeIndex], route[nodeIndex + 1])
                }
            }
            return result
        }
    }
}