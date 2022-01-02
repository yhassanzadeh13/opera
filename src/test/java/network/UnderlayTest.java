package network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.Fixtures.identifierListFixture;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import utils.NoopOrchestrator;

/**
 * Test the communication and termination of every network underlay.
 */
public class UnderlayTest {

  private static final HashMap<AbstractMap.SimpleEntry<String, Integer>, Underlay> allUnderlays = new HashMap<>();

  @AfterAll
  static void terminate() {
    for (Underlay underlay : allUnderlays.values()) {
      underlay.terminate();
    }
  }

  ArrayList<FixtureNode> nodeListFixture(UnderlayType underlayName) {
    final int NodeCount = 50; // total number of nodes in test
    ArrayList<FixtureNode> nodes = new ArrayList<>();
    ArrayList<UUID> allId = identifierListFixture(NodeCount);
    HashMap<UUID, AbstractMap.SimpleEntry<String, Integer>> allFullAddresses = new HashMap<>();
    HashMap<AbstractMap.SimpleEntry<String, Integer>, Boolean> isReady = new HashMap<>();


    for (int i = 0; i < NodeCount; i++) {
      UUID id = allId.get(i);

      MiddleLayer middleLayer = new MiddleLayer(id,
          allFullAddresses,
          isReady,
          new NoopOrchestrator(),
          new NoopCollector());

      FixtureNode node = new FixtureNode(id, allId, middleLayer);
      middleLayer.setOverlay(node);
      Underlay underlay = UnderlayFactory.newUnderlay(underlayName, 0, middleLayer);
      assert underlay != null;
      int port = underlay.getPort();
      allFullAddresses.put(id, new AbstractMap.SimpleEntry<>(underlay.getAddress(), port));
      middleLayer.setUnderlay(underlay);

      nodes.add(node);
      allUnderlays.put(new AbstractMap.SimpleEntry<>(underlay.getAddress(), port), underlay);
    }


    return nodes;
  }

  @Test
  void atestTcp() {
    ArrayList<FixtureNode> tcpNodes = nodeListFixture(UnderlayType.TCP_PROTOCOL);
    assure(tcpNodes);
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
    } catch (Exception e) {
      e.printStackTrace();
    }

    // check that all nodes received threadCount - 1 messages
    for (FixtureNode node : instances) {
      assertEquals(instances.size() - 1, node.receivedMessages.get());
    }
  }

  @Test
  void btestUdp() {
    // generate middle layers
    ArrayList<FixtureNode> udpNodes = nodeListFixture(UnderlayType.UDP_PROTOCOL);
    assure(udpNodes);
  }

  @Test
  void ctestRmi() {
    // generate middle layers
    ArrayList<FixtureNode> javaRmiNodes = nodeListFixture(UnderlayType.JAVA_RMI);
    assure(javaRmiNodes);
  }

  @Test
  void testLocal() {
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

      MiddleLayer middleLayer = new MiddleLayer(id,
          allFullAddresses,
          isReady,
          new NoopOrchestrator(),
          new NoopCollector());
      FixtureNode node = new FixtureNode(id, allId, middleLayer);
      middleLayer.setOverlay(node);

      LocalUnderlay underlay = new LocalUnderlay(address, port, allLocalUnderlay);
      underlay.initialize(port, middleLayer);

      middleLayer.setUnderlay(underlay);
      instances.add(node);
      allLocalUnderlay.put(new AbstractMap.SimpleEntry<>(address, port), underlay);
    }

    assure(instances);
  }
}
