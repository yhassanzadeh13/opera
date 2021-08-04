package simulatorexamples.serversbattle;

import simulator.Simulator;
import underlay.UnderlayType;

public class Main {

  public static void main(String[] args) {
    Contestant fixtureNode = new Contestant();
    Simulator<Contestant> sim = new Simulator<Contestant>(fixtureNode, 4, UnderlayType.TCP_PROTOCOL);
    sim.constantSimulation(10000);
  }
}
