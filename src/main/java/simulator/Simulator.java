package simulator;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import metrics.integration.MetricServer;
import metrics.integration.MetricsNetwork;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.NetworkProtocol;
import network.UnderlayFactory;
import network.local.LocalUnderlay;
import node.BaseNode;
import node.Identifier;
import node.IdentifierGenerator;
import utils.SimpleEntryComparable;
import utils.churn.ChurnGenerator;

/**
 * Simulator simulates situations between nodes with actions performed between the nodes.
 * Simulator also can create new instances for the nodes.
 * Simulator can simulate in two ways: churn-based, time-based.
 */
public class Simulator implements Orchestrator {
  private static final Random rand = new Random();
  private static final Logger log = OperaLogger.getLoggerForSimulator(Simulator.class.getName());
  /**
   * Timeout for waiting for all nodes to be ready in milliseconds.
   */
  private final int readyTimeoutMs = 1000;
  private final ArrayList<Identifier> allId;
  private final HashMap<Identifier, InetSocketAddress> allFullAddresses;
  private final HashMap<InetSocketAddress, Boolean> isReady;
  private final Factory factory;
  private final ArrayList<Identifier> offlineNodes = new ArrayList<>();
  private final CountDownLatch allNodesReady;
  // TODO: do we need this? we already have allNetworks
  private final HashMap<InetSocketAddress, LocalUnderlay> allLocalUnderlay = new HashMap<>();
  private final SimulatorMetricsCollector simulatorMetricsCollector;
  private final MetricsNetwork metricsNetwork;
  private final MetricServer metricServer;

  /**
   * All the network instances of the nodes in the simulation.
   */
  private HashMap<InetSocketAddress, network.Network> allNetworks;
  private PriorityQueue<SimpleEntryComparable<Double, Identifier>> onlineNodes = new PriorityQueue<>();

  /**
   * Initializes a new simulation.
   *
   * @param factory     factory object to create nodes based on inventory.
   * @param networkType the type of simulated communication protocol(**tcp**, **javarmi**,
   *                    **udp**, and **mockNetwork*)
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "factory is externally mutable")
  public Simulator(Factory factory, NetworkProtocol networkType) {
    this.factory = factory;
    this.isReady = new HashMap<>();
    int startPort = 2000;
    this.allId = generateIds(factory.getTotalNodes());
    this.allFullAddresses = generateFullAddressed(factory.getTotalNodes(), startPort + 1);
    this.metricsNetwork = new MetricsNetwork();
    this.metricServer = new MetricServer();

    allNodesReady = new CountDownLatch(factory.getTotalNodes());
    this.simulatorMetricsCollector = new SimulatorMetricsCollector();
    this.generateNodesInstances(networkType);
  }

  /**
   * Generate new random identifier for the nodes.
   *
   * @param n number of nodes
   * @return ArrayList of random n ids
   */
  private ArrayList<Identifier> generateIds(int n) {
    log.info("generating identifiers of {} nodes for simulation", n);

    ArrayList<Identifier> identifiers = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      Identifier id = IdentifierGenerator.newIdentifier();
      log.info("generated identifier {} for node#{}", id, i);
      identifiers.add(id);
    }

