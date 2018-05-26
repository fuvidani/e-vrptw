package at.ac.tuwien.otl.evrptw.util

import at.ac.tuwien.otl.evrptw.construction.IConstructionHeuristic
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 0.1.0
 * @since 0.1.0
 */
class EVRPTWSolver {

    fun solve(instance: EVRPTWInstance, constructionHeuristic: IConstructionHeuristic): EVRPTWSolution {
        return constructionHeuristic.generateSolution(instance)
    }
}