package scenario.integrita;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.model.Event;
import node.BaseNode;
import node.Identifier;
import scenario.integrita.events.Push;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.user.User;

import java.util.ArrayList;


/**
 * Integrita client implementation.
 */
public class Client extends User implements BaseNode {
  Identifier id;
  network.Network network;
  ArrayList<Identifier> ids; // all ids inclding self
  private Logger logger;

  public Client() {

  }

  /**
   * Constructor of Client.
   *
   * @param selfId  identifier of the node.
   * @param network network of the node.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of MiddleLayer")
  public Client(Identifier selfId, network.Network network) {
    this.id = selfId;
    this.network = network;
    this.logger = OperaLogger.getLoggerForNodeComponent(this.getClass().getCanonicalName(), selfId, "integrita_client");
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
    this.logger.info("received message from {} with content {}", originId, msg.toString());
  }

  @Override
  public BaseNode newInstance(Identifier selfId, String nameSpace, network.Network network) {
    return new Client(selfId, network);
  }
}
