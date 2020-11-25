package lightchain.events;

import Node.BaseNode;
import Underlay.packets.Event;

public class SubmitBlockEvent implements Event {
  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return false;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
