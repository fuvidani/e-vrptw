package at.ac.tuwien.otl.evrptw.dto

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 0.1.0
 * @since 0.1.0
 */
data class Route(private val maxCapacity:Double,
                 private val maxBatteryCapacity: Double,
                 private val depot: EVRPTWInstance.Depot,
                 private var currentCapacity: Double,
                 private var currentBatteryCapacity: Double,
                 private val visitedNodes: MutableList<EVRPTWInstance.Node> = mutableListOf(depot)) {

    fun addNode(node: EVRPTWInstance.Node) : Boolean {

        if (!nodeViolatesConstraints(node)) {

            return true
        }
        return false
    }

    private fun nodeViolatesConstraints(node: EVRPTWInstance.Node) : Boolean {
        return false
    }
}