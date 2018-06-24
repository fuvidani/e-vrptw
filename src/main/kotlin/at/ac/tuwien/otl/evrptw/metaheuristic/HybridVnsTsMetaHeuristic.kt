package at.ac.tuwien.otl.evrptw.metaheuristic

import at.ac.tuwien.otl.evrptw.Main
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.dto.NeighbourhoodStructure
import at.ac.tuwien.otl.evrptw.dto.Route
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.COOLING_FACTOR
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_DIST
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_FEAS
import at.ac.tuwien.otl.evrptw.metaheuristic.tabusearch.TabuSearch
import at.ac.tuwien.otl.evrptw.verifier.EVRPTWRouteVerifier
import java.util.Random
import java.util.logging.Logger

/**
 * <h4>About this class</h4>

 * <p>Description of this class</p>
 *
 * @author David Molnar
 * @version 1.0.0
 * @since 1.0.0
 */
class HybridVnsTsMetaHeuristic(private val logEnabled: Boolean = true) : IMetaHeuristic {
    private val log: Logger = Logger.getLogger(this.javaClass.name)
    private val neighbourSolutionGenerator = ShakingNeighbourSolutionGenerator()
    private val tabuSearch = TabuSearch(logEnabled)
    private val random = Random(java.lang.Double.doubleToLongBits(Math.random()))
    private var temperature = 0.0

    override fun improveSolution(evrptwSolution: EVRPTWSolution): EVRPTWSolution {
        temperature = Main.instanceToInitTemperatureMap[evrptwSolution.instance.name]!!
        var bestFeasibleSolution = evrptwSolution
        var bestSolution = evrptwSolution
        var k = 1
        var i = 0
        var feasibilityPhase = true

        while (feasibilityPhase || (!feasibilityPhase && i < N_DIST)) {
            val newSolution = neighbourSolutionGenerator.generateRandomPoint(bestSolution, k)
            val optimizedNewSolution = tabuSearch.apply(newSolution)

            if (acceptSimulatedAnnealing(optimizedNewSolution, bestSolution)) {
                bestSolution = optimizedNewSolution
                if (optimizedNewSolution.fitnessValue.fitness == optimizedNewSolution.cost) {
                    bestFeasibleSolution = optimizedNewSolution
                }
                k = 1
            } else {
                k = (k % NeighbourhoodStructure.STRUCTURES.size) + 1
            }

            if (feasibilityPhase) {
                if (!feasible(bestSolution)) {
                    if (i == N_FEAS) {
                        log("!!!!!!!  Splitting routes !!!!!!")
                        bestSolution = addVehicle(bestSolution)
                        i = -1
                    }
                } else {
                    feasibilityPhase = false
                    i = -1
                }
            }
            i++
        }

        return bestFeasibleSolution
    }

    private fun acceptSimulatedAnnealing(optimizedNewSolution: EVRPTWSolution, bestSolution: EVRPTWSolution): Boolean {
        val accept: Boolean
        accept = if (optimizedNewSolution.fitnessValue.fitness == optimizedNewSolution.cost) {
            optimizedNewSolution.fitnessValue.fitness < bestSolution.fitnessValue.fitness
        } else {
            if (optimizedNewSolution.fitnessValue.fitness < bestSolution.fitnessValue.fitness) {
                val exponent =
                    (-Math.abs(optimizedNewSolution.fitnessValue.fitness - bestSolution.fitnessValue.fitness)) / temperature
                val probabilityAccept = Math.exp(exponent)
                val randomNumber = random.nextDouble()
                log("SA accept probability: $randomNumber < $probabilityAccept")
                randomNumber < probabilityAccept
            } else {
                false
            }
        }
        temperature *= COOLING_FACTOR
        return accept
    }

    private fun feasible(solution: EVRPTWSolution): Boolean {
        return EVRPTWRouteVerifier(solution.instance).verify(solution.routes, solution.cost, false)
    }

    private fun addVehicle(solution: EVRPTWSolution): EVRPTWSolution {
        val newRoutes = mutableListOf<MutableList<EVRPTWInstance.Node>>()
        for (routeIndex in 0 until solution.fitnessValue.routeViolations.size) {
            val routeViolation = solution.fitnessValue.routeViolations[routeIndex]
            val route = solution.routes[routeIndex]
            if (route.size > 3 && (routeViolation.batteryCapacityViolation > 0.0 || routeViolation.timeWindowViolation > 0.0 || routeViolation.capacityViolation > 0.0)) {
                newRoutes.addAll(splitRoute(route, solution.instance))
            } else {
                newRoutes.add(solution.copyOfRoute(solution.routes[routeIndex]))
            }
        }
        return EVRPTWSolution(solution.instance, newRoutes, Route.calculateTotalDistance(newRoutes, solution.instance))
    }

    private fun splitRoute(
        route: List<EVRPTWInstance.Node>,
        instance: EVRPTWInstance
    ): MutableList<MutableList<EVRPTWInstance.Node>> {
        val newRoutes = mutableListOf<MutableList<EVRPTWInstance.Node>>()
        val firstRoute = route.subList(0, route.size / 2).toMutableList()
        firstRoute.add(instance.depot)
        val secondRoute = route.subList(route.size / 2, route.size).toMutableList()
        secondRoute.add(0, instance.depot)
        newRoutes.add(firstRoute)
        newRoutes.add(secondRoute)
        return newRoutes
    }

    private fun log(message: String) {
        if (logEnabled) {
            log.info(message)
        }
    }
}