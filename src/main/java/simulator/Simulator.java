package simulator;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import metrics.MetricsCollector;
import metrics.opera.OperaCollector;
import node.BaseNode;
import org.apache.log4j.Logger;
import underlay.MiddleLayer;
import underlay.UnderlayFactory;
import underlay.UnderlayType;
import underlay.local.LocalUnderlay;
import underlay.packets.Event;
import utils.SimpleEntryComparable;
import utils.SimulatorUtils;
import utils.generator.BaseGenerator;
import utils.generator.GaussianGenerator;


/**
 * Simulator simulates situations between nodes with actions performed between the nodes.
 * Simulator also can create new instances for the nodes.
 * Simulator can simulate in two ways: churn-based, time-based.
 *
 * @param <T> Type of the BaseNode.
 */
public class Simulator<T extends BaseNode> implements BaseNode, Orchestrator {
  private static final Random rand = new Random();
  private static final UUID SimulatorID = UUID.randomUUID();
  public static Logger log = Logger.getLogger(Simulator.class.getName());
  private final ArrayList<UUID> allId;
  private final HashMap<UUID, SimpleEntry<String, Integer>> allFullAddresses;
  private final HashMap<SimpleEntry<String, Integer>, Boolean> isReady;
  private final T factory;
  private final ArrayList<UUID> offlineNodes = new ArrayList<>();
  private final CountDownLatch count;
  private final HashMap<SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay = new HashMap<>();
  private final MetricsCollector metricsCollector;
  public HashMap<String, Integer> nodesSimulatedLatency = new HashMap<>();
  private HashMap<SimpleEntry<String, Integer>, MiddleLayer> allMiddleLayers;
  private PriorityQueue<SimpleEntryComparable<Long, UUID>> onlineNodes = new PriorityQueue<>();

