package utils;

import java.util.ArrayList;

import metrics.MetricsCollector;
import network.MiddleLayer;
import network.packets.Event;
import node.BaseNode;
import node.Identifier;

/**
 * A basic BaseNode to check whether Utils coded correctly or not.
 */
public class FixtureNode implements BaseNode {
  private Identifier selfId;
  private ArrayList<Identifier> allId;
  private MiddleLayer network;

  public FixtureNode() {
  }

  FixtureNode(Identifier selfId, MiddleLayer network) {
    this.selfId = selfId;
    this.network = network;
  }


  @Override
  public void onCreate(ArrayList<Identifier> allId) {
    this.allId = allId;
    network.ready();
  }

  @Override
  public void onStart() {

  }

  @Override
  public void onStop() {
    this.network.stop();
  }

  @Override
  public BaseNode newInstance(Identifier selfId, String nameSpace, MiddleLayer network, MetricsCollector metricsCollector) {
    return new FixtureNode(selfId, network);
  }

  @Override
  public void onNewMessage(Identifier originId, Event msg) {

  }
}
