package scenario.integrita.events;

import node.BaseNode;
import underlay.packets.Event;

public class Pull implements Event {
  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return false;
  }

  @Override
  public String logMessage() {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }
}
