package network;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;

/**
 * A basic BaseNode to check whether Underlays coded correctly or not.
 */
public class FixtureNode implements BaseNode {
  private final Identifier selfId;
  private final ArrayList<Identifier> allId;
  private final MiddleLayer network;
  public AtomicInteger receivedMessages = new AtomicInteger(0);


  public FixtureNode(Identifier selfId, ArrayList<Identifier> allId, MiddleLayer network) {
    this.selfId = selfId;
    this.network = network;
    this.allId = allId;
  }


  @Override
  public void onCreate(ArrayList<Identifier> allId) {
  }

  @Override
  public void onStart() {
    for (Identifier id : allId) {
      if (id != selfId) {
        network.send(id, new FixtureEvent());
      }
    }
  }

  @Override
  public void onStop() {
  }

  @Override
  public BaseNode newInstance(Identifier selfId, String nameSpace, MiddleLayer network) {
    return null;
  }

  @Override
  public void onNewMessage(Identifier originId, Event msg) {
    this.receivedMessages.incrementAndGet();
  }
}
