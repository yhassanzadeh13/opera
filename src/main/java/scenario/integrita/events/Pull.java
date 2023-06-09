package scenario.integrita.events;

import network.packets.Event;
import node.BaseNode;

/**
 * Pull event encapsulates a client-side pull request to the server.
 */
public class Pull implements Event {
  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return false;
  }

  @Override
  public int size() {
    return 0;
  }
}
