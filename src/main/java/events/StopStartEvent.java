package events;

import network.packets.Event;
import node.BaseNode;

/**
 * StopStartEvent is an event can be used to call onstart or onstop method of the node.
 */
public class StopStartEvent implements Event {
  private final String address;
  private final int port;
  private final boolean start;

  /**
   * create new stop/start event to be invoked by the node.
   *
   * @param address address of the target node
   * @param port    port of the target node
   * @param start   true for start, false for stop
   */
  StopStartEvent(String address, int port, boolean start) {
    this.address = address;
    this.port = port;
    this.start = start;
  }

  private String getFullAddress() {
    return this.address + ":" + this.port;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    if (start) {
      hostNode.onStart();
    } else {
      hostNode.onStop();
    }
    return true;
  }

  @Override
  public String logMessage() {
    if (start) {
      return "[" + this.getFullAddress() + "] node has started";
    } else {
      return "[" + this.getFullAddress() + "] node has been terminated";
    }
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }

  public boolean getState() {
    return start;
  }

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }
}
