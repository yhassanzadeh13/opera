package network;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.Fixtures.nodeListFixture;
import network.local.LocalUnderlay;
import node.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Fixtures;
import utils.NoopOrchestrator;
import utils.timeouts.Eventually;

/**
 * Tests the communication and termination of every network underlay.
 */
public class UnderlayTest {

  /**
   * Starts the given nodes and fails if they cannot be started within the specified timeout.
   *
   * @param nodes    The nodes to start.
   * @param timeout  The maximum time to wait for all nodes to start.
   * @param timeUnit The time unit of the timeout argument.
   * @throws InterruptedException If the current thread is interrupted while waiting.
   */
  private static void startNodes(ArrayList<FixtureNode> nodes, long timeout, TimeUnit timeUnit) throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(nodes.size());

    // Start all instances
    for (FixtureNode node : nodes) {
      new Thread(() -> {
        node.onStart();
        countDownLatch.countDown();
      }).start();
    }

    boolean startedOnTime = countDownLatch.await(timeout, timeUnit);
    assertTrue(startedOnTime, "Nodes did not start on time.");
  }

  /**
   * Stops the given nodes and fails if they cannot be stopped within the specified timeout.
   *
   * @param nodes    The nodes to stop.
   * @param timeout  The maximum time to wait for all nodes to stop.
   * @param timeUnit The time unit of the timeout argument.
   */
  private static void stopNodes(ArrayList<FixtureNode> nodes, long timeout, TimeUnit timeUnit) {
    CountDownLatch countDownLatch = new CountDownLatch(nodes.size());

    // Stop all nodes
    for (FixtureNode node : nodes) {
      new Thread(() -> {
        node.onStop();
        countDownLatch.countDown();
      }).start();
    }

    try {
      boolean stoppedOnTime = countDownLatch.await(timeout, timeUnit);
      assertTrue(stoppedOnTime, "Nodes did not stop on time.");
    } catch (InterruptedException e) {
      Assertions.fail("Stop nodes interrupted: ", e);
    }
  }

  /**
   * Asserts that all nodes are correctly started and communication is established.
   *
   * @param nodes The nodes to check.
   */
  private void assertNodesCommunication(ArrayList<FixtureNode> nodes) {
    try {
      Eventually.eventually(20_000, 100, () -> {
        for (FixtureNode node : nodes) {
          if (node.receivedMessages.get() != nodes.size() - 1) {
            return false;
          }
        }
        return true;
      });
    } catch (Exception e) {
      Assertions.fail("Communication not established: ", e);
    }
  }

  @Test
  void tcpTest() throws InterruptedException {
    ArrayList<FixtureNode> tcpNodes = nodeListFixture(NetworkProtocol.TCP_PROTOCOL, 20);
    startNodes(tcpNodes, 60, TimeUnit.SECONDS);
    assertNodesCommunication(tcpNodes);
    stopNodes(tcpNodes, 60, TimeUnit.SECONDS);
  }

  @Test
  void udpTest() throws InterruptedException {
    ArrayList<FixtureNode> udpNodes = nodeListFixture(NetworkProtocol.UDP_PROTOCOL, 20);
    startNodes(udpNodes, 60, TimeUnit.SECONDS);
    assertNodesCommunication(udpNodes);
    stopNodes(udpNodes, 60, TimeUnit.SECONDS);
  }

  @Test
  void rmiTest() throws InterruptedException {
    ArrayList<FixtureNode> javaRmiNodes = nodeListFixture(NetworkProtocol.JAVA_RMI, 20);
    startNodes(javaRmiNodes, 60, TimeUnit.SECONDS);
    assertNodesCommunication(javaRmiNodes);
    stopNodes(javaRmiNodes, 60, TimeUnit.SECONDS);
  }

  @Test
  void localTest() throws InterruptedException {
    final int nodeCount = 50;
    ArrayList<FixtureNode> instances = generateLocalNodes(nodeCount);
    startNodes(instances, 60, TimeUnit.SECONDS);
    assertNodesCommunication(instances);
    stopNodes(instances, 60, TimeUnit.SECONDS);
  }

  /**
   * Generates local nodes for testing.
   *
   * @param nodeCount The number of nodes to generate.
   * @return A list of generated nodes.
   */
  private ArrayList<FixtureNode> generateLocalNodes(int nodeCount) {
    HashMap<AbstractMap.SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay = new HashMap<>();
    ArrayList<FixtureNode> instances = new ArrayList<>();
    ArrayList<Identifier> allId = Fixtures.identifierListFixture(nodeCount);
    HashMap<Identifier, AbstractMap.SimpleEntry<String, Integer>> allFullAddresses = new HashMap<>();
    HashMap<AbstractMap.SimpleEntry<String, Integer>, Boolean> isReady = new HashMap<>();

    // Generate full addresses
    try {
      String address = Inet4Address.getLocalHost().getHostAddress();
      for (int i = 0; i < nodeCount; i++) {
        allFullAddresses.put(allId.get(i), new AbstractMap.SimpleEntry<>(address, i));
        isReady.put(new AbstractMap.SimpleEntry<>(address, i), true);
      }
    } catch (UnknownHostException e) {
      Assertions.fail("Failed to generate full addresses: ", e);
    }

    for (int i = 0; i < nodeCount; i++) {
      Identifier id = allId.get(i);
      String address = allFullAddresses.get(id).getKey();
      int port = allFullAddresses.get(id).getValue();

      Network network = new Network(id, allFullAddresses, new NoopOrchestrator());
      FixtureNode node = new FixtureNode(id, allId, network);
      network.setNode(node);

      LocalUnderlay underlay = new LocalUnderlay(address, port, allLocalUnderlay);
      underlay.initialize(port, network);

      network.setUnderlay(underlay);
      instances.add(node);
      allLocalUnderlay.put(new AbstractMap.SimpleEntry<>(address, port), underlay);
    }

    return instances;
  }
}
