package scenario.integrita;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import metrics.MetricsCollector;
import network.Network;
import network.packets.Event;
import node.BaseNode;
import node.Identity;
import scenario.integrita.events.PushResp;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.utils.StatusCode;
import underlay.Network;

/**
 * Integrita server implementation.
 */
public class Server implements BaseNode {
  UUID id;
  Network network;
  ArrayList<Identity> ids; // all ids including self
  HashMap<NodeAddress, HistoryTreeNode> db = new HashMap<>();

  public Server() {
  }

  public Server(UUID selfId, Network network) {
    this.id = selfId;
    this.network = network;
  }

  @Override
  public void onCreate(ArrayList<Identity> identities) {
    this.ids = identities;
    this.network.ready();
  }

  // BaseNode interface implementation ------------
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
  public BaseNode newInstance(UUID selfId, String nameSpace, Network network, MetricsCollector metrics) {
    Server server = new Server(selfId, network);
    return server;
  }
}
