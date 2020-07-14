package SimulatorExamples.HelloServers;

import Simulator.Simulator;

import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        Simulator<myNode> simulation = new Simulator<myNode>(new myNode(UUID.randomUUID()), 20);
        simulation.start(10000);
    }
}
