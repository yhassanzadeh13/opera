package SkipGraph.packets;

import java.io.Serializable;

public class Request implements Serializable {

    private static int lastId = 0;

    public final int flowId;

    public Request() {
        this.flowId = lastId++;
    }
}
