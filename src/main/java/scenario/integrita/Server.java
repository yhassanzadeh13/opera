package scenario.integrita;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import metrics.MetricsCollector;
import node.BaseNode;
import scenario.integrita.events.PushResp;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.utils.StatusCode;
import underlay.MiddleLayer;
import underlay.packets.Event;

/**
 * Integrita server implementation.
 */
public class Server implements BaseNode {
  UUID id;
  MiddleLayer network;
  ArrayList<UUID> ids; // all ids including self
  HashMap<NodeAddress, HistoryTreeNode> db = new HashMap<>();
  int index;
  int totalNumServers;
  NodeAddress status;

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

  // Integrita methods

  /**
   * implements integrita push algorithm.
   *
   * @param node the history tree node
   * @return
   */
  public boolean push(HistoryTreeNode node) {
    // check the membership

    // check the correctness of server index
    int expected = NodeAddress.F(node.addr, totalNumServers);
    if (expected != this.index) {
      return false;
    }

    // check the labels
    int dist = NodeAddress.L(node.addr) - NodeAddress.L(this.status);
    if (dist != this.totalNumServers) {
      return false;
    }

    // check the signature and the hash integrity
    if (NodeAddress.isLeaf(node.addr)) {
      // @TODO verify the signature
      // @TODO check the hash correctness
    }

    // check the tree digest
    if (NodeAddress.isTreeDigest(node.addr)){
      // @TODO verify the signature
    }

    // insert to the db
    

    return true;
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
  public BaseNode newInstance(UUID selfId, String nameSpace, MiddleLayer network, MetricsCollector metrics) {
    Server server = new Server(selfId, network);
    return server;
  }
}
