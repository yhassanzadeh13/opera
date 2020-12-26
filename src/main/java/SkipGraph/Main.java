package SkipGraph;

import Simulator.Simulator;

public class Main {

    public static void main(String[] args) {
        RequestResponseLayer fixtureNode = new RequestResponseLayer();
        Simulator<RequestResponseLayer> sim = new Simulator<>(fixtureNode, 8, "tcp");
        sim.constantSimulation(1);
    }

}
