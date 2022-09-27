package scenario.integrita;

import java.util.ArrayList;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import metrics.MetricsCollector;
import network.MiddleLayer;
import network.packets.Event;
import node.BaseNode;
import node.Identifier;
import scenario.integrita.events.Push;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.user.User;


/**
 * Integrita client implementation.
 */
public class Client extends User implements BaseNode {
  Identifier id;
  MiddleLayer network;
  ArrayList<Identifier> ids; // all ids inclding self

  public Client() {

  }

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of MiddleLayer")
  public Client(Identifier selfId, MiddleLayer network) {
    this.id = selfId;
    this.network = network;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of allId")
  public void onCreate(ArrayList<Identifier> allId) {
    this.ids = allId;
    this.network.ready();
  }

  @Override
  public void onStart() {
    for (Identifier receiver : ids) {
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
  public void onNewMessage(Identifier originId, Event msg) {
    System.out.println("Sender UUID: " + originId.toString() + " message " + msg.logMessage());

  }

  @Override
  public BaseNode newInstance(Identifier selfId, String nameSpace, MiddleLayer network, MetricsCollector metrics) {
    Client client = new Client(selfId, network);
    return client;
  }
}
