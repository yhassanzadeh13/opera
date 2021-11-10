package metrics;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import metrics.opera.OperaCollector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class OperaCounterTest {

  static final int THREAD_CNT = 50;
  static final int ITERATIONS = 50;
  static JDKRandomGenerator rand = new JDKRandomGenerator();
  CountDownLatch count;
  private MetricsCollector metricsCollector;


  @BeforeEach
  public void setup() {
    metricsCollector = new OperaCollector();
  }

  @Test
  void valueTest() {
    final String TEST_COUNTER = "test_counter";
    final String SUBSYSTEM_COUNTER_TEST = "subsystem_counter_test";

    metricsCollector.counter().register(
        TEST_COUNTER,
        Constants.Namespace.TEST,
        SUBSYSTEM_COUNTER_TEST,
        " "
    );
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
      count.await();
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