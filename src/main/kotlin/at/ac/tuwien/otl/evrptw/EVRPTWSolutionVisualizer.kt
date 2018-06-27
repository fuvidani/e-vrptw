package at.ac.tuwien.otl.evrptw

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import org.graphstream.graph.implementations.AbstractEdge
import org.graphstream.graph.implementations.MultiGraph
import org.graphstream.graph.implementations.MultiNode
import org.graphstream.ui.graphicGraph.GraphicEdge
import java.awt.Color
import java.io.File

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 1.0.0
 * @since 1.0.0
 */
class EVRPTWSolutionVisualizer {
    private var edgeIdCounter = 1000

    fun visualizeSolution(instance: EVRPTWInstance) {
        val solution = loadSolutionForInstance(instance)
        plotSolution(instance, solution)
    }

    private fun plotSolution(instance: EVRPTWInstance, solution: EVRPTWSolution) {
        val graph = MultiGraph(instance.name)
        graph.addAttribute("ui.screenshot", "plots/${instance.name}.png")
        graph.addAttribute("ui.quality")
        graph.addAttribute("ui.antialias")

        // plot depot
        graph.addNode<MultiNode>(instance.depot.id.toString())
        val depotNode = graph.getNode<MultiNode>(instance.depot.id.toString())
        depotNode.setAttribute("xy", instance.getLocation(instance.depot).x, instance.getLocation(instance.depot).y)
//        depotNode.setAttribute("ui.label", instance.depot.id)
        depotNode.addAttribute("ui.style", "fill-color: red;")

        // plot recharge stations
        for (station in instance.rechargingStations) {
            graph.addNode<MultiNode>(station.id.toString())
            val stationNode = graph.getNode<MultiNode>(station.id.toString())
            stationNode.setAttribute("xy", station.location.x, station.location.y)
//            stationNode.setAttribute("ui.label", station.name)
            stationNode.addAttribute("ui.style", "fill-color: #2E8B57;")
        }

        // plot customers
        for (customer in instance.customers) {
            graph.addNode<MultiNode>(customer.id.toString())
            val customerNode = graph.getNode<MultiNode>(customer.id.toString())
            customerNode.setAttribute("xy", customer.location.x, customer.location.y)
//            customerNode.setAttribute("ui.label", customer.name)
        }

        // plot routes
        for (route in solution.routes) {
            val edgeColor = Color((Math.random() * 0x1000000).toInt())
            for (i in 0 until route.size - 1) {
                graph.addEdge<GraphicEdge>((++edgeIdCounter).toString(), route[i].id, route[i + 1].id)
                val edge = graph.getEdge<AbstractEdge>(edgeIdCounter.toString())
                edge.setAttribute("ui.style", "size: 3px; fill-color: rgb(${edgeColor.red}, ${edgeColor.green}, ${edgeColor.blue});")
            }
        }

        graph.display(false)
    }

    private fun loadSolutionForInstance(instance: EVRPTWInstance): EVRPTWSolution {
        File("solutions/" + instance.name + "_sol.txt").bufferedReader().use { reader ->
            reader.readLine() // skip first line

            val cost = reader.readLine().toDouble()

            val routes = mutableListOf<MutableList<EVRPTWInstance.Node>>()

            val iter = reader.lines().iterator()
            while (iter.hasNext()) {
                val line = iter.next()

                val nodeStrings = line.replace(" ", "").split(",")

                val route = mutableListOf<EVRPTWInstance.Node>()

                for (nodeString in nodeStrings) {
                    when {
                        nodeString.startsWith("D") -> route.add(instance.depot)
                        nodeString.startsWith("S") -> route.add(instance.rechargingStationMap[nodeString] as EVRPTWInstance.Node)
                        else -> route.add(instance.customerMap[nodeString] as EVRPTWInstance.Node)
                    }
                }

                routes.add(route)
            }

            return EVRPTWSolution(instance, routes, cost)
        }
    }
}