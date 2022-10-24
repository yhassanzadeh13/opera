package events;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;

/**
 * Ready Event is an event which can be used to declare a node is ready.
 */
public class ReadyEvent implements Event {
  private final Identifier nodeId;
  private final String fullAddress;

  /**
   * create new ready event.
   *
   * @param nodeId      identifier of the node
   * @param fullAddress full address of the node
   */
  ReadyEvent(Identifier nodeId, String fullAddress) {
    this.nodeId = nodeId;
    this.fullAddress = fullAddress;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return true;
  }

  @Override
  public String logMessage() {
    return "[" + this.fullAddress + "] node is ready";
  }
}
