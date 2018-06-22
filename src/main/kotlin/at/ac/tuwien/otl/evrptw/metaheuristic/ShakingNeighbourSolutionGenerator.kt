package at.ac.tuwien.otl.evrptw.metaheuristic

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.dto.ExchangeSequence
import at.ac.tuwien.otl.evrptw.dto.NeighbourhoodStructure
import java.util.Random

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class ShakingNeighbourSolutionGenerator {

    private val random = Random(123456)

    fun generateRandomPoint(solution: EVRPTWSolution, neighbour: Int): EVRPTWSolution {
        val result = EVRPTWSolution(solution) // we need a fresh very deep copy

        val routes: MutableList<MutableList<EVRPTWInstance.Node>> = mutableListOf()
        val neighbourhoodStructure = NeighbourhoodStructure.STRUCTURES[neighbour]!!

        if (result.routes.size < neighbourhoodStructure.numberOfInvolvedRoutes) {
            throw RuntimeException("ERROR: fewer routes than involved routes")
        }

        for (i in 0 until neighbourhoodStructure.numberOfInvolvedRoutes) {
            val index = random.nextInt(result.routes.size)

            routes.add(result.routes[index])
            result.routes.removeAt(index)
        }

        val exchangeSequences = mutableListOf<ExchangeSequence>()

        for (route in routes) {
            val upperBound = Math.min(neighbourhoodStructure.maxVertices, route.size - 2)
            val numberOfSuccessiveVertices = if (upperBound == 1) {
                1
            } else {
                random.nextInt(upperBound) + 1
            }
            val startIndex = random.nextInt((route.size - numberOfSuccessiveVertices - 1)) + 1

            val exchangeSequence = ExchangeSequence(
                route.subList(
                    startIndex,
                    startIndex + numberOfSuccessiveVertices
                ).toList(), startIndex
            )
            exchangeSequences.add(exchangeSequence)

            route.subList(startIndex, startIndex + numberOfSuccessiveVertices).clear()
        }

        for (i in 0 until routes.size) {
            val startIndex = exchangeSequences[i].startIndex
            val sequence: List<EVRPTWInstance.Node> = if (i == 0) {
                exchangeSequences[exchangeSequences.size - 1].nodes
            } else {
                exchangeSequences[i - 1].nodes
            }

            routes[i].addAll(startIndex, sequence)
        }

        result.routes.addAll(routes)

        return result
    }
}