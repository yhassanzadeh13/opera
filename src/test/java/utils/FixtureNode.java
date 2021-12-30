package utils;

import java.util.ArrayList;
import java.util.UUID;
import metrics.MetricsCollector;
import node.BaseNode;
import underlay.Network;
import underlay.packets.Event;

/**
 * A basic BaseNode to check whether Utils coded correctly or not.
 */
public class FixtureNode implements BaseNode {
  private UUID selfId;
  private ArrayList<UUID> allId;
  private Network network;
  public int receivedMessages = 0;

  FixtureNode() {
  }

  FixtureNode(UUID selfId, Network network) {
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
  public BaseNode newInstance(UUID selfId, String nameSpace, Network network, MetricsCollector metricsCollector) {
    return new FixtureNode(selfId, network);
  }

  @Override
  public void onNewMessage(UUID originId, Event msg) {

  }
}
