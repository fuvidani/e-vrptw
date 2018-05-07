package at.ac.tuwien.otl.evrptw.construction

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.dto.Route
import com.sun.javafx.geom.Line2D
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 0.1.0
 * @since 0.1.0
 */
class ConstructionHeuristic : IConstructionHeuristic {

    private val random = Random()

    override fun generateSolution(instance: EVRPTWInstance): EVRPTWSolution {
        val routes = mutableListOf<MutableList<EVRPTWInstance.Node>>()
        routes.add(mutableListOf())
        routes.add(mutableListOf())
        val randomLocation = randomPoint(instance.customers)
        println("Random point: ${randomLocation.x}, ${randomLocation.y}")
        val depot = instance.depot as EVRPTWInstance.Depot
        val depotToRandomPoint = Line2D(depot.location.x.toFloat(),depot.location.y.toFloat(),randomLocation.x.toFloat(),randomLocation.y.toFloat())
        val sortedCustomers = instance.customers.stream()
                .sorted { customer1, customer2 ->
                    val depotToCustomer1 = Line2D(depot.location.x.toFloat(),depot.location.y.toFloat(),customer1.location.x.toFloat(),customer1.location.y.toFloat())
                    val depotToCustomer2 = Line2D(depot.location.x.toFloat(),depot.location.y.toFloat(),customer2.location.x.toFloat(),customer2.location.y.toFloat())
                    val angleCustomer1 = angleBetweenTwoLines(depotToRandomPoint,depotToCustomer1)
                    val angleCustomer2 = angleBetweenTwoLines(depotToRandomPoint, depotToCustomer2)
                    angleCustomer1.compareTo(angleCustomer2)
                }
                .collect(Collectors.toList())
        println("Depot (${depot.location.x}, ${depot.location.y})")
        val customer1 = sortedCustomers[0]
        val customer2 = sortedCustomers[1]
        val customer3 = sortedCustomers[3]
        val depotToCustomer1 = Line2D(depot.location.x.toFloat(),depot.location.y.toFloat(),customer1.location.x.toFloat(),customer1.location.y.toFloat())
        val depotToCustomer2 = Line2D(depot.location.x.toFloat(),depot.location.y.toFloat(),customer2.location.x.toFloat(),customer2.location.y.toFloat())
        val depotToCustomer3 = Line2D(depot.location.x.toFloat(),depot.location.y.toFloat(),customer3.location.x.toFloat(),customer3.location.y.toFloat())
        val angleCustomer1 = angleBetweenTwoLines(depotToRandomPoint,depotToCustomer1)
        val angleCustomer2 = angleBetweenTwoLines(depotToRandomPoint, depotToCustomer2)
        val angleCustomer3 = angleBetweenTwoLines(depotToRandomPoint, depotToCustomer3)
        println("Angle customer 1: $angleCustomer1")
        println("Angle customer 2: $angleCustomer2")
        println("Angle customer 2: $angleCustomer3")

        //println("Customer1 (${sortedCustomers[0].location.x},${sortedCustomers[0].location.y})")
        //println("Customer2 (${sortedCustomers[1].location.x},${sortedCustomers[1].location.y})")
        val route = Route(0.0,0.0,depot,0.0,0.0)
        return EVRPTWSolution(instance, routes,0.0)
    }

    private fun randomPoint(customers: List<EVRPTWInstance.Customer>) : EVRPTWInstance.Node.Location {
        /*val angle = random.nextInt(360)
        val x = Math.cos(angle.toDouble())
        val y = Math.sin(angle.toDouble())
        return EVRPTWInstance.Node.Location(x, y)*/
        val maxX = customers.stream().mapToDouble { it.location.x }.max().asDouble
        val maxY = customers.stream().mapToDouble { it.location.y }.max().asDouble
        val minX = customers.stream().mapToDouble { it.location.x }.min().asDouble
        val minY = customers.stream().mapToDouble { it.location.y }.min().asDouble
        val randomX = ThreadLocalRandom.current().nextDouble(minX, maxX)
        val randomY = ThreadLocalRandom.current().nextDouble(minY, maxY)
        return EVRPTWInstance.Node.Location(randomX, randomY)
    }

    private fun angleBetweenTwoLines(firstLine2D: Line2D, secondsLine2D: Line2D) : Double {
        val angle1 = Math.atan2((firstLine2D.y1.toDouble() - firstLine2D.y2.toDouble()),(firstLine2D.x1.toDouble() - firstLine2D.x2.toDouble()))
        val angle2 = Math.atan2((secondsLine2D.y1 - secondsLine2D.y2).toDouble(),(secondsLine2D.x1 - secondsLine2D.x2).toDouble())
        return Math.toDegrees(Math.abs(angle1) - Math.abs(angle2))
    }
}