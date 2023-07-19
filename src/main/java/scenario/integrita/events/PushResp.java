package scenario.integrita.events;

import network.model.Event;
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

  @Override
  public String toString() {
    return "PushResp{"
      + "msg='" + msg + '\''
      + ", code=" + code
      + '}';
  }
}
