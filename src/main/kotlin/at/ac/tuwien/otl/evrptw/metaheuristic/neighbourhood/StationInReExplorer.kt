package at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class StationInReExplorer : INeighbourhoodExplorer<EVRPTWSolution> {

    override fun exploreEverySolution(initialSolution: EVRPTWSolution): List<EVRPTWSolution> {
        // inserts or removes recharging stations between nodes
        return listOf(EVRPTWSolution(initialSolution))
    }
}