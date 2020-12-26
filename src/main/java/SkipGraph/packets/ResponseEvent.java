package SkipGraph.packets;

import Node.BaseNode;
import Underlay.packets.Event;

public class ResponseEvent implements Event {

    public final Response response;

    public ResponseEvent(Response response) {
        this.response = response;
    }

    @Override
    public boolean actionPerformed(BaseNode hostNode) {
        return false;
    }

    @Override
    public String logMessage() {
        return null;
    }
}