    return identifiers;
  }

  /**
   * Creates identifier and address mapping for the nodes.
   *
   * @param n         number of nodes in the simulation.
   * @param startPort starting port for the nodes.
   * @return HashMap of identifier to address mapping.
   */
  private HashMap<Identifier, InetSocketAddress> generateFullAddressed(int n, int startPort) {
    log.info("generating full addresses of {} nodes for simulation", n);

    HashMap<Identifier, InetSocketAddress> identifierToAddress = new HashMap<>();
    try {
      // TODO: replace with localhost.
      String address = Inet4Address.getLocalHost().getHostAddress();
      for (int i = 0; i < n; i++) {
        identifierToAddress.put(allId.get(i), new InetSocketAddress(address, startPort + i));
      }
    } catch (UnknownHostException e) {
      log.fatal("failed to get localhost address", e);
    }
    return identifierToAddress;
  }

  /**
   * Generate new instances for the nodes and add them to the network.
   */
  private void generateNodesInstances(NetworkProtocol networkType) {
    this.allNetworks = new HashMap<>();

    // generate nodes, and middle layers instances
    int globalIndex = 0;
    for (Recipe r : this.factory.getRecipes()) {
      for (int i = 0; i < r.getTotal(); i++) {
        Identifier id = allId.get(globalIndex++);

        isReady.put(this.allFullAddresses.get(id), false);
        network.Network network = new network.Network(id, this.allFullAddresses, this);

        BaseNode node = r.getBaseNode().newInstance(id, r.getNameSpace(), network);
        network.setNode(node);
        this.allNetworks.put(this.allFullAddresses.get(id), network);
      }
    }

    // generate new underlays and assign them to the nodes middles layers.
    for (Map.Entry<InetSocketAddress, network.Network> node : this.allNetworks.entrySet()) {
      network.Network network = node.getValue();
      InetSocketAddress address = node.getKey();
      int port = node.getKey().getPort();
      if (networkType != NetworkProtocol.MOCK_NETWORK) {
        network.setUnderlay(UnderlayFactory.newUnderlay(networkType, port, network));
      } else {
        LocalUnderlay underlay = UnderlayFactory.getMockUnderlay(address, network, allLocalUnderlay);
        network.setUnderlay(underlay);
        allLocalUnderlay.put(node.getKey(), underlay);
      }
      // call the node onCreat method of the nodes
      network.create(this.allId);
    }
  }

  /**
   * Starts the simulator and spawns the identified number of nodes.
   */
  public void start() {
    log.info("simulation started");
    boolean isAllReady = false;

    try {
      this.metricServer.start();
    } catch (IllegalStateException e) {
      log.fatal("metric server failed to start", e);
    }

    this.metricsNetwork.runMetricsTestNet();

    try {
      isAllReady = allNodesReady.await(readyTimeoutMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      log.fatal("interrupted while waiting for nodes to be ready", e);
    }

    if (!isAllReady) {
      log.fatal("{} ms timeout on starting all nodes", readyTimeoutMs);
    }

    // start all nodes in new threads
    for (network.Network middleNetwork : this.allNetworks.values()) {
      // TODO: this should also have a timeout.
      middleNetwork.start();
    }
  }

  /**
   * terminates the simulator and all spawned nodes.
   */
  public void terminate() {
    log.info("terminating simulation");

    //terminating all nodes
    while (!onlineNodes.isEmpty()) {
      Identifier id = onlineNodes.poll().getValue();
      log.debug("terminating node {}", id);
      this.done(id);
      log.info("node {} terminated", id);
    }

    try {
      this.metricServer.terminate();
      log.info("metric server terminated");
    } catch (IllegalStateException e) {
      log.fatal("metric server failed to stop", e);
    }
  }

  /**
   * Should be called by the node to declare itself ready for simulation.
   *
   * @param nodeId identifier of the node
   */
  @Override
  public void ready(Identifier nodeId) {
    this.isReady.put(this.allFullAddresses.get(nodeId), true);
    log.info("node {} is ready", nodeId);

    // start the nodes directly if the simulation is running, or wait for all nodes to be ready
    // in case otherwise.
    if (this.allNodesReady.getCount() <= 0) {
      this.getMiddleLayer(nodeId).start();
    } else {
      allNodesReady.countDown();
    }
  }

  // TODO: do we need this?
  public network.Network getMiddleLayer(Identifier id) {
    InetSocketAddress address = this.allFullAddresses.get(id);
    return this.allNetworks.get(address);
  }

  /**
   * Should be called by the node when it is done with the simulation and want to terminate.
   *
   * @param nodeId identifier of the node
   */
  @Override
  public void done(Identifier nodeId) {
    log.info("node {} is done", nodeId);

    // mark the nodes as not ready
    isReady.put(this.allFullAddresses.get(nodeId), false);
    InetSocketAddress address = allFullAddresses.get(nodeId);

    // stop the nodes on a new thread
    network.Network network = this.allNetworks.get(address);
    if (network != null) {
      network.stop();
    }
    // add the offline nodes list
    this.offlineNodes.add(nodeId);

    log.info("node {} has been stopped", nodeId);
  }

  /**
   * Used to start the simulation.
   * It calls the onStart method for all nodes to start the simulation.
   *
   * @param duration duration of the simulation
   */
  public void constantSimulation(int duration) {
    this.start();
    log.info("constant time simulation started for a lifetime of {} ms", duration);

    try {
      Thread.sleep(duration);
    } catch (Exception e) {
      log.fatal("could not continue simulation", e);
    }

    log.info("constant time simulation has ended, total lifetime was {} ms", duration);

    this.terminate();
  }

  /**
   * Simulate churn based on inter-arrival time and session length.
   *
   * @param lifeTime               duration of the simulation.
   * @param interArrivalGen        inter-arrival generator, i.e., time between two consecutive
   *                               arrivals in the system.
   * @param sessionLengthGenerator session length generator, i.e., online duration of a node in
   *                               the system.
   */
  public void churnSimulation(long lifeTime, ChurnGenerator interArrivalGen,
                              ChurnGenerator sessionLengthGenerator) {
    // initialize all nodes
    this.start();
    log.info("churn simulation started for a lifetime of {} ms", lifeTime);

    // hold the current online nodes, with their termination time stamp.
    this.onlineNodes = new PriorityQueue<>();

    // assign initial terminate time stamp for all nodes
    long startTime = System.currentTimeMillis();
    for (Identifier id : this.allId) {
      if (isReady.get(this.allFullAddresses.get(id))) {
        double sessionLength = sessionLengthGenerator.next();
        log.info("generated new session length of {} ms for node {}, termination at {}", id,
                 sessionLength, startTime + sessionLength);
        onlineNodes.add(new SimpleEntryComparable<>(startTime + sessionLength, id));
        this.simulatorMetricsCollector.onNewSessionLengthGenerated(id, sessionLength);
      }
    }

    // hold next arrival time
    double interArrivalTime = interArrivalGen.next();
    this.simulatorMetricsCollector.onNewInterArrivalGenerated(interArrivalTime);
    double nextArrival = System.currentTimeMillis() + interArrivalTime;

    while (System.currentTimeMillis() - startTime < lifeTime) {
      this.simulatorMetricsCollector.updateOnlineNodes(onlineNodes.size());
      this.simulatorMetricsCollector.updateOfflineNodes(offlineNodes.size());

      if (!onlineNodes.isEmpty()) {
        assert onlineNodes.peek() != null;
        if (System.currentTimeMillis() > onlineNodes.peek().getKey()) {
          // terminate the node with the nearest termination time (if the time met)
          // `done` will add the node to the offline nodes
          Identifier id = Objects.requireNonNull(onlineNodes.poll()).getValue();
          log.info("session length is done, switching node {} to offline", id);
          this.done(id);
        }
      }
      if (System.currentTimeMillis() >= nextArrival) {
        // pool a random node from the offline nodes (if exists) and start it in a new thread.
        if (this.offlineNodes.isEmpty()) {
          continue;
        }

        int ind = rand.nextInt(this.offlineNodes.size());
        Identifier id = this.offlineNodes.get(ind);
        log.info("(arrival) switching node {} to online", id);
        this.offlineNodes.remove(ind);

        // creat the new node in a new thread
        // Once the node call `ready` method, the node's onStart method will be called
        network.Network network = this.getMiddleLayer(id);
        network.initUnderLay();
        network.create(this.allId);

        // TODO: this logic has a repetition (see earlier in the code)
        // assign a termination time
        double sessionLength = sessionLengthGenerator.next();
        // TODO: fix this
        this.simulatorMetricsCollector.onNewSessionLengthGenerated(id, sessionLength);
        log.info("generated new session length of {} ms for node {}, termination at {}", id,
                 sessionLength, startTime + sessionLength);
        this.onlineNodes.add(
          new SimpleEntryComparable<>(System.currentTimeMillis() + sessionLength, id));

        // assign a next node arrival time
        interArrivalTime = interArrivalGen.next();
        this.simulatorMetricsCollector.onNewInterArrivalGenerated(interArrivalTime);
        nextArrival = System.currentTimeMillis() + interArrivalTime;
        Duration nextArrivalDuration = Duration.ofMillis((int) interArrivalTime);
        log.info("next node {} in {} hours {} minutes {} seconds {} milliseconds", id,
                 nextArrivalDuration.toHoursPart(), nextArrivalDuration.toMinutesPart(),
                 nextArrivalDuration.toSecondsPart(), nextArrivalDuration.toMillisPart());
      }
    }

    log.info("churn simulation has ended, total lifetime was {} ms", lifeTime);

    this.terminate();
  }

  /**
   * get all nodes identifier.
   *
   * @return nodes' identifier.
   **/
  public ArrayList<Identifier> getAllId() {
    return (ArrayList<Identifier>) this.allId.clone();
  }

}
