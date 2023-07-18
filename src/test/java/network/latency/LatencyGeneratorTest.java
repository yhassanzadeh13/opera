package network.latency;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import node.Identifier;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.Test;
import utils.Fixtures;


public class LatencyGeneratorTest {
  static final int NODE_CNT = 1000;

  @Test
  void TestDelayBidirectional() {
    ArrayList<Identifier> ids = Fixtures.identifierListFixture(NODE_CNT);
    LatencyGenerator latencyGenerator = new LatencyGenerator();

    for (int i = 0; i < NODE_CNT; i++) {
      for (int j = 0; j < i; j++) {
        double delay1 = latencyGenerator.getSimulatedLatency(ids.get(i), ids.get(j), true);
        double delay2 = latencyGenerator.getSimulatedLatency(ids.get(j), ids.get(i), true);
        assertEquals(delay1, delay2);
      }
    }
  }

  @Test
  void TestDelayDistribution() {
    final int ITERATIONS = 20000;
    final int EPS = 10;
    ArrayList<Identifier> ids = Fixtures.identifierListFixture(NODE_CNT);
    LatencyGenerator latencyGenerator = new LatencyGenerator();
    JDKRandomGenerator rand = new JDKRandomGenerator();

    int totalDelay = 0;

    for (int i = 0; i < ITERATIONS; i++) {
      int ind1 = rand.nextInt(NODE_CNT);
      int ind2 = rand.nextInt(NODE_CNT);
      double delay = latencyGenerator.getSimulatedLatency(ids.get(ind1), ids.get(ind2), true);
      totalDelay += delay;
    }
    double mean = (double) totalDelay / ITERATIONS;
    assertTrue(Math.abs(mean - LatencyGenerator.MeanLatency) <= EPS);

    // TODO: add std test
  }
}
