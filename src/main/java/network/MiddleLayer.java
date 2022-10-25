package network;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import events.StopStartEvent;
import metrics.MetricsCollector;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.latency.LatencyGenerator;
import network.packets.Event;
import network.packets.Request;
import node.BaseNode;
import node.Identifier;
import simulator.Orchestrator;
import simulator.Simulator;

/**
 * Represents a mediator between the overlay and the underlay. The requests coming from the underlay are directed
 * to the overlay and the responses emitted by the overlay are returned to the underlay. The requests coming from
 * the overlay are either directed to the underlay or to another local overlay, and the emitted response is returned
 * to the overlay.
 */
// TODO: rename MiddleLayer to NetworkBroker
public class MiddleLayer {
  private static final Logger logger = OperaLogger.getLogger(MiddleLayer.class.getCanonicalName());
  //TODO add bucket size to the default metrics
  private final HashMap<Identifier, SimpleEntry<String, Integer>> allFullAddresses;
  private final Identifier nodeId;
  // TODO : make the communication between the nodes and the simulator (the master node) through the network
  private final Orchestrator orchestrator;
  private final MiddleLayerMetricsCollector metricsCollector;
  private final LatencyGenerator latencyGenerator;
  private Underlay underlay;
  private BaseNode overlay;

  /**
   * Constructor of MiddleLayer.
   *
   * @param nodeId           identifier of the node
   * @param allFullAddresses Hashmap of the all addresses
   * @param isReady          Hashmap of whether nodes are ready or not
   * @param orchestrator     Orchestrator for the middle layer
   * @param metricsCollector Metrics collector for the middle layer
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of allFullAddresses")
  public MiddleLayer(Identifier nodeId,
                     HashMap<Identifier, SimpleEntry<String, Integer>> allFullAddresses, // TODO: change to an array of address info
                     HashMap<SimpleEntry<String, Integer>, Boolean> isReady, // TODO: isReady can be removed.
                     Orchestrator orchestrator,
                     MetricsCollector metricsCollector) {

    if (orchestrator == null) {
      logger.fatal("cannot initialize simulator with a null runtime");
    }

    this.nodeId = nodeId;
    this.allFullAddresses = allFullAddresses;
    this.orchestrator = orchestrator;
    this.metricsCollector = new MiddleLayerMetricsCollector(metricsCollector);
    this.latencyGenerator = new LatencyGenerator();
  }

  public Underlay getUnderlay() {
    return underlay;
  }

  public void setUnderlay(Underlay underlay) {
    this.underlay = underlay;
  }

  public BaseNode getOverlay() {
    return overlay;
  }

  public void setOverlay(BaseNode overlay) {
    this.overlay = overlay;
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
    int sleepTime = this.latencyGenerator.getSimulatedLatency(nodeId, destinationId, true);
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException ex) {
      logger.fatal("failed to sleep thread for the simulated delay, sleep time:" + sleepTime, ex);
    }

    // Bounce the request up.
    boolean success = underlay.sendMessage(destinationAddress, port, request);
    if (success) {
      logger.info("event sent from  {} to {}", nodeId, destinationId);
    } else {
      logger.warn("event failed to send from {} to {}", nodeId, destinationId);
    }

    // updates metrics
    this.metricsCollector.onMessageSent(nodeId, destinationId);

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
    this.metricsCollector.onMessageReceived(nodeId,
        request.getOriginalId(),
        request.getEvent().size(),
        request.getSentTimeStamp());

    // TODO: add request type
    logger.info("event received from {} to {}", request.getOriginalId(), nodeId);

    // check if the event is start, stop event and handle it directly
    if (request.getEvent() instanceof StopStartEvent) {
      StopStartEvent event = (StopStartEvent) request.getEvent();
      if (event.getState()) {
        this.start();
      } else {
        this.stop();
      }
    } else {
      overlay.onNewMessage(request.getOriginalId(), request.getEvent());
    }

  }

  /**
   * start the node in a new thread.
   * This method will be called once the simulator send a start event to the node
   */
  public void start() {
    logger.info("starting node {} on address {}", nodeId, getAddress(nodeId));
    new Thread(() -> overlay.onStart()).start();
  }

  /**
   * Terminates the node in a new thread.
   */
  public void stop() {
    new Thread(() -> {
      overlay.onStop();
      boolean success = underlay.terminate();
      if (success) {
        logger.info("node {} terminated", nodeId);
      } else {
        logger.error("could not terminate node {} on {}", nodeId, getAddress(nodeId));
      }
    }).start();
  }

  /**
   * declare the node as ready (called by the overlay).
   */
  public void ready() {
    logger.info("node {} is ready and has started on {}", nodeId, getAddress(nodeId));
    this.orchestrator.ready(this.nodeId);
  }

  /**
   * request node termination (called by the overlay).
   */
  // TODO: this method is not used. remove it.
  public void done() {
    logger.debug("node {} requests termination", nodeId);
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
    new Thread(() -> overlay.onCreate(allId)).start();
  }
}
