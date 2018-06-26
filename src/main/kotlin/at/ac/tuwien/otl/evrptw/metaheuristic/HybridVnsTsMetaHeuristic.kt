package at.ac.tuwien.otl.evrptw.metaheuristic

/* ktlint-disable no-wildcard-imports */
import at.ac.tuwien.otl.evrptw.Main
import at.ac.tuwien.otl.evrptw.dto.*
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.ALPHA
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.ALPHA_DEFAULT
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.BETA
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.BETA_DEFAULT
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.COOLING_FACTOR
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.FIBONACCI
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.GAMMA
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.GAMMA_DEFAULT
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.NO_CHANGE_THRESHOLD
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_DIST
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.N_FEAS
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.VIOLATION_FACTOR_ABSOLUTE_MIN
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.VIOLATION_FACTOR_BELOW_MIN_DESCENT_RATE
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.VIOLATION_FACTOR_DECREASE_RATE
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.VIOLATION_FACTOR_INCREASE_RATE
import at.ac.tuwien.otl.evrptw.metaheuristic.Constants.Companion.VIOLATION_FACTOR_MIN
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
    private var infeasibleSequenceCounter = 0
    private var feasibleSequenceCounter = 0

    override fun improveSolution(evrptwSolution: EVRPTWSolution): EVRPTWSolution {
        temperature = Main.instanceToInitTemperatureMap[evrptwSolution.instance.name]!!
        resetParameters()

        var bestFeasibleSolution = evrptwSolution
        var bestSolution = evrptwSolution
        var k = 1
        var i = 0
        var feasibilityPhase = true

        while (feasibilityPhase || (!feasibilityPhase && i < N_DIST)) {
            println("VNS Iteration: $i")
            val newSolution = neighbourSolutionGenerator.generateRandomPoint(bestSolution, k)
            val optimizedNewSolution = tabuSearch.apply(newSolution)
            lastSavedSolutions.add(optimizedNewSolution)
            if (lastSavedSolutions.size > NO_CHANGE_THRESHOLD) {
                lastSavedSolutions.removeAt(0)
            }
            adjustParameters()

            if (acceptSimulatedAnnealing(optimizedNewSolution, bestSolution)) {
                log("$$$ New best solution $$$. Cost: ${optimizedNewSolution.cost}" + "Cap-Violation: ${optimizedNewSolution.fitnessValue.totalCapacityViolation}, " +
                        "TW-Violation: ${optimizedNewSolution.fitnessValue.totalTimeWindowViolation}, " +
                        "Bat-Violation: ${optimizedNewSolution.fitnessValue.totalBatteryCapacityViolation}, " +
                        "Fitness: ${optimizedNewSolution.fitnessValue.fitness}")
                bestSolution = optimizedNewSolution
                if (optimizedNewSolution.fitnessValue.fitness == optimizedNewSolution.cost && optimizedNewSolution.cost < bestFeasibleSolution.cost) {
                    log("New best feasible solution with cost ${optimizedNewSolution.cost}")
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

    private fun resetParameters() {
        ALPHA = ALPHA_DEFAULT
        BETA = BETA_DEFAULT
        GAMMA = GAMMA_DEFAULT
        lastSavedSolutions.clear()
        thresholdCounter = 0
        infeasibleSequenceCounter = 0
        feasibleSequenceCounter = 0
    }

    private fun adjustParameters() {
        val lastSolution = lastSavedSolutions.last()
        if (lastSolution.fitnessValue.fitness == lastSolution.cost) {
            thresholdCounter++
        } else {
            thresholdCounter--
        }
        if (thresholdCounter == NO_CHANGE_THRESHOLD) {
            decreaseRateAlpha()
            decreaseRateBeta()
            decreaseRateGamma()
            /*ALPHA = if (ALPHA > ALPHA_DEFAULT) ALPHA_DEFAULT else Math.max(ALPHA - VIOLATION_FACTOR_DECREASE_RATE, VIOLATION_FACTOR_MIN)
            BETA = if (BETA > BETA_DEFAULT) BETA_DEFAULT else Math.max(BETA - VIOLATION_FACTOR_DECREASE_RATE, VIOLATION_FACTOR_MIN)
            GAMMA = if (GAMMA > GAMMA_DEFAULT) GAMMA_DEFAULT else Math.max(GAMMA - VIOLATION_FACTOR_DECREASE_RATE, VIOLATION_FACTOR_MIN)*/
            thresholdCounter = 0
            infeasibleSequenceCounter = 0
            feasibleSequenceCounter++
            log("-- VIOLATION FACTORS DECREASED = ($ALPHA, $BETA, $GAMMA). Fibonacci multiplier: ${FIBONACCI[feasibleSequenceCounter--]}")
        } else if (thresholdCounter == -NO_CHANGE_THRESHOLD) {
            val numberOfCapacityViolations =
                lastSavedSolutions.stream().filter { it.fitnessValue.totalCapacityViolation > 0.0 }.toList().size
            val numberOfTimeWindowViolations =
                lastSavedSolutions.stream().filter { it.fitnessValue.totalTimeWindowViolation > 0.0 }.toList().size
            val numberOfBatteryCapacityViolations =
                lastSavedSolutions.stream().filter { it.fitnessValue.totalBatteryCapacityViolation > 0.0 }.toList().size
            val indexOfMultiplier = infeasibleSequenceCounter % FIBONACCI.size
            ALPHA += VIOLATION_FACTOR_INCREASE_RATE * numberOfCapacityViolations * FIBONACCI[indexOfMultiplier]
            BETA += VIOLATION_FACTOR_INCREASE_RATE * numberOfTimeWindowViolations * FIBONACCI[indexOfMultiplier]
            GAMMA += VIOLATION_FACTOR_INCREASE_RATE * numberOfBatteryCapacityViolations * FIBONACCI[indexOfMultiplier]
            thresholdCounter = 0
            infeasibleSequenceCounter++
            feasibleSequenceCounter = 0
            log("++ VIOLATION FACTORS INCREASED = ($ALPHA, $BETA, $GAMMA). Fibonacci multiplier: ${FIBONACCI[infeasibleSequenceCounter--]}")
        }
    }

    private fun decreaseRateAlpha() {
        ALPHA = when {
            ALPHA > ALPHA_DEFAULT -> ALPHA_DEFAULT
            ALPHA <= VIOLATION_FACTOR_MIN -> Math.max(ALPHA - VIOLATION_FACTOR_BELOW_MIN_DESCENT_RATE, VIOLATION_FACTOR_ABSOLUTE_MIN)
            else -> Math.max(ALPHA - VIOLATION_FACTOR_DECREASE_RATE, VIOLATION_FACTOR_MIN)
        }
    }

    private fun decreaseRateBeta() {
        BETA = when {
            BETA > BETA_DEFAULT -> BETA_DEFAULT
            BETA <= VIOLATION_FACTOR_MIN -> Math.max(BETA - VIOLATION_FACTOR_BELOW_MIN_DESCENT_RATE, VIOLATION_FACTOR_ABSOLUTE_MIN)
            else -> Math.max(BETA - VIOLATION_FACTOR_DECREASE_RATE, VIOLATION_FACTOR_MIN)
        }
    }

    private fun decreaseRateGamma() {
        GAMMA = when {
            GAMMA > GAMMA_DEFAULT -> GAMMA_DEFAULT
            GAMMA <= VIOLATION_FACTOR_MIN -> Math.max(GAMMA - VIOLATION_FACTOR_BELOW_MIN_DESCENT_RATE, VIOLATION_FACTOR_ABSOLUTE_MIN)
            else -> Math.max(GAMMA - VIOLATION_FACTOR_DECREASE_RATE, VIOLATION_FACTOR_MIN)
        }
    }

    private fun acceptSimulatedAnnealing(optimizedNewSolution: EVRPTWSolution, bestSolution: EVRPTWSolution): Boolean {
        val accept: Boolean
        accept = if (optimizedNewSolution.fitnessValue.fitness == optimizedNewSolution.cost) {
            when {
                bestSolution.fitnessValue.fitness != bestSolution.cost -> {
                    log("Accepting new feasible solution, because current best is infeasible")
                    true
                }
                optimizedNewSolution.fitnessValue.fitness < bestSolution.fitnessValue.fitness -> {
                    log("Accepting new feasible solution, because new is better than current best feasible")
                    true
                }
                else -> false
            }
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