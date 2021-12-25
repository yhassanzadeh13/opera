package node;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import metrics.MetricsCollector;
import underlay.MiddleLayer;
import underlay.packets.Event;


/**
 * A Node object which can do tasks that a basenode can do.
 *
 * @param <T> variable type of the factory
 */
public class NodeThread<T extends BaseNode> extends Thread {

  private final AtomicBoolean running = new AtomicBoolean(false);
  BaseNode node;
  UUID selfId;
  ArrayList<UUID> allId;
  MetricsCollector metrics;

  /**
   * Constructs a new factory instance using the given parameters.
   *
   * @param factory     Consists of middlelayer, IDs and metric collector
   * @param selfId      Id of the node
   * @param allId       List of Id's of all the nodes
   * @param middleLayer layer of the modes
   * @param metrics     is a metric collector.
   */
  public NodeThread(T factory, UUID selfId, ArrayList<UUID> allId, MiddleLayer middleLayer, MetricsCollector metrics) {
    this.selfId = selfId;
    this.allId = allId;
    this.metrics = metrics;
    node = factory.newInstance(selfId, null, middleLayer, metrics);
  }

  @Override
  public void run() {
    while (running.get()) {
      continue;
    }
  }

  public void terminate() {
    node.onStop();
    running.set(false);
  }

  public void onNewMessage(UUID originId, Event msg) {
    node.onNewMessage(originId, msg);
  }

  public void onCreate(ArrayList<UUID> allId) {
    node.onCreate(allId);
  }

  public void onStart() {
    node.onStart();
  }

  public void onStop() {
    node.onStop();
  }

}
