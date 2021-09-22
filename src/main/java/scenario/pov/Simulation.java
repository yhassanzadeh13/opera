package scenario.pov;

import simulator.Simulator;
import underlay.UnderlayType;

/**
 * Simulation uses simulator to simulate an event with given number of given node and in a given duration of time.
 */
public class Simulation {
  /**
   * Simulates the event in a mock network type underlay with @numNodes times fixture node in @duration ms.
   */
  public static void main(String[] args) {

    int numNodes = 21;
    int duration = 1000000;

    LightChainNode fixtureNode = new LightChainNode();
    Simulator<LightChainNode> simulator = new Simulator<>(fixtureNode, numNodes, UnderlayType.MOCK_NETWORK);

    simulator.constantSimulation(duration);
  }

}
