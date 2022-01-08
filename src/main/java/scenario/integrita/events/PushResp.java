package scenario.integrita.events;

import network.packets.Event;
import node.BaseNode;
import scenario.integrita.utils.StatusCode;

/**
 * PushResp encapsulates the server-side reply to a client's push request.
 */
public class PushResp implements Event {
  StatusCode code;
  String msg;


  public PushResp(StatusCode code, String msg) {
    this.msg = msg;
    this.code = code;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public String toString() {
    return "PushResp{"
            + "msg='" + msg + '\''
            + ", code=" + code
            + '}';
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return false;
  }

  @Override
  public String logMessage() {
    return this.toString();
  }

  @Override
  public int size() {
    return 0;
  }
}