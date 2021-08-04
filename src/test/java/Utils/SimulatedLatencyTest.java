package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.Test;
import simulator.Simulator;
import underlay.UnderlayType;

public class SimulatedLatencyTest {

  static final int ITERATIONS = 20000;
  static final int EPS = 10;
  static JDKRandomGenerator rand = new JDKRandomGenerator();
  static final int NODE_CNT = 1000;
  final int simulatorMean = 159;
  final int simulatorStd = 96;

  final int[][] delay = new int[NODE_CNT][NODE_CNT];

  @Test
  void gaussianDelay() {
    FixtureNode fixtureNode = new FixtureNode();
    Simulator<FixtureNode> simulation = new Simulator<FixtureNode>(fixtureNode, NODE_CNT, UnderlayType.MOCK_NETWORK);

    // generate delay for each pair of nodes
    for (int i = 0; i < NODE_CNT; i++) {
      for (int j = i + 1; j < NODE_CNT; j++) {
        delay[i][j] = simulation.getSimulatedLatency(simulation.getAllId().get(i), simulation.getAllId().get(j), true);
      }


    }

    // assure that the delay is bidirectional by generating delay for each pair of nodes
    for (int i = 0; i < NODE_CNT; i++) {
      for (int j = 0; j < i; j++) {
        int delay1 = simulation.getSimulatedLatency(simulation.getAllId().get(i), simulation.getAllId().get(j), true);
        int delay2 = simulation.getSimulatedLatency(simulation.getAllId().get(j), simulation.getAllId().get(i), true);
        assertEquals(delay1, delay2);
      }
    }

    int totalDelay = 0;
    // generate simulated delay
    for (int i = 0; i < ITERATIONS; i++) {
      int ind1 = rand.nextInt(NODE_CNT);
      int ind2 = rand.nextInt(NODE_CNT);
      int delay = simulation.getSimulatedLatency(simulation.getAllId().get(ind1),
            simulation.getAllId().get(ind2),
            true);
      totalDelay += delay;
    }
    double mean = (double) totalDelay / ITERATIONS;
    assertTrue(Math.abs(mean - simulatorMean) <= EPS);
  }
}
