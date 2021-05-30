package SimulatorExamples.HelloServers;

import Node.NodeFactory;
import Simulator.Simulator;
import Utils.Generator.UniformGenerator;
import Utils.Generator.WeibullGenerator;

public class Main {

    public static void main(String[] args) {
        myNode fixtureNode = new myNode();

        NodeFactory factory = new NodeFactory();
        factory.put(fixtureNode, 5);

        Simulator<myNode> simulation = new Simulator<myNode>(factory, "mockNetwork");
        simulation.churnSimulation(100000, new UniformGenerator(500, 1000), new UniformGenerator(2000, 3000));
    }
}
