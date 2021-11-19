package metrics;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
  private static final int THREAD_CNT = 50;
  private static final int ITERATIONS = 50;
  private static final JDKRandomGenerator rand = new JDKRandomGenerator();
  private MetricsCollector metricsCollector;


  @BeforeEach
  public void setup() {
    metricsCollector = new OperaCollector();
    metricsCollector.counter().register(
        TEST_COUNTER,
        Constants.Namespace.TEST,
        SUBSYSTEM_COUNTER_TEST,
        "test counter help"
    );
  }

  /**
   * Tests correctness of counter for a single node,
   * increments value of counter several times, and evaluates final value.
   */
  @Test
  void singleNodeTest(){
    UUID id = UUID.randomUUID();
    long total = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      int v = rand.nextInt(1000);
      total += v;
      metricsCollector.counter().inc(TEST_COUNTER, id, v);
    }
    assertEquals(total, metricsCollector.counter().get(TEST_COUNTER, id));
  }

  /**
   * Tests correctness of counter collector for multiple nodes under concurrent setup.
   * Each node's counter value is incremented multiple times concurrently (with others), and
   * test checks the final value on each node.
   */
  @Test
  void multiNodeTest() {
    AtomicInteger assertionErrorCount = new AtomicInteger();
    ArrayList<UUID> allId = Fixtures.identifierListFixture(THREAD_CNT);
    CountDownLatch counterIncThread = new CountDownLatch(THREAD_CNT);

    for (UUID nodeId : allId) {
      new Thread(() -> {
        for(int j = 0; j < ITERATIONS; j++) {
          try {
            assertTrue(metricsCollector.counter().inc(TEST_COUNTER, nodeId));
          } catch (AssertionError e){
            assertionErrorCount.incrementAndGet();
          }
        }
        counterIncThread.countDown();
      }).start();
    }

    try {
      boolean onTime = counterIncThread.await(60, TimeUnit.SECONDS);
      assertTrue(onTime, "setting counters are not done on time");
    } catch (Exception e) {
      e.printStackTrace();
    }

    assertEquals(0, assertionErrorCount.get(), "unsuccessful threads");

    for (UUID nodeId : allId) {
     assertEquals(ITERATIONS, metricsCollector.counter().get(TEST_COUNTER, nodeId));
    }
  }
}