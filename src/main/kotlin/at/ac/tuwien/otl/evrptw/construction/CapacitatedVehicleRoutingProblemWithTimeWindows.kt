package at.ac.tuwien.otl.evrptw.construction

import com.google.ortools.constraintsolver.FirstSolutionStrategy
import com.google.ortools.constraintsolver.RoutingModel
import com.google.ortools.constraintsolver.RoutingSearchParameters
import java.util.ArrayList
import java.util.logging.Logger
import java.util.Random
import com.google.ortools.constraintsolver.NodeEvaluator2

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class CapacitatedVehicleRoutingProblemWithTimeWindows {

    private val logger = Logger.getLogger(CapacitatedVehicleRoutingProblemWithTimeWindows::class.java.name)

    // Locations representing either an order location or a vehicle route
    // start/end.
    private val locations = ArrayList<Pair<Int, Int>>()

    // Quantity to be picked up for each order.
    private val orderDemands = ArrayList<Int>()
    // Time window in which each order must be performed.
    private val orderTimeWindows = ArrayList<Pair<Int, Int>>()
    // Penalty cost "paid" for dropping an order.
    private val orderPenalties = ArrayList<Int>()

    // Capacity of the vehicles.
    private var vehicleCapacity = 0
    // Latest time at which each vehicle must end its tour.
    private val vehicleEndTime = ArrayList<Int>()
    // Cost per unit of distance of each vehicle.
    private val vehicleCostCoefficients = ArrayList<Int>()
    // Vehicle start and end indices. They have to be implemented as int[] due
    // to the available SWIG-ed interface.
    private var vehicleStarts: IntArray? = null
    private var vehicleEnds: IntArray? = null
    private val randomGenerator = Random(0xBEEF)

    init {
        System.load("${System.getProperty("user.dir")}/libs/libjniortools.jnilib")
    }

    /**
     * Creates order data. Location of the order is random, as well as its
     * demand (quantity), time window and penalty.
     *
     * @param numberOfOrders number of orders to build.
     * @param xMax maximum x coordinate in which orders are located.
     * @param yMax maximum y coordinate in which orders are located.
     * @param demandMax maximum quantity of a demand.
     * @param timeWindowMax maximum starting time of the order time window.
     * @param timeWindowWidth duration of the order time window.
     * @param penaltyMin minimum pernalty cost if order is dropped.
     * @param penaltyMax maximum pernalty cost if order is dropped.
     */
    fun buildOrders(
        numberOfOrders: Int,
        xMax: Int,
        yMax: Int,
        demandMax: Int,
        timeWindowMax: Int,
        timeWindowWidth: Int,
        penaltyMin: Int,
        penaltyMax: Int
    ) {
        logger.info("Building orders.")
        for (order in 0 until numberOfOrders) {
            locations.add(
                Pair.of(
                    randomGenerator.nextInt(xMax + 1),
                    randomGenerator.nextInt(yMax + 1)
                )
            )
            orderDemands.add(randomGenerator.nextInt(demandMax + 1))
            val timeWindowStart = randomGenerator.nextInt(timeWindowMax + 1)
            orderTimeWindows.add(Pair.of(timeWindowStart, timeWindowStart + timeWindowWidth))
            orderPenalties.add(randomGenerator.nextInt(penaltyMax - penaltyMin + 1) + penaltyMin)
        }
    }

    /**
     * Creates fleet data. Vehicle starting and ending locations are random, as
     * well as vehicle costs per distance unit.
     *
     * @param numberOfVehicles
     * @param xMax maximum x coordinate in which orders are located.
     * @param yMax maximum y coordinate in which orders are located.
     * @param endTime latest end time of a tour of a vehicle.
     * @param capacity capacity of a vehicle.
     * @param costCoefficientMax maximum cost per distance unit of a vehicle
     * (mimimum is 1),
     */
    fun buildFleet(
        numberOfVehicles: Int,
        xMax: Int,
        yMax: Int,
        endTime: Int,
        capacity: Int,
        costCoefficientMax: Int
    ) {
        logger.info("Building fleet.")
        vehicleCapacity = capacity
        vehicleStarts = IntArray(numberOfVehicles)
        vehicleEnds = IntArray(numberOfVehicles)
        for (vehicle in 0 until numberOfVehicles) {
            (vehicleStarts as IntArray)[vehicle] = locations.size
            locations.add(
                Pair.of(
                    randomGenerator.nextInt(xMax + 1),
                    randomGenerator.nextInt(yMax + 1)
                )
            )
            (vehicleEnds as IntArray)[vehicle] = locations.size
            locations.add(
                Pair.of(
                    randomGenerator.nextInt(xMax + 1),
                    randomGenerator.nextInt(yMax + 1)
                )
            )
            vehicleEndTime.add(endTime)
            vehicleCostCoefficients.add(randomGenerator.nextInt(costCoefficientMax) + 1)
        }
    }

    fun solve(numberOfOrders: Int, numberOfVehicles: Int) {
        logger.info(
            "Creating model with " + numberOfOrders + " orders and " +
                    numberOfVehicles + " vehicles."
        )
        // Finalizing model
        val numberOfLocations = locations.size

        val model = RoutingModel(
            numberOfLocations, numberOfVehicles,
            vehicleStarts, vehicleEnds
        )

        // Setting up dimensions
        val bigNumber = 100000
        val manhattanCallback = object : NodeEvaluator2() {
            override fun run(firstIndex: Int, secondIndex: Int): Long {
                return try {
                    val firstLocation = locations[firstIndex]
                    val secondLocation = locations[secondIndex]
                    (Math.abs(firstLocation.first - secondLocation.first) + Math.abs(firstLocation.second - secondLocation.second)).toLong()
                } catch (throwable: Throwable) {
                    logger.warning(throwable.message)
                    0
                }
            }
        }
        model.addDimension(manhattanCallback, bigNumber.toLong(), bigNumber.toLong(), false, "time")
        val demandCallback = object : NodeEvaluator2() {
            override fun run(firstIndex: Int, secondIndex: Int): Long {
                return try {
                    if (firstIndex < numberOfOrders) {
                        orderDemands[firstIndex].toLong()
                    } else 0
                } catch (throwable: Throwable) {
                    logger.warning(throwable.message)
                    0
                }
            }
        }
        model.addDimension(demandCallback, 0, vehicleCapacity.toLong(), true, "capacity")

        // Setting up vehicles
        for (vehicle in 0 until numberOfVehicles) {
            val costCoefficient = vehicleCostCoefficients[vehicle]
            val manhattanCostCallback = object : NodeEvaluator2() {
                override fun run(firstIndex: Int, secondIndex: Int): Long {
                    return try {
                        val firstLocation = locations[firstIndex]
                        val secondLocation = locations[secondIndex]
                        (costCoefficient * ((Math.abs(firstLocation.first - secondLocation.first) + Math.abs(
                            firstLocation.second - secondLocation.second
                        )))).toLong()
                    } catch (throwable: Throwable) {
                        logger.warning(throwable.message)
                        0
                    }
                }
            }
            model.setArcCostEvaluatorOfVehicle(manhattanCostCallback, vehicle)
            model.cumulVar(model.end(vehicle), "time").setMax(vehicleEndTime[vehicle].toLong())
        }

        // Setting up orders
        for (order in 0 until numberOfOrders) {
            model.cumulVar(order.toLong(), "time").setRange(
                orderTimeWindows[order].first.toLong(),
                orderTimeWindows[order].second.toLong()
            )
            val orders = intArrayOf(order)
            model.addDisjunction(orders, orderPenalties[order].toLong())
        }

        // Solving
        val parameters = RoutingSearchParameters.newBuilder()
            .mergeFrom(RoutingModel.defaultSearchParameters())
            .setFirstSolutionStrategy(FirstSolutionStrategy.Value.ALL_UNPERFORMED)
            .build()

        logger.info("Search")
        val solution = model.solveWithParameters(parameters)

        if (solution != null) {
            var output = "Total cost: " + solution.objectiveValue() + "\n"
            // Dropped orders
            var dropped = ""
            for (order in 0 until numberOfOrders) {
                if (solution.value(model.nextVar(order.toLong())) == order.toLong()) {
                    dropped += " $order"
                }
            }
            if (dropped.isNotEmpty()) {
                output += "Dropped orders:$dropped\n"
            }
            // Routes
            for (vehicle in 0 until numberOfVehicles) {
                var route = "Vehicle $vehicle: "
                var order = model.start(vehicle)
                if (model.isEnd(solution.value(model.nextVar(order)))) {
                    route += "Empty"
                } else {
                    while (!model.isEnd(order)) {
                        val load = model.cumulVar(order, "capacity")
                        val time = model.cumulVar(order, "time")
                        route += ((order).toString() + " Load(" + solution.value(load) + ") " +
                                "Time(" + solution.min(time) + ", " + solution.max(time) +
                                ") -> ")
                        order = solution.value(model.nextVar(order))
                    }
                    val load = model.cumulVar(order, "capacity")
                    val time = model.cumulVar(order, "time")
                    route += ((order).toString() + " Load(" + solution.value(load) + ") " +
                            "Time(" + solution.min(time) + ", " + solution.max(time) + ")")
                }
                output += route + "\n"
            }
            logger.info(output)
        }
    }
}

internal class Pair<out K, out V>(val first: K, val second: V) {
    companion object {

        fun <K, V> of(element0: K, element1: V): Pair<K, V> {
            return Pair(element0, element1)
        }
    }
}