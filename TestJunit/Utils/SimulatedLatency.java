package Utils;

import Simulator.Simulator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimulatedLatency {

    static final int ITERATIONS = 20000;
    static final int EPS = 10;
    static JDKRandomGenerator rand = new JDKRandomGenerator();
    static final int NODE_CNT = 1000;
    final int SIMULATOR_MEAN = 159;
    final int SIMULATOR_STD = 96;

    final int delay[][] = new int[NODE_CNT][NODE_CNT];

    @Test
    void GaussianDelay(){
        FixtureNode fixtureNode = new FixtureNode();
        Simulator<FixtureNode> simulation = new Simulator<FixtureNode>(fixtureNode, NODE_CNT, "mockNetwork");

        // generate delay for each pair of nodes
        for(int i = 0; i < NODE_CNT; i++){
            for(int j = i + 1; j < NODE_CNT; j++){
                delay[i][j] = simulation.getSimulatedLatency(simulation.getAllID().get(i), simulation.getAllID().get(j), true);
            }


        }

        // assure that the delay is bidirectional by generating delay for each pair of nodes
        for(int i = 0; i < NODE_CNT; i++){
            for(int j = 0; j < i; j++){
                int delay1 = simulation.getSimulatedLatency(simulation.getAllID().get(i), simulation.getAllID().get(j), true);
                int delay2 = simulation.getSimulatedLatency(simulation.getAllID().get(j), simulation.getAllID().get(i), true);
                assertEquals(delay1, delay2);
            }
        }

        int total_delay = 0;
        // generate simulated delay
        for(int i = 0; i < ITERATIONS; i++){
            int ind1 = rand.nextInt(NODE_CNT);
            int ind2 = rand.nextInt(NODE_CNT);
            int delay = simulation.getSimulatedLatency(simulation.getAllID().get(ind1), simulation.getAllID().get(ind2), true);
            total_delay += delay;
        }
        double mean = (double) total_delay / ITERATIONS;
        assertTrue(Math.abs(mean - SIMULATOR_MEAN) <= EPS);
    }
}
