package SkipGraph.packets;

import Node.BaseNode;
import Underlay.packets.Event;

public class RequestEvent implements Event {

    public final Request request;

    public RequestEvent(Request request) {
        this.request = request;
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
