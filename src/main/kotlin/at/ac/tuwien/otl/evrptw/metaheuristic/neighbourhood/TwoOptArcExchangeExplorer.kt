package at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import java.util.Random

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class TwoOptArcExchangeExplorer :
    INeighbourhoodExplorer<EVRPTWSolution> {

    private val random = Random(java.lang.Double.doubleToLongBits(Math.random()))

    override fun exploreEverySolution(initialSolution: EVRPTWSolution): List<EVRPTWSolution> {
        // ....
        return listOf(EVRPTWSolution(initialSolution))
    }

    private fun calculateTotalCostBasedOnOtherSolution(
        baselineSolution: EVRPTWSolution,
        newSolution: EVRPTWSolution
    ): Double {
        // ...
        return baselineSolution.cost
    }
}