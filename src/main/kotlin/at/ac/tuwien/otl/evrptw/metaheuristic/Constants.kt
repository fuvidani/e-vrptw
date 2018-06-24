package at.ac.tuwien.otl.evrptw.metaheuristic

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class Constants private constructor() {

    companion object {
        const val N_DIST = 5
        const val N_FEAS = 5
        const val N_TABU = 50
        const val N_PENALTY = 2
        const val COOLING_FACTOR = 0.9
        const val TABU_TENURE_MIN = 15
        const val TABU_TENURE_MAX = 30
        const val ALPHA = 2
        const val BETA = 2
        const val GAMMA = 2
    }
}