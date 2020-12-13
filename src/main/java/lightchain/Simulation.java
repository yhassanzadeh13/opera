package lightchain;

import Simulator.Simulator;

public class Simulation {

  public static void main(String args[]) {

    int numNodes = 21;

    LightChainNode fixtureNode = new LightChainNode();
    Simulator<LightChainNode> simulator = new Simulator<>(fixtureNode, numNodes, "mockNetwork");

    simulator.constantSimulation(1000000);
  }

}
