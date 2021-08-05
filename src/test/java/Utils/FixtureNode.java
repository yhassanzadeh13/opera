package utils;

import java.util.ArrayList;
import java.util.UUID;
import metrics.MetricsCollector;
import node.BaseNode;
import underlay.MiddleLayer;
import underlay.packets.Event;

/**
 * A basic BaseNode to check whether Utils coded correctly or not.
 */
public class FixtureNode implements BaseNode {
  private UUID selfId;
  private ArrayList<UUID> allId;
  private MiddleLayer network;
  public int receivedMessages = 0;

  FixtureNode() {
  }

  FixtureNode(UUID selfId, MiddleLayer network) {
    this.selfId = selfId;
    this.network = network;
  }


  @Override
  public void onCreate(ArrayList<UUID> allId) {
    this.allId = allId;
    network.ready();
  }

  @Override
  public void onStart() {

  }

  @Override
  public void onStop() {
  }

  @Override
  public BaseNode newInstance(UUID selfId, MiddleLayer network, MetricsCollector metricsCollector) {
    return new FixtureNode(selfId, network);
  }

  @Override
  public void onNewMessage(UUID originId, Event msg) {

  }
}
