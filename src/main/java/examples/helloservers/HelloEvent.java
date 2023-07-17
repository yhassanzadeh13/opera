package examples.helloservers;

import network.model.Event;

/**
 * HelloEvent is an event which enables node to send "Thank You" if the message is "Hello"
 * else sends "Hello" message back to that node.
 */
public class HelloEvent implements Event {
  private final String msg;

  /**
   * Constructor.
   *
   * @param msg message to send.
   */
  public HelloEvent(String msg) {
    this.msg = msg;
  }

  public String getMsg() {
    return msg;
  }
}
