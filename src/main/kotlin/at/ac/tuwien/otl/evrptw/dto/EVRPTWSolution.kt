package at.ac.tuwien.otl.evrptw.dto

import java.io.File

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 0.1.0
 * @since 0.1.0
 */
data class EVRPTWSolution(
        private val instance: EVRPTWInstance,
        val routes: MutableList<MutableList<EVRPTWInstance.Node>>,
        var cost: Double
) {
    fun writeToFile() {
        File("solutions/" + instance.name + "_sol.txt").bufferedWriter().use { out ->
            out.write("# solution for " + instance.name)
            out.newLine()
            out.write(cost.toString())
            out.newLine()
            routes.forEach {
                for (i: Int in 0..it.size) {
                    if (i <= it.size - 2) {
                        out.write(it[i].toString() + ", ")
                    } else if (i == it.size - 1) {
                        out.write(it[i].toString())
                    }
                }
                out.newLine()
            }
        }
    }
}
