package at.ac.tuwien.otl.evrptw.metaheuristic.neighbourhood

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface INeighbourhoodExplorer<T> {

    /**
     * Explores (enumerates) every possible solution in the search space
     * of this neighbourhood based on the initial solution and returns them
     * in a list.
     *
     * @param initialSolution an initial solution for which the neighbours should
     * be calculated
     * @return a list of neighbour solutions, not necessarily sorted
     */
    fun exploreEverySolution(initialSolution: T): List<T>
}