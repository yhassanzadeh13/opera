package simulatorexamples.serversbattle;

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
    Contestant fixtureNode = new Contestant();
    Simulator<Contestant> sim = new Simulator<Contestant>(fixtureNode, 4, UnderlayType.TCP_PROTOCOL);
    sim.constantSimulation(10000);
  }
}
