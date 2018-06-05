package at.ac.tuwien.otl.evrptw

import at.ac.tuwien.otl.evrptw.construction.PendularRouteConstructionHeuristic
import at.ac.tuwien.otl.evrptw.dto.EVRPTWRouteVerifier
import at.ac.tuwien.otl.evrptw.util.EVRPTWSolver
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
        private val instances = listOf("c103_21", "c105_21", "c204_21", "r102_21", "r107_21", "r205_21", "r211_21", "rc101_21", "rc106_21", "rc203_21")
        private val constructionHeuristic = PendularRouteConstructionHeuristic()
        private val solver = EVRPTWSolver()

        @JvmStatic
        fun main(args: Array<String>) {
            for (i: Int in 3..3) {
                val instanceString = instances[i]
                val instanceLoader = InstanceLoader()
                val instance = instanceLoader.load(instanceString)
                val solution = solver.solve(instance, constructionHeuristic)
                val verifier = EVRPTWRouteVerifier(instance)
                val verified = verifier.verify(solution.routes, solution.cost, false)
                // solution.writeToFile()
                println("instance: $instanceString, verified: $verified, total cost: ${solution.cost}")
            }
        }
    }
}