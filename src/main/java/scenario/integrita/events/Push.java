package scenario.integrita.events;

import node.BaseNode;
import underlay.packets.Event;

public class Push implements Event {
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
        return msg;
    }

    @Override
    public int size() {
        return 0;
    }
}
