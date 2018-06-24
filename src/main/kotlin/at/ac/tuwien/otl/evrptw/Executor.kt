package at.ac.tuwien.otl.evrptw

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class Executor private constructor() {

    companion object {
        private var executorService: ExecutorService? = null

        fun getExecutorService(): ExecutorService {
            if (executorService == null) {
                executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
            }
            return executorService!!
        }
    }
}