package at.ac.tuwien.otl.evrptw

import at.ac.tuwien.otl.evrptw.construction.ConstructionHeuristic
import at.ac.tuwien.otl.evrptw.dto.InstanceLoader
import at.ac.tuwien.otl.evrptw.util.EVRPTWSolver

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

        private val testInstances = listOf("c101C10","r102C10","rc201C10")
        private val constructionHeuristic = ConstructionHeuristic()
        private val solver = EVRPTWSolver()

        @JvmStatic
        fun main(args: Array<String>) {
            println("Hello World")
            val instanceLoader = InstanceLoader()
            val instance  = instanceLoader.load(testInstances[0])
            val solution = solver.solve(instance, constructionHeuristic)
            println(solution)
        }
    }
}