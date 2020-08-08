package SimulatorExamples.ServersBattle;

import Simulator.Simulator;

import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        Contestant dummyNode = new Contestant(null, null);
        Simulator<Contestant> sim = new Simulator<Contestant>(dummyNode, 4, "tcp");
        sim.start(10000);
    }
}
