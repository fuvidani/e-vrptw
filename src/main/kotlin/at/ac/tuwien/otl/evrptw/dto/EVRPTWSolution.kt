package at.ac.tuwien.otl.evrptw.dto

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
    var cost: Double
) {
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
