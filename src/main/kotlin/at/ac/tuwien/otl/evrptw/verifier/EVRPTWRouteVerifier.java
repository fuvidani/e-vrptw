/*
 * Copyright 2017 Gerhard Hiermann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.ac.tuwien.otl.evrptw.verifier;

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance;
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance.Node;
import java.util.List;
import java.util.Locale;

public class EVRPTWRouteVerifier {
    private EVRPTWInstance instance;

    public EVRPTWRouteVerifier(EVRPTWInstance instance) {
        this.instance = instance;
    }

    public boolean verify(List<List<Node>> routes, double cost, boolean detailedMode) {
        double c = 0.0;
        for(int i = 0; i < routes.size(); i++) {
            c = c + calculate(routes.get(i), i + 1, detailedMode);
        }
        if(Double.isFinite(c))
            c = Double.valueOf(String.format(Locale.ENGLISH, "%.3f", c));
        if(detailedMode) {
            if(Double.isFinite(c)) {
                System.out.println(String.format(Locale.ENGLISH, "Total cost = %.3f", c));
                if(c != Double.valueOf(String.format(Locale.ENGLISH, "%.3f", cost))) {
                    System.out.println("WARNING: provided costs does not match with the value from the solution file!");
                }
            } else
                System.out.println("at least one route is infeasible!");
        }
        return Double.isFinite(c);
    }

    public double calculateTotalCost(List<List<Node>> routes, boolean detailedMode) {
        double c = 0.0;
        for(int i = 0; i < routes.size(); i++) {
            c = c + calculate(routes.get(i), i + 1, detailedMode);
        }
        return c;
    }

    public double calculate(List<Node> route, int id, boolean detailedMode) {
        int from = 1;
        int to = route.size() - 1;

        Node prevNode = route.get(0);

        double q = 0.0;
        double distance = 0.0;

        double y = instance.getVehicleEnergyCapacity();
        double yInfeasible = 0.0;

        double ST = instance.getServiceTime(prevNode);
        double D = ST;
        double TW = 0.0;
        double E = instance.getTimewindow(prevNode).getStart();
        double L = instance.getTimewindow(prevNode).getEnd();
        double deltaWT = 0.0;
        double deltaTW = 0.0;

        for(int i = from; i <= to; i++) {
            Node arriveAtNode = route.get(i);

            double timeInNode = 0.0;
            double travelTime = 0.0;
            double delta = 0.0;

            travelTime = instance.getTravelTime(prevNode, arriveAtNode);
            distance = distance + instance.getTravelDistance(prevNode, arriveAtNode);

            delta = D - TW + travelTime;

            double travelDistance = instance.getTravelDistance(prevNode, arriveAtNode);
            y -= (travelDistance * instance.getVehicleEnergyConsumption());
            if(y < 0.0) {
                yInfeasible -= y;
                y = 0.0;
            }

            deltaWT = Math.max(0, instance.getTimewindow(arriveAtNode).getStart() - delta - L);
            deltaTW = Math.max(0, E + delta - instance.getTimewindow(arriveAtNode).getEnd());

            timeInNode = instance.getServiceTime(arriveAtNode);
            if(instance.isRechargingStation(arriveAtNode)) {
                double refuelTime =
                    (instance.getVehicleEnergyCapacity() - y) * // used fuel
                        instance.getRechargingRate(arriveAtNode); // refill rate

                timeInNode = refuelTime;

                y = instance.getVehicleEnergyCapacity();
            } else {
                // do nothing
            }

            if(instance.isRechargingStation(arriveAtNode)) {
                delta += timeInNode;
                deltaWT = Math.max(0, instance.getTimewindow(arriveAtNode).getStart() - delta - L);
                deltaTW = Math.max(0, E + delta - instance.getTimewindow(arriveAtNode).getEnd());
            } else
                q = q + instance.getDemand(arriveAtNode);

            D = D + timeInNode + deltaWT + travelTime;
            ST += timeInNode;
            TW = TW + deltaTW;
            E = Math.max(instance.getTimewindow(arriveAtNode).getStart() - delta, E) - deltaWT;
            L = Math.min(instance.getTimewindow(arriveAtNode).getEnd() - delta, L) + deltaTW;

            prevNode = arriveAtNode;
        }

        double qInf = Math.max(0.0, q - instance.getVehicleCapacity());
        boolean infeasible = qInf > 0.0 || TW > 0.0 || yInfeasible > 0.0;
        if(detailedMode) {
            System.out.println(
                String.format(Locale.ENGLISH,
                    "Route %d: %s" + "\n" +
                        "- load:\t\t%4.0f" + "\n" +
                        "- distance:\t%7.3f" + "\n" +
                        "- duration:\t%7.3f" + "\n" +
                        "- violations:\t %s",
                    id, route.toString(), q, distance, D, (infeasible) ? "YES" : "none"
                ));
            if(infeasible) {
                if(qInf > 0.0)
                    System.out.println(String.format(Locale.ENGLISH, "-- load:\t%4.0f", qInf));
                if(TW > 0.0)
                    System.out.println(String.format(Locale.ENGLISH, "-- time:\t%7.3f", TW));
                if(yInfeasible > 0.0)
                    System.out.println(String.format(Locale.ENGLISH, "-- energy:\t%7.3f", yInfeasible));
            }
        }
        if(infeasible)
            return Double.POSITIVE_INFINITY;
        else return distance;
    }
}
