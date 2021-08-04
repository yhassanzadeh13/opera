package node;

import underlay.MiddleLayer;
import underlay.packets.Event;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import metrics.MetricsCollector;

/**
 *
 *
 * @param <T>
 */
public class NodeThread<T extends BaseNode> extends Thread {

  private final AtomicBoolean running = new AtomicBoolean(false);
  BaseNode node;
  UUID selfId;
  ArrayList<UUID> allId;
  MetricsCollector metrics;

  /**
   *
   * @param factory
   * @param selfId
   * @param allId
   * @param middleLayer
   * @param metrics
   */
  public NodeThread(T factory, UUID selfId, ArrayList<UUID> allId, MiddleLayer middleLayer, MetricsCollector metrics) {
    this.selfId = selfId;
    this.allId = allId;
    this.metrics = metrics;
    node = factory.newInstance(selfId, middleLayer, metrics);
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
