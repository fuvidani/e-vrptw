package at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.callable

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.TwoOrOptExplorer

/**
 * <h4>About this class</h4>
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 26.06.2018
 */
class TwoOrOptExplorerCallable(
    initialSolution: EVRPTWSolution,
    startAtIncl: Int,
    endAtIncl: Int,
    explorer: TwoOrOptExplorer
) :
        INeighbourhoodExplorerCallable<EVRPTWSolution>(initialSolution, startAtIncl, endAtIncl, explorer)