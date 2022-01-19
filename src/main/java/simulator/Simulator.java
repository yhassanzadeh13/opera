package simulator;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CountDownLatch;

import metrics.MetricsCollector;
import metrics.opera.OperaCollector;
import network.MiddleLayer;
import network.NetworkProtocol;
import network.UnderlayFactory;
import network.local.LocalUnderlay;
import node.BaseNode;
import org.apache.log4j.Logger;
import utils.SimpleEntryComparable;
import metrics.PrometheusClient;
import utils.generator.BaseGenerator;


/**
 * Simulator simulates situations between nodes with actions performed between the nodes.
 * Simulator also can create new instances for the nodes.
 * Simulator can simulate in two ways: churn-based, time-based.
 */
public class Simulator implements Orchestrator {
  private static final Random rand = new Random();
  public static Logger log = Logger.getLogger(Simulator.class.getName());
  private final ArrayList<UUID> allId;
  private final HashMap<UUID, SimpleEntry<String, Integer>> allFullAddresses;
  private final HashMap<SimpleEntry<String, Integer>, Boolean> isReady;
  private final Factory factory;
  private final ArrayList<UUID> offlineNodes = new ArrayList<>();
  private final CountDownLatch count;
  private final HashMap<SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay = new HashMap<>();
  private final MetricsCollector metricsCollector;
  private final SimulatorMetricsCollector simulatorMetricsCollector;

  private HashMap<SimpleEntry<String, Integer>, MiddleLayer> allMiddleLayers;
  private PriorityQueue<SimpleEntryComparable<Long, UUID>> onlineNodes = new PriorityQueue<>();

  /**
   * Initializes a new simulation.
   *
   * @param factory     factory object to create nodes based on inventory.
   * @param networkType the type of simulated communication protocol(**tcp**, **javarmi**, **udp**, and **mockNetwork*)
   */
  public Simulator(Factory factory, NetworkProtocol networkType) {
    this.factory = factory;
    this.isReady = new HashMap<>();
    int startPort = 2000;
    this.allId = generateIds(factory.getTotalNodes());
    this.allFullAddresses = generateFullAddressed(factory.getTotalNodes(), startPort + 1);

    try {
      PrometheusClient.start();
    } catch (IllegalStateException e)  {
      log.fatal(e);
      System.exit(1);
    }


    // CountDownLatch for awaiting the start of the simulation until all nodes are ready
    count = new CountDownLatch(factory.getTotalNodes());

    // initializes metrics collector
    this.metricsCollector = new OperaCollector();
    this.simulatorMetricsCollector = new SimulatorMetricsCollector(this.metricsCollector);

    this.generateNodesInstances(networkType);
  }

  /**
   * getter for the simulator logger of log4j.
   *
   * @return the simulator logger
   */
  public static Logger getLogger() {
    return log;
  }

  /**
   * Generate new random UUID for the nodes.
   *
   * @param n number of nodes
   * @return ArrayList of random n ids
   */
  private ArrayList<UUID> generateIds(int n) {
    log.info("Generating IDs for " + n + " node..");

    ArrayList<UUID> tmp = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      tmp.add(UUID.randomUUID());
    }

