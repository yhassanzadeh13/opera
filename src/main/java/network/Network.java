package network;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.latency.LatencyGenerator;
import network.model.Event;
import network.model.Request;
import node.BaseNode;
import node.Identifier;
import simulator.Orchestrator;

/**
 * Represents the high level networking layer of the individual nodes. Each instance of this class
 * is responsible for a single node. It is responsible for sending and receiving messages to and from
 * other nodes by interacting with the lower level underlay primitives, e.g., TCP, UDP, etc.
 */
public class Network {
  private final Logger logger;

  /**
   * Hash table of the full addresses of all the nodes in the network.
   * The key is the identifier of the node and the value is a pair of the IP address and the port.
   * The IP address is a string and the port is an integer.
   */
  private final HashMap<Identifier, SimpleEntry<String, Integer>> allFullAddresses;

  /**
   * Identifier of the node itself.
   */
  private final Identifier nodeId;

  /**
   * The Orchestrator is the interface of the network with the rest of the simulator.
   */
  private final Orchestrator orchestrator;

  /**
   * The metrics collector.
   */
  private final NetworkCollector metricsCollector;

  /**
   * The latency generator. Used to generate synthetic latency between nodes imitating real world networks.
   */
  private final LatencyGenerator latencyGenerator;

  /**
   * The underlay is the low level networking layer of the individual nodes, it represents a single TCP/IP transport
   * layer protocol, e.g., TCP, UDP, etc. It is responsible for sending and receiving messages to and from other nodes.
   */
  private Underlay underlay;

  /**
   * The node is the instance of the node that owns this network instance, it is responsible for handling the messages.
   */
  private BaseNode node;


  /**
   * Creates a new network instance.
   *
   * @param nodeId           the unique identifier of the node.
   * @param allFullAddresses the full addresses of all the nodes in the network.
   * @param orchestrator     the orchestrator.
   * @throws IllegalStateException if the orchestrator is null.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "allFullAddresses is externally mutable")
  public Network(Identifier nodeId,
                 HashMap<Identifier, SimpleEntry<String, Integer>> allFullAddresses,
                 Orchestrator orchestrator) throws IllegalStateException {
    if (orchestrator == null) {
      throw new IllegalArgumentException("orchestrator cannot be null");
    }

    this.nodeId = nodeId;
    this.logger = OperaLogger.getLoggerForNodeComponent(Network.class.getCanonicalName(), nodeId, "middlelayer");
    this.allFullAddresses = allFullAddresses;
    this.orchestrator = orchestrator;
    this.metricsCollector = OperaMiddlewareCollector.getInstance();
    this.latencyGenerator = new LatencyGenerator();
  }

  public Underlay getUnderlay() {
    return underlay;
  }

  public void setUnderlay(Underlay underlay) {
    this.underlay = underlay;
  }

  public Identifier getNodeId() {
    return nodeId;
  }

  public BaseNode getNode() {
    return node;
  }

  public void setNode(BaseNode node) {
    this.node = node;
  }

  /**
   * Called by the overlay to send requests to the underlay.
   *
   * @param destinationId destination node unique identifier.
   * @param event         the event.
   * @return true if event was sent successfully. false, otherwise.
   */
  public boolean send(Identifier destinationId, Event event) {
    // check the readiness of the destination node
    SimpleEntry<String, Integer> fullAddress = allFullAddresses.get(destinationId);

    // wrap the event by request class
    Request request = new Request(event, this.nodeId, destinationId);
    String destinationAddress = fullAddress.getKey();
    Integer port = fullAddress.getValue();

    // sleep for the simulated duration
    double sleepTime = this.latencyGenerator.getSimulatedLatency(nodeId, destinationId, true);
    try {
      // TODO: fix this, we don't need to sleep!
      Thread.sleep((int) sleepTime);
    } catch (InterruptedException ex) {
      this.logger.fatal("failed to sleep thread for the simulated delay, sleep time {}", sleepTime, ex);
    }

    // Bounce the request up.
    boolean success = underlay.sendMessage(destinationAddress, port, request);
    if (success) {
      this.logger.debug("sent event to {}", destinationId);
    } else {
      this.logger.warn("failed to send event to {}", destinationId);
    }

    this.metricsCollector.onMessageSent(nodeId, event.size());
    return success;
  }

  public String getAddress(Identifier nodeId) {
    SimpleEntry<String, Integer> address = allFullAddresses.get(nodeId);
    return address.getKey() + ":" + address.getValue();
  }

  /**
   * Called by the underlay to collect the response from the overlay.
   */
  public void receive(Request request) {
    this.metricsCollector.onMessageReceived(nodeId, request.getEvent().size(), request.getSentTimeStamp());
    node.onNewMessage(request.getOriginalId(), request.getEvent());
    // TODO: add request type
    this.logger.debug("event received from {}", request.getOriginalId());
  }


  /**
   * start the node in a new thread.
   * This method will be called once the simulator send a start event to the node
   */
  public void start() {
    this.logger.info("starting node on address {}", getAddress(nodeId));
    new Thread(() -> node.onStart()).start();
  }

  /**
   * Terminates the node in a new thread.
   */
  public void stop() {
    new Thread(() -> {
      node.onStop();
      try {
        underlay.terminate();
      } catch (IllegalStateException e) {
        logger.fatal("failed to terminate the underlay", e);
      }
    }).start();
  }

  /**
   * declare the node as ready (called by the overlay).
   */
  public void ready() {
    this.logger.info("node is ready and has started on {}", getAddress(nodeId));
    this.orchestrator.ready(this.nodeId);
  }

  /**
   * request node termination (called by the overlay).
   */
  // TODO: this method is not used. remove it.
  public void done() {
    logger.debug("node requests termination");
    this.orchestrator.done(this.nodeId);
  }

  /**
   * Underlay initializer.
   */
  public void initUnderLay() {
    logger.info("initializing middlelayer for node {} on address {}", nodeId, getAddress(nodeId));

    int port = this.allFullAddresses.get(this.nodeId).getValue();
    this.underlay.initUnderlay(port);
  }

  /**
   * Call the node onCreat on a new thread.
   *
   * @param allId List of IDs of all nodes.
   */
  public void create(ArrayList<Identifier> allId) {
    logger.info("creating node {} on address {}", nodeId, getAddress(nodeId));
    new Thread(() -> node.onCreate(allId)).start();
  }
}
