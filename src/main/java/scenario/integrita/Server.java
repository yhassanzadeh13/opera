package scenario.integrita;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import metrics.MetricsCollector;
import network.MiddleLayer;
import network.packets.Event;
import node.BaseNode;
import scenario.integrita.events.PushResp;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.utils.StatusCode;
/**
 * Integrita server implementation.
 */
public class Server implements BaseNode {
  UUID id;
  MiddleLayer network;
  ArrayList<UUID> ids; // all ids including self
  HashMap<NodeAddress, HistoryTreeNode> db = new HashMap<>();

  public Server() {
  }

  public Server(UUID selfId, MiddleLayer network) {
    this.id = selfId;
    this.network = network;
  }

  @Override
  public void onCreate(ArrayList<UUID> allId) {
    this.ids = allId;
    this.network.ready();
  }

  @Override
  public void onStart() {

  }

  @Override
  public void onStop() {

  }

  @Override
  public void onNewMessage(UUID originId, Event msg) {
    System.out.println("Sender UUID: " + originId.toString() + " message " + msg.logMessage());
    PushResp pushResp = new PushResp(StatusCode.Accept, "Hello Back");
    network.send(originId, pushResp);
  }

  @Override
  public BaseNode newInstance(UUID selfId, String nameSpace, MiddleLayer network, MetricsCollector metrics) {
    Server server = new Server(selfId, network);
    return server;
  }
}
