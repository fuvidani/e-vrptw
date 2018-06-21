package at.ac.tuwien.otl.evrptw

import at.ac.tuwien.otl.evrptw.construction.IConstructionHeuristic
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.metaheuristic.IMetaHeuristic

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 0.1.0
 * @since 0.1.0
 */
class EVRPTWSolver {

    fun solve(instance: EVRPTWInstance, constructionHeuristic: IConstructionHeuristic, metaHeuristic: IMetaHeuristic): EVRPTWSolution {
        val solution = constructionHeuristic.generateSolution(instance)

        return metaHeuristic.improveSolution(solution)
    }
}