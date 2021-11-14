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


class OperaCounterTest {
  private final static String TEST_COUNTER = "test_counter";
  private final static String SUBSYSTEM_COUNTER_TEST = "subsystem_counter_test";
  static final int THREAD_CNT = 50;
  static final int ITERATIONS = 50;
  static JDKRandomGenerator rand = new JDKRandomGenerator();
  private MetricsCollector metricsCollector;


  @BeforeEach
  public void setup() {
    metricsCollector = new OperaCollector();
  }

  /**
   * Tests correctness of counter for a single node,
   * increments value of counter several times, and evaluates final value.
   */
  @Test
  void singleNodeTest(){

  }

  @Test
  void valueTest() {
    metricsCollector.counter().register(
        TEST_COUNTER,
        Constants.Namespace.TEST,
        SUBSYSTEM_COUNTER_TEST,
        "test counter help"
    );
    ArrayList<UUID> allId = Fixtures.identifierListFixture(THREAD_CNT)
    CountDownLatch counterIncThread = new CountDownLatch(THREAD_CNT);

    // increment single entry
    UUID id = UUID.randomUUID();
    long tot = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      int v = rand.nextInt(1000);
      tot += v;
      metricsCollector.counter().inc("testCounter", id, v);
    }
    assertEquals(tot, metricsCollector.counter().get("testCounter", id));

    for (UUID nodeId : allId) {
      new Thread() {
        @Override
        public void run() {
          threadtestCounter(nodeId, ITERATIONS);
        }
      }.start();
    }

    try {
      counterIncThread.await();
    } catch (Exception e) {
      e.printStackTrace();
    }

    tot = 0;
    for (UUID nodeId : allId) {
      tot += metricsCollector.counter().get("testCounter", nodeId);
    }
    assertEquals(ITERATIONS * THREAD_CNT, tot);
  }

  void threadtestCounter(UUID nodeId, int iterations) {
    while (iterations-- > 0) {
      assertTrue(metricsCollector.counter().inc("testCounter", nodeId));
    }
    count.countDown();
  }
}