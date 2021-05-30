package SimulatorExamples.ServersBattle;

import Node.NodeFactory;
import Simulator.Simulator;

public class Main {

    public static void main(String[] args) {
        Contestant fixtureNode = new Contestant();

        NodeFactory factory = new NodeFactory();
        factory.put(fixtureNode, 4);

        Simulator<Contestant> sim = new Simulator<Contestant>(factory, "tcp");
        sim.constantSimulation(10000);
    }
}
