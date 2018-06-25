package at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.callable

import at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood.INeighbourhoodExplorer
import java.util.concurrent.Callable

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
abstract class INeighbourhoodExplorerCallable<T>(
    private val initialSolution: T,
    private val startAtIncl: Int,
    private val endAtIncl: Int,
    private val explorer: INeighbourhoodExplorer<T>
) : Callable<List<T>> {

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    override fun call(): List<T> {
        return explorer.exploreEverySolution(initialSolution, startAtIncl, endAtIncl)
    }
}