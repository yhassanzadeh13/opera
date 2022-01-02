package scenario.integrita.events;

import node.BaseNode;
import underlay.packets.Event;

/**
 *  PullResp event encapsulates a sever-side response to the client's pull request.
 */
public class PullResp implements Event {
  String msg;

  public void setMsg(String msg) {
    this.msg = msg;
  }

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
