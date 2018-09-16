package at.ac.tuwien.otl.evrptw.dto

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 1.0.0
 * @since 1.0.0
 */
data class NeighbourhoodStructure(
    val numberOfInvolvedRoutes: Int,
    val maxVertices: Int
) {
    companion object {
        val STRUCTURES = mapOf(
            1 to NeighbourhoodStructure(2, 1),
            2 to NeighbourhoodStructure(2, 2),
            3 to NeighbourhoodStructure(2, 3),
            4 to NeighbourhoodStructure(2, 4),
            5 to NeighbourhoodStructure(2, 5),
            6 to NeighbourhoodStructure(3, 1),
            7 to NeighbourhoodStructure(3, 2),
            8 to NeighbourhoodStructure(3, 3),
            9 to NeighbourhoodStructure(3, 4),
            10 to NeighbourhoodStructure(3, 5),
            11 to NeighbourhoodStructure(4, 1),
            12 to NeighbourhoodStructure(4, 2),
            13 to NeighbourhoodStructure(4, 3),
            14 to NeighbourhoodStructure(4, 4),
            15 to NeighbourhoodStructure(4, 5)
        )
    }
}