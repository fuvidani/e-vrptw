package at.ac.tuwien.otl.evrptw.dto

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 0.1.0
 * @since 0.1.0
 */
data class EVRPTWSolution (val instance: EVRPTWInstance,
                           val routes: MutableList<MutableList<EVRPTWInstance.Node>>,
                           var cost: Double)