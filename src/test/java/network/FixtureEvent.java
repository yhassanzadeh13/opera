package network;

import network.packets.Event;
import node.BaseNode;

/**
 * A basic Event to use to check whether Underlays coded correctly.
 */
public class FixtureEvent implements Event {
  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return true;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
