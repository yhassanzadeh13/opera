package network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.Fixtures.nodeListFixture;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import metrics.NoopCollector;
import network.local.LocalUnderlay;
import org.junit.jupiter.api.Test;
import utils.NoopOrchestrator;

/**
 * Test the communication and termination of every network underlay.
 */
public class UnderlayTest {

  @Test
  void tcpTest() {
    ArrayList<FixtureNode> tcpNodes = nodeListFixture(NetworkProtocol.TCP_PROTOCOL, 20);
    assure(tcpNodes);
    stopNodes(tcpNodes);
  }

  @Test
  void udpTest() {
    ArrayList<FixtureNode> udpNodes = nodeListFixture(NetworkProtocol.UDP_PROTOCOL, 20);
    assure(udpNodes);
    stopNodes(udpNodes);
  }

  @Test
  void rmiTest() {
    ArrayList<FixtureNode> javaRmiNodes = nodeListFixture(NetworkProtocol.JAVA_RMI, 20);
    assure(javaRmiNodes);
    stopNodes(javaRmiNodes);
  }

  private static void stopNodes(ArrayList<FixtureNode> nodes) {
    for (FixtureNode node: nodes) {
      node.onStop();
    }
  }


  // TODO: add documentations
  void assure(ArrayList<FixtureNode> instances) {
    CountDownLatch countDownLatch = new CountDownLatch(instances.size());


    // start all instances
    for (FixtureNode node : instances) {
      new Thread(() -> {
        node.onStart();
        countDownLatch.countDown();
      }).start();
    }
    try {
      boolean onTime = countDownLatch.await(60, TimeUnit.SECONDS);
      assertTrue(onTime, "threads are not done on time");

      // sleeps for 2 seconds to make sure all messages exchanged.
      Thread.sleep(20_000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // check that all nodes received threadCount - 1 messages
    for (FixtureNode node : instances) {
      assertEquals(instances.size() - 1, node.receivedMessages.get());
    }
  }

  @Test
  void localTest() {
    final int NodeCount = 50; // total number of nodes in test
    HashMap<AbstractMap.SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay = new HashMap<>();
    // generate middle layers
    ArrayList<FixtureNode> instances = new ArrayList<>();
    ArrayList<UUID> allId = new ArrayList<>();
    HashMap<UUID, AbstractMap.SimpleEntry<String, Integer>> allFullAddresses = new HashMap<>();
    HashMap<AbstractMap.SimpleEntry<String, Integer>, Boolean> isReady = new HashMap<>();

    // generate IDs
    for (int i = 0; i < NodeCount; i++) {
      allId.add(UUID.randomUUID());
    }

    // generate full addresses
    try {
      String address = Inet4Address.getLocalHost().getHostAddress();
      for (int i = 0; i < NodeCount; i++) {

        allFullAddresses.put(allId.get(i), new AbstractMap.SimpleEntry<>(address, i));
        isReady.put(new AbstractMap.SimpleEntry<>(address, i), true);
      }

    } catch (UnknownHostException e) {
      e.printStackTrace();
    }

    for (int i = 0; i < NodeCount; i++) {
      UUID id = allId.get(i);
      String address = allFullAddresses.get(id).getKey();
      int port = allFullAddresses.get(id).getValue();

      Network network = new Network(id,
          allFullAddresses,
          isReady,
          new NoopOrchestrator(),
          new NoopCollector());
      FixtureNode node = new FixtureNode(id, allId, network);
      network.setOverlay(node);

      LocalUnderlay underlay = new LocalUnderlay(address, port, allLocalUnderlay);
      underlay.initialize(port, network);

      network.setUnderlay(underlay);
      instances.add(node);
      allLocalUnderlay.put(new AbstractMap.SimpleEntry<>(address, port), underlay);
    }

    assure(instances);
  }
}
