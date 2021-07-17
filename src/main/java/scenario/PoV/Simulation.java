package scenario.PoV;

import Simulator.Simulator;
import Underlay.UnderlayType;

public class Simulation {

  public static void main(String args[]) {

    int numNodes = 21;

    LightChainNode fixtureNode = new LightChainNode();
    Simulator<LightChainNode> simulator = new Simulator<>(fixtureNode, numNodes, UnderlayType.MOCK_NETWORK);

    simulator.constantSimulation(1000000);
  }

}
