package examples.helloservers;

import java.util.ArrayList;
import java.util.Random;

import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.model.Event;
import node.BaseNode;
import node.Identifier;

/**
 * MyNode is a basenode to be fixture node for the hello servers simulation.
 */
public class MyNode implements BaseNode {
  /**
   * Maximum sleep time in milliseconds before sending a message.
   */
  private static final int MAX_SLEEP_TIME_MS = 1000;

  /**
   * Random number generator.
   */
  private static final Random RNG = new Random();

  /**
   * List of all the nodes in the network.
   */
  private ArrayList<Identifier> allId;

  /**
   * Network object, which is used to send messages to other nodes.
   */
  private network.Network network;

  /**
   * Logger.
   */
  private Logger logger;

  /**
   * Constructor.
   *
   * @param selfId      the ID of the node itself.
   * @param nodeNetwork network object, which is used to send messages to other nodes.
   */
  // TODO: enable metrics
  MyNode(final Identifier selfId, final network.Network nodeNetwork) {
    this.network = nodeNetwork;
    this.logger = OperaLogger.getLoggerForNodeComponent(this.getClass().getSimpleName(), selfId);
  }

  /**
   * Default constructor, called by the simulation engine, do not call it directly!.
   */
  public MyNode() {

  }


  /**
   * Callback method which is called when the node is created. It is called before the simulation starts.
   *
   * @param identifiers the identifier of all nodes in the simulation. This includes the current node.
   */
  @Override
  public void onCreate(final ArrayList<Identifier> identifiers) {
    this.allId = new ArrayList<>(identifiers);
    network.ready();
  }

  @Override
  public void onStart() {
    this.sendNewMessage("Hello");
  }

  /**
   * Sends message to a random node.
   *
   * @param msg msg to send
   */
  public void sendNewMessage(final String msg) {
    if (allId.isEmpty()) {
      return;
    }
    int ind = RNG.nextInt(allId.size());
    HelloEvent helloMessage = new HelloEvent(msg);
    boolean success = network.send(allId.get(ind), helloMessage);
    if (success) {
      this.logger.info("sent message to {} with content {}", allId.get(ind), msg);
    } else {
      this.logger.info("failed to send message to {} with content {}", allId.get(ind), msg);
    }
  }

  @Override
  public void onStop() {
  }

  /**
   * This is a callback method which is called when a new message is received. It sleeps for a random time
   * and then sends a message back to the sender node.
   *
   * @param originId the ID of the sender node.
   * @param msg      the content of the message.
   */
  @Override
  public void onNewMessage(final Identifier originId, final Event msg) {
    try {
      Thread.sleep(RNG.nextInt(MAX_SLEEP_TIME_MS));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    this.logger.info("received message from {} with content {}", originId, msg);

    HelloEvent helloMessage = (HelloEvent) msg;
    if (helloMessage.getMsg().equals("Hello")) {
      this.sendNewMessage("Thank You");
    } else {
      this.sendNewMessage("Hello");
    }
  }

  /**
   * Factory method for creating new nodes.
   *
   * @param nodeId      the ID of the new node.
   * @param nameSpace   string tag to virtually group the nodes (with identical tags).
   * @param nodeNetwork networking component of the node.
   * @return the new node.
   */
  @Override
  public BaseNode newInstance(final Identifier nodeId, final String nameSpace, final network.Network nodeNetwork) {
    return new MyNode(nodeId, nodeNetwork);
  }
}
