package simulatorexamples.helloservers;

import simulator.Simulator;
import underlay.UnderlayType;
import utils.generator.UniformGenerator;

public class Main {

  public static void main(String[] args) {
    MyNode fixtureNode = new MyNode();
    Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode, 5, UnderlayType.MOCK_NETWORK);
    simulation.churnSimulation(100000, new UniformGenerator(500, 1000), new UniformGenerator(2000, 3000));
  }
}
