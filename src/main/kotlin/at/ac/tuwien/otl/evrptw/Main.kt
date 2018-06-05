package at.ac.tuwien.otl.evrptw

import at.ac.tuwien.otl.evrptw.construction.TimeOrientedNearestNeighbourHeuristic
import at.ac.tuwien.otl.evrptw.dto.EVRPTWRouteVerifier
import at.ac.tuwien.otl.evrptw.util.EVRPTWSolver
import at.ac.tuwien.otl.evrptw.dto.InstanceLoader
import java.util.HashSet
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance



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
        private val instances = listOf("c103_21", "c105_21", "c204_21", "r102_21", "r107_21", "r205_21", "r211_21", "rc101_21", "rc106_21", "rc203_21")
        private val constructionHeuristic = TimeOrientedNearestNeighbourHeuristic()
        private val solver = EVRPTWSolver()

        @JvmStatic
        fun main(args: Array<String>) {
            for (i in 0..9) {
                runAlgorithmOnInstance(i, false)
            }

//            runAlgorithmOnInstance(7, true)
        }

        private fun runAlgorithmOnInstance(instanceId: Int, detailed: Boolean) {
            val instanceString = instances[instanceId]
            val instanceLoader = InstanceLoader()
            val instance = instanceLoader.load(instanceString)
            val solution = solver.solve(instance, constructionHeuristic)
            val nodesMissing = allNodesInRoutes(instance, solution.routes)

            val verifier = EVRPTWRouteVerifier(instance)
            val verified = verifier.verify(solution.routes, solution.cost, detailed)

            solution.writeToFile()

            println("instanceId: $instanceId, instance: $instanceString, verified: $verified, total cost: ${solution.cost}")
            if (nodesMissing.isNotEmpty()){
                println("missing customers: $nodesMissing")
            }
            println()
        }

        private fun allNodesInRoutes(instance: EVRPTWInstance, routes: List<List<EVRPTWInstance.Node>>): Set<EVRPTWInstance.Node> {
            val nodes = HashSet<EVRPTWInstance.Node>(instance.customers)
            for (route in routes)
                for (n in route) {
                    nodes.remove(n)
                }
            return nodes
        }
    }
}