package scenario.integrita;

import java.util.ArrayList;
import java.util.UUID;

import metrics.MetricsCollector;
import network.Network;
import network.packets.Event;
import node.BaseNode;
import scenario.integrita.events.Push;
import scenario.integrita.historytree.HistoryTreeNode;


/**
 * Integrita client implementation.
 */
public class Client implements BaseNode {
  UUID id;
  Network network;
  ArrayList<UUID> ids; // all ids inclding self

  public Client() {

  }

  public Client(UUID selfId, Network network) {
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
    for (UUID receiver : ids) {
      if (receiver.equals(this.id)) {
        continue;
      }
      // create an empty node
      Push pushMsg = new Push(new HistoryTreeNode(), "Hello");
      network.send(receiver, pushMsg);
    }
  }

  @Override
  public void onStop() {

  }

  @Override
  public void onNewMessage(UUID originId, Event msg) {
    System.out.println("Sender UUID: " + originId.toString() + " message " + msg.logMessage());

  }

  @Override
  public BaseNode newInstance(UUID selfId, String nameSpace, Network network, MetricsCollector metrics) {
    Client client = new Client(selfId, network);
    return client;
  }
}
