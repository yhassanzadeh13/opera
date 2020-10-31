package SimulatorExamples.ServersBattle;

import Simulator.Simulator;

public class Main {

    public static void main(String[] args) {
        Contestant fixtureNode = new Contestant();
        Simulator<Contestant> sim = new Simulator<Contestant>(fixtureNode, 4, "tcp");
        sim.constantSimulation(10000);
    }
}
