package scenario.integrita.events;

import node.BaseNode;
import scenario.integrita.utils.StatusCode;
import underlay.packets.Event;

public class PushResp implements Event {
    String msg;
    StatusCode code;

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public PushResp(StatusCode code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    @Override
    public String toString() {
        return "PushResp{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                '}';
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
