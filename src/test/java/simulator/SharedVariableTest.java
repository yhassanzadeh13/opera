package simulator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import node.Identifier;
import node.IdentifierGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.SharedVariable;

class SharedVariableTest {

  // increase the sleeping duration when increasing the thread count and iterations
  static final int THREAD_CNT = 50;
  static final int ITERATIONS = 30;

  static JDKRandomGenerator rand = new JDKRandomGenerator();
  static HashMap<Identifier, Long> values = new HashMap<>();
  CountDownLatch count;

  @Test
  void read_write() {
    // test unregistered variable
    Assertions.assertFalse(SharedVariable.getInstance().write(IdentifierGenerator.newIdentifier(), "Test", 5));

    ArrayList<Identifier> allId = new ArrayList<>();
    while (allId.size() != THREAD_CNT) {
      allId.add(IdentifierGenerator.newIdentifier());
    }
    count = new CountDownLatch(THREAD_CNT);


    // register new variable
    assertTrue(SharedVariable.getInstance().register("Test", allId));
    // attempt to write from node 1
    assertFalse(SharedVariable.getInstance().write(allId.get(0), "Test", 5));
    // acquire lock then write
    assertTrue(SharedVariable.getInstance().requestLock(allId.get(0), "Test"));
    assertTrue(SharedVariable.getInstance().write(allId.get(0), "Test", 5));
    // attempt to acquire lock from node 2
    assertFalse(SharedVariable.getInstance().requestLock(allId.get(1), "Test"));
    // release lock then acquire lock from node 2
    SharedVariable.getInstance().releaseLock(allId.get(0), "Test");
    assertTrue(SharedVariable.getInstance().requestLock(allId.get(1), "Test"));
    // read from node 2
    assertEquals(new AbstractMap.SimpleEntry<>(allId.get(0), 5),
            SharedVariable.getInstance().read(allId.get(1),
                    "Test"));
    // read from node 3
    assertEquals(new AbstractMap.SimpleEntry<>(allId.get(0), 5),
            SharedVariable.getInstance().read(allId.get(2),
                    "Test"));
    // read again from node 3 should throws an exception
    assertNull(SharedVariable.getInstance().read(allId.get(2), "Test"));
    // unregistered variable
    try {
      SharedVariable.getInstance().read(allId.get(2), "Test1");
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }

    assertTrue(SharedVariable.getInstance().register("Count", allId));
    for (Identifier id : allId) {
      values.put(id, 1L);
    }
    for (Identifier id : allId) {
      new Thread(() -> threadTest(id, ITERATIONS)).start();
    }
    try {
      count.await();
    } catch (Exception e) {
      e.printStackTrace();
    }
    long sample = -1;
    for (Identifier nodeId : allId) {
      long x = 1;
      while (!SharedVariable.getInstance().isEmpty(nodeId, "Count")) {
        x = (long) SharedVariable.getInstance().read(nodeId, "Count").getValue();
        values.put(nodeId, values.get(nodeId) | x);
      }
      if (sample == -1) {
        sample = values.get(nodeId);
      } else {
        assertEquals(sample, values.get(nodeId));
      }
    }
  }

  private void threadTest(Identifier nodeId, int iterations) {
    while (iterations-- > 0) {
      assertFalse(SharedVariable.getInstance().write(nodeId, "Count", 1));
      if (rand.nextBoolean()) {
        continue;
      }

      long x = 1;
      while (!SharedVariable.getInstance().isEmpty(nodeId, "Count")) {
        x = (long) SharedVariable.getInstance().read(nodeId, "Count").getValue();
        values.put(nodeId, values.get(nodeId) | x);
      }
      x *= 2;
      if (rand.nextBoolean() && SharedVariable.getInstance().requestLock(nodeId, "Count")) {
        assertTrue(SharedVariable.getInstance().write(nodeId, "Count", x));
        values.put(nodeId, values.get(nodeId) | x);
        SharedVariable.getInstance().releaseLock(nodeId, "Count");
      } else {
        assertFalse(SharedVariable.getInstance().write(nodeId, "Count", x));
      }
    }
    count.countDown();
  }
}