package network;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import metrics.MetricsCollector;
import node.BaseNode;
import network.packets.Event;

/**
 * A basic BaseNode to check whether Underlays coded correctly or not.
 */
public class FixtureNode implements BaseNode {
  private final UUID selfId;
  private final ArrayList<UUID> allId;
  private final MiddleLayer network;
  public AtomicInteger receivedMessages = new AtomicInteger(0);

  FixtureNode(UUID selfId, ArrayList<UUID> allId, MiddleLayer network) {
    this.selfId = selfId;
    this.network = network;
    this.allId = allId;
  }


  @Override
  public void onCreate(ArrayList<UUID> allId) {
  }

  @Override
  public void onStart() {
    for (UUID id : allId) {
      if (id != selfId) {
        network.send(id, new FixtureEvent());
      }
    }
  }

  @Override
  public void onStop() {
  }

  @Override
  public BaseNode newInstance(UUID selfId, String nameSpace, MiddleLayer network, MetricsCollector metrics) {
    return null;
  }

  @Override
  public void onNewMessage(UUID originId, Event msg) {
    this.receivedMessages.incrementAndGet();
  }
}
