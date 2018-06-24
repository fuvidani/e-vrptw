package at.ac.tuwien.otl.evrptw.metaheuristic

import at.ac.tuwien.otl.evrptw.Main
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance
import at.ac.tuwien.otl.evrptw.dto.EVRPTWSolution
import at.ac.tuwien.otl.evrptw.dto.NeighbourhoodStructure
import at.ac.tuwien.otl.evrptw.dto.Route
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.ALPHA
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.ALPHA_DEFAULT
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.BETA
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.BETA_DEFAULT
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.COOLING_FACTOR
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.GAMMA
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.GAMMA_DEFAULT
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.NO_CHANGE_THRESHOLD
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_DIST
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_FEAS
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.PARAM_INCREASE_RATE
import at.ac.tuwien.otl.evrptw.metaheuristic.tabusearch.TabuSearch
import at.ac.tuwien.otl.evrptw.verifier.EVRPTWRouteVerifier
import java.util.Random
import java.util.logging.Logger
import kotlin.streams.toList

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
    private val lastSavedSolutions = mutableListOf<EVRPTWSolution>()
    private var temperature = 0.0
    private var thresholdCounter = 0

    override fun improveSolution(evrptwSolution: EVRPTWSolution): EVRPTWSolution {
        temperature = Main.instanceToInitTemperatureMap[evrptwSolution.instance.name]!!
        lastSavedSolutions.clear()
        thresholdCounter = 0
        var bestFeasibleSolution = evrptwSolution
        var bestSolution = evrptwSolution
        var k = 1
        var i = 0
        var feasibilityPhase = true

        while (feasibilityPhase || (!feasibilityPhase && i < N_DIST)) {
            val newSolution = neighbourSolutionGenerator.generateRandomPoint(bestSolution, k)
            val optimizedNewSolution = tabuSearch.apply(newSolution)
            lastSavedSolutions.add(optimizedNewSolution)
            if (lastSavedSolutions.size > NO_CHANGE_THRESHOLD) {
                lastSavedSolutions.removeAt(0)
            }
            adjustParameters()

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

    private fun adjustParameters() {
        val lastSolution = lastSavedSolutions.last()
        if (lastSolution.fitnessValue.fitness == lastSolution.cost) {
            thresholdCounter++
        } else {
            thresholdCounter--
        }
        if (thresholdCounter == NO_CHANGE_THRESHOLD) {
            ALPHA = ALPHA_DEFAULT
            BETA = BETA_DEFAULT
            GAMMA = GAMMA_DEFAULT
            log("PARAMETERS SET BACK TO DEFAULT = ($ALPHA, $BETA, $GAMMA)")
        } else if (thresholdCounter == -NO_CHANGE_THRESHOLD) {
            val numberOfCapacityViolations =
                lastSavedSolutions.stream().filter { it.fitnessValue.totalCapacityViolation > 0.0 }.toList().size
            val numberOfTimeWindowViolations =
                lastSavedSolutions.stream().filter { it.fitnessValue.totalTimeWindowViolation > 0.0 }.toList().size
            val numberOfBatteryCapacityViolations =
                lastSavedSolutions.stream().filter { it.fitnessValue.totalBatteryCapacityViolation > 0.0 }.toList().size
            ALPHA += PARAM_INCREASE_RATE * numberOfCapacityViolations
            BETA += PARAM_INCREASE_RATE * numberOfTimeWindowViolations
            GAMMA += PARAM_INCREASE_RATE * numberOfBatteryCapacityViolations
            log("PARAMETERS INCREASED = ($ALPHA, $BETA, $GAMMA)")
        }
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