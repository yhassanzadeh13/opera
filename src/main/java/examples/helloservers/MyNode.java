package examples.helloservers;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import metrics.MetricsCollector;
import node.BaseNode;
import node.Identity;
import underlay.Network;
import underlay.packets.Event;

/**
 * MyNode is a base node to be fixture node for the hello servers simulation.
 */
public class MyNode implements BaseNode {
  private UUID selfId;
  private ArrayList<Identity> identities;
  private Network network;
  private MetricsCollector metricsCollector; // TODO: enable metrics

  MyNode(UUID selfId, Network network, MetricsCollector metricsCollector) {
    this.selfId = selfId;
    this.network = network;
    this.metricsCollector = metricsCollector;
  }

  public MyNode() {

  }


  @Override
  public void onCreate(ArrayList<Identity> identities) {
    this.identities = identities;
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
    if (identities.isEmpty()) {
      return;
    }
    Random rand = new Random();
    int randomIndex = rand.nextInt(identities.size());
    UUID randomIdentifier = identities.get(randomIndex).getIdentifier();
    SendHello helloMessage = new SendHello(msg, selfId, randomIdentifier);
    network.send(randomIdentifier, helloMessage);
  }

  @Override
  public void onStop() {
  }

  @Override
  public void onNewMessage(UUID originId, Event msg) {
    try {
      Random rand = new Random();
      Thread.sleep(rand.nextInt(1000));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    msg.actionPerformed(this);
  }

  @Override
  public BaseNode newInstance(UUID selfId, String nameSpace, Network network, MetricsCollector metricsCollector) {
    return new MyNode(selfId, network, metricsCollector);
  }
}
