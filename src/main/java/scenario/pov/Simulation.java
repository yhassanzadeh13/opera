package scenario.pov;

import node.BaseNode;
import node.NodeFactory;
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
    RegistryNode registryNode = new RegistryNode();

    NodeFactory factory = new NodeFactory();
    factory.put(registryNode, 1);
    factory.put(fixtureNode, 21);

    Simulator<BaseNode> simulator = new Simulator<>(factory, UnderlayType.MOCK_NETWORK);
    simulator.constantSimulation(duration);
  }

}
