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

package at.ac.tuwien.otl.evrptw.loader;

import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance;
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance.BEVehicleType;
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance.Customer;
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance.Depot;
import at.ac.tuwien.otl.evrptw.dto.EVRPTWInstance.RechargingStation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class InstanceLoader {

    public String getBenchmarkName() {
        return "Schneider";
    }

    public EVRPTWInstance load(final String fileName) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/instances/"+ fileName + ".txt")));
        Data data = parseData(in);

        List<Depot> depots = new ArrayList<>();
        List<RechargingStation> rcstations = new ArrayList<>();
        List<Customer> customers = new ArrayList<>();

        for(Node node : data.nodes)
            switch(node.type) {
                case "d":
                    depots.add(parseDepotNode(node));
                    break;
                case "f":
                    rcstations.add(parseRechargeStationNode(node, data.properties.rechargeRate));
                    break;
                case "c":
                    customers.add(parseCustomerNode(node));
                    break;
                default:
                    System.out.println("unknown node type " + node.type + " - skipping entry");
            }

        BEVehicleType vType = new BEVehicleType(0, "BEV", data.properties.fuelCapacity,
            data.properties.fuelConsumption, data.properties.loadCapacity, 0000);
        return new EVRPTWInstance(fileName, depots.get(0), rcstations, customers, vType);
    }

    private static String nextLine(BufferedReader in, boolean skipEmpty) throws IOException {
        String line = "";
        while((line = in.readLine()) != null && line.trim().isEmpty() && skipEmpty) ;
        return line;
    }

    private static String nextLineValue(BufferedReader in) throws IOException {
        String line = "";
        while(line.trim().isEmpty() && (line = in.readLine()) != null) ;
        return line.substring(line.indexOf("/") + 1, line.lastIndexOf("/"));
    }

    private static Depot parseDepotNode(Node node) throws IOException {
        return new Depot(node.id, node.name, node.x, node.y, node.start, node.end);
    }

    private static RechargingStation parseRechargeStationNode(Node node, double rechargeRate) throws IOException {
        return new RechargingStation(node.id, node.name, node.x, node.y,
            node.start, node.end, rechargeRate);
    }

    private static Customer parseCustomerNode(Node node) throws IOException {
        return new Customer(node.id, node.name, node.x, node.y,
            node.start, node.end, node.demand, node.service);
    }

    private static List<Node> parseNodes(BufferedReader in) throws IOException {
        ArrayList<Node> nodes = new ArrayList<>();

        String line;
        for(int id = 0; !(line = nextLine(in, false).trim()).equals("") && line != null; id++)
            nodes.add(parseNode(id, line));

        return nodes;
    }

    private static Node parseNode(int id, String line) throws IOException {
        StringTokenizer tok = new StringTokenizer(line);
        Node node = new Node();
        node.id = id;
        node.name = tok.nextToken(); // id
        node.type = tok.nextToken();  // type
        node.x = Double.parseDouble(tok.nextToken()); // x
        node.y = Double.parseDouble(tok.nextToken()); // y
        node.demand = Double.parseDouble(tok.nextToken());   // demand
        node.start = Double.parseDouble(tok.nextToken()); // ready
        node.end = Double.parseDouble(tok.nextToken()); // due
        node.service = Double.parseDouble(tok.nextToken());// service
        return node;
    }

    private static Properties parseProperties(BufferedReader in) throws IOException {
        Properties props = new Properties();
        props.fuelCapacity = Double.parseDouble(nextLineValue(in)); //Q Vehicle fuel tank capacity /77.75/
        props.loadCapacity = Double.parseDouble(nextLineValue(in)); //C Vehicle load capacity /200.0/
        props.fuelConsumption = Double.parseDouble(nextLineValue(in)); //r fuel consumption rate /1.0/
        props.rechargeRate = Double.parseDouble(nextLineValue(in)); //g inverse refueling rate /3.47/
        props.avgVelocity = Double.parseDouble(nextLineValue(in)); //v average Velocity /1.0/
        return props;
    }

    private static Data parseData(BufferedReader in) throws IOException {
        Data data = new Data();
        nextLine(in, true); // Header
        data.nodes = parseNodes(in);
        data.properties = parseProperties(in);
        return data;
    }

    public static class Data {
        List<Node> nodes;
        Properties properties;
    }

    static class Node {
        int id;
        String name;
        String type;
        double x;
        double y;
        double demand;
        double start;
        double end;
        double service;
    }

    static class Properties {
        double fuelCapacity; //Q Vehicle fuel tank capacity /77.75/
        double loadCapacity; //C Vehicle load capacity /200.0/
        double fuelConsumption; //r fuel consumption rate /1.0/
        double rechargeRate; //g inverse refueling rate /3.47/
        double avgVelocity; //v average Velocity /1.0/
    }
}
