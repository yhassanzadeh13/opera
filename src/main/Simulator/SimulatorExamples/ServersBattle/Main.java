package SimulatorExamples.ServersBattle;

import Simulator.Simulator;

import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        Contestant fixtureNode = new Contestant();
        Simulator<Contestant> sim = new Simulator<Contestant>(fixtureNode, 4, "tcp");
        sim.start(10000);
    }
}
