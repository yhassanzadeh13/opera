package network;

import node.BaseNode;
import network.packets.Event;

/**
 * A basic Event to use to check whether Underlays coded correctly.
 */
public class FixtureEvent implements Event {
  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
