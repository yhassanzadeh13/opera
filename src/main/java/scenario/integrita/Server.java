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
  // Integrita related fields
  int index; // server's index
  int totalServers; // total number of servers
  byte[] vk; // server's verification key
  byte[] sk; // server's signature key
  HistoryTreeStore db;
  NodeAddress status; // the last node address seen by the server

  // simulator related properties
  UUID id;
  MiddleLayer network;
  // all UUIDs including self
  ArrayList<UUID> ids;

  // Constructors -------------------------------------------------------------------------
  public Server() {
  }

  public Server(UUID selfId, MiddleLayer network) {
    this.id = selfId;
    this.network = network;
  }

  public Server(int index, int totalServers) {
    this.index = index;
    this.totalServers = totalServers;

    this.db = new HistoryTreeStore();

    // generate signature keys
    byte[][] keys = Signature.keyGen();
    this.sk = keys[0];
    this.vk = keys[1];
  }

  // Integrita RPCs ---------------------------------------------------------------------
  public Tuple push(HistoryTreeNode historyTreeNode) {
    // @TODO check the user membership via signature

    // check whether the node is submitted to the right server
    int serverIndex = NodeAddress.mapServerIndex(historyTreeNode.addr, totalServers);
    if (serverIndex != this.index) {
      return new Tuple(new Object[]{StatusCode.Reject, null});
    }

    // the difference between the label of supplied node and the status of the server
    // should be equal to the total number of servers
    if (this.status != null) {
      int diff = NodeAddress.toLabel(historyTreeNode.addr) - NodeAddress.toLabel(this.status);
      if (diff != totalServers) {
        return new Tuple(new Object[]{StatusCode.Reject, null});
      }
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
      byte[] signature = Signature.sign(msg, vk);
      return new Tuple(new Object[]{StatusCode.Accept, signature});
    }

    // if nothing goes wrong, then the push request is done successfully
    return new Tuple(new Object[]{StatusCode.Accept, null});
  }

  // BaseNode interface implementation ---------------------------------------------------

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
