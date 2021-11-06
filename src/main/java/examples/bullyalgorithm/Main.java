package examples.bullyalgorithm;

import simulator.Simulator;
import underlay.UnderlayType;

/**
 * Bully algorithm is an election algorithm which aim to find the node with maximum UUID
 * and set it coordinator for all nodes with victory and election messages.
 */
public class Main {
  /**
   * Starts a new bully algorithm with n fixture nods.
   */
  public static void main(String[] args) {
    int n = 5;
    MyNode fixtureNode = new MyNode();
    Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode, n, UnderlayType.MOCK_NETWORK);
    simulation.constantSimulation(10000);


  }

}
