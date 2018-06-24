package at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.callable

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.TwoOptArcExchangeExplorer

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class TwoOptArcExchangeExplorerCallable(initialSolution: EVRPTWSolution, explorer: TwoOptArcExchangeExplorer) :
    INeighbourhoodExplorerCallable<EVRPTWSolution>(initialSolution, explorer)