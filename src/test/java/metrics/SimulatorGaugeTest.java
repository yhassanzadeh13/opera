package metrics;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import metrics.opera.OperaCollector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Fixtures;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorGaugeTest {
  private static final String TEST_GAUGE = "test_gauge";
  private static final String SUBSYSTEM_GAUGE_TEST = "subsystem_gauge_test";
  private static final int THREAD_CNT = 50;
  private static final int ITERATIONS = 2000;
  private static final double EPS = 0.01;
  private static final JDKRandomGenerator rand = new JDKRandomGenerator();
  private CountDownLatch count;
  private MetricsCollector metricsCollector;


  @BeforeEach
  void setup() {
    metricsCollector = new OperaCollector();
    metricsCollector.gauge().register(
        TEST_GAUGE,
        Constants.Namespace.TEST,
        SUBSYSTEM_GAUGE_TEST,
        "test gauge help");
  }

  /**
   * Tests correctness of gauge for a single node.
   * Sets value of gauge several times, and evaluates
   * only the last value successfully overwrites all
   * preceding sets.
   */
  @Test
  void singleNodeTest() {
    UUID id = UUID.randomUUID();

    long finalValue = 0;

    // sets the gauge value several times
    for (int i = 0; i < ITERATIONS; i++) {
      int v = rand.nextInt(1000);
      finalValue = v;
      metricsCollector.gauge().set(TEST_GAUGE, id, v);
    }

    // only final value for gauge should be recorded and all interim values
    // must be overwritten.
    assertEquals(finalValue, metricsCollector.gauge().get(TEST_GAUGE, id));
  }

  /**
   * Tests correctness of gauge collector for multiple nodes under concurrent setup.
   * Each node's guage value is set to its global index, and evaluates the correctness
   * of set.
   */
  @Test
  void multiNodeTest() {
    ArrayList<UUID> allId = Fixtures.identifierListFixture(THREAD_CNT);
    count = new CountDownLatch(THREAD_CNT);

    for (int i = 0; i < allId.size(); i++) {
      int finalI = i;
      new Thread(() -> {
        // sets gauge value for each metric as its corresponding node index.
        // TODO: assertion errors.
        assertTrue(metricsCollector.gauge().set(TEST_GAUGE, allId.get(finalI), finalI));
        count.countDown();
      }).start();
    }

    try {
      // TODO: timeout
      count.await();
    } catch (Exception e) {
      e.printStackTrace();
    }

    for (int i = 0; i < allId.size(); i++) {
      assertEquals(metricsCollector.gauge().get(TEST_GAUGE, allId.get(i)), i);
    }

  }
}