package SimulatorExamples.BullyAlgorithm;

import Simulator.Simulator;
import Utils.Generator.UniformGenerator;
import Utils.Generator.WeibullGenerator;

public class Main {
    public static void main(String[] args) {
        MyNode fixtureNode = new MyNode();
        Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode, 5, "mockNetwork");
        simulation.churnSimulation(100000, new UniformGenerator(500, 1000), new UniformGenerator(2000, 3000));
    }

}
