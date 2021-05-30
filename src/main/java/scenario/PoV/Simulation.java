package scenario.PoV;

import Node.BaseNode;
import Node.NodeFactory;
import Simulator.Simulator;

public class Simulation {

  public static void main(String args[]) {

    LightChainNode fixtureNode = new LightChainNode();
    RegistryNode registryNode = new RegistryNode();

    NodeFactory factory = new NodeFactory();
    // putting the registry node first, since the first node created is determined as registry
    factory.put(registryNode, 1);
    factory.put(fixtureNode, 21);

    Simulator<BaseNode> simulator = new Simulator<>(factory, "mockNetwork");
    simulator.constantSimulation(1000000);
  }

}
