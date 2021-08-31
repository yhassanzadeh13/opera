package metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import metrics.opera.OperaCollector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimulatorGaugeTest {
  static final int THREAD_CNT = 50;
  static final int ITERATIONS = 2000;
  static final double EPS = 0.01;
  static JDKRandomGenerator rand = new JDKRandomGenerator();
  CountDownLatch count;
  private MetricsCollector metricsCollector;


  @BeforeEach
  void setup() {
    metricsCollector = new OperaCollector();
  }

  @Test
  void valueTest() {
    assertTrue(metricsCollector.gauge().register("TestGauge"));
    ArrayList<UUID> allId = new ArrayList<>();
    while (allId.size() != THREAD_CNT) {
      allId.add(UUID.randomUUID());
    }
    count = new CountDownLatch(THREAD_CNT);

    // increment single entry
    UUID id = UUID.randomUUID();
    long tot = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      int v = rand.nextInt(1000);
      tot += v;
      metricsCollector.gauge().inc("TestGauge", id, v);
    }
    assertEquals(tot, metricsCollector.gauge().get("TestGauge", id));

    for (UUID nodeId : allId) {
      new Thread(() -> threadTest(nodeId, ITERATIONS)).start();
    }

    try {
      count.await();
    } catch (Exception e) {
      e.printStackTrace();
    }

    tot = 0;
    for (UUID nodeId : allId) {
      tot += metricsCollector.gauge().get("TestGauge", nodeId);
    }
    assertTrue(Math.abs(tot / (ITERATIONS * THREAD_CNT)) <= EPS);
  }

  void threadTest(UUID nodeId, int iterations) {
    while (iterations-- > 0) {
      if (rand.nextBoolean()) {
        assertTrue(metricsCollector.gauge().inc("TestGauge", nodeId));
      } else {
        assertTrue(metricsCollector.gauge().dec("TestGauge", nodeId));
      }
    }
    count.countDown();
  }

}