  /**
   * Initializes a new simulation.
   *
   * @param factory     a dummy factory instance of special node class.
   * @param n           the number of nodes.
   * @param networkType the type of simulated communication protocol(**tcp**, **javarmi**, **udp**, and **mockNetwork*)
   */
  public Simulator(T factory, int n, UnderlayType networkType) {
    this.factory = factory;
    this.isReady = new HashMap<>();
    this.allId = generateIds(n);
    int startPort = 2000;
    this.allFullAddresses = generateFullAddressed(n, startPort + 1);

    SimulatorUtils.configurePrometheus();

    // CountDownLatch for awaiting the start of the simulation until all nodes are ready
    count = new CountDownLatch(n);

    // initializes metrics collector
    this.metricsCollector = new OperaCollector();

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
  private void generateNodesInstances(UnderlayType networkType) {
    this.allMiddleLayers = new HashMap<>();
    log.debug("[simulator.simulator] Generating new nodes instances");

    // generate nodes, and middle layers instances
    for (UUID id : allId) {
      isReady.put(this.allFullAddresses.get(id), false);
      MiddleLayer middleLayer = new MiddleLayer(id, this.allFullAddresses, isReady, this, this.metricsCollector);
      BaseNode node = factory.newInstance(id, middleLayer, this.metricsCollector);
      middleLayer.setOverlay(node);
      this.allMiddleLayers.put(this.allFullAddresses.get(id), middleLayer);
    }

    // generate new underlays and assign them to the nodes middles layers.
    for (Map.Entry<SimpleEntry<String, Integer>, MiddleLayer> node : this.allMiddleLayers.entrySet()) {
      MiddleLayer middleLayer = node.getValue();
      String address = node.getKey().getKey();
      int port = node.getKey().getValue();
      if (networkType != UnderlayType.MOCK_NETWORK) {
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

  @Override
  public void onCreate(ArrayList<UUID> allId) {
  }

  @Override
  public void onStart() {
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

  @Override
  public void onStop() {
    log.info("Nodes will be terminated....");

    //terminating all nodes
    while (!onlineNodes.isEmpty()) {
      this.done(onlineNodes.poll().getValue());
    }
  }

  @Override
  public void onNewMessage(UUID originId, Event msg) {
    // TODO make the communication between the nodes and the master nodes through the underlay
    msg.actionPerformed(this);
  }

  @Override
  public BaseNode newInstance(UUID selfId, MiddleLayer middleLayer, MetricsCollector metricsCollector) {
    return null;
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
      middleLayer.stop(fullAddress.getKey(), fullAddress.getValue());
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
   * get the simulated delay based on the normal distribution extracted from the AWS.
   *
   * @param nodeA         first node
   * @param nodeB         second node
   * @param bidirectional True, if simulated latency from A to B is the same as from B to A
   * @return new simulated latency
   */
  @Override
  public int getSimulatedLatency(UUID nodeA, UUID nodeB, boolean bidirectional) {
    if (bidirectional && nodeA.compareTo(nodeB) < 0) {
      UUID tmp = nodeA;
      nodeA = nodeB;
      nodeB = tmp;
    }
    String hash = SimulatorUtils.hashPairOfNodes(nodeA, nodeB);
    if (!this.nodesSimulatedLatency.containsKey(hash)) {
      final int mean = 159;
      final int std = 96;
      GaussianGenerator generator = new GaussianGenerator(mean, std);
      this.nodesSimulatedLatency.put(hash, generator.next());
    }
    return this.nodesSimulatedLatency.get(hash);
  }

  /**
   * Used to start the simulation.
   * It calls the onStart method for all nodes to start the simulation.
   *
   * @param duration duration of the simulation
   */
  public void constantSimulation(int duration) {
    this.onStart();
    getLogger().info("Simulation started");

    try {
      Thread.sleep(duration);
    } catch (Exception e) {
      e.printStackTrace();
    }

    log.info("Simulation duration finished");

    this.onStop();
  }

  public String getAddress(UUID nodeId) {
    SimpleEntry<String, Integer> address = allFullAddresses.get(nodeId);
    return address.getKey() + ":" + address.getValue();
  }

  /**
   * Simulate churn based on inter-arrival time and session length.
   *
   * @param duration duration of the simulator
   * @param arrival  inter-arrival
   * @param session  session length
   */
  public void churnSimulation(long duration, BaseGenerator arrival, BaseGenerator session) {
    final String sessionMetric = "SessionLength";
    final String arrivalMetric = "InterArrival";

    // initialize all nodes
    this.onStart();
    System.out.println("Simulation started");
    getLogger().info("Simulation started");

    // register a prometheus histogram for session length
    double[] labels = new double[10];
    for (int i = 1; i <= 10; i++) {
      labels[10 - i] = session.mn + (double) (session.mx - session.mn) / i + 0.001;
    }
    this.metricsCollector.histogram().register(sessionMetric, labels);

    // register a prometheus histogram for inter arrival time
    for (int i = 1; i <= 10; i++) {
      labels[10 - i] = arrival.mn + (double) (arrival.mx - arrival.mn) / i + 0.001;
    }
    this.metricsCollector.histogram().register(arrivalMetric, labels);

    // hold the current online nodes, with their termination time stamp.
    this.onlineNodes = new PriorityQueue<>();

    // assign initial terminate time stamp for all nodes
    long time = System.currentTimeMillis();
    for (UUID id : this.allId) {
      if (isReady.get(this.allFullAddresses.get(id))) {
        int ex = session.next();
        log.info("[simulator.simulator] new session for node " + getAddress(id) + ": " + ex + " ms");
        onlineNodes.add(new SimpleEntryComparable<>(time + ex, id));
        this.metricsCollector.histogram().observe(sessionMetric, id, ex);
      }
    }
    // hold next arrival time
    long nxtArrival = System.currentTimeMillis() + arrival.next();
    this.metricsCollector.histogram().observe(arrivalMetric, SimulatorID, nxtArrival);

    while (System.currentTimeMillis() - time < duration) {
      if (!onlineNodes.isEmpty() && System.currentTimeMillis() > onlineNodes.peek().getKey()) {
        // terminate the node with the nearest termination time (if the time met)
        // `done` will add the node to the offline nodes
        UUID id = onlineNodes.poll().getValue();
        log.info("[simulator.simulator] Deactivating node " + getAddress(id));
        this.done(id);
      }
      if (System.currentTimeMillis() >= nxtArrival) {
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
        int ex = session.next();
        log.info("[simulator.simulator] new session for node " + getAddress(id) + ": " + ex + " ms");
        this.onlineNodes.add(new SimpleEntryComparable<>(System.currentTimeMillis() + ex, id));
        this.metricsCollector.histogram().observe(sessionMetric, id, ex);

        // assign a next node arrival time
        nxtArrival = System.currentTimeMillis() + arrival.next();
        log.info("[simulator.simulator] next node arrival: " + nxtArrival);
        this.metricsCollector.histogram().observe(arrivalMetric, SimulatorID, nxtArrival);
      }
    }

    log.info("Simulation duration finished");
    System.out.println("Simulation duration finished");

    // stop the simulation.
    this.onStop();
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
