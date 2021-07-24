package SimulatorExamples.ServersBattle;

import Simulator.Simulator;
import Underlay.UnderlayType;

public class Main {

    public static void main(String[] args) {
        Contestant fixtureNode = new Contestant();
        Simulator<Contestant> sim = new Simulator<Contestant>(fixtureNode, 4, UnderlayType.TCP_PROTOCOL);
        sim.constantSimulation(10000);
    }
}
