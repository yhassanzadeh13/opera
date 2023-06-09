package examples.helloservers;

import java.io.Serializable;

import network.packets.Event;
import node.BaseNode;

/**
 * HelloEvent is an event which enables node to send "Thank You" if the message is "Hello"
 * else sends "Hello" message back to that node.
 */
public class HelloEvent implements Event, Serializable {
  private final String msg;

  /**
   * Constructor.
   *
   * @param msg        message to send.
   */
  public HelloEvent(String msg) {
    this.msg = msg;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return true;
  }

  public String getMsg() {
    return msg;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
