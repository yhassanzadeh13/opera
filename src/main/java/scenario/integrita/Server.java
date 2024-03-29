package scenario.integrita;

import java.util.ArrayList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import groovy.lang.Tuple;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.model.Event;
import node.BaseNode;
import node.Identifier;
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
  @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "skipping unread fields error, work-in-progress")
  byte[] sk; // server's signature key
  HistoryTreeStore db;
  NodeAddress status; // the last node address seen by the server
  // simulator related properties
  @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "skipping unread fields error, work-in-progress")
  Identifier id;
  network.Network network;
  // all identifiers including self
  @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "skipping unread fields error, work-in-progress")
  ArrayList<Identifier> ids;
  private Logger logger;

  // Constructors -------------------------------------------------------------------------

  /**
   * Constructor.
   */
  public Server() {
  }

  /**
   * Constructor.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of network")
  public Server(Identifier selfId, network.Network network) {
    this.id = selfId;
    this.network = network;
    this.logger = OperaLogger.getLoggerForNodeComponent(this.getClass().getCanonicalName(), selfId, "integrita_server");
  }

  /**
   * Constructor.
   */
  public Server(int index, int totalServers) {
    this.index = index;
    this.totalServers = totalServers;

    this.db = new HistoryTreeStore();

    // generate signature keys
    byte[][] keys = Signature.keyGen();
    this.sk = keys[0];
    this.vk = keys[1];
  }

  // getters and setters ---------------------

  public NodeAddress getStatus() {
    return status;
  }

  // Integrita RPCs ---------------------------------------------------------------------

  /**
   * receives a HistoryTreeNode and updates its local db accordingly.
   */
  public Tuple push(HistoryTreeNode historyTreeNode) {
    // @TODO check the user membership via signature

    // check whether the node is submitted to the right server
    int serverIndex = NodeAddress.mapServerIndex(historyTreeNode.addr, totalServers);
    if (serverIndex != this.index) {
      return new Tuple(StatusCode.Reject, null);
    }

    // the difference between the label of supplied node and the status of the server
    // should be equal to the total number of servers
    if (this.status != null) {
      int diff = NodeAddress.toLabel(historyTreeNode.addr) - NodeAddress.toLabel(this.status);
      if (diff != totalServers) {
        return new Tuple(StatusCode.Reject, null);
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
      if (!res) {
        return new Tuple(StatusCode.Reject, null);
      }
    }

    // verify user-side signature on the tree digest
    if (NodeAddress.isTreeDigest(historyTreeNode.addr)) {
      // verify signature
      String msg = historyTreeNode.toLeaf();
      if (!Signature.verify(msg, historyTreeNode.signature, this.db.getVerificationKey(historyTreeNode.userId))) {
        new Tuple(StatusCode.Reject, null);
      }
    }

    // update the database just for non-temporary nodes
    if (!NodeAddress.isTemporary(historyTreeNode.addr) || NodeAddress.isTreeDigest(historyTreeNode.addr)) {
      db.insert(historyTreeNode);
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
      return new Tuple(StatusCode.Accept, signature);
    }

    // if nothing goes wrong, then the push request is done successfully
    return new Tuple(StatusCode.Accept, null);
  }

  // BaseNode interface implementation ---------------------------------------------------

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of allId")
  public void onCreate(ArrayList<Identifier> allId) {
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
  public void onNewMessage(Identifier originId, Event msg) {
    this.logger.info("received a new message from {} with content {}", originId, msg.toString());
    PushResp pushResp = new PushResp(StatusCode.Accept, "Hello Back");
    network.send(originId, pushResp);
  }

  @Override
  public BaseNode newInstance(Identifier selfId, String nameSpace, network.Network network) {
    return new Server(selfId, network);
  }
}
