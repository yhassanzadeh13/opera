package scenario.integrita.events;

import network.model.Event;

/**
 * PullResp event encapsulates a sever-side response to the client's pull request.
 */
public class PullResp implements Event {
    String msg;

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
