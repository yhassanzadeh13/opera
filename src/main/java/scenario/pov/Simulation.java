package scenario.pov;

import network.UnderlayType;
import simulator.Factory;
import simulator.Recipe;
import simulator.Simulator;

/**
 * Simulation uses simulator to simulate an event with given number of given node and in a given duration of time.
 */
public class Simulation {
  /**
   * Simulates the event in a mock network type underlay with @numNodes times fixture node in @duration ms.
   */
  public static void main(String[] args) {
    int duration = 1000000;
    final String nameSpace = "demo-lightchain";
    Factory factory = new Factory();
    factory.addRecipe(new Recipe(new LightChainNode(), nameSpace, 21));
    Simulator simulator = new Simulator(factory, UnderlayType.MOCK_NETWORK);
    simulator.constantSimulation(duration);
  }

}
