package SimulatorExamples.HelloServers;

import Simulator.Simulator;
import Utils.Generator.UniformGenerator;
import Utils.Generator.WeibullGenerator;

public class Main {

    public static void main(String[] args) {
        myNode fixtureNode = new myNode();
        Simulator<myNode> simulation = new Simulator<myNode>(fixtureNode, 5, "mockNetwork");
        simulation.constantSimulation(10000);
        simulation.churnSimulation(10000, new UniformGenerator(1000, 3000),
                new WeibullGenerator(1000, 3000, 1, 4));
    }
}
