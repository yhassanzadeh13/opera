package SimulatorExamples.BullyAlgorithm;

import Simulator.Simulator;

public class Main {
    public static void main(String[] args) {
        /**
         * Starts a new bully algorithm with N fixture nods.
         * Bully algorithm is an election algorithm which aim to find the node with maximum UUID
         * and set it coordinator for all nodes with victory and election messages.
         */
        int N = 5;
        MyNode fixtureNode = new MyNode();
        Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode, N, "mockNetwork");
        fixtureNode.setSimulation(simulation);
        simulation.constantSimulation(10000);



    }

}
