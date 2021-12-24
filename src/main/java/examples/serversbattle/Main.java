package examples.serversbattle;

import simulator.Factory;
import simulator.Recipe;
import simulator.Simulator;
import underlay.UnderlayType;

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
    final String nameSpace = "demo-contestant";
    Factory factory = new Factory();
    factory.AddRecipe(new Recipe(new Contestant(), nameSpace, 4));
    Simulator simulator = new Simulator(factory, UnderlayType.TCP_PROTOCOL);
    simulator.constantSimulation(10000);
  }
}
