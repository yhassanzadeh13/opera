package simulatorevents;

import java.util.UUID;
import node.BaseNode;
import underlay.packets.Event;


public class ReadyEvent implements Event {
  private UUID nodeId;
  private String fullAddress;

  /**
   * create new ready event. Used by the node to declare itself as ready
   *
   * @param nodeId      UUID of the node
   * @param fullAddress full address of the node
   */
  ReadyEvent(UUID nodeId, String fullAddress) {
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
