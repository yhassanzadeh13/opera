package network;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.Fixtures.nodeListFixture;
import network.local.LocalUnderlay;
import node.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Fixtures;
import utils.NoopOrchestrator;

/**
 * Test the communication and termination of every network underlay.
 */
public class UnderlayTest {

  private static void stopNodes(ArrayList<FixtureNode> nodes) {
    for (FixtureNode node : nodes) {
      node.onStop();
    }
  }

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
    } catch (InterruptedException e) {
      Assertions.fail(e);
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
    ArrayList<Identifier> allId = Fixtures.identifierListFixture(NodeCount);
    HashMap<Identifier, AbstractMap.SimpleEntry<String, Integer>> allFullAddresses = new HashMap<>();
    HashMap<AbstractMap.SimpleEntry<String, Integer>, Boolean> isReady = new HashMap<>();

    // generate full addresses
    try {
      String address = Inet4Address.getLocalHost().getHostAddress();
      for (int i = 0; i < NodeCount; i++) {

        allFullAddresses.put(allId.get(i), new AbstractMap.SimpleEntry<>(address, i));
        isReady.put(new AbstractMap.SimpleEntry<>(address, i), true);
      }
    } catch (UnknownHostException e) {
      Assertions.fail(e);
    }

    for (int i = 0; i < NodeCount; i++) {
      Identifier id = allId.get(i);
      String address = allFullAddresses.get(id).getKey();
      int port = allFullAddresses.get(id).getValue();

      MiddleLayer middleLayer = new MiddleLayer(id, allFullAddresses, new NoopOrchestrator());
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
