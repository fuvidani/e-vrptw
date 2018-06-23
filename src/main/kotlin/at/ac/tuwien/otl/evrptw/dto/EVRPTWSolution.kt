package at.ac.tuwien.otl.evrptw.dto

import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.ALPHA
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.BETA
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.GAMMA
import at.ac.tuwien.otl.evrptw.verifier.EVRPTWRouteVerifier
import java.io.File
import java.util.stream.Collectors

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 0.1.0
 * @since 0.1.0
 */
data class EVRPTWSolution(
    val instance: EVRPTWInstance,
    val routes: MutableList<MutableList<EVRPTWInstance.Node>>,
    val cost: Double
) {

    val fitnessValue: FitnessValue = calculateFitnessValue()

    private fun calculateFitnessValue(): FitnessValue {
        return FitnessValue(
            cost,
            calculateTotalCapacityViolation(),
            calculateTotalTimeWindowViolation(),
            calculateTotalBatteryCapacityViolation()
        )
    }

    private fun calculateTotalCapacityViolation(): Double {
        var result = 0.0

        for (route in routes) {
            val demandSum = route.stream().filter { it is EVRPTWInstance.Customer }
                .mapToDouble { (it as EVRPTWInstance.Customer).demand }.sum()
            result += Math.max(demandSum - instance.vehicleCapacity, 0.0)
        }

        return result
    }

    private fun calculateTotalTimeWindowViolation(): Double {
        return EVRPTWRouteVerifier.calculateTotalTimeWindowViolation(instance, routes)
    }

    private fun calculateTotalBatteryCapacityViolation(): Double {
        var result = 0.0

        for (route in routes) {
            var batteryViolation = 0.0
            var lastNotCustomerIndex = 0
            for (nodeIndex in 0 until route.size) {
                if (route[nodeIndex] is EVRPTWInstance.Depot || route[nodeIndex] is EVRPTWInstance.RechargingStation) {
                    val currentViolation = batteryDemandTo(route, lastNotCustomerIndex, nodeIndex)
                    batteryViolation += Math.max(currentViolation - instance.vehicleEnergyCapacity, 0.0)
                    lastNotCustomerIndex = nodeIndex
                }
            }
            result += batteryViolation
        }

        return result
    }

    private fun batteryDemandTo(route: List<EVRPTWInstance.Node>, startIndex: Int, nodeIndex: Int): Double {
        if (startIndex == nodeIndex) {
            return 0.0
        }
        return batteryDemandTo(route, startIndex, nodeIndex - 1) + instance.getTravelDistance(
            route[nodeIndex - 1],
            route[nodeIndex]
        ) * instance.vehicleEnergyConsumption
    }

    /**
     * Copy constructor.
     */
    constructor(solution: EVRPTWSolution) : this(solution.instance, solution.routes
        .stream()
        .map {
            it
                .stream()
                .map { node ->
                    when (node) {
                        is EVRPTWInstance.Depot -> EVRPTWInstance.Depot(
                            node.id,
                            node.name,
                            node.location.x,
                            node.location.y,
                            node.timeWindow.start,
                            node.timeWindow.end
                        )
                        is EVRPTWInstance.Customer -> EVRPTWInstance.Customer(
                            node.id,
                            node.name,
                            node.location.x,
                            node.location.y,
                            node.timeWindow.start,
                            node.timeWindow.end,
                            node.demand,
                            node.serviceTime
                        )
                        else -> EVRPTWInstance.RechargingStation(
                            (node as EVRPTWInstance.RechargingStation).id,
                            node.name,
                            node.location.x,
                            node.location.y,
                            node.timeWindow.start,
                            node.timeWindow.end,
                            node.rechargingRate
                        )
                    }
                }
                .collect(Collectors.toList())
                .toMutableList()
        }
        .collect(Collectors.toList())
        .toMutableList(),
        solution.cost)

    fun writeToFile() {
        File("solutions/" + instance.name + "_sol.txt").bufferedWriter().use { out ->
            out.write("# solution for " + instance.name)
            out.newLine()
            out.write(cost.toString())
            out.newLine()
            routes.forEach {
                for (i: Int in 0..it.size) {
                    if (i <= it.size - 2) {
                        out.write(it[i].toString() + ", ")
                    } else if (i == it.size - 1) {
                        out.write(it[i].toString())
                    }
                }
                out.newLine()
            }
        }
    }
}

data class FitnessValue(
    val totalTravelDistance: Double,
    val totalCapacityViolation: Double,
    val totalTimeWindowViolation: Double,
    val totalBatteryCapacityViolation: Double
) {
    val fitness =
        totalTravelDistance + (ALPHA * totalCapacityViolation) + (BETA * totalTimeWindowViolation) + (GAMMA * totalBatteryCapacityViolation)
}
