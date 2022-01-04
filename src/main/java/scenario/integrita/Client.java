package scenario.integrita;

import java.util.ArrayList;
import java.util.UUID;

import metrics.MetricsCollector;
import network.Network;
import network.packets.Event;
import node.BaseNode;
import node.Identity;
import scenario.integrita.events.Push;
import scenario.integrita.historytree.HistoryTreeNode;


/**
 * Integrita client implementation.
 */
public class Client implements BaseNode {
  UUID id;
  Network network;
  ArrayList<Identity> identities;

  public Client() {

  }

  public Client(UUID selfId, Network network) {
    this.id = selfId;
    this.network = network;
  }

  @Override
  public void onCreate(ArrayList<Identity> identities) {
    this.identities = identities;
    this.network.ready();
  }

  @Override
  public void onStart() {
    for (Identity receiver : this.identities) {
      if (receiver.getIdentifier().equals(this.id)) {
        continue;
      }
      // create an empty node
      Push pushMsg = new Push(new HistoryTreeNode(), "Hello");
      network.send(receiver.getIdentifier(), pushMsg);
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
