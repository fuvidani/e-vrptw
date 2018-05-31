package at.ac.tuwien.otl.evrptw

import at.ac.tuwien.otl.evrptw.util.EVRPTWSolver
import at.ac.tuwien.otl.evrptw.construction.CapacitatedVehicleRoutingProblemWithTimeWindows
import at.ac.tuwien.otl.evrptw.construction.Pair
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.InstanceLoader

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class Main {

    companion object {

        private val testInstances = listOf("c101C10", "r102C10", "rc201C10")
        // private val constructionHeuristic = ConstructionHeuristic()
        private val solver = EVRPTWSolver()

        @JvmStatic
        fun main(args: Array<String>) {
            println("Hello World")
            val instanceLoader = InstanceLoader()
            val instance  = instanceLoader.load(testInstances[0])
            //val locations : MutableList<Pair<Int, Int>> = instance.customers.map { Pair(it.location.x.toInt(),it.location.y.toInt()) }.toMutableList()
            val numberOfVehicles = 5
            val startLocations = IntArray(numberOfVehicles)
            val endLocations = IntArray(numberOfVehicles)
            for (index in 0 until numberOfVehicles) {
                //startLocations[index] = (instance.depot as EVRPTWInstance.Depot).location.x.toInt()
                startLocations[index] = instance.numCustomers
                //endLocations[index] = (instance.depot as EVRPTWInstance.Depot).location.y.toInt()
                endLocations[index] = instance.numCustomers
            }

            val problem = CapacitatedVehicleRoutingProblemWithTimeWindows(
                //listOf(Pair((instance.depot as EVRPTWInstance.Depot).location.x.toInt(),(instance.depot as EVRPTWInstance.Depot).location.y.toInt())) +
                        instance.customers.map { Pair(it.location.x.toInt(),it.location.y.toInt()) } + listOf(Pair((instance.depot as EVRPTWInstance.Depot).location.x.toInt(),(instance.depot as EVRPTWInstance.Depot).location.y.toInt())),
                instance.customers.map { it.demand.toInt() },
                instance.customers.map { Pair(it.timeWindow.start.toInt(),it.timeWindow.end.toInt()) },
                instance.customers.map { 0 },
                200,
                listOf(1236,1236,1236,1236,1236,1236,1236,1236,1236,1236,1236,1236),
                instance.customers.map { instance.vehicleEnergyConsumption.toInt() },
                startLocations,
                endLocations
            )
            /*val xMax = 20
            val yMax = 20
            val demandMax = 3
            val timeWindowMax = 24 * 60
            val timeWindowWidth = 4 * 60
            val penaltyMin = 50
            val penaltyMax = 100
            val endTime = 24 * 60
            val costCoefficientMax = 3

            val orders = 100
            val vehicles = 20
            val capacity = 50

            problem.buildOrders(
                orders,
                xMax,
                yMax,
                demandMax,
                timeWindowMax,
                timeWindowWidth,
                penaltyMin,
                penaltyMax
            )
            problem.buildFleet(
                vehicles,
                xMax,
                yMax,
                endTime,
                capacity,
                costCoefficientMax
            )*/
            problem.solve(instance.numCustomers, numberOfVehicles)
        }
    }
}