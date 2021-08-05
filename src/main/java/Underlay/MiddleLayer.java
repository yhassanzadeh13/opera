package underlay;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import metrics.MetricsCollector;
import node.BaseNode;
import org.apache.log4j.Logger;
import simulator.Orchestrator;
import simulator.Simulator;
import simulatorevents.StopStartEvent;
import underlay.packets.Event;
import underlay.packets.Request;
import utils.SimulatorUtils;

/**
 * Represents a mediator between the overlay and the underlay. The requests coming from the underlay are directed
 * to the overlay and the responses emitted by the overlay are returned to the underlay. The requests coming from
 * the overlay are either directed to the underlay or to another local overlay, and the emitted response is returned
 * to the overlay.
 */
public class MiddleLayer {
  private static final Logger log = Logger.getLogger(MiddleLayer.class.getName()); // todo: logger should be passed down
  //TODO add bucket size to the default metrics
  private final String delayMetric = "Delay";
  private final String sentMsgCntMetric = "Sent_Messages";
  private final String receivedMsgCntMetric = "Received_Messages";
  private final HashMap<UUID, SimpleEntry<String, Integer>> allFullAddresses;
  private final UUID nodeId;
  // TODO : make the communication between the nodes and the simulator (the master node) through the network
  private final Orchestrator orchestrator;
  private final MetricsCollector metricsCollector;
  private Underlay underlay;
  private BaseNode overlay;

  /**
   * Constructor of MiddleLayer.
   *
   * @param nodeId Id of the node
   * @param allFullAddresses Hashmap of the all addresses
   * @param isReady Hashmap of whether nodes are ready or not
   * @param orchestrator Orchestrator for the middle layer
   * @param metricsCollector Metrics collector for the middle layer
   */
  public MiddleLayer(UUID nodeId,
                     HashMap<UUID, SimpleEntry<String, Integer>> allFullAddresses,
                     HashMap<SimpleEntry<String, Integer>, Boolean> isReady, // TODO: isReady can be removed.
                     Orchestrator orchestrator,
                     MetricsCollector metricsCollector) {

    if (orchestrator == null) {
      log.fatal("cannot initialize simulator with a null runtime");
    }

    this.nodeId = nodeId;
    this.allFullAddresses = allFullAddresses;
    this.orchestrator = orchestrator;
    this.metricsCollector = metricsCollector;

    this.metricsCollector.histogram().register(delayMetric);
    this.metricsCollector.counter().register(sentMsgCntMetric);
    this.metricsCollector.counter().register(receivedMsgCntMetric);
    this.metricsCollector.histogram().register(metrics.Metrics.PACKET_SIZE,
          new double[]{1.0, 2.0, 3.0, 5.0, 10.0, 15.0, 20.0});
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
   * @param destinationId destenation node unique id.
   * @param event         the event.
   * @return true if event was sent successfully. false, otherwise.
   */
  public boolean send(UUID destinationId, Event event) {
    // check the readiness of the destination node
    SimpleEntry<String, Integer> fullAddress = allFullAddresses.get(destinationId);

    // update metrics
    this.metricsCollector.counter().inc(sentMsgCntMetric, nodeId);
    this.metricsCollector.histogram().startTimer(delayMetric, nodeId, sentBucketHash(destinationId));


    // wrap the event by request class
    Request request = new Request(event, this.nodeId, destinationId);
    String destinationAddress = fullAddress.getKey();
    Integer port = fullAddress.getValue();

    // sleep for the simulated duration
    int sleepTime = this.orchestrator.getSimulatedLatency(nodeId, destinationId, true);
    try {
      Thread.sleep(sleepTime);
    } catch (Exception e) {
      Simulator.getLogger().error("[MiddleLayer] Thread failed to sleep for the simulated delay, sleep time:"
            + sleepTime);
    }

    // Bounce the request up.
    boolean success = underlay.sendMessage(destinationAddress, port, request);

    if (success) {
      log.info("[MiddleLayer] " + this.getAddress(nodeId) + " : node sent an event " + event.logMessage());
    } else {
      log.debug("[MiddleLayer] " + this.getAddress(nodeId) + " : node could not send an event " + event.logMessage());
    }

    return success;
  }

  private String sentBucketHash(UUID id) {
    return SimulatorUtils.hashPairOfNodes(nodeId, id);
  }

  public String getAddress(UUID nodeId) {
    SimpleEntry<String, Integer> address = allFullAddresses.get(nodeId);
    return address.getKey() + ":" + address.getValue();
  }

  /**
   * Called by the underlay to collect the response from the overlay.
   */
  public void receive(Request request) {
    // check the readiness of the overlay
    SimpleEntry<String, Integer> fullAddress = allFullAddresses.get(nodeId);

    this.metricsCollector.counter().inc(receivedMsgCntMetric, nodeId);
    this.metricsCollector.histogram().tryObserveDuration(delayMetric, receivedBucketHash(request.getOriginalId()));


    log.info("[MiddleLayer] " + this.getAddress(nodeId)
          + " : node received an event " + request.getEvent().logMessage());

    // check if the event is start, stop event and handle it directly
    if (request.getEvent() instanceof StopStartEvent) {
      StopStartEvent event = (StopStartEvent) request.getEvent();
      if (event.getState()) {
        this.start();
      } else {
        this.stop(event.getAddress(), event.getPort());
      }
    } else {
      this.metricsCollector.histogram().observe(metrics.Metrics.PACKET_SIZE,
            request.getOriginalId(),
            request.getEvent().size());
      overlay.onNewMessage(request.getOriginalId(), request.getEvent());
    }

  }

  private String receivedBucketHash(UUID id) {
    return SimulatorUtils.hashPairOfNodes(id, nodeId);
  }

  /**
   * start the node in a new thread.
   * This method will be called once the simulator send a start event to the node
   */
  public void start() {
    log.info("[MiddleLayer] node " + getAddress(nodeId) + " is starting");
    new Thread(() -> overlay.onStart()).start();
  }

  /**
   * Terminates the node in a new thread.
   */
  public void stop(String address, int port) {
    new Thread(() -> {
      overlay.onStop();
      boolean success = underlay.terminate(address, port);
      if (success) {
        log.info("[MiddleLayer] node " + getAddress(nodeId) + " is terminating");
      } else {
        log.error("[MiddleLayer] node " + getAddress(nodeId) + " could not be terminated");
      }
    }).start();
  }

  /**
   * declare the node as ready (called by the overlay).
   */
  public void ready() {
    log.info("[MiddleLayer] node " + getAddress(nodeId) + " is ready");
    this.orchestrator.ready(this.nodeId);
  }

  /**
   * request node termination (called by the overlay).
   */
  public void done() {
    log.info("[MiddleLayer] node " + getAddress(nodeId) + " requests termination");
    this.orchestrator.done(this.nodeId);
  }

  /**
   * Underlay initializer.
   */
  public void initUnderLay() {
    log.info("[MiddleLayer] initializing the underlay for node " + getAddress(nodeId));

    int port = this.allFullAddresses.get(this.nodeId).getValue();
    this.underlay.initUnderlay(port);
  }

  /**
   * Call the node onCreat on a new thread.
   *
   * @param allId List of IDs of all nodes.
   */
  public void create(ArrayList<UUID> allId) {
    log.info("[MiddleLayer] creating node " + getAddress(nodeId));
    new Thread(() -> overlay.onCreate(allId)).start();
  }
}
