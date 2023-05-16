package simulator;

import java.net.Inet4Address;
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
import network.MiddleLayer;
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
  private final HashMap<Identifier, SimpleEntry<String, Integer>> allFullAddresses;
  private final HashMap<SimpleEntry<String, Integer>, Boolean> isReady;
  private final Factory factory;
  private final ArrayList<Identifier> offlineNodes = new ArrayList<>();
  private final CountDownLatch allNodesReady;
  private final HashMap<SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay =
          new HashMap<>();
  private final SimulatorMetricsCollector simulatorMetricsCollector;
  private final MetricsNetwork metricsNetwork;
  private final MetricServer metricServer;

  private HashMap<SimpleEntry<String, Integer>, MiddleLayer> allMiddleLayers;
  private PriorityQueue<SimpleEntryComparable<Double, Identifier>> onlineNodes =
          new PriorityQueue<>();

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

  private HashMap<Identifier, SimpleEntry<String, Integer>> generateFullAddressed(int n,
                                                                                  int startPort) {
    log.info("generating full addresses of {} nodes for simulation", n);

    // TODO: introduce address object.
    HashMap<Identifier, SimpleEntry<String, Integer>> identifierToAddress = new HashMap<>();
    try {
      // TODO: replace with localhost.
      String address = Inet4Address.getLocalHost().getHostAddress();
      for (int i = 0; i < n; i++) {
        identifierToAddress.put(allId.get(i), new SimpleEntry<>(address, startPort + i));
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
    this.allMiddleLayers = new HashMap<>();

    // generate nodes, and middle layers instances
    int globalIndex = 0;
    for (Recipe r : this.factory.getRecipes()) {
      for (int i = 0; i < r.getTotal(); i++) {
        Identifier id = allId.get(globalIndex++);

        isReady.put(this.allFullAddresses.get(id), false);
        MiddleLayer middleLayer = new MiddleLayer(id, this.allFullAddresses, this);

        BaseNode node = r.getBaseNode().newInstance(id, r.getNameSpace(), middleLayer);
        middleLayer.setOverlay(node);
        this.allMiddleLayers.put(this.allFullAddresses.get(id), middleLayer);
      }
    }

    // generate new underlays and assign them to the nodes middles layers.
    for (Map.Entry<SimpleEntry<String, Integer>, MiddleLayer> node :
            this.allMiddleLayers.entrySet()) {
      MiddleLayer middleLayer = node.getValue();
      String address = node.getKey().getKey();
      int port = node.getKey().getValue();
      if (networkType != NetworkProtocol.MOCK_NETWORK) {
        middleLayer.setUnderlay(UnderlayFactory.newUnderlay(networkType, port, middleLayer));
      } else {
        LocalUnderlay underlay = UnderlayFactory.getMockUnderlay(address, port, middleLayer,
                allLocalUnderlay);
        middleLayer.setUnderlay(underlay);
        allLocalUnderlay.put(node.getKey(), underlay);
      }
      // call the node onCreat method of the nodes
      middleLayer.create(this.allId);
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
    for (MiddleLayer middleNetwork : this.allMiddleLayers.values()) {
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

  public MiddleLayer getMiddleLayer(Identifier id) {
    SimpleEntry<String, Integer> address = this.allFullAddresses.get(id);
    return this.allMiddleLayers.get(address);
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
    SimpleEntry<String, Integer> fullAddress = allFullAddresses.get(nodeId);

    // stop the nodes on a new thread
    MiddleLayer middleLayer = this.allMiddleLayers.get(fullAddress);
    if (middleLayer != null) {
      middleLayer.stop();
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

  public String getAddress(Identifier nodeId) {
    SimpleEntry<String, Integer> address = allFullAddresses.get(nodeId);
    return address.getKey() + ":" + address.getValue();
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
    long time = System.currentTimeMillis();
    for (Identifier id : this.allId) {
      if (isReady.get(this.allFullAddresses.get(id))) {
        double sessionLength = sessionLengthGenerator.next();
        log.info("generated new session length of {} ms for node {}, termination at {}", id,
                sessionLength, time + sessionLength);
        onlineNodes.add(new SimpleEntryComparable<>(time + sessionLength, id));
        // TODO: fix this
        this.simulatorMetricsCollector.onNewSessionLengthGenerated(id, (int) sessionLength);
      }
    }
    // hold next arrival time
    double interArrivalTime = interArrivalGen.next();
    // TODO: fix this
    this.simulatorMetricsCollector.onNewInterArrivalGenerated((int) interArrivalTime);
    double nextArrival = System.currentTimeMillis() + interArrivalTime;

    while (System.currentTimeMillis() - time < lifeTime) {
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
        MiddleLayer middleLayer = this.getMiddleLayer(id);
        middleLayer.initUnderLay();
        middleLayer.create(this.allId);

        // TODO: this logic has a repetition (see earlier in the code)
        // assign a termination time
        double sessionLength = sessionLengthGenerator.next();
        // TODO: fix this
        this.simulatorMetricsCollector.onNewSessionLengthGenerated(id, (int) sessionLength);
        log.info("generated new session length of {} ms for node {}, termination at {}", id,
                sessionLength, time + sessionLength);
        this.onlineNodes.add(
                new SimpleEntryComparable<>(System.currentTimeMillis() + sessionLength, id));

        // assign a next node arrival time
        interArrivalTime = interArrivalGen.next();
        // TODO: fix this
        this.simulatorMetricsCollector.onNewInterArrivalGenerated((int) interArrivalTime);
        nextArrival = System.currentTimeMillis() + interArrivalTime;
        Duration nextArrivalDuration = Duration.ofMillis((int) interArrivalTime);
        log.info("next node {} in {} hours {} minutes {} seconds {} milliseconds", id,
                nextArrivalDuration.toHoursPart(), nextArrivalDuration.toMinutesPart(),
                nextArrivalDuration.toSecondsPart(), nextArrivalDuration.toMillis());
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
