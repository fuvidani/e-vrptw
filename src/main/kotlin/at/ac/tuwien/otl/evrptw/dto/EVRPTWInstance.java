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

package at.ac.tuwien.otl.evrptw.dto;

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance.Node.TimeWindow;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EVRPTWInstance {
    private String name;
    private Depot depot;
    private List<Customer> customers;
    private Map<String, Customer> customerMap;
    private List<RechargingStation> rechargingStations;
    private Map<String, RechargingStation> rechargingStationMap;
    private List<Node> nodes;
    private BEVehicleType vehicleType;

    public EVRPTWInstance(String name, Depot depot, List<RechargingStation> rechargingStations,
        List<Customer> customers, BEVehicleType vehicleType) {
        this.name = name;
        this.depot = depot;

        this.customerMap = new HashMap<>(customers.size() + 1, 1.0f);
        for(Customer c : customers) {
            this.customerMap.put(c.name, c);
        }
        this.customers = customers;

        this.rechargingStationMap = new HashMap<>(rechargingStations.size() + 1, 1.0f);
        for(RechargingStation r : rechargingStations) {
            this.rechargingStationMap.put(r.name, r);
        }
        this.rechargingStations = rechargingStations;

        this.nodes = new ArrayList<>(rechargingStations.size() + customers.size() + 1);
        nodes.add(depot);
        nodes.addAll(rechargingStations);
        nodes.addAll(customers);
        this.vehicleType = vehicleType;
    }

    public String getName() {
        return name;
    }

    public boolean isDepot(Node n) {
        return n.id == 0;
    }

    public boolean isRechargingStation(Node n) {
        return n.id > 0 && n.id < rechargingStations.size() + 1;
    }

    private boolean isMandatory(Node n) {
        return n.id > rechargingStations.size();
    }

    RechargingStation getRechargingStation(String name) {
        return rechargingStationMap.get(name);
    }

    public RechargingStation getRechargingStation(Node n) {
        return rechargingStations.get(n.id - 1);
    }

    public Customer getCustomer(String name) {
        return customerMap.get(name);
    }

    public Customer getCustomer(Node n) {
        return customers.get(n.id - (rechargingStations.size() + 1));
    }

    public Node getDepot() {
        return depot;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<RechargingStation> getRechargingStations() {
        return rechargingStations;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int getNumNodes() {
        return nodes.size();
    }

    public int getNumCustomers() {
        return customers.size();
    }

    public int getMaxVehicles() {
        return 12;
    }

    public double getVehicleCapacity() {
        return vehicleType.loadCapacity;
    }

    public double getVehicleEnergyCapacity() {
        return vehicleType.energyCapacity;
    }

    public Map<String, Customer> getCustomerMap() {
        return customerMap;
    }

    public Map<String, RechargingStation> getRechargingStationMap() {
        return rechargingStationMap;
    }

    public BEVehicleType getVehicleType() {
        return vehicleType;
    }

    public double getVehicleEnergyConsumption() {
        return vehicleType.energyConsumption;
    }

    public double calculateEuclidianDistance(Node n1, Node n2) {
        final Node.Location n1Loc = getLocation(n1);
        final Node.Location n2Loc = getLocation(n2);
        return Math.sqrt(Math.pow(n1Loc.x - n2Loc.x, 2)
            + Math.pow(n1Loc.y - n2Loc.y, 2));
    }

    public double getTravelDistance(Node n1, Node n2) {
        return calculateEuclidianDistance(n1, n2);
    }

    public double getTravelTime(Node n1, Node n2) {
        return getTravelDistance(n1, n2);
    }

    public double getDemand(Node node) {
        if(!isMandatory(node)) return 0;
        return customers.get((int) node.id - (rechargingStations.size() + 1)).demand;
    }

    public Node.Location getLocation(Node node) {
        if(node.id == depot.id) return depot.location;
        else if(node.id > rechargingStations.size()) return customers
            .get((int) node.id - (rechargingStations.size() + 1)).location;
        else
            return rechargingStations.get(node.id - 1).location;
    }

    public TimeWindow getTimewindow(Node node) {
        if(node.id == depot.id) return depot.timeWindow;
        else if(node.id > rechargingStations.size()) return customers
            .get((int) node.id - (rechargingStations.size() + 1)).timeWindow;
        else
            return rechargingStations.get(node.id - 1).timeWindow;
    }

    public double getServiceTime(Node node) {
        if(!isMandatory(node)) return 0;
        return customers.get((int) node.id - (rechargingStations.size() + 1)).serviceTime;
    }

    public double getRechargingRate(Node node) {
        if(!isRechargingStation(node)) return 0;
        return rechargingStations.get(node.id - 1).rechargingRate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof EVRPTWInstance)) return false;

        final EVRPTWInstance that = (EVRPTWInstance) o;

        return new EqualsBuilder()
                .append(getName(), that.getName())
                .append(getDepot(), that.getDepot())
                .append(getCustomers(), that.getCustomers())
                .append(getCustomerMap(), that.getCustomerMap())
                .append(getRechargingStations(), that.getRechargingStations())
                .append(getRechargingStationMap(), that.getRechargingStationMap())
                .append(getNodes(), that.getNodes())
                .append(getVehicleType(), that.getVehicleType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getDepot())
                .append(getCustomers())
                .append(getCustomerMap())
                .append(getRechargingStations())
                .append(getRechargingStationMap())
                .append(getNodes())
                .append(getVehicleType())
                .toHashCode();
    }

    public static class Node {
        final int id;

        public int getId() {
            return id;
        }

        Node(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;

            if (!(o instanceof Node)) return false;

            final Node node = (Node) o;

            return new EqualsBuilder()
                    .append(getId(), node.getId())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(getId())
                    .toHashCode();
        }

        public static class Location {
            final double x, y;

            public Location(double x, double y) {
                this.x = x;
                this.y = y;
            }

            public double getX() {
                return x;
            }

            public double getY() {
                return y;
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) return true;

                if (!(o instanceof Location)) return false;

                final Location location = (Location) o;

                return new EqualsBuilder()
                        .append(getX(), location.getX())
                        .append(getY(), location.getY())
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(17, 37)
                        .append(getX())
                        .append(getY())
                        .toHashCode();
            }
        }

        public static class TimeWindow {
            final double start, end;

            private TimeWindow(double start, double end) {
                this.start = start;
                this.end = end;
            }

            public double getStart() {
                return start;
            }

            public double getEnd() {
                return end;
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) return true;

                if (!(o instanceof TimeWindow)) return false;

                final TimeWindow that = (TimeWindow) o;

                return new EqualsBuilder()
                        .append(getStart(), that.getStart())
                        .append(getEnd(), that.getEnd())
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(17, 37)
                        .append(getStart())
                        .append(getEnd())
                        .toHashCode();
            }
        }
    }

    public static class Customer extends Node {
        final String name;
        final Location location;
        final double demand;
        final TimeWindow timeWindow;
        final double serviceTime;

        public Customer(int id, String name, double x, double y, double start, double end,
            double demand,
            double serviceTime) {
            super(id);
            this.name = name;
            this.location = new Location(x, y);
            this.demand = demand;
            this.timeWindow = new TimeWindow(start, end);
            this.serviceTime = serviceTime;
        }

        public String getName() {
            return name;
        }

        public Location getLocation() {
            return location;
        }

        public double getDemand() {
            return demand;
        }

        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        public double getServiceTime() {
            return serviceTime;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;

            if (!(o instanceof Customer)) return false;

            final Customer customer = (Customer) o;

            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(getDemand(), customer.getDemand())
                    .append(getServiceTime(), customer.getServiceTime())
                    .append(getName(), customer.getName())
                    .append(getLocation(), customer.getLocation())
                    .append(getTimeWindow(), customer.getTimeWindow())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .appendSuper(super.hashCode())
                    .append(getName())
                    .append(getLocation())
                    .append(getDemand())
                    .append(getTimeWindow())
                    .append(getServiceTime())
                    .toHashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Depot extends Node {
        final String name;
        final Location location;
        final TimeWindow timeWindow;

        public Depot(int id, String name, double x, double y, double start, double end) {
            super(id);
            this.name = name;
            this.location = new Location(x, y);
            this.timeWindow = new TimeWindow(start, end);
        }

        public String getName() {
            return name;
        }

        public Location getLocation() {
            return location;
        }

        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;

            if (!(o instanceof Depot)) return false;

            final Depot depot = (Depot) o;

            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(getName(), depot.getName())
                    .append(getLocation(), depot.getLocation())
                    .append(getTimeWindow(), depot.getTimeWindow())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .appendSuper(super.hashCode())
                    .append(getName())
                    .append(getLocation())
                    .append(getTimeWindow())
                    .toHashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class RechargingStation extends Node {
        final String name;
        final Location location;
        final TimeWindow timeWindow;
        final double rechargingRate;

        public RechargingStation(int id, String name, double x, double y, double start, double end,
            double rechargingRate) {
            super(id);
            this.name = name;
            this.location = new Location(x, y);
            this.timeWindow = new TimeWindow(start, end);
            this.rechargingRate = rechargingRate;
        }

        public String getName() {
            return name;
        }

        public Location getLocation() {
            return location;
        }

        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        public double getRechargingRate() {
            return rechargingRate;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;

            if (!(o instanceof RechargingStation)) return false;

            final RechargingStation that = (RechargingStation) o;

            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(getRechargingRate(), that.getRechargingRate())
                    .append(getName(), that.getName())
                    .append(getLocation(), that.getLocation())
                    .append(getTimeWindow(), that.getTimeWindow())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .appendSuper(super.hashCode())
                    .append(getName())
                    .append(getLocation())
                    .append(getTimeWindow())
                    .append(getRechargingRate())
                    .toHashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static class VehicleType {
        final int id;
        final String name;

        private VehicleType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;

            if (!(o instanceof VehicleType)) return false;

            final VehicleType that = (VehicleType) o;

            return new EqualsBuilder()
                    .append(id, that.id)
                    .append(name, that.name)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(id)
                    .append(name)
                    .toHashCode();
        }
    }

    public static class BEVehicleType extends VehicleType {
        final double energyCapacity, energyConsumption, loadCapacity, fixedCosts;

        public BEVehicleType(int id, String name, double fuelCapacity, double fuelConsumption,
            double loadCapacity,
            double fixedCosts) {
            super(id, name);
            this.energyCapacity = fuelCapacity;
            this.energyConsumption = fuelConsumption;
            this.loadCapacity = loadCapacity;
            this.fixedCosts = fixedCosts;
        }

        public double getLoadCapacity() {
            return loadCapacity;
        }

        public double getEnergyCapacity() {
            return energyCapacity;
        }

        public double getEnergyConsumption() {
            return energyConsumption;
        }

        public double getFixedCosts() {
            return fixedCosts;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;

            if (!(o instanceof BEVehicleType)) return false;

            final BEVehicleType that = (BEVehicleType) o;

            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(getEnergyCapacity(), that.getEnergyCapacity())
                    .append(getEnergyConsumption(), that.getEnergyConsumption())
                    .append(getLoadCapacity(), that.getLoadCapacity())
                    .append(getFixedCosts(), that.getFixedCosts())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .appendSuper(super.hashCode())
                    .append(getEnergyCapacity())
                    .append(getEnergyConsumption())
                    .append(getLoadCapacity())
                    .append(getFixedCosts())
                    .toHashCode();
        }
    }
}
