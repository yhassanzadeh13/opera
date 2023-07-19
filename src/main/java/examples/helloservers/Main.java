package examples.helloservers;

import network.NetworkProtocol;
import simulator.Factory;
import simulator.Recipe;
import simulator.Simulator;
import utils.churn.ChurnGenerator;

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
    final String nameSpace = "example-helloservers";
    Factory factory = new Factory();
    factory.addRecipe(new Recipe(new MyNode(), nameSpace, 5));
    Simulator simulator = new Simulator(factory, NetworkProtocol.MOCK_NETWORK);

    ChurnGenerator interArrival = new utils.churn.GaussianGenerator(10_000, 20_000);
    ChurnGenerator sessionLength = new utils.churn.GaussianGenerator(10_000, 20_000);
    simulator.churnSimulation(1_000_000, interArrival, sessionLength);
  }
}
