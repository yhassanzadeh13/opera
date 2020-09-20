package SimulatorExamples.HelloServers;

import Simulator.Simulator;
import underlay.MiddleLayer;

import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        myNode fixtureNode = new myNode();
        Simulator<myNode> simulation = new Simulator<myNode>(fixtureNode, 5, "mockNetwork");
        simulation.start(10000);
    }
}
