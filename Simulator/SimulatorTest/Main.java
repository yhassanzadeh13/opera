package SimulatorTest;

import Simulator.Simulator;

import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        Simulator<myNode> sim = new Simulator<myNode>(new myNode(UUID.randomUUID()), 20);
        sim.start();

    }
}