    return tmp;
  }

  private HashMap<UUID, SimpleEntry<String, Integer>> generateFullAddressed(int n, int startPort) {
    log.info("Generating full Addresses for " + n + " node..");

    HashMap<UUID, SimpleEntry<String, Integer>> tmp = new HashMap<>();
    try {
      String address = Inet4Address.getLocalHost().getHostAddress();
      for (int i = 0; i < n; i++) {
        tmp.put(allId.get(i), new SimpleEntry<>(address, startPort + i));
      }
    } catch (UnknownHostException e) {
      log.error("[simulator.simulator] Could not acquire the local host name during initialization.");
      e.printStackTrace();
    }
    return tmp;
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
        UUID id = allId.get(globalIndex++);

        isReady.put(this.allFullAddresses.get(id), false);
        MiddleLayer middleLayer = new MiddleLayer(id, this.allFullAddresses, isReady, this, this.metricsCollector);

        BaseNode node = r.getBaseNode().newInstance(id, r.getNameSpace(), middleLayer, this.metricsCollector);
        middleLayer.setOverlay(node);
        this.allMiddleLayers.put(this.allFullAddresses.get(id), middleLayer);
      }
    }

    // generate new underlays and assign them to the nodes middles layers.
    for (Map.Entry<SimpleEntry<String, Integer>, MiddleLayer> node : this.allMiddleLayers.entrySet()) {
      MiddleLayer middleLayer = node.getValue();
      String address = node.getKey().getKey();
      int port = node.getKey().getValue();
      if (networkType != NetworkProtocol.MOCK_NETWORK) {
        middleLayer.setUnderlay(UnderlayFactory.newUnderlay(networkType, port, middleLayer));
      } else {
        LocalUnderlay underlay = UnderlayFactory.getMockUnderlay(address, port, middleLayer, allLocalUnderlay);
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
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    log.info("New simulation started on " + dtf.format(now));

    try {
      count.await();
    } catch (Exception e) {
      log.fatal("[simulator.simulator] Count down latch could not wait " + e.getMessage());
    }

    // start all nodes in new threads
    for (MiddleLayer middleNetwork : this.allMiddleLayers.values()) {
      middleNetwork.start();
    }
  }

  /**
   * terminates the simulator and all spawned nodes.
   */
  public void terminate() {
    log.info("Nodes will be terminated....");

    //terminating all nodes
    while (!onlineNodes.isEmpty()) {
      this.done(onlineNodes.poll().getValue());
    }
  }

  /**
   * Should be called by the node to declare itself ready for simulation.
   *
   * @param nodeId ID of the node
   */
  @Override
  public void ready(UUID nodeId) {
    this.isReady.put(this.allFullAddresses.get(nodeId), true);
    // log.info(nodeId + ": node is ready");

    // start the nodes directly if the simulation is running, or wait for all nodes to be ready in case otherwise.
    if (this.count.getCount() <= 0) {
      this.getMiddleLayer(nodeId).start();
    } else {
      count.countDown();
    }
  }

  public MiddleLayer getMiddleLayer(UUID id) {
    SimpleEntry<String, Integer> address = this.allFullAddresses.get(id);
    return this.allMiddleLayers.get(address);
  }

  /**
   * Should be called by the node when it is done with the simulation and want to terminate.
   *
   * @param nodeId ID of the node
   */
  @Override
  public void done(UUID nodeId) {
    // logging
    log.info(getAddress(nodeId) + ": node is terminating...");

    // mark the nodes as not ready
    isReady.put(this.allFullAddresses.get(nodeId), false);
    SimpleEntry<String, Integer> fullAddress = allFullAddresses.get(nodeId);

    // stop the nodes on a new thread
    try {
      MiddleLayer middleLayer = this.allMiddleLayers.get(fullAddress);
      middleLayer.stop();
    } catch (NullPointerException e) {
      log.error("[simulator.simulator] Cannot find node " + getAddress(nodeId));
      log.debug("[simulator.simulator] Node " + getAddress(nodeId) + " has already been terminate");
    }

    // add the offline nodes list
    this.offlineNodes.add(nodeId);

    // logging
    log.info(getAddress(nodeId) + ": node has been terminated");
  }


  /**
   * Used to start the simulation.
   * It calls the onStart method for all nodes to start the simulation.
   *
   * @param duration duration of the simulation
   */
  public void constantSimulation(int duration) {
    this.start();
    getLogger().info("Simulation started");

    try {
      Thread.sleep(duration);
    } catch (Exception e) {
      e.printStackTrace();
    }

    log.info("Simulation duration finished");

    this.terminate();
  }

  public String getAddress(UUID nodeId) {
    SimpleEntry<String, Integer> address = allFullAddresses.get(nodeId);
    return address.getKey() + ":" + address.getValue();
  }

  /**
   * Simulate churn based on inter-arrival time and session length.
   *
   * @param lifeTime               duration of the simulation.
   * @param interArrivalGen        inter-arrival generator, i.e., time between two consecutive arrivals in the system.
   * @param sessionLengthGenerator session length generator, i.e., online duration of a node in the system.
   */
  public void churnSimulation(long lifeTime, BaseGenerator interArrivalGen, BaseGenerator sessionLengthGenerator) {
    // initialize all nodes
    this.start();
    System.out.println("Simulation started");
    getLogger().info("Simulation started");

    // TODO: move these to a churn simulator unit so that one can register labels via
    // the simulator's constructor.
    // register a prometheus histogram for session length
    double[] labels = new double[10];
    for (int i = 1; i <= 10; i++) {
      labels[10 - i] = sessionLengthGenerator.mn
          + (double) (sessionLengthGenerator.mx - sessionLengthGenerator.mn) / i + 0.001;
    }
    // this.metricsCollector.histogram().register(sessionMetric, labels);

    // register a prometheus histogram for inter arrival time
    for (int i = 1; i <= 10; i++) {
      labels[10 - i] = interArrivalGen.mn + (double) (interArrivalGen.mx - interArrivalGen.mn) / i + 0.001;
    }
    // this.metricsCollector.histogram().register(arrivalMetric, labels);

    // hold the current online nodes, with their termination time stamp.
    this.onlineNodes = new PriorityQueue<>();

    // assign initial terminate time stamp for all nodes
    long time = System.currentTimeMillis();
    for (UUID id : this.allId) {
      if (isReady.get(this.allFullAddresses.get(id))) {
        int sessionLength = sessionLengthGenerator.next();
        log.info("[simulator.simulator] new session for node " + getAddress(id) + ": " + sessionLength + " ms");
        onlineNodes.add(new SimpleEntryComparable<>(time + sessionLength, id));
        this.simulatorMetricsCollector.onNewSessionLengthGenerated(id, sessionLength);
      }
    }
    // hold next arrival time
    int interArrivalTime = interArrivalGen.next();
    this.simulatorMetricsCollector.onNewInterArrivalGenerated(interArrivalTime);
    long nextArrival = System.currentTimeMillis() + interArrivalTime;

    while (System.currentTimeMillis() - time < lifeTime) {
      if (!onlineNodes.isEmpty()) {
        assert onlineNodes.peek() != null;
        if (System.currentTimeMillis() > onlineNodes.peek().getKey()) {
          // terminate the node with the nearest termination time (if the time met)
          // `done` will add the node to the offline nodes
          UUID id = Objects.requireNonNull(onlineNodes.poll()).getValue();
          log.info("[simulator.simulator] Deactivating node " + getAddress(id));
          this.done(id);
        }
      }
      if (System.currentTimeMillis() >= nextArrival) {
        // pool a random node from the offline nodes (if exists) and start it in a new thread.
        if (this.offlineNodes.isEmpty()) {
          continue;
        }

        int ind = rand.nextInt(this.offlineNodes.size());
        UUID id = this.offlineNodes.get(ind);
        log.info("[simulator.simulator] Activating node " + getAddress(id));
        this.offlineNodes.remove(ind);

        // creat the new node in a new thread
        // Once the node call `ready` method, the node's onStart method will be called
        MiddleLayer middleLayer = this.getMiddleLayer(id);
        middleLayer.initUnderLay();
        middleLayer.create(this.allId);

        // assign a termination time
        int sessionLength = sessionLengthGenerator.next();
        this.simulatorMetricsCollector.onNewSessionLengthGenerated(id, sessionLength);
        log.info("[simulator.simulator] new session for node " + getAddress(id) + ": " + sessionLength + " ms");
        this.onlineNodes.add(new SimpleEntryComparable<>(System.currentTimeMillis() + sessionLength, id));

        // assign a next node arrival time
        interArrivalTime = interArrivalGen.next();
        this.simulatorMetricsCollector.onNewInterArrivalGenerated(interArrivalTime);
        nextArrival = System.currentTimeMillis() + interArrivalTime;
        log.info("[simulator.simulator] next node arrival: " + nextArrival);
      }
    }

    log.info("Simulation duration finished");
    System.out.println("Simulation duration finished");

    // stop the simulation.
    this.terminate();
  }

  /**
   * get all nodes ID.
   *
   * @return nodes' UUIDs
   **/
  public ArrayList<UUID> getAllId() {
    return this.allId;
  }

}
