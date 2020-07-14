package SimulatorExamples.ServersBattle;

import Simulator.Simulator;

import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        Simulator<Contestant> sim = new Simulator<Contestant>(new Contestant(UUID.randomUUID()), 4);
        sim.start(10000);
    }
}
