package examples.serversbattle;

import network.UnderlayType;
import simulator.Factory;
import simulator.Recipe;
import simulator.Simulator;

/**
 * Servers battle is a simulator with 4 nodes.
 * In this simulation all nodes have levels and health and they fight over their level and health until one node wins.
 */
public class Main {
  /**
   * creates a new serversbattle simulation for 4 nodes.
   *
   * @param args nothing
   */
  public static void main(String[] args) {
    final String nameSpace = "example-contestant";
    Factory factory = new Factory();
    factory.addRecipe(new Recipe(new Contestant(), nameSpace, 4));
    Simulator simulator = new Simulator(factory, UnderlayType.TCP_PROTOCOL);
    simulator.constantSimulation(10000);
  }
}
