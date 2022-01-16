package scenario.integrita;

import java.util.ArrayList;
import java.util.UUID;

import groovy.lang.Tuple;
import metrics.MetricsCollector;
import network.MiddleLayer;
import network.packets.Event;
import node.BaseNode;
import scenario.integrita.database.HistoryTreeStore;
import scenario.integrita.events.PushResp;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.signature.Signature;
import scenario.integrita.utils.StatusCode;

/**
 * Integrita server implementation.
 */
public class Server implements BaseNode {
  UUID id;
  MiddleLayer network;
  ArrayList<UUID> ids; // all ids including self
  HistoryTreeStore db;
  int totalServers;
  int index;
  NodeAddress status;
  byte[] signVerificationKey; // server's vk

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

  public Tuple push(HistoryTreeNode historyTreeNode) {
    // @TODO check the user membership via signature

    // check whether the node is submitted to the right server
    int serverIndex = NodeAddress.mapServerIndex(historyTreeNode.addr, totalServers);
    if (serverIndex != this.index) {
      return new Tuple(new Object[]{StatusCode.Reject, null});
    }

    // the difference between the label of supplied node and the status of the server
    // should be equal to the total number of servers
    int diff = NodeAddress.toLabel(historyTreeNode.addr) - NodeAddress.toLabel(this.status);
    if (diff != totalServers) {
      return new Tuple(new Object[]{StatusCode.Reject, null});
    }

    // verify user-side signature on the leaf
    // needed for the authorization
    if (NodeAddress.isLeaf(historyTreeNode.addr)) {
      //  @TODO check the hash value
      // @TODO retrieve user vk and verify the signature
      byte[] vk = db.getVerificationKey(historyTreeNode.userId);
      String msg = historyTreeNode.toLeaf();
      boolean res = Signature.verify(msg, historyTreeNode.signature, vk);
      if (res == false) {
        return new Tuple(new Object[]{StatusCode.Reject, null});
      }
    }

    // verify user-side signature on the tree digest
    if (NodeAddress.isTreeDigest(historyTreeNode.addr)) {
      // verify signature
      String msg = historyTreeNode.toLeaf();
      if (!Signature.verify(msg, historyTreeNode.signature, this.db.getVerificationKey(historyTreeNode.userId))) {
        new Tuple(new Object[]{StatusCode.Reject, null});
      }
    }

    // update the database just for non-empty nodes
    if (!NodeAddress.isTemporary(historyTreeNode.addr)) {
      db.historyTreeNodes.put(historyTreeNode.addr, historyTreeNode);
    }

    // remove tree digests of the old operations
    // except the first operation
    db.cleanDigests(historyTreeNode.addr);

    // update the state variable
    this.status = historyTreeNode.addr;

    // server should sign tree digests
    if (NodeAddress.isTreeDigest(historyTreeNode.addr)) {
      String msg = historyTreeNode.toLeaf();
      byte[] signature = Signature.sign(msg, signVerificationKey);
      return new Tuple(new Object[]{StatusCode.Accept, signature});
    }

    // if nothing goes wrong, then the push request is done successfully
    return new Tuple(new Object[]{StatusCode.Accept, null});
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
