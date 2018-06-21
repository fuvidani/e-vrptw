package at.ac.tuwien.otl.evrptw.metaheuristic

import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 1.0.0
 * @since 1.0.0
 */
interface IMetaHeuristic {
    fun improveSolution(evrptwSolution: EVRPTWSolution): EVRPTWSolution
}