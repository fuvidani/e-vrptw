package at.ac.tuwien.otl.evrptw.dto

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 1.0.0
 * @since 1.0.0
 */
data class ExchangeSequence(
    val nodes: List<EVRPTWInstance.Node>,
    val startIndex: Int
)