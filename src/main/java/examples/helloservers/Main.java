package examples.helloservers;

import node.NodeFactory;
import simulator.Simulator;
import underlay.UnderlayType;
import utils.generator.UniformGenerator;

/**
 * Hello Servers is a simulator example which consist of 5 nodes.
 * When the simulation started nodes sends "Hello" message to other nodes.
 * If a node receives a "Hello" message it sends "Thank You" else sends "Hello" message back to that node.
 * Simulation goes like this infinitely.
 */
public class Main {
  /**
   * creates a new helloservers simulation for 5 nodes.
   *
   * @param args nothing
   */
  public static void main(String[] args) {
    MyNode fixtureNode = new MyNode();
    NodeFactory factory = new NodeFactory();
    factory.put(fixtureNode, 5);
    Simulator<MyNode> simulation = new Simulator<>(factory, UnderlayType.MOCK_NETWORK);
    simulation.churnSimulation(100000, new UniformGenerator(500, 1000), new UniformGenerator(2000, 3000));
  }
}
