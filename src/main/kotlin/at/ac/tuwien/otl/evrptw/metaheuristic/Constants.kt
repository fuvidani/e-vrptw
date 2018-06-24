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
        const val N_DIST = 15
        const val N_FEAS = 5
        const val N_TABU = 30
        const val N_PENALTY = 2
        const val COOLING_FACTOR = 0.9
        const val TABU_TENURE_MIN = 15
        const val TABU_TENURE_MAX = 30
        const val NO_CHANGE_THRESHOLD = 2
        const val ALPHA_DEFAULT = 1.0
        const val BETA_DEFAULT = 1.0
        const val GAMMA_DEFAULT = 1.0
        const val VIOLATION_FACTOR_INCREASE_RATE = 0.5
        const val VIOLATION_FACTOR_DECREASE_RATE = 0.5
        var ALPHA = ALPHA_DEFAULT
        var BETA = BETA_DEFAULT
        var GAMMA = GAMMA_DEFAULT
    }
}