package SimulatorExamples.HelloServers;

import Simulator.Simulator;
import underlay.MiddleLayer;

import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        myNode dummyNode = new myNode(null, null);
        Simulator<myNode> simulation = new Simulator<myNode>(dummyNode, 3, "tcp");
        simulation.start(3000);
    }
}
