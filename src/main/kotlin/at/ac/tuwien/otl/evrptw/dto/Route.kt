package at.ac.tuwien.otl.evrptw.dto

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 0.1.0
 * @since 0.1.0
 */
data class Route(
        private val instance: EVRPTWInstance
) {
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
            println("insert node $node")

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
        return false
    }

    private fun addDepotToRoute(depot: EVRPTWInstance.Depot): Boolean {
        val travelTime = instance.getTravelTime(visitedNodes.last(), depot)
        if (travelTime * instance.vehicleType.energyConsumption > currentBatteryCapacity) {
            println("could not add depot")

            return false
        }
        println("insert DEPOT")
        val travelDistance = instance.getTravelDistance(visitedNodes.last(), depot)
        currentTravelDistance += travelDistance
        currentTravelTime += travelTime
        visitedNodes.add(depot)
        return true
    }

    private fun nodeViolatesConstraints(node: EVRPTWInstance.Node): Boolean {
        // check capacity constraint
        if (currentCapacity + instance.getDemand(node) > instance.vehicleCapacity) {
            println("load capacity violation")
            return true
        }


        // check if arrival in time window
        val travelTimeFromLastToNode = instance.getTravelTime(visitedNodes.last(), node)
        val travelTimeSum = currentTravelTime + travelTimeFromLastToNode

        if (travelTimeSum > instance.getTimewindow(node).end) {
            println("time window violation")
            return true
        }


        // check if enough energy to node and after to depot/recharge station
        val sortedList = instance.rechargingStations.sortedWith(compareBy({ instance.getTravelDistance(node, it) }))
        val travelTimeFromNodeToNearestRechargeStation = instance.getTravelTime(node, sortedList[0])
        val travelTimeFromNodeToDepot = instance.getTravelTime(node, depot)
        val additionalTravelTimeWithDepot = travelTimeFromLastToNode + travelTimeFromNodeToDepot
        val additionalTravelTimeWithNearestRechargeStation = travelTimeFromLastToNode + travelTimeFromNodeToNearestRechargeStation
        val travelTimeFromNearestRechargeStationToDepot = instance.getTravelTime(sortedList[0],depot)

        if (additionalTravelTimeWithDepot * instance.vehicleType.energyConsumption > currentBatteryCapacity || currentTravelTime + additionalTravelTimeWithDepot + instance.getServiceTime(node) > instance.getTimewindow(depot).end) {
            val futureRechargeTime = (instance.vehicleEnergyCapacity - (currentBatteryCapacity - additionalTravelTimeWithNearestRechargeStation * instance.vehicleType.energyConsumption) ) * sortedList[0].rechargingRate
            if (additionalTravelTimeWithNearestRechargeStation * instance.vehicleType.energyConsumption > currentBatteryCapacity || currentTravelTime + additionalTravelTimeWithNearestRechargeStation + instance.getServiceTime(node) + travelTimeFromNearestRechargeStationToDepot + futureRechargeTime > instance.getTimewindow(depot).end){
                println("HEAVY violation")
                return true
            }
        }

        return false
    }

    private fun addRechargeToRoute(rechargeStation: EVRPTWInstance.RechargingStation): Boolean {
        val travelDistance = instance.getTravelDistance(visitedNodes.last(), rechargeStation)
        val travelTime = instance.getTravelTime(visitedNodes.last(), rechargeStation)
        if (travelTime * instance.vehicleType.energyConsumption > currentBatteryCapacity) {
            println("could not add recharge")
            return false
        }
        println("inserted RECHARGE")
        currentTravelDistance += travelDistance
        currentBatteryCapacity -= instance.vehicleType.energyConsumption * travelDistance
        val rechargingTime = (instance.vehicleEnergyCapacity - currentBatteryCapacity) * rechargeStation.rechargingRate
        println("rechargintime: $rechargingTime")
        currentTravelTime += travelTime + rechargingTime
        currentBatteryCapacity = instance.vehicleEnergyCapacity
        visitedNodes.add(rechargeStation)

        return true
    }
}