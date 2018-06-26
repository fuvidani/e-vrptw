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
        const val N_DIST = 50
        const val N_FEAS = 5
        const val N_TABU = 50
        const val N_PENALTY = 2
        const val COOLING_FACTOR = 0.9
        const val TABU_TENURE_MIN = 15
        const val TABU_TENURE_MAX = 30
        const val NO_CHANGE_THRESHOLD = 3
        const val ALPHA_DEFAULT = 1.0
        const val BETA_DEFAULT = 10.0
        const val GAMMA_DEFAULT = 10.0
        const val VIOLATION_FACTOR_MIN = 1.0
        const val VIOLATION_FACTOR_BELOW_MIN_DESCENT_RATE = 0.1
        const val VIOLATION_FACTOR_ABSOLUTE_MIN = 0.1
        const val VIOLATION_FACTOR_INCREASE_RATE = 2.0
        const val VIOLATION_FACTOR_DECREASE_RATE = 5.0
        const val ALPHA_STARTING = 1.0
        const val BETA_STARTING = 2.0
        const val GAMMA_STARTING = 2.0
        var ALPHA = ALPHA_STARTING
        var BETA = BETA_STARTING
        var GAMMA = GAMMA_STARTING
        val FIBONACCI = arrayOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610)
    }
}