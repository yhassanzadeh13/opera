package examples.helloservers;

import java.util.ArrayList;
import java.util.Random;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;

/**
 * MyNode is a basenode to be fixture node for the hello servers simulation.
 */
public class MyNode implements BaseNode {
  private static final Random rng = new Random();
  private Identifier selfId;
  private ArrayList<Identifier> allId;
  private network.Network network;

  // TODO: enable metrics
  MyNode(Identifier selfId, network.Network network) {
    this.selfId = selfId;
    this.network = network;
  }

  public MyNode() {

  }


  @Override
  public void onCreate(final ArrayList<Identifier> allId) {
    this.allId = (ArrayList<Identifier>) allId.clone();
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
  public void sendNewMessage(String msg) {
    if (allId.isEmpty()) {
      return;
    }
    int ind = rng.nextInt(allId.size());
    HelloEvent helloMessage = new HelloEvent(msg, selfId, allId.get(ind));
    network.send(allId.get(ind), helloMessage);
  }

  @Override
  public void onStop() {
  }

  @Override
  public void onNewMessage(Identifier originId, Event msg) {
    try {
      Thread.sleep(rng.nextInt(1000));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    msg.actionPerformed(this);
  }

  @Override
  public BaseNode newInstance(Identifier selfId, String nameSpace, network.Network network) {
    return new MyNode(selfId, network);
  }
}
