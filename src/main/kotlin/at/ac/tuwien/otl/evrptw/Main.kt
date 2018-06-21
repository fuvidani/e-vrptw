package at.ac.tuwien.otl.evrptw

import at.ac.tuwien.otl.evrptw.construction.TimeOrientedNearestNeighbourHeuristic
import at.ac.tuwien.otl.evrptw.verifier.EVRPTWRouteVerifier
import at.ac.tuwien.otl.evrptw.loader.InstanceLoader
import java.util.HashSet
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.metaheuristic.HybridVnsTsMetaHeuristic
import java.util.concurrent.TimeUnit

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
        private val constructionHeuristic = TimeOrientedNearestNeighbourHeuristic(false)
        private val metaHeuristic = HybridVnsTsMetaHeuristic()
        private val solver = EVRPTWSolver()
        private const val rampUpRuns = 20
        private const val measuredRuns = 30

        @JvmStatic
        fun main(args: Array<String>) {
            // Ramp-up phase, ignore runtimes
            /*for (j in 1..rampUpRuns) {
                for (i in 0..9) {
                    runAlgorithmOnInstance(i, false)
                }
            }
            val instanceRuntimeMap: MutableMap<Int, MutableList<Long>> = mutableMapOf()
            for (j in 1..measuredRuns) {
                println("\n\n\nRun number $j")
                for (i in 0..9) {
                    val runtimeInNano = runAlgorithmOnInstance(i, false)
                    if (instanceRuntimeMap[i] == null) {
                        instanceRuntimeMap[i] = mutableListOf()
                    }
                    instanceRuntimeMap[i]!!.add(runtimeInNano)
                }
            }
            println("\nAvg. runtime for each instance across $measuredRuns runs")
            for (i in 0..9) {
                println("instanceId: $i, avg. runtime: ${TimeUnit.NANOSECONDS.toMillis(instanceRuntimeMap[i]!!.average().toLong())} ms")
            }*/
            runAlgorithmOnInstance(0, false)
        }

        private fun runAlgorithmOnInstance(instanceId: Int, detailed: Boolean): Long {
            val instanceString = instances[instanceId]
            val instanceLoader = InstanceLoader()
            val instance = instanceLoader.load(instanceString)
            val start = System.nanoTime()
            val solution = solver.solve(instance, constructionHeuristic, metaHeuristic)
            val durationInNano = System.nanoTime() - start
            val nodesMissing = allNodesInRoutes(instance, solution.routes)

            val verifier = EVRPTWRouteVerifier(instance)
            val verified = verifier.verify(solution.routes, solution.cost, detailed)

            solution.writeToFile()

            println(
                "instanceId: $instanceId, instance: $instanceString, verified: $verified, total cost: ${solution.cost}, runtime: ${TimeUnit.NANOSECONDS.toMillis(
                    durationInNano
                )} ms"
            )
            if (nodesMissing.isNotEmpty()) {
                println("missing customers: $nodesMissing")
                println()
            }
            return durationInNano
